package com.quantaida.mc.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.entity.SystemEntities.SysUser;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import com.quantaida.mc.mapper.SysUserMapper;
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
@RequestMapping({"/api/mini/auth", "/api/h5/auth"})
@RequiredArgsConstructor
public class MiniAuthController {
    private final AuthService authService;
    private final PlatformTenantMapper tenantMapper;

    @Value("${app.tenant.default-code:demo}")
    private String defaultTenantCode;

    public record LoginReq(String code, String tenantCode, String unionid) {
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginReq req) {
        String code = StringUtils.hasText(req.tenantCode()) ? req.tenantCode() : defaultTenantCode;
        com.quantaida.mc.common.TenantContext.setTenantId(resolveTenant(code));
        return R.ok(authService.miniLogin(req.code(), code, req.unionid()));
    }

    private Long resolveTenant(String code) {
        PlatformTenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<PlatformTenant>()
                .eq(PlatformTenant::getTenantCode, code));
        if (tenant == null) {
            throw new BizException(404, "租户不存在");
        }
        if ("SUSPENDED".equals(tenant.getStatus()) || "EXPIRED".equals(tenant.getStatus())) {
            throw new BizException(40301, "租户已停用或到期");
        }
        return tenant.getId();
    }

    @PostMapping("/profile")
    public R<Map<String, Object>> profile(@RequestBody Map<String, Object> body) {
        return R.ok(body);
    }
}
