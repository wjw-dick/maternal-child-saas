package com.quantaida.mc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.UserContext;
import com.quantaida.mc.entity.PlatformEntities.PlatformAudit;
import com.quantaida.mc.entity.PlatformEntities.PlatformBill;
import com.quantaida.mc.entity.PlatformEntities.PlatformPlan;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenantAdmin;
import com.quantaida.mc.entity.SystemEntities.SysUser;
import com.quantaida.mc.mapper.PlatformAuditMapper;
import com.quantaida.mc.mapper.PlatformBillMapper;
import com.quantaida.mc.mapper.PlatformPlanMapper;
import com.quantaida.mc.mapper.PlatformTenantAdminMapper;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import com.quantaida.mc.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final PlatformTenantMapper tenantMapper;
    private final PlatformPlanMapper planMapper;
    private final PlatformBillMapper billMapper;
    private final PlatformAuditMapper platformAuditMapper;
    private final PlatformTenantAdminMapper tenantAdminMapper;
    private final SysUserMapper userMapper;
    private final com.quantaida.mc.mapper.CommonMapper commonMapper;
    private final ProvisionService provisionService;
    private final QuotaService quotaService;

    public record CreateReq(Long planId, String tenantCode, String name, String industry, String contact,
                            String phone, String adminUsername, String adminPassword, Integer trialDays) {
    }

    public Page<PlatformTenant> page(long page, long size, String keyword, String status) {
        LambdaQueryWrapper<PlatformTenant> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(PlatformTenant::getName, keyword).or().like(PlatformTenant::getTenantCode, keyword));
        }
        if (StringUtils.hasText(status)) {
            qw.eq(PlatformTenant::getStatus, status);
        }
        qw.orderByDesc(PlatformTenant::getCreatedAt);
        return tenantMapper.selectPage(new Page<>(page, size), qw);
    }

    @Transactional
    public PlatformTenant create(CreateReq req) {
        if (!StringUtils.hasText(req.tenantCode()) || !StringUtils.hasText(req.name())) {
            throw new BizException(400, "租户编码与名称必填");
        }
        if (tenantMapper.selectCount(new LambdaQueryWrapper<PlatformTenant>()
                .eq(PlatformTenant::getTenantCode, req.tenantCode())) > 0) {
            throw new BizException(400, "租户编码已存在");
        }
        PlatformPlan plan = planMapper.selectById(req.planId());
        if (plan == null) {
            throw new BizException(400, "请选择有效套餐");
        }
        int trialDays = req.trialDays() != null ? req.trialDays() :
                (plan.getTrialDays() != null ? plan.getTrialDays() : 14);
        PlatformTenant tenant = new PlatformTenant();
        tenant.setTenantCode(req.tenantCode());
        tenant.setName(req.name());
        tenant.setIndustry(req.industry());
        tenant.setContact(req.contact());
        tenant.setPhone(req.phone());
        tenant.setPlanId(plan.getId());
        tenant.setStatus(trialDays > 0 ? "TRIAL" : "ACTIVE");
        tenant.setTrialEndAt(LocalDateTime.now().plusDays(trialDays));
        tenant.setExpireAt(LocalDateTime.now().plusDays(trialDays));
        tenant.setSeatLimit(plan.getSeatLimit());
        tenant.setMpAccountLimit(plan.getMpAccountLimit());
        tenant.setStorageQuotaGb(plan.getStorageQuotaGb());
        tenant.setSmsQuota(plan.getSmsQuota());
        tenant.setApiQuota(plan.getApiQuota());
        tenantMapper.insert(tenant);

        SysUser admin = provisionService.provision(tenant.getId(), req.adminUsername(), req.adminPassword(),
                req.name() + "管理员");
        PlatformTenantAdmin ta = new PlatformTenantAdmin();
        ta.setTenantId(tenant.getId());
        ta.setUserId(admin.getId());
        ta.setIsPrimary(1);
        tenantAdminMapper.insert(ta);
        platformAudit(tenant.getId(), "tenant:create", "tenant:" + tenant.getId(), "SUCCESS", req.tenantCode());
        return tenant;
    }

    public PlatformTenant update(Long id, PlatformTenant patch) {
        PlatformTenant tenant = requireTenant(id);
        if (patch.getName() != null) tenant.setName(patch.getName());
        if (patch.getIndustry() != null) tenant.setIndustry(patch.getIndustry());
        if (patch.getContact() != null) tenant.setContact(patch.getContact());
        if (patch.getPhone() != null) tenant.setPhone(patch.getPhone());
        if (patch.getRemark() != null) tenant.setRemark(patch.getRemark());
        if (patch.getPlanId() != null && !patch.getPlanId().equals(tenant.getPlanId())) {
            PlatformPlan plan = planMapper.selectById(patch.getPlanId());
            if (plan == null) throw new BizException(400, "套餐不存在");
            tenant.setPlanId(plan.getId());
            tenant.setSeatLimit(plan.getSeatLimit());
            tenant.setMpAccountLimit(plan.getMpAccountLimit());
            tenant.setStorageQuotaGb(plan.getStorageQuotaGb());
            tenant.setSmsQuota(plan.getSmsQuota());
            tenant.setApiQuota(plan.getApiQuota());
        }
        tenantMapper.updateById(tenant);
        platformAudit(id, "tenant:update", "tenant:" + id, "SUCCESS", tenant.getTenantCode());
        return tenant;
    }

    public void changeStatus(Long id, String status) {
        PlatformTenant tenant = requireTenant(id);
        if (!List.of("ACTIVE", "SUSPENDED", "TRIAL", "EXPIRED").contains(status)) {
            throw new BizException(400, "非法状态");
        }
        tenant.setStatus(status);
        tenantMapper.updateById(tenant);
        platformAudit(id, "tenant:status:" + status, "tenant:" + id, "SUCCESS", tenant.getTenantCode());
    }

    public PlatformTenant renew(Long id, int months) {
        PlatformTenant tenant = requireTenant(id);
        LocalDateTime base = tenant.getExpireAt() == null || tenant.getExpireAt().isBefore(LocalDateTime.now())
                ? LocalDateTime.now() : tenant.getExpireAt();
        tenant.setExpireAt(base.plusMonths(months));
        tenant.setStatus("ACTIVE");
        tenantMapper.updateById(tenant);
        platformAudit(id, "tenant:renew", "tenant:" + id, "SUCCESS", months + "个月");
        return tenant;
    }

    public void delete(Long id) {
        PlatformTenant tenant = requireTenant(id);
        tenantMapper.deleteById(id);
        platformAudit(id, "tenant:delete", "tenant:" + id, "SUCCESS", "归档保留，不影响其他租户");
    }

    public PlatformTenant requireTenant(Long id) {
        PlatformTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BizException(404, "租户不存在");
        }
        return tenant;
    }

    public PlatformPlan requirePlan(Long id) {
        PlatformPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BizException(404, "套餐不存在");
        }
        return plan;
    }

    public int generateBills(String period) {
        String p = StringUtils.hasText(period) ? period : YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<PlatformTenant> tenants = tenantMapper.selectList(new LambdaQueryWrapper<PlatformTenant>()
                .in(PlatformTenant::getStatus, List.of("ACTIVE", "TRIAL", "OVERDUE")));
        int count = 0;
        for (PlatformTenant tenant : tenants) {
            if (billMapper.selectCount(new LambdaQueryWrapper<PlatformBill>()
                    .eq(PlatformBill::getTenantId, tenant.getId())
                    .eq(PlatformBill::getPeriod, p)
                    .eq(PlatformBill::getItem, "SEAT")) > 0) {
                continue;
            }
            PlatformPlan plan = planMapper.selectById(tenant.getPlanId());
            int seats = (int) commonMapper.countByTenant("sys_user", tenant.getId());
            PlatformBill bill = new PlatformBill();
            bill.setTenantId(tenant.getId());
            bill.setPeriod(p);
            bill.setItem("SEAT");
            bill.setQuantity(seats);
            bill.setAmount(plan == null || plan.getPrice() == null ? BigDecimal.ZERO : plan.getPrice());
            bill.setStatus("UNPAID");
            billMapper.insert(bill);
            count++;
        }
        platformAudit("bill:generate", "period:" + p, "SUCCESS", "生成 " + count + " 条");
        return count;
    }

    public void markPaid(Long billId) {
        PlatformBill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new BizException(404, "账单不存在");
        }
        bill.setStatus("PAID");
        bill.setPaidAt(LocalDateTime.now());
        billMapper.updateById(bill);
        platformAudit(bill.getTenantId(), "bill:paid", "bill:" + billId, "SUCCESS", null);
    }

    public void platformAudit(String action, String target, String result, String detail) {
        platformAudit(null, action, target, result, detail);
    }

    /** 平台审计带租户维度，便于平台端按租户筛选（checklist §二）。 */
    public void platformAudit(Long tenantId, String action, String target, String result, String detail) {
        PlatformAudit audit = new PlatformAudit();
        UserContext.LoginUser user = UserContext.get();
        audit.setTenantId(tenantId);
        audit.setOperatorType(user == null ? "SYSTEM" : user.userType());
        audit.setOperatorId(user == null ? null : user.userId());
        audit.setOperatorName(user == null ? "system" : user.username());
        audit.setAction(action);
        audit.setTarget(target);
        audit.setResult(result);
        audit.setDetail(detail);
        platformAuditMapper.insert(audit);
    }

    public List<PlatformTenant> allTenants() {
        return tenantMapper.selectList(null);
    }
}
