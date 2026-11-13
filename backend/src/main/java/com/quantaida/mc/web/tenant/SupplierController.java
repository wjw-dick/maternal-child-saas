package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizSupplier;
import com.quantaida.mc.mapper.BizSupplierMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/supplier")
public class SupplierController extends BaseCrudController<BizSupplier> {
    public SupplierController(BizSupplierMapper mapper) {
        super(mapper, "supplier", "name,code");
    }
}
