package com.quantaida.mc.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.Guard;
import com.quantaida.mc.common.R;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.entity.SystemEntities.SysSsoConfig;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import com.quantaida.mc.mapper.SysSsoConfigMapper;
import com.quantaida.mc.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * SSO 回调登录。
 *
 * <p>沙箱模式（app.sso.mock=true）用于联调完整登录链路：IdP 认证完成后把映射账号传入，
 * 本接口签发 JWT。生产模式必须由 IdP 适配器校验 ticket/code 后再调用 {@link AuthService#ssoLogin}。</p>
 */
@RestController
@RequestMapping("/api/sso")
@RequiredArgsConstructor
public class SsoAuthController {
    private final PlatformTenantMapper tenantMapper;
    private final SysSsoConfigMapper ssoConfigMapper;
    private final AuthService authService;
    private final Guard guard;

    public record LoginReq(String tenantCode, String protocol, String ticket, String username) {
    }

    @Value("${app.sso.mock:false}")
    private boolean mock;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginReq req) {
        String code = StringUtils.hasText(req.tenantCode()) ? req.tenantCode() : "demo";
        guard.rateLimit("sso:" + code, 20, 60);
        PlatformTenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<PlatformTenant>()
                .eq(PlatformTenant::getTenantCode, code));
        if (tenant == null) {
            throw new BizException(404, "租户不存在");
        }
        if ("SUSPENDED".equals(tenant.getStatus()) || "EXPIRED".equals(tenant.getStatus())) {
            throw new BizException(40301, "租户已停用或到期");
        }
        TenantContext.setTenantId(tenant.getId());
        TenantContext.setTenantCode(tenant.getTenantCode());
        SysSsoConfig config = ssoConfigMapper.selectOne(new LambdaQueryWrapper<>());
        if (config == null || config.getEnabled() == null || config.getEnabled() == 0) {
            throw new BizException(403, "SSO 未启用，请使用账号密码登录");
        }
        if (StringUtils.hasText(req.protocol()) && !req.protocol().equalsIgnoreCase(config.getProtocol())) {
            throw new BizException(400, "SSO 协议不匹配");
        }
        if (!StringUtils.hasText(req.ticket())) {
            throw new BizException(400, "缺少 SSO 票据");
        }
        if (!mock) {
            // 生产：此处应调用 CAS /p3/serviceValidate 或 OAuth2/OIDC token + userinfo 校验票据。
            throw new BizException(501, "SSO 真实票据校验未配置 IdP 适配器，请联系运维");
        }
        return R.ok(authService.ssoLogin(req.username(), tenant.getId(), config.getAccountMapping()));
    }
}
