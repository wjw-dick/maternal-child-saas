package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizSku;
import com.quantaida.mc.mapper.BizSkuMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/sku")
public class SkuController extends BaseCrudController<BizSku> {
    public SkuController(BizSkuMapper mapper) {
        super(mapper, "sku", "skuCode");
    }
}
