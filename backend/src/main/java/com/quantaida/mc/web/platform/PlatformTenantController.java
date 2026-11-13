package com.quantaida.mc.web.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.PlatformEntities.PlatformPlan;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.entity.PlatformEntities.PlatformAudit;
import com.quantaida.mc.mapper.PlatformAuditMapper;
import com.quantaida.mc.mapper.PlatformPlanMapper;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import com.quantaida.mc.service.QuotaService;
import com.quantaida.mc.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform/tenants")
@RequiredArgsConstructor
public class PlatformTenantController {
    private final TenantService tenantService;
    private final QuotaService quotaService;
    private final PlatformTenantMapper tenantMapper;
    private final PlatformPlanMapper planMapper;
    private final PlatformAuditMapper auditMapper;

    @GetMapping
    public R<IPage<PlatformTenant>> page(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String status) {
        return R.ok(tenantService.page(page, size, keyword, status));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        PlatformTenant tenant = tenantService.requireTenant(id);
        return R.ok(Map.of("tenant", tenant, "usage", quotaService.todayUsage(id) == null
                ? Map.of() : quotaService.todayUsage(id)));
    }

    @PostMapping
    public R<PlatformTenant> create(@RequestBody TenantService.CreateReq req) {
        return R.ok(tenantService.create(req));
    }

    @PutMapping("/{id}")
    public R<PlatformTenant> update(@PathVariable Long id, @RequestBody PlatformTenant patch) {
        return R.ok(tenantService.update(id, patch));
    }

    @PostMapping("/{id}/status")
    public R<Void> status(@PathVariable Long id, @RequestParam String status) {
        tenantService.changeStatus(id, status);
        return R.ok();
    }

    @PostMapping("/{id}/renew")
    public R<PlatformTenant> renew(@PathVariable Long id, @RequestParam(defaultValue = "12") int months) {
        return R.ok(tenantService.renew(id, months));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return R.ok();
    }

    @GetMapping("/plans")
    public R<List<PlatformPlan>> plans() {
        return R.ok(planMapper.selectList(new LambdaQueryWrapper<PlatformPlan>()
                .eq(PlatformPlan::getStatus, "ACTIVE")));
    }

    @GetMapping("/{id}/audits")
    public R<List<PlatformAudit>> audits(@PathVariable Long id) {
        return R.ok(auditMapper.selectList(new LambdaQueryWrapper<PlatformAudit>()
                .eq(PlatformAudit::getTenantId, id).orderByDesc(PlatformAudit::getCreatedAt).last("LIMIT 200")));
    }

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        long total = tenantMapper.selectCount(null);
        long active = tenantMapper.selectCount(new LambdaQueryWrapper<PlatformTenant>()
                .eq(PlatformTenant::getStatus, "ACTIVE"));
        long trial = tenantMapper.selectCount(new LambdaQueryWrapper<PlatformTenant>()
                .eq(PlatformTenant::getStatus, "TRIAL"));
        long suspended = tenantMapper.selectCount(new LambdaQueryWrapper<PlatformTenant>()
                .eq(PlatformTenant::getStatus, "SUSPENDED"));
        return R.ok(Map.of("total", total, "active", active, "trial", trial, "suspended", suspended));
    }
}
