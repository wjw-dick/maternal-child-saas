package com.quantaida.mc.web.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.PlatformEntities.PlatformBill;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.mapper.PlatformBillMapper;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform/dashboard")
@RequiredArgsConstructor
public class PlatformDashboardController {
    private final PlatformTenantMapper tenantMapper;
    private final PlatformBillMapper billMapper;

    @GetMapping
    public R<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        long total = tenantMapper.selectCount(null);
        long active = tenantMapper.selectCount(new LambdaQueryWrapper<PlatformTenant>()
                .eq(PlatformTenant::getStatus, "ACTIVE"));
        long expiring = tenantMapper.selectCount(new LambdaQueryWrapper<PlatformTenant>()
                .le(PlatformTenant::getExpireAt, LocalDateTime.now().plusDays(30))
                .ge(PlatformTenant::getExpireAt, LocalDateTime.now()));
        List<PlatformBill> bills = billMapper.selectList(new LambdaQueryWrapper<PlatformBill>()
                .eq(PlatformBill::getStatus, "PAID"));
        BigDecimal revenue = bills.stream().map(PlatformBill::getAmount)
                .filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("tenantTotal", total);
        data.put("tenantActive", active);
        data.put("tenantExpiring", expiring);
        data.put("revenue", revenue);
        data.put("bills", bills.size());
        return R.ok(data);
    }
}
