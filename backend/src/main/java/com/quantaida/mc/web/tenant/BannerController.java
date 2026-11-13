package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.CmsEntities.CmsBanner;
import com.quantaida.mc.mapper.CmsBannerMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/banner")
public class BannerController extends BaseCrudController<CmsBanner> {
    public BannerController(CmsBannerMapper mapper) {
        super(mapper, "banner", "title");
    }
}
