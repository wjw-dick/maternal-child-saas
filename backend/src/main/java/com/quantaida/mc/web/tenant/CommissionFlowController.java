package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizCommissionFlow;
import com.quantaida.mc.mapper.BizCommissionFlowMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/commission-flows")
public class CommissionFlowController extends BaseCrudController<BizCommissionFlow> {
    public CommissionFlowController(BizCommissionFlowMapper mapper) {
        super(mapper, "commission", "");
    }
}
