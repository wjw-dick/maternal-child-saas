package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.SystemEntities.SysLanguage;
import com.quantaida.mc.mapper.SysLanguageMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/languages")
public class LanguageController extends BaseCrudController<SysLanguage> {
    public LanguageController(SysLanguageMapper mapper) {
        super(mapper, "i18n", "name,code");
    }
}
