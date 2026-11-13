package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizPointsRule;
import com.quantaida.mc.mapper.BizPointsRuleMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/points-rules")
public class PointsRuleController extends BaseCrudController<BizPointsRule> {
    public PointsRuleController(BizPointsRuleMapper mapper) {
        super(mapper, "pointsRule", "name,rule_type");
    }
}
