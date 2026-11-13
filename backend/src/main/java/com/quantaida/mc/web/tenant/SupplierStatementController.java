package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizSupplierStatement;
import com.quantaida.mc.mapper.BizSupplierStatementMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/supplier-statements")
public class SupplierStatementController extends BaseCrudController<BizSupplierStatement> {
    public SupplierStatementController(BizSupplierStatementMapper mapper) {
        super(mapper, "supplierstatement", "period");
    }
}
