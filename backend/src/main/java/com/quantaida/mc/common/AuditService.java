package com.quantaida.mc.common;

import com.quantaida.mc.entity.SystemEntities.SysAuditLog;
import com.quantaida.mc.mapper.SysAuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final SysAuditLogMapper auditLogMapper;

    public void log(String module, String action, String result) {
        try {
            SysAuditLog logRow = new SysAuditLog();
            UserContext.LoginUser user = UserContext.get();
            if (user != null) {
                logRow.setUserId(user.userId());
                logRow.setUserName(user.username());
            }
            logRow.setModule(module);
            logRow.setAction(action);
            logRow.setResult(result);
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                logRow.setMethod(attrs.getRequest().getMethod() + " " + attrs.getRequest().getRequestURI());
                logRow.setIp(attrs.getRequest().getRemoteAddr());
            }
            auditLogMapper.insert(logRow);
        } catch (Exception e) {
            log.warn("审计日志写入失败: {}", e.getMessage());
        }
    }
}
