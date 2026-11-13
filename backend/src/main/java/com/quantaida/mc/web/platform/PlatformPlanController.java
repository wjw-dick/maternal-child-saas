package com.quantaida.mc.web.platform;

import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.PlatformEntities.PlatformPlan;
import com.quantaida.mc.mapper.PlatformPlanMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/plans")
public class PlatformPlanController extends BaseCrudController<PlatformPlan> {
    public PlatformPlanController(PlatformPlanMapper mapper) {
        super(mapper, "platform:plan", "name,code");
    }
}
