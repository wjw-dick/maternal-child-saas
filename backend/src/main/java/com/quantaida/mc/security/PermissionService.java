package com.quantaida.mc.security;

import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.UserContext;
import com.quantaida.mc.entity.SystemEntities.SysRole;
import com.quantaida.mc.mapper.SysMenuMapper;
import com.quantaida.mc.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final Map<String, Set<String>> cache = new ConcurrentHashMap<>();

    public void require(String perm) {
        UserContext.LoginUser user = UserContext.get();
        if (user == null) {
            throw new BizException(401, "请先登录");
        }
        SysRole role = roleMapper.selectById(user.roleId());
        if (role != null && ("PLATFORM_ADMIN".equals(role.getCode()) || "TENANT_ADMIN".equals(role.getCode()))) {
            return;
        }
        if (!perms(user.tenantId(), user.roleId()).contains(perm)) {
            throw new BizException(403, "无操作权限：" + perm);
        }
    }

    public Set<String> perms(Long tenantId, Long roleId) {
        String key = tenantId + ":" + roleId;
        return cache.computeIfAbsent(key, k -> {
            List<String> codes = menuMapper.selectPermsByRole(roleId);
            return Set.copyOf(codes);
        });
    }

    public void evict(Long tenantId, Long roleId) {
        cache.remove(tenantId + ":" + roleId);
    }

    public void evictTenant(Long tenantId) {
        cache.keySet().removeIf(k -> k.startsWith(tenantId + ":"));
    }
}
