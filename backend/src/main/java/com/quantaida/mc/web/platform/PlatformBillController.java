package com.quantaida.mc.web.platform;

import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.PlatformEntities.PlatformBill;
import com.quantaida.mc.entity.PlatformEntities.PlatformPlan;
import com.quantaida.mc.mapper.PlatformBillMapper;
import com.quantaida.mc.mapper.PlatformPlanMapper;
import com.quantaida.mc.service.TenantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform/bills")
@RequiredArgsConstructor
public class PlatformBillController {
    private final PlatformBillMapper billMapper;
    private final PlatformPlanMapper planMapper;
    private final TenantService tenantService;

    @GetMapping
    public R<IPage<PlatformBill>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) Long tenantId,
                                       @RequestParam(required = false) String period,
                                       @RequestParam(required = false) String status) {
        LambdaQueryWrapper<PlatformBill> qw = new LambdaQueryWrapper<>();
        if (tenantId != null) qw.eq(PlatformBill::getTenantId, tenantId);
        if (StringUtils.hasText(period)) qw.eq(PlatformBill::getPeriod, period);
        if (StringUtils.hasText(status)) qw.eq(PlatformBill::getStatus, status);
        qw.orderByDesc(PlatformBill::getCreatedAt);
        return R.ok(billMapper.selectPage(new Page<>(page, size), qw));
    }

    @PostMapping("/generate")
    public R<Map<String, Object>> generate(@RequestParam(required = false) String period) {
        int count = tenantService.generateBills(period);
        return R.ok(Map.of("generated", count));
    }

    @PostMapping("/{id}/pay")
    public R<Void> pay(@PathVariable Long id) {
        tenantService.markPaid(id);
        return R.ok();
    }

    @GetMapping("/summary")
    public R<Map<String, Object>> summary() {
        List<PlatformBill> bills = billMapper.selectList(new LambdaQueryWrapper<PlatformBill>()
                .ge(PlatformBill::getCreatedAt, LocalDateTime.now().minusMonths(1)));
        BigDecimal total = bills.stream().map(PlatformBill::getAmount).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = bills.stream().filter(b -> "PAID".equals(b.getStatus()))
                .map(PlatformBill::getAmount).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<PlatformPlan> plans = planMapper.selectList(null);
        return R.ok(Map.of("total", total, "paid", paid, "plans", plans.size()));
    }
}
