package com.quantaida.mc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.Guard;
import com.quantaida.mc.common.NoGenerator;
import com.quantaida.mc.common.TenantContext;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 公共基础设施验收：防重、编号、SSO 沙箱登录、限流。
 * Redis 未启用时验证单机降级路径，保证本地与生产同一套调用代码。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InfrastructureTest {

    @Autowired
    private Guard guard;

    @Autowired
    private NoGenerator noGenerator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    void setUp() {
        TenantContext.setTenantId(1L);
        Integer count = jdbc.queryForObject(
                "select count(*) from sys_sso_config where tenant_id=1 and deleted=0", Integer.class);
        if (count == null || count == 0) {
            jdbc.update("insert into sys_sso_config (tenant_id, enabled, protocol, base_url, client_id, " +
                            "account_mapping, deleted, created_at, updated_at) values (1,1,'OIDC',?,?,?,0,now(),now())",
                    "https://idp.example.com", "mc-client", "username");
        } else {
            jdbc.update("update sys_sso_config set enabled=1, protocol='OIDC', account_mapping='username' where tenant_id=1");
        }
    }

    @Test
    void idempotentRejectsDuplicateSubmit() {
        String key = "test-idem-" + System.nanoTime();
        guard.idempotent(key);
        assertThatThrownBy(() -> guard.idempotent(key))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("重复提交");
    }

    @Test
    void rateLimitBlocksOverLimit() {
        String key = "test-rate-" + System.nanoTime();
        guard.rateLimit(key, 2, 60);
        guard.rateLimit(key, 2, 60);
        assertThatThrownBy(() -> guard.rateLimit(key, 2, 60))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("频繁");
    }

    @Test
    void numberGeneratorProducesUniqueTenantNumbers() {
        String a = noGenerator.gen("TST");
        String b = noGenerator.gen("TST");
        assertThat(a).startsWith("TST").hasSize(3 + 14 + 6);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void sandboxSsoLoginIssuesToken() throws Exception {
        TenantContext.setTenantId(1L);
        String body = objectMapper.writeValueAsString(Map.of(
                "tenantCode", "demo",
                "protocol", "OIDC",
                "ticket", "sandbox-ticket",
                "username", "admin"));
        String response = mockMvc.perform(post("/api/sso/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        assertThat(node.path("data").path("mapping").asText()).isEqualTo("username");
    }
}
