package com.quantaida.mc.web.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.R;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import com.quantaida.mc.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/tenant/auth")
@RequiredArgsConstructor
public class TenantAuthController {
    private final AuthService authService;
    private final PlatformTenantMapper tenantMapper;

    @Value("${app.tenant.default-code:demo}")
    private String defaultTenantCode;

    public record LoginReq(String username, String password, String tenantCode) {
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginReq req) {
        String code = StringUtils.hasText(req.tenantCode()) ? req.tenantCode() : defaultTenantCode;
        PlatformTenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<PlatformTenant>()
                .eq(PlatformTenant::getTenantCode, code));
        if (tenant == null) {
            throw new BizException(404, "租户不存在");
        }
        if ("SUSPENDED".equals(tenant.getStatus()) || "EXPIRED".equals(tenant.getStatus())) {
            throw new BizException(40301, "租户已停用或到期");
        }
        TenantContext.setTenantId(tenant.getId());
        return R.ok(authService.login(req.username(), req.password(), tenant.getId(), "TENANT"));
    }
}
