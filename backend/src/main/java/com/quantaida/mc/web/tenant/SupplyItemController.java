package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizSupplyItem;
import com.quantaida.mc.mapper.BizSupplyItemMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/supply-items")
public class SupplyItemController extends BaseCrudController<BizSupplyItem> {
    public SupplyItemController(BizSupplyItemMapper mapper) {
        super(mapper, "supplyitem", "");
    }
}
