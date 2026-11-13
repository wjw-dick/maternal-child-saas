package com.quantaida.mc.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.quantaida.mc.entity.BizEntities.BizAppointment;
import com.quantaida.mc.entity.BizEntities.BizCoupon;
import com.quantaida.mc.mapper.BizAppointmentMapper;
import com.quantaida.mc.mapper.BizCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * 租户每日维护任务：优惠券过期处理 + 次日预约提醒事件。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyMaintenanceJob implements TenantJob {
    private final BizCouponMapper couponMapper;
    private final BizAppointmentMapper appointmentMapper;
    private final EventBus eventBus;

    @Override
    public String name() {
        return "daily-maintenance";
    }

    @Override
    public void run(Long tenantId) {
        int expired = couponMapper.update(null, new LambdaUpdateWrapper<BizCoupon>()
                .eq(BizCoupon::getStatus, "ACTIVE")
                .lt(BizCoupon::getValidTo, LocalDate.now())
                .set(BizCoupon::getStatus, "EXPIRED"));
        long tomorrowCount = appointmentMapper.selectCount(new LambdaQueryWrapper<BizAppointment>()
                .eq(BizAppointment::getAppointDate, LocalDate.now().plusDays(1))
                .in(BizAppointment::getStatus, "PENDING", "CONFIRMED"));
        if (tomorrowCount > 0) {
            eventBus.publish("notify.appointment-reminder", Map.of(
                    "tenantId", tenantId,
                    "appointmentCount", tomorrowCount,
                    "remindDate", LocalDate.now().plusDays(1).toString()));
        }
        log.info("[JOB] 租户 {} 维护完成：过期券={} 次日预约={}", tenantId, expired, tomorrowCount);
    }
}
