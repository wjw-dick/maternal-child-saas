package com.quantaida.mc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.PlatformEntities.PlatformQuotaUsage;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.mapper.CommonMapper;
import com.quantaida.mc.mapper.PlatformQuotaUsageMapper;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 五类配额服务端强校验：席位 / 公众号 / 存储 / 短信 / API。
 */
@Service
@RequiredArgsConstructor
public class QuotaService {
    private final PlatformTenantMapper tenantMapper;
    private final PlatformQuotaUsageMapper usageMapper;
    private final CommonMapper commonMapper;

    public PlatformTenant tenant(Long tenantId) {
        PlatformTenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BizException(404, "租户不存在");
        }
        return tenant;
    }

    public void checkSeat(Long tenantId) {
        PlatformTenant tenant = tenant(tenantId);
        long used = commonMapper.countByTenant("sys_user", tenantId);
        if (tenant.getSeatLimit() != null && used >= tenant.getSeatLimit()) {
            throw new BizException(40201, "席位数已用尽（" + used + "/" + tenant.getSeatLimit() + "），请升级套餐");
        }
    }

    public void checkMpAccount(Long tenantId) {
        PlatformTenant tenant = tenant(tenantId);
        long used = commonMapper.countByTenant("wx_account", tenantId);
        if (tenant.getMpAccountLimit() != null && used >= tenant.getMpAccountLimit()) {
            throw new BizException(40202, "公众号数量已达上限，请升级套餐");
        }
    }

    public void checkStorage(Long tenantId, long addBytes) {
        PlatformTenant tenant = tenant(tenantId);
        if (tenant.getStorageQuotaGb() == null) {
            return;
        }
        long limitBytes = tenant.getStorageQuotaGb() * 1024L * 1024L * 1024L;
        PlatformQuotaUsage usage = todayUsage(tenantId);
        long used = usage == null || usage.getUsedStorageMb() == null ? 0 : usage.getUsedStorageMb() * 1024L * 1024L;
        if (used + addBytes > limitBytes) {
            throw new BizException(40203, "存储空间不足，请升级套餐");
        }
    }

    public void checkSms(Long tenantId, int count) {
        PlatformTenant tenant = tenant(tenantId);
        PlatformQuotaUsage usage = todayUsage(tenantId);
        int used = usage == null || usage.getUsedSms() == null ? 0 : usage.getUsedSms();
        if (tenant.getSmsQuota() != null && used + count > tenant.getSmsQuota()) {
            throw new BizException(40204, "短信额度不足，请升级套餐");
        }
    }

    public void checkApi(Long tenantId) {
        PlatformTenant tenant = tenant(tenantId);
        PlatformQuotaUsage usage = todayUsage(tenantId);
        int used = usage == null || usage.getUsedApi() == null ? 0 : usage.getUsedApi();
        if (tenant.getApiQuota() != null && used >= tenant.getApiQuota()) {
            throw new BizException(40205, "API 调用额度已达上限，请升级套餐");
        }
    }

    public void addApiUsage(Long tenantId) {
        PlatformQuotaUsage usage = todayUsage(tenantId);
        if (usage == null) {
            usage = new PlatformQuotaUsage();
            usage.setTenantId(tenantId);
            usage.setUsedApi(1);
            usage.setUsedSeats((int) commonMapper.countByTenant("sys_user", tenantId));
            usage.setUsedMp((int) commonMapper.countByTenant("wx_account", tenantId));
            usage.setUsedStorageMb(0L);
            usage.setUsedSms(0);
            usage.setStatDate(LocalDate.now());
            usageMapper.insert(usage);
        } else {
            usage.setUsedApi((usage.getUsedApi() == null ? 0 : usage.getUsedApi()) + 1);
            usageMapper.updateById(usage);
        }
    }

    public void addSmsUsage(Long tenantId, int count) {
        PlatformQuotaUsage usage = todayUsage(tenantId);
        if (usage == null) {
            usage = new PlatformQuotaUsage();
            usage.setTenantId(tenantId);
            usage.setUsedSms(count);
            usage.setUsedApi(0);
            usage.setUsedSeats((int) commonMapper.countByTenant("sys_user", tenantId));
            usage.setUsedMp((int) commonMapper.countByTenant("wx_account", tenantId));
            usage.setUsedStorageMb(0L);
            usage.setStatDate(LocalDate.now());
            usageMapper.insert(usage);
        } else {
            usage.setUsedSms((usage.getUsedSms() == null ? 0 : usage.getUsedSms()) + count);
            usageMapper.updateById(usage);
        }
    }

    public void addStorageUsage(Long tenantId, long bytes) {
        PlatformQuotaUsage usage = todayUsage(tenantId);
        long addMb = Math.max(1, bytes / 1024 / 1024);
        if (usage == null) {
            usage = new PlatformQuotaUsage();
            usage.setTenantId(tenantId);
            usage.setUsedStorageMb(addMb);
            usage.setUsedApi(0);
            usage.setUsedSms(0);
            usage.setUsedSeats((int) commonMapper.countByTenant("sys_user", tenantId));
            usage.setUsedMp((int) commonMapper.countByTenant("wx_account", tenantId));
            usage.setStatDate(LocalDate.now());
            usageMapper.insert(usage);
        } else {
            usage.setUsedStorageMb((usage.getUsedStorageMb() == null ? 0 : usage.getUsedStorageMb()) + addMb);
            usageMapper.updateById(usage);
        }
    }

    public PlatformQuotaUsage todayUsage(Long tenantId) {
        return usageMapper.selectOne(new LambdaQueryWrapper<PlatformQuotaUsage>()
                .eq(PlatformQuotaUsage::getTenantId, tenantId)
                .eq(PlatformQuotaUsage::getStatDate, LocalDate.now()));
    }

    public void refreshUsage(Long tenantId) {
        PlatformQuotaUsage usage = todayUsage(tenantId);
        if (usage == null) {
            usage = new PlatformQuotaUsage();
            usage.setTenantId(tenantId);
            usage.setStatDate(LocalDate.now());
            usage.setUsedStorageMb(0L);
            usage.setUsedSms(0);
            usage.setUsedApi(0);
            usageMapper.insert(usage);
        }
        usage.setUsedSeats((int) commonMapper.countByTenant("sys_user", tenantId));
        usage.setUsedMp((int) commonMapper.countByTenant("wx_account", tenantId));
        usageMapper.updateById(usage);
    }

    public Long currentTenantId() {
        return TenantContext.getTenantId();
    }
}
