package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizPointsFlow;
import com.quantaida.mc.mapper.BizPointsFlowMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/points-flows")
public class PointsFlowController extends BaseCrudController<BizPointsFlow> {
    public PointsFlowController(BizPointsFlowMapper mapper) {
        super(mapper, "pointsFlow", "biz_no,remark");
    }
}
