package com.quantaida.mc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.BizEntities.BizPointsRule;
import com.quantaida.mc.entity.SystemEntities.SysConfig;
import com.quantaida.mc.entity.SystemEntities.SysLanguage;
import com.quantaida.mc.entity.SystemEntities.SysMenu;
import com.quantaida.mc.entity.SystemEntities.SysMessageChannel;
import com.quantaida.mc.entity.SystemEntities.SysRole;
import com.quantaida.mc.entity.SystemEntities.SysRoleMenu;
import com.quantaida.mc.entity.SystemEntities.SysSmsChannel;
import com.quantaida.mc.entity.SystemEntities.SysSsoConfig;
import com.quantaida.mc.entity.SystemEntities.SysStorageConfig;
import com.quantaida.mc.entity.SystemEntities.SysUser;
import com.quantaida.mc.mapper.BizPointsRuleMapper;
import com.quantaida.mc.mapper.SysConfigMapper;
import com.quantaida.mc.mapper.SysLanguageMapper;
import com.quantaida.mc.mapper.SysMenuMapper;
import com.quantaida.mc.mapper.SysMessageChannelMapper;
import com.quantaida.mc.mapper.SysRoleMapper;
import com.quantaida.mc.mapper.SysRoleMenuMapper;
import com.quantaida.mc.mapper.SysSmsChannelMapper;
import com.quantaida.mc.mapper.SysSsoConfigMapper;
import com.quantaida.mc.mapper.SysStorageConfigMapper;
import com.quantaida.mc.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租户初始化模板：默认角色/菜单/参数/存储目录，开通即用（T20）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProvisionService {
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysConfigMapper configMapper;
    private final SysLanguageMapper languageMapper;
    private final SysUserMapper userMapper;
    private final SysStorageConfigMapper storageConfigMapper;
    private final SysMessageChannelMapper messageChannelMapper;
    private final SysSmsChannelMapper smsChannelMapper;
    private final SysSsoConfigMapper ssoConfigMapper;
    private final BizPointsRuleMapper pointsRuleMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SysUser provision(Long tenantId, String adminUsername, String adminPassword, String adminNickname) {
        Long origin = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(0L);
            List<SysRole> roleTemplates = roleMapper.selectList(null);
            List<SysMenu> menuTemplates = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getScope, "TENANT").orderByAsc(SysMenu::getSortNo));
            List<SysConfig> configTemplates = configMapper.selectList(null);
            List<SysLanguage> languageTemplates = languageMapper.selectList(null);

            TenantContext.setTenantId(tenantId);
            Map<Long, Long> roleIdMap = new HashMap<>();
            for (SysRole template : roleTemplates) {
                SysRole exists = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getCode, template.getCode()));
                if (exists != null) {
                    roleIdMap.put(template.getId(), exists.getId());
                    continue;
                }
                SysRole role = new SysRole();
                role.setName(template.getName());
                role.setCode(template.getCode());
                role.setDataScope(template.getDataScope());
                role.setRemark(template.getRemark());
                roleMapper.insert(role);
                roleIdMap.put(template.getId(), role.getId());
            }

            if (menuMapper.selectCount(null) == 0) {
                Map<Long, Long> menuIdMap = new HashMap<>();
                for (SysMenu template : menuTemplates) {
                    SysMenu menu = new SysMenu();
                    menu.setParentId(template.getParentId() == null ? 0L : menuIdMap.getOrDefault(template.getParentId(), 0L));
                    menu.setScope("TENANT");
                    menu.setName(template.getName());
                    menu.setCode(template.getCode());
                    menu.setMenuType(template.getMenuType());
                    menu.setPath(template.getPath());
                    menu.setComponent(template.getComponent());
                    menu.setIcon(template.getIcon());
                    menu.setSortNo(template.getSortNo());
                    menu.setVisible(template.getVisible());
                    menu.setStatus(template.getStatus());
                    menuMapper.insert(menu);
                    menuIdMap.put(template.getId(), menu.getId());
                }
            }

            SysRole adminRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getCode, "TENANT_ADMIN"));
            if (adminRole != null && roleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>()
                    .eq(SysRoleMenu::getRoleId, adminRole.getId())) == 0) {
                List<SysMenu> menus = menuMapper.selectList(null);
                for (SysMenu menu : menus) {
                    SysRoleMenu rm = new SysRoleMenu();
                    rm.setRoleId(adminRole.getId());
                    rm.setMenuId(menu.getId());
                    roleMenuMapper.insert(rm);
                }
            }

            for (SysConfig template : configTemplates) {
                if (configMapper.selectCount(new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, template.getConfigKey())) > 0) {
                    continue;
                }
                SysConfig config = new SysConfig();
                config.setConfigKey(template.getConfigKey());
                config.setConfigValue(template.getConfigValue());
                config.setGroupCode(template.getGroupCode());
                config.setEncrypted(template.getEncrypted());
                config.setRemark(template.getRemark());
                configMapper.insert(config);
            }

            for (SysLanguage template : languageTemplates) {
                if (languageMapper.selectCount(new LambdaQueryWrapper<SysLanguage>()
                        .eq(SysLanguage::getCode, template.getCode())) > 0) {
                    continue;
                }
                SysLanguage language = new SysLanguage();
                language.setCode(template.getCode());
                language.setName(template.getName());
                language.setEnabled(template.getEnabled());
                language.setIsDefault(template.getIsDefault());
                language.setSortNo(template.getSortNo());
                languageMapper.insert(language);
            }

            if (storageConfigMapper.selectCount(null) == 0) {
                SysStorageConfig storage = new SysStorageConfig();
                storage.setDriver("LOCAL");
                storage.setConfigJson("{\"root\":\"./data/storage\",\"publicPrefix\":\"/files\"}");
                storage.setEnabled(1);
                storageConfigMapper.insert(storage);
            }
            if (messageChannelMapper.selectCount(null) == 0) {
                for (String channel : List.of("MP_TEMPLATE", "MINI_SUBSCRIBE", "APP_PUSH", "INNER")) {
                    SysMessageChannel config = new SysMessageChannel();
                    config.setChannel(channel);
                    config.setEnabled("INNER".equals(channel) ? 1 : 0);
                    messageChannelMapper.insert(config);
                }
            }
            if (smsChannelMapper.selectCount(null) == 0) {
                for (String provider : List.of("ALIYUN", "TENCENT", "HUAWEI", "JDCLOUD", "AWS_SNS", "LOCAL_LOG")) {
                    SysSmsChannel config = new SysSmsChannel();
                    config.setProvider(provider);
                    config.setEnabled("LOCAL_LOG".equals(provider) ? 1 : 0);
                    smsChannelMapper.insert(config);
                }
            }
            if (ssoConfigMapper.selectCount(null) == 0) {
                SysSsoConfig sso = new SysSsoConfig();
                sso.setEnabled(0);
                sso.setProtocol("OIDC");
                ssoConfigMapper.insert(sso);
            }
            if (pointsRuleMapper.selectCount(null) == 0) {
                BizPointsRule rule = new BizPointsRule();
                rule.setName("消费积分");
                rule.setRuleType("CONSUME");
                rule.setRatio(new BigDecimal("1"));
                rule.setEnabled(1);
                pointsRuleMapper.insert(rule);
            }

            if (adminRole == null) {
                throw new BizException(500, "租户角色模板缺失");
            }
            String username = StringUtils.hasText(adminUsername) ? adminUsername : "admin";
            String password = StringUtils.hasText(adminPassword) ? adminPassword : "admin123";
            SysUser admin = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username));
            if (admin == null) {
                admin = new SysUser();
                admin.setUsername(username);
                admin.setPassword(passwordEncoder.encode(password));
                admin.setNickname(StringUtils.hasText(adminNickname) ? adminNickname : "租户管理员");
                admin.setUserType("TENANT");
                admin.setRoleId(adminRole.getId());
                admin.setStatus("ACTIVE");
                userMapper.insert(admin);
            }
            log.info("租户 {} 初始化完成，管理员 {}", tenantId, username);
            return admin;
        } finally {
            TenantContext.setTenantId(origin);
        }
    }

    public List<Map<String, Object>> defaultRoles() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("code", "STORE_MANAGER", "name", "店长"));
        list.add(Map.of("code", "NURSE", "name", "护理师"));
        return list;
    }
}
