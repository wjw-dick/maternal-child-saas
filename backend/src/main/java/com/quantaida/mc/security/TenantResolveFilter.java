package com.quantaida.mc.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 子域名 / X-Tenant-Code 解析租户上下文（未登录场景也生效）。
 */
@Component
@RequiredArgsConstructor
public class TenantResolveFilter extends OncePerRequestFilter {
    private final PlatformTenantMapper tenantMapper;
    private final Map<String, Long> cache = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Value("${app.tenant.default-code:demo}")
    private String defaultTenantCode;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String code = request.getHeader("X-Tenant-Code");
        if (!StringUtils.hasText(code)) {
            String host = request.getServerName();
            if (StringUtils.hasText(host) && host.contains(".")) {
                String prefix = host.split("\\.")[0];
                if (!"www".equals(prefix) && !"api".equals(prefix) && !"platform".equals(prefix)) {
                    code = prefix;
                }
            }
        }
        if (StringUtils.hasText(code)) {
            Long tenantId = cache.computeIfAbsent(code, c -> {
                PlatformTenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<PlatformTenant>()
                        .eq(PlatformTenant::getTenantCode, c));
                return tenant == null ? 0L : tenant.getId();
            });
            TenantContext.setTenantId(tenantId);
            TenantContext.setTenantCode(code);
        } else if (!request.getRequestURI().startsWith("/api/platform")) {
            TenantContext.setTenantId(cache.computeIfAbsent(defaultTenantCode, c -> {
                PlatformTenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<PlatformTenant>()
                        .eq(PlatformTenant::getTenantCode, c));
                return tenant == null ? 0L : tenant.getId();
            }));
            TenantContext.setTenantCode(defaultTenantCode);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
