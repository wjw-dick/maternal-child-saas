package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizActivitySignup;
import com.quantaida.mc.mapper.BizActivitySignupMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/activity-signups")
public class ActivitySignupController extends BaseCrudController<BizActivitySignup> {
    public ActivitySignupController(BizActivitySignupMapper mapper) {
        super(mapper, "activitysignup", "phone");
    }
}
