package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizService;
import com.quantaida.mc.mapper.BizServiceMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/service")
public class ServiceController extends BaseCrudController<BizService> {
    public ServiceController(BizServiceMapper mapper) {
        super(mapper, "service", "name,code");
    }
}
