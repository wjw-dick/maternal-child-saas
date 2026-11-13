package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizProduct;
import com.quantaida.mc.mapper.BizProductMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/product")
public class ProductController extends BaseCrudController<BizProduct> {
    public ProductController(BizProductMapper mapper) {
        super(mapper, "product", "name,spuCode,brand");
    }
}
