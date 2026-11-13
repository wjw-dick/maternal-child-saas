package com.quantaida.mc.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.BizEntities.BizDistributor;
import com.quantaida.mc.entity.BizEntities.BizStaff;
import com.quantaida.mc.entity.BizEntities.BizSupplier;
import com.quantaida.mc.entity.SystemEntities.SysRole;
import com.quantaida.mc.entity.SystemEntities.SysUser;
import com.quantaida.mc.mapper.BizDistributorMapper;
import com.quantaida.mc.mapper.BizStaffMapper;
import com.quantaida.mc.mapper.BizSupplierMapper;
import com.quantaida.mc.mapper.SysRoleMapper;
import com.quantaida.mc.mapper.SysUserMapper;
import com.quantaida.mc.service.ProvisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 演示数据初始化：平台超管、演示租户默认角色/菜单/参数与多角色账号。
 * 生产环境可通过 demo.seed=false 关闭。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoInitializer implements ApplicationRunner {
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final ProvisionService provisionService;
    private final PasswordEncoder passwordEncoder;
    private final BizStaffMapper staffMapper;
    private final BizSupplierMapper supplierMapper;
    private final BizDistributorMapper distributorMapper;

    @Override
    public void run(ApplicationArguments args) {
        seedPlatformAdmin();
        seedTenant();
    }

    private void seedPlatformAdmin() {
        Long origin = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(0L);
            if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, "admin")) == 0) {
                SysUser admin = new SysUser();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setNickname("平台超管");
                admin.setUserType("PLATFORM");
                admin.setRoleId(1L);
                admin.setStatus("ACTIVE");
                userMapper.insert(admin);
                log.info("平台超管已创建：admin / admin123");
            }
        } finally {
            TenantContext.setTenantId(origin);
        }
    }

    private void seedTenant() {
        Long origin = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(1L);
            provisionService.provision(1L, "admin", "admin123", "悦宝管理员");
            createUser("nurse", "李护理", "NURSE", 1201L, 1001L, null, null);
            createUser("clerk", "前台小陈", "FRONT_DESK", null, 1001L, null, null);
            createUser("finance", "财务小周", "FINANCE", null, null, null, null);
            createUser("supplier01", "优品供应链", "SUPPLIER", null, null, 5001L, null);
            createUser("dist01", "宝妈联盟渠道", "DISTRIBUTOR", null, null, null, 6001L);
            bindStaff();
        } finally {
            TenantContext.setTenantId(origin);
        }
    }

    private void createUser(String username, String nickname, String roleCode, Long staffId, Long storeId,
                            Long supplierId, Long distributorId) {
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)) > 0) {
            return;
        }
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, roleCode));
        if (role == null) {
            return;
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setNickname(nickname);
        user.setUserType("TENANT");
        user.setRoleId(role.getId());
        user.setStaffId(staffId);
        user.setStoreId(storeId);
        user.setSupplierId(supplierId);
        user.setDistributorId(distributorId);
        user.setStatus("ACTIVE");
        userMapper.insert(user);
    }

    private void bindStaff() {
        SysUser nurse = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "nurse"));
        if (nurse == null) {
            return;
        }
        BizStaff staff = staffMapper.selectById(1201L);
        if (staff != null && staff.getUserId() == null) {
            staff.setUserId(nurse.getId());
            staffMapper.updateById(staff);
        }
        BizSupplier supplier = supplierMapper.selectById(5001L);
        if (supplier != null) {
            // 供应商账号已通过 supplier_id 关联，档案无需反向字段
            supplier.setStatus("ACTIVE");
        }
        BizDistributor distributor = distributorMapper.selectById(6001L);
        if (distributor != null) {
            distributor.setStatus("ACTIVE");
        }
    }
}
