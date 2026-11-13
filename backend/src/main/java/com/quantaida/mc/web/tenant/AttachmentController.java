package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.SystemEntities.SysAttachment;
import com.quantaida.mc.mapper.SysAttachmentMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/attachments")
public class AttachmentController extends BaseCrudController<SysAttachment> {
    public AttachmentController(SysAttachmentMapper mapper) {
        super(mapper, "attachment", "name");
    }
}
