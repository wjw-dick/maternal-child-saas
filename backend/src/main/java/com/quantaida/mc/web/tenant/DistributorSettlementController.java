package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizDistributorSettlement;
import com.quantaida.mc.mapper.BizDistributorSettlementMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/distributor-settlements")
public class DistributorSettlementController extends BaseCrudController<BizDistributorSettlement> {
    public DistributorSettlementController(BizDistributorSettlementMapper mapper) {
        super(mapper, "settlement", "settlementNo");
    }
}
