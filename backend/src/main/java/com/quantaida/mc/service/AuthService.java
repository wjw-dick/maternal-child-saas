package com.quantaida.mc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.SensitiveUtil;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.BizEntities.BizMember;
import com.quantaida.mc.entity.SystemEntities.SysUser;
import com.quantaida.mc.entity.SystemEntities.SysRole;
import com.quantaida.mc.mapper.BizMemberMapper;
import com.quantaida.mc.mapper.SysRoleMapper;
import com.quantaida.mc.mapper.SysUserMapper;
import com.quantaida.mc.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final BizMemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SensitiveUtil sensitiveUtil;

    public Map<String, Object> login(String username, String password, Long tenantId, String expectedType) {
        TenantContext.setTenantId(tenantId);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException(401, "账号或密码错误");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BizException(403, "账号已停用");
        }
        if (expectedType != null && !expectedType.equals(user.getUserType())) {
            throw new BizException(403, "账号类型不匹配");
        }
        user.setLastLoginAt(LocalDateTime.now());
        user.setPassword(null);
        userMapper.updateById(user);
        String token = jwtUtil.create(user.getId(), tenantId, user.getUsername(), user.getUserType(),
                user.getRoleId(), null);
        return Map.of("token", token, "user", user);
    }

    /**
     * SSO 登录：由外部 IdP 完成认证后，按账号映射找到本租户用户并签发 JWT。
     * 真实票据校验由 SsoAuthController 在生产模式调用 IdP；本地沙箱允许 mock。
     */
    public Map<String, Object> ssoLogin(String username, Long tenantId, String mapping) {
        if (!StringUtils.hasText(username)) {
            throw new BizException(400, "SSO 未返回可映射的账号");
        }
        TenantContext.setTenantId(tenantId);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BizException(403, "SSO 账号映射失败，请联系管理员");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BizException(403, "账号已停用");
        }
        user.setLastLoginAt(LocalDateTime.now());
        user.setPassword(null);
        userMapper.updateById(user);
        String token = jwtUtil.create(user.getId(), tenantId, user.getUsername(), user.getUserType(),
                user.getRoleId(), null);
        return Map.of("token", token, "user", user, "mapping", mapping == null ? "username" : mapping);
    }

    public Map<String, Object> miniLogin(String code, String tenantCode, String unionid) {        if (!StringUtils.hasText(code)) {
            throw new BizException(400, "缺少微信授权 code");
        }
        String openid = "openid_" + code;
        String uid = StringUtils.hasText(unionid) ? unionid : "unionid_" + code;
        BizMember member = memberMapper.selectOne(new LambdaQueryWrapper<BizMember>()
                .eq(BizMember::getUnionid, uid));
        if (member == null) {
            member = new BizMember();
            member.setMemberNo("M" + System.currentTimeMillis());
            member.setName("微信会员");
            member.setPhone(null);
            member.setPhoneEnc(null);
            member.setPhoneHash(null);
            member.setGender("U");
            member.setLevel("NORMAL");
            member.setBalance(BigDecimal.ZERO);
            member.setPoints(0);
            member.setSource("MINI");
            member.setOpenid(openid);
            member.setUnionid(uid);
            member.setStatus("ACTIVE");
            memberMapper.insert(member);
        } else {
            member.setOpenid(openid);
            memberMapper.updateById(member);
        }
        String token = jwtUtil.create(member.getId(), TenantContext.getTenantId(), "member", "MEMBER",
                16L, member.getId());
        return Map.of("token", token, "member", member);
    }

    public Map<String, Object> me() {
        var user = com.quantaida.mc.common.UserContext.get();
        if (user == null) {
            throw new BizException(401, "请先登录");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.userId());
        data.put("tenantId", user.tenantId());
        data.put("username", user.username());
        data.put("userType", user.userType());
        data.put("roleId", user.roleId());
        if ("MEMBER".equals(user.userType()) && user.memberId() != null) {
            BizMember member = memberMapper.selectById(user.memberId());
            data.put("member", member);
        } else {
            SysUser userRow = userMapper.selectById(user.userId());
            if (userRow != null) {
                userRow.setPassword(null);
                if (userRow.getRoleId() != null) {
                    SysRole role = roleMapper.selectById(userRow.getRoleId());
                    if (role != null) {
                        data.put("roleCode", role.getCode());
                        data.put("roleName", role.getName());
                    }
                }
            }
            data.put("user", userRow);
        }
        return data;
    }

    public void changePassword(String oldPassword, String newPassword) {
        var ctx = com.quantaida.mc.common.UserContext.get();
        if (ctx == null) {
            throw new BizException(401, "请先登录");
        }
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new BizException(400, "新密码至少 6 位");
        }
        SysUser user = userMapper.selectById(ctx.userId());
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException(400, "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public String encode(String raw) {
        return passwordEncoder.encode(raw);
    }

    public SensitiveUtil sensitive() {
        return sensitiveUtil;
    }
}
