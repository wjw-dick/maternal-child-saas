package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.CmsEntities.CmsCase;
import com.quantaida.mc.mapper.CmsCaseMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/cases")
public class CaseController extends BaseCrudController<CmsCase> {
    public CaseController(CmsCaseMapper mapper) {
        super(mapper, "cmscase", "title");
    }
}
