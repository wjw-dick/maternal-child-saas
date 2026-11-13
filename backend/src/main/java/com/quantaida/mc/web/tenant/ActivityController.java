package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizActivity;
import com.quantaida.mc.mapper.BizActivityMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/activity")
public class ActivityController extends BaseCrudController<BizActivity> {
    public ActivityController(BizActivityMapper mapper) {
        super(mapper, "activity", "title");
    }
}
