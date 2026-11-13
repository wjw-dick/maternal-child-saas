package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizSupplyOrder;
import com.quantaida.mc.mapper.BizSupplyOrderMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/supply-orders")
public class SupplyOrderController extends BaseCrudController<BizSupplyOrder> {
    public SupplyOrderController(BizSupplyOrderMapper mapper) {
        super(mapper, "supply", "supplyNo");
    }
}
