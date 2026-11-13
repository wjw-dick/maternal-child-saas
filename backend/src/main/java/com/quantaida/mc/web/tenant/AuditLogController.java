package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.SystemEntities.SysAuditLog;
import com.quantaida.mc.mapper.SysAuditLogMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/audit-logs")
public class AuditLogController extends BaseCrudController<SysAuditLog> {
    public AuditLogController(SysAuditLogMapper mapper) {
        super(mapper, "audit", "action");
    }
}
