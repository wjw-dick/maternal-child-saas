package com.quantaida.mc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 多租户隔离与端隔离验收（checklist §一）：
 * 1. 租户 A 列表不可见租户 B 数据；
 * 2. 直接拼接租户 B 资源 ID 返回 403 并写入安全审计；
 * 3. 平台账号不可进入租户业务接口，租户账号不可进入平台接口。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TenantIsolationTest {

    private static final long OTHER_TENANT_ID = 2L;
    private static final long OTHER_MEMBER_ID = 900001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tenantToken;
    private String platformToken;

    @BeforeAll
    void setUp() throws Exception {
        Integer tenants = jdbc.queryForObject(
                "select count(*) from platform_tenant where id=?", Integer.class, OTHER_TENANT_ID);
        if (tenants == null || tenants == 0) {
            jdbc.update("insert into platform_tenant (id, tenant_code, name, industry, contact, plan_id, status, " +
                            "seat_limit, mp_account_limit, storage_quota_gb, sms_quota, api_quota, deleted, created_at, updated_at) " +
                            "values (?,?,?,?,?,?,?,?,?,?,?,?,0,now(),now())",
                    OTHER_TENANT_ID, "isolation-b", "隔离测试租户B", "母婴护理", "测试", 1L, "ACTIVE",
                    5, 1, 10, 1000, 10000);
        }
        Integer members = jdbc.queryForObject(
                "select count(*) from biz_member where id=?", Integer.class, OTHER_MEMBER_ID);
        if (members == null || members == 0) {
            jdbc.update("insert into biz_member (id, tenant_id, member_no, name, level, balance, points, status, " +
                            "deleted, created_at, updated_at) values (?,?,?,?,?,?,?,?,0,now(),now())",
                    OTHER_MEMBER_ID, OTHER_TENANT_ID, "T2-ISO-001", "越权会员", "NORMAL", 0, 0, "ACTIVE");
        }

        tenantToken = login("/api/tenant/auth/login",
                Map.of("username", "admin", "password", "admin123", "tenantCode", "demo"));
        platformToken = login("/api/platform/auth/login",
                Map.of("username", "admin", "password", "admin123"));
    }

    private String login(String path, Map<String, String> body) throws Exception {
        String response = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.path("data").path("token").asText();
    }

    @Test
    void listOnlyContainsOwnTenantData() throws Exception {
        String response = mockMvc.perform(get("/api/tenant/members")
                        .param("page", "1")
                        .param("size", "200")
                        .header("Authorization", "Bearer " + tenantToken)
                        .header("X-Tenant-Code", "demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain("越权会员");
        assertThat(response).doesNotContain("T2-ISO-001");
    }

    @Test
    void crossTenantResourceIdReturns403AndAudits() throws Exception {
        mockMvc.perform(get("/api/tenant/members/" + OTHER_MEMBER_ID)
                        .header("Authorization", "Bearer " + tenantToken)
                        .header("X-Tenant-Code", "demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.msg").value("无权访问其他租户数据"));

        Long audits = jdbc.queryForObject(
                "select count(*) from sys_audit_log where tenant_id=1 and action='cross-tenant-access' and result='DENIED'",
                Long.class);
        assertThat(audits).isNotNull().isGreaterThan(0);
    }

    @Test
    void platformAccountCannotAccessTenantBusinessApi() throws Exception {
        mockMvc.perform(get("/api/tenant/members")
                        .header("Authorization", "Bearer " + platformToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void tenantAccountCannotAccessPlatformApi() throws Exception {
        mockMvc.perform(get("/api/platform/tenants")
                        .header("Authorization", "Bearer " + tenantToken)
                        .header("X-Tenant-Code", "demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void demoAdminCanReadTenantDashboard() throws Exception {
        mockMvc.perform(get("/api/tenant/dashboard")
                        .header("Authorization", "Bearer " + tenantToken)
                        .header("X-Tenant-Code", "demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.members").exists());
    }
}
