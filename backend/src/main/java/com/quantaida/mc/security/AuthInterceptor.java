package com.quantaida.mc.security;

import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.common.UserContext;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    private final PlatformTenantMapper tenantMapper;
    private final Map<Long, Long> tenantCheckedAt = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BizException(401, "请先登录");
        }
        Claims claims = jwtUtil.parse(auth.substring(7));
        Long tenantId = claims.get("tenantId", Number.class).longValue();
        String userType = claims.get("userType", String.class);
        Long roleId = claims.get("roleId") == null ? null : ((Number) claims.get("roleId")).longValue();
        Long memberId = claims.get("memberId") == null ? null : ((Number) claims.get("memberId")).longValue();
        TenantContext.setTenantId(tenantId);
        UserContext.set(new UserContext.LoginUser(Long.valueOf(claims.getSubject()), tenantId,
                claims.get("username", String.class), userType, roleId, null, memberId));

        String path = request.getRequestURI();
        if (path.startsWith("/api/platform") && !"PLATFORM".equals(userType)) {
            throw new BizException(403, "平台接口禁止租户账号访问");
        }
        if (!path.startsWith("/api/platform") && "PLATFORM".equals(userType) && path.startsWith("/api/tenant")) {
            throw new BizException(403, "平台账号不可进入租户业务接口");
        }
        if (path.startsWith("/api/mini") && !"MEMBER".equals(userType)) {
            throw new BizException(403, "请使用会员身份访问");
        }
        if (path.startsWith("/api/h5") && !"MEMBER".equals(userType) && !"TENANT".equals(userType)) {
            throw new BizException(403, "请使用会员身份访问");
        }

        assertTenantActive(tenantId);

        if (handler instanceof HandlerMethod hm) {
            RequirePerm rp = hm.getMethodAnnotation(RequirePerm.class);
            if (rp == null) {
                rp = hm.getBeanType().getAnnotation(RequirePerm.class);
            }
            if (rp != null) {
                permissionService.require(rp.value());
            }
        }
        return true;
    }

    private void assertTenantActive(Long tenantId) {
        if (tenantId == null || tenantId == 0L) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = tenantCheckedAt.get(tenantId);
        if (last != null && now - last < 30_000L) {
            return;
        }
        PlatformTenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BizException(403, "租户不存在");
        }
        String status = tenant.getStatus();
        if ("SUSPENDED".equals(status) || "EXPIRED".equals(status)) {
            throw new BizException(40301, "租户已停用或到期，业务接口已关闭");
        }
        tenantCheckedAt.put(tenantId, now);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
