package com.quantaida.mc.web.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.AuditService;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.SystemEntities.SysSsoConfig;
import com.quantaida.mc.mapper.SysSsoConfigMapper;
import com.quantaida.mc.security.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant/sso-config")
@RequiredArgsConstructor
public class SsoConfigController {
    private final SysSsoConfigMapper ssoConfigMapper;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @GetMapping
    public R<SysSsoConfig> get() {
        permissionService.require("sso:read");
        SysSsoConfig config = ssoConfigMapper.selectOne(new LambdaQueryWrapper<>());
        if (config == null) {
            config = new SysSsoConfig();
            config.setProtocol("OIDC");
            config.setEnabled(0);
        } else if (StringUtils.hasText(config.getClientSecret())) {
            config.setClientSecret("******");
        }
        return R.ok(config);
    }

    @GetMapping("/protocols")
    public R<List<String>> protocols() {
        return R.ok(List.of("CAS", "OAUTH2", "OIDC", "SA_TOKEN"));
    }

    @PostMapping
    public R<SysSsoConfig> save(@RequestBody SysSsoConfig patch) {
        permissionService.require("sso:write");
        SysSsoConfig config = ssoConfigMapper.selectOne(new LambdaQueryWrapper<>());
        if (config == null) {
            config = new SysSsoConfig();
            config.setEnabled(patch.getEnabled() == null ? 0 : patch.getEnabled());
            config.setProtocol(patch.getProtocol());
            config.setBaseUrl(patch.getBaseUrl());
            config.setClientId(patch.getClientId());
            config.setClientSecret(patch.getClientSecret());
            config.setAccountMapping(patch.getAccountMapping());
            ssoConfigMapper.insert(config);
        } else {
            config.setEnabled(patch.getEnabled());
            if (StringUtils.hasText(patch.getProtocol())) config.setProtocol(patch.getProtocol());
            if (patch.getBaseUrl() != null) config.setBaseUrl(patch.getBaseUrl());
            if (patch.getClientId() != null) config.setClientId(patch.getClientId());
            if (StringUtils.hasText(patch.getClientSecret()) && !"******".equals(patch.getClientSecret())) {
                config.setClientSecret(patch.getClientSecret());
            }
            if (patch.getAccountMapping() != null) config.setAccountMapping(patch.getAccountMapping());
            ssoConfigMapper.updateById(config);
        }
        auditService.log("sso", "save:" + config.getProtocol(), "SUCCESS");
        return R.ok(config);
    }

    @PostMapping("/authorize-url")
    public R<Map<String, String>> authorizeUrl(@RequestBody Map<String, String> body) {
        permissionService.require("sso:read");
        SysSsoConfig config = ssoConfigMapper.selectOne(new LambdaQueryWrapper<>());
        if (config == null || config.getEnabled() == null || config.getEnabled() == 0) {
            return R.ok(Map.of("enabled", "false", "url", ""));
        }
        String base = config.getBaseUrl() == null ? "" : config.getBaseUrl().replaceAll("/$", "");
        String path = switch (config.getProtocol()) {
            case "CAS" -> "/cas/login?service=" + body.getOrDefault("service", "");
            case "OAUTH2" -> "/oauth/authorize?client_id=" + config.getClientId() + "&response_type=code&redirect_uri="
                    + body.getOrDefault("service", "");
            case "SA_TOKEN" -> "/sso/authorize?client_id=" + config.getClientId();
            default -> "/oidc/authorize?client_id=" + config.getClientId() + "&response_type=code&scope=openid&redirect_uri="
                    + body.getOrDefault("service", "");
        };
        return R.ok(Map.of("enabled", "true", "url", base + path));
    }
}
