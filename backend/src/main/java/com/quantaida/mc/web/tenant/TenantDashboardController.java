package com.quantaida.mc.web.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.R;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.PlatformEntities.PlatformNotice;
import com.quantaida.mc.entity.PlatformEntities.PlatformNoticeRead;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.mapper.PlatformNoticeMapper;
import com.quantaida.mc.mapper.PlatformNoticeReadMapper;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import com.quantaida.mc.mapper.SysUserMapper;
import com.quantaida.mc.entity.SystemEntities.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantDashboardController {
    private final JdbcTemplate jdbc;
    private final PlatformNoticeMapper noticeMapper;
    private final PlatformNoticeReadMapper noticeReadMapper;
    private final PlatformTenantMapper tenantMapper;
    private final SysUserMapper userMapper;

    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        Long tid = TenantContext.getTenantId();
        Map<String, Object> data = new HashMap<>();
        data.put("members", count("select count(*) from biz_member where tenant_id=? and deleted=0", tid));
        data.put("appointmentsToday", count("select count(*) from biz_appointment where tenant_id=? and deleted=0 and appoint_date=?", tid, LocalDate.now()));
        data.put("ordersToday", count("select count(*) from biz_order where tenant_id=? and deleted=0 and created_at>=?", tid, LocalDate.now().atStartOfDay()));
        data.put("revenue", sum("select coalesce(sum(pay_amount),0) from biz_order where tenant_id=? and deleted=0 and pay_status='PAID'", tid));
        data.put("ordersPending", count("select count(*) from biz_order where tenant_id=? and deleted=0 and status='PENDING'", tid));
        data.put("appointmentsPending", count("select count(*) from biz_appointment where tenant_id=? and deleted=0 and status='PENDING'", tid));
        data.put("lowStock", count("select count(*) from biz_sku where tenant_id=? and deleted=0 and stock<=safety_stock", tid));
        data.put("licenseExpiring", count("select count(*) from biz_staff_license where tenant_id=? and deleted=0 and expire_at<=?", tid, LocalDate.now().plusDays(30)));
        return R.ok(data);
    }

    @GetMapping("/report")
    public R<Map<String, Object>> report() {
        Long tid = TenantContext.getTenantId();
        Map<String, Object> data = new HashMap<>();
        data.put("memberCount", count("select count(*) from biz_member where tenant_id=? and deleted=0", tid));
        data.put("orderCount", count("select count(*) from biz_order where tenant_id=? and deleted=0", tid));
        data.put("paidAmount", sum("select coalesce(sum(pay_amount),0) from biz_order where tenant_id=? and deleted=0 and pay_status='PAID'", tid));
        data.put("appointmentCount", count("select count(*) from biz_appointment where tenant_id=? and deleted=0", tid));
        data.put("walletBalance", sum("select coalesce(sum(balance),0) from biz_wallet where tenant_id=? and deleted=0", tid));
        data.put("commission", sum("select coalesce(sum(amount),0) from biz_commission_flow where tenant_id=? and deleted=0", tid));
        List<Map<String, Object>> daily = jdbc.queryForList(
                "select cast(created_at as date) d, count(*) c, coalesce(sum(pay_amount),0) amount from biz_order " +
                        "where tenant_id=? and deleted=0 group by cast(created_at as date) order by d desc limit 7", tid);
        data.put("daily", daily);
        return R.ok(data);
    }

    @GetMapping("/notices")
    public R<List<PlatformNotice>> notices() {
        Long tid = TenantContext.getTenantId();
        PlatformTenant tenant = tenantMapper.selectById(tid);
        List<PlatformNotice> all = noticeMapper.selectList(new LambdaQueryWrapper<PlatformNotice>()
                .eq(PlatformNotice::getStatus, "PUBLISHED").orderByDesc(PlatformNotice::getPublishedAt));
        return R.ok(all.stream().filter(n -> n.getTargetType() == null || "ALL".equals(n.getTargetType())
                || (n.getTargetTenantIds() != null && n.getTargetTenantIds().contains(String.valueOf(tid)))
                || ("PLAN".equals(n.getTargetType()) && tenant != null && n.getTargetTenantIds() != null
                && n.getTargetTenantIds().contains(String.valueOf(tenant.getPlanId())))).toList());
    }

    @PostMapping("/notices/{id}/read")
    public R<Void> read(@PathVariable Long id) {
        PlatformNoticeRead read = new PlatformNoticeRead();
        read.setNoticeId(id);
        read.setTenantId(TenantContext.getTenantId());
        SysUser user = userMapper.selectById(com.quantaida.mc.common.UserContext.userId());
        read.setUserId(user == null ? null : user.getId());
        read.setReadAt(LocalDateTime.now());
        noticeReadMapper.insert(read);
        return R.ok();
    }

    private Long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0 : value;
    }

    private java.math.BigDecimal sum(String sql, Object... args) {
        java.math.BigDecimal value = jdbc.queryForObject(sql, java.math.BigDecimal.class, args);
        return value == null ? java.math.BigDecimal.ZERO : value;
    }
}
