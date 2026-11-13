package com.quantaida.mc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.EventBus;
import com.quantaida.mc.common.Guard;
import com.quantaida.mc.common.NoGenerator;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.BizEntities.BizAppointment;
import com.quantaida.mc.entity.BizEntities.BizDispatch;
import com.quantaida.mc.entity.BizEntities.BizSchedule;
import com.quantaida.mc.mapper.BizAppointmentMapper;
import com.quantaida.mc.mapper.BizDispatchMapper;
import com.quantaida.mc.mapper.BizScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final BizAppointmentMapper appointmentMapper;
    private final BizDispatchMapper dispatchMapper;
    private final BizScheduleMapper scheduleMapper;
    private final NoGenerator noGenerator;
    private final Guard guard;
    private final EventBus eventBus;

    public record CreateReq(Long memberId, Long babyId, Long storeId, Long serviceId, Long staffId,
                            LocalDate appointDate, String timeSlot, String appointType, String address,
                            String contact, String phone, String remark, String idempotencyKey) {
    }

    @Transactional
    public BizAppointment create(CreateReq req) {
        if (req.appointDate() == null || req.timeSlot() == null) {
            throw new BizException(400, "请选择预约日期与时段");
        }
        if (req.idempotencyKey() != null) {
            guard.idempotent("appointment:" + TenantContext.getTenantId() + ":" + req.idempotencyKey());
        }
        if (req.staffId() != null) {
            BizSchedule schedule = scheduleMapper.selectOne(new LambdaQueryWrapper<BizSchedule>()
                    .eq(BizSchedule::getStaffId, req.staffId())
                    .eq(BizSchedule::getWorkDate, req.appointDate())
                    .eq(BizSchedule::getTimeSlot, req.timeSlot())
                    .last("LIMIT 1"));
            if (schedule != null) {
                int capacity = schedule.getCapacity() == null ? 1 : schedule.getCapacity();
                int booked = schedule.getBooked() == null ? 0 : schedule.getBooked();
                if (booked >= capacity) {
                    throw new BizException(400, "该时段已约满，请选择其他时段");
                }
                schedule.setBooked(booked + 1);
                scheduleMapper.updateById(schedule);
            }
        }
        BizAppointment appointment = new BizAppointment();
        appointment.setAppointmentNo(noGenerator.gen("AP"));
        appointment.setMemberId(req.memberId());
        appointment.setBabyId(req.babyId());
        appointment.setStoreId(req.storeId());
        appointment.setServiceId(req.serviceId());
        appointment.setStaffId(req.staffId());
        appointment.setAppointDate(req.appointDate());
        appointment.setTimeSlot(req.timeSlot());
        appointment.setAppointType(req.appointType() == null ? "STORE" : req.appointType());
        appointment.setAddress(req.address());
        appointment.setContact(req.contact());
        appointment.setPhone(req.phone());
        appointment.setRemark(req.remark());
        appointment.setVerifyCode(String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000)));
        appointment.setStatus(req.staffId() == null ? "PENDING" : "CONFIRMED");
        appointmentMapper.insert(appointment);
        if (req.staffId() != null) {
            BizDispatch dispatch = new BizDispatch();
            dispatch.setAppointmentId(appointment.getId());
            dispatch.setStaffId(req.staffId());
            dispatch.setStatus("PENDING");
            dispatchMapper.insert(dispatch);
        }
        eventBus.publish("appointment.created", Map.of("appointmentId", appointment.getId(),
                "appointmentNo", appointment.getAppointmentNo(), "appointDate", appointment.getAppointDate().toString(),
                "timeSlot", appointment.getTimeSlot(), "status", appointment.getStatus()));
        long delay = Duration.between(LocalDateTime.now(),
                appointment.getAppointDate().minusDays(1).atTime(10, 0)).toMillis();
        if (delay > 0) {
            eventBus.publishDelayed("notify.appointment-reminder", Map.of(
                    "appointmentId", appointment.getId(),
                    "appointmentNo", appointment.getAppointmentNo()), delay);
        }
        return appointment;
    }

    public BizAppointment dispatch(Long appointmentId, Long staffId) {
        BizAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        appointment.setStaffId(staffId);
        appointment.setStatus("CONFIRMED");
        appointmentMapper.updateById(appointment);
        BizDispatch dispatch = dispatchMapper.selectOne(new LambdaQueryWrapper<BizDispatch>()
                .eq(BizDispatch::getAppointmentId, appointmentId).last("LIMIT 1"));
        if (dispatch == null) {
            dispatch = new BizDispatch();
            dispatch.setAppointmentId(appointmentId);
            dispatch.setStaffId(staffId);
            dispatch.setStatus("PENDING");
            dispatchMapper.insert(dispatch);
        } else {
            dispatch.setStaffId(staffId);
            dispatch.setStatus("PENDING");
            dispatchMapper.updateById(dispatch);
        }
        return appointment;
    }

    public BizAppointment verify(String code) {
        BizAppointment appointment = appointmentMapper.selectOne(new LambdaQueryWrapper<BizAppointment>()
                .eq(BizAppointment::getVerifyCode, code).last("LIMIT 1"));
        if (appointment == null) {
            throw new BizException(404, "核销码无效");
        }
        if (!"CONFIRMED".equals(appointment.getStatus()) && !"PENDING".equals(appointment.getStatus())) {
            throw new BizException(400, "当前预约状态不可核销");
        }
        appointment.setStatus("COMPLETED");
        appointmentMapper.updateById(appointment);
        return appointment;
    }

    public BizAppointment cancel(Long id) {
        BizAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        appointment.setStatus("CANCELLED");
        appointmentMapper.updateById(appointment);
        eventBus.publish("appointment.cancelled", Map.of("appointmentId", id,
                "appointmentNo", appointment.getAppointmentNo()));
        if (appointment.getStaffId() != null) {
            BizSchedule schedule = scheduleMapper.selectOne(new LambdaQueryWrapper<BizSchedule>()
                    .eq(BizSchedule::getStaffId, appointment.getStaffId())
                    .eq(BizSchedule::getWorkDate, appointment.getAppointDate())
                    .eq(BizSchedule::getTimeSlot, appointment.getTimeSlot())
                    .last("LIMIT 1"));
            if (schedule != null && schedule.getBooked() != null && schedule.getBooked() > 0) {
                schedule.setBooked(schedule.getBooked() - 1);
                scheduleMapper.updateById(schedule);
            }
        }
        return appointment;
    }

    public BizAppointment reschedule(Long id, LocalDate date, String slot) {
        BizAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        if ("COMPLETED".equals(appointment.getStatus())) {
            throw new BizException(400, "已完成预约不可改约");
        }
        cancel(id);
        BizAppointment fresh = appointmentMapper.selectById(id);
        fresh.setStatus("CONFIRMED");
        fresh.setAppointDate(date);
        fresh.setTimeSlot(slot);
        fresh.setVerifyCode(String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000)));
        appointmentMapper.updateById(fresh);
        return fresh;
    }

    public BizDispatch accept(Long appointmentId, Long staffId) {
        BizDispatch dispatch = dispatchMapper.selectOne(new LambdaQueryWrapper<BizDispatch>()
                .eq(BizDispatch::getAppointmentId, appointmentId).last("LIMIT 1"));
        if (dispatch == null) {
            dispatch = new BizDispatch();
            dispatch.setAppointmentId(appointmentId);
            dispatch.setStaffId(staffId);
        }
        dispatch.setStatus("ACCEPTED");
        dispatch.setAcceptedAt(LocalDateTime.now());
        if (dispatch.getId() == null) {
            dispatchMapper.insert(dispatch);
        } else {
            dispatchMapper.updateById(dispatch);
        }
        return dispatch;
    }
}
