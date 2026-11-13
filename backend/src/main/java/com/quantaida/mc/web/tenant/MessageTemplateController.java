package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.SystemEntities.SysMessageTemplate;
import com.quantaida.mc.mapper.SysMessageTemplateMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/message-templates")
public class MessageTemplateController extends BaseCrudController<SysMessageTemplate> {
    public MessageTemplateController(SysMessageTemplateMapper mapper) {
        super(mapper, "message", "node");
    }
}
