package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizBaby;
import com.quantaida.mc.mapper.BizBabyMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/baby")
public class BabyController extends BaseCrudController<BizBaby> {
    public BabyController(BizBabyMapper mapper) {
        super(mapper, "baby", "name");
    }
}
