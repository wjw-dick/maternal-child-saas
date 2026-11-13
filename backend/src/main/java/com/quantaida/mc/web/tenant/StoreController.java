package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizStore;
import com.quantaida.mc.mapper.BizStoreMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/store")
public class StoreController extends BaseCrudController<BizStore> {
    public StoreController(BizStoreMapper mapper) {
        super(mapper, "store", "name,code,address");
    }
}
