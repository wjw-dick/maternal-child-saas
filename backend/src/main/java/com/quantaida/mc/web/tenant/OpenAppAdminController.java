package com.quantaida.mc.web.tenant;

import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.SystemEntities.OpenApp;
import com.quantaida.mc.mapper.OpenAppMapper;
import com.quantaida.mc.service.QuotaService;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tenant/open-apps")
public class OpenAppAdminController extends BaseCrudController<OpenApp> {
    private final QuotaService quotaService;

    public OpenAppAdminController(OpenAppMapper mapper, QuotaService quotaService) {
        super(mapper, "openapp", "name,appKey");
        this.quotaService = quotaService;
    }

    @Override
    protected void beforeCreate(OpenApp app) {
        if (!StringUtils.hasText(app.getAppKey())) {
            app.setAppKey("ak_" + UUID.randomUUID().toString().replace("-", ""));
        }
        if (!StringUtils.hasText(app.getAppSecret())) {
            app.setAppSecret(UUID.randomUUID().toString().replace("-", ""));
        }
        app.setUsedApi(0);
        if (app.getApiQuota() == null) {
            app.setApiQuota(10000);
        }
        if (!StringUtils.hasText(app.getStatus())) {
            app.setStatus("ACTIVE");
        }
    }

    @Override
    protected void beforeUpdate(OpenApp app) {
        if (StringUtils.hasText(app.getAppSecret()) && app.getAppSecret().contains("******")) {
            app.setAppSecret(null);
        }
    }
}
