package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizSchedule;
import com.quantaida.mc.mapper.BizScheduleMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/schedule")
public class ScheduleController extends BaseCrudController<BizSchedule> {
    public ScheduleController(BizScheduleMapper mapper) {
        super(mapper, "schedule", "timeSlot");
    }
}
