package com.quantaida.mc.web.tenant;

import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.BizEntities.BizAppointment;
import com.quantaida.mc.mapper.BizAppointmentMapper;
import com.quantaida.mc.service.AppointmentService;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tenant/appointments")
public class AppointmentController extends BaseCrudController<BizAppointment> {
    private final AppointmentService appointmentService;

    public AppointmentController(BizAppointmentMapper mapper, AppointmentService appointmentService) {
        super(mapper, "appointment", "appointmentNo,contact,phone");
        this.appointmentService = appointmentService;
    }

    @PostMapping("/create")
    public R<BizAppointment> create(@RequestBody AppointmentService.CreateReq req) {
        permissionService.require("appointment:write");
        return R.ok(appointmentService.create(req));
    }

    @PostMapping("/{id}/dispatch")
    public R<BizAppointment> dispatch(@PathVariable Long id, @RequestParam Long staffId) {
        permissionService.require("appointment:write");
        return R.ok(appointmentService.dispatch(id, staffId));
    }

    @PostMapping("/verify")
    public R<BizAppointment> verify(@RequestParam String code) {
        permissionService.require("appointment:write");
        return R.ok(appointmentService.verify(code));
    }

    @PostMapping("/{id}/cancel")
    public R<BizAppointment> cancel(@PathVariable Long id) {
        permissionService.require("appointment:write");
        return R.ok(appointmentService.cancel(id));
    }

    @PostMapping("/{id}/reschedule")
    public R<BizAppointment> reschedule(@PathVariable Long id, @RequestParam LocalDate date,
                                        @RequestParam String timeSlot) {
        permissionService.require("appointment:write");
        return R.ok(appointmentService.reschedule(id, date, timeSlot));
    }
}
