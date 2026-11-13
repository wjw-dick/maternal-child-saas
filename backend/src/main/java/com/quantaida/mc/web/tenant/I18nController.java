package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.SystemEntities.SysI18n;
import com.quantaida.mc.mapper.SysI18nMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/i18n")
public class I18nController extends BaseCrudController<SysI18n> {
    public I18nController(SysI18nMapper mapper) {
        super(mapper, "i18n", "i18nKey");
    }
}
