package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizDistributor;
import com.quantaida.mc.mapper.BizDistributorMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/distributor")
public class DistributorController extends BaseCrudController<BizDistributor> {
    public DistributorController(BizDistributorMapper mapper) {
        super(mapper, "distributor", "name,code");
    }
}
