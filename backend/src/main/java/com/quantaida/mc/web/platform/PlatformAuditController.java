package com.quantaida.mc.web.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.PlatformEntities.PlatformAudit;
import com.quantaida.mc.mapper.PlatformAuditMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/audit")
@RequiredArgsConstructor
public class PlatformAuditController {
    private final PlatformAuditMapper auditMapper;

    @GetMapping
    public R<IPage<PlatformAudit>> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size,
                                        @RequestParam(required = false) Long tenantId,
                                        @RequestParam(required = false) String action) {
        LambdaQueryWrapper<PlatformAudit> qw = new LambdaQueryWrapper<>();
        if (tenantId != null) qw.eq(PlatformAudit::getTenantId, tenantId);
        if (StringUtils.hasText(action)) qw.like(PlatformAudit::getAction, action);
        qw.orderByDesc(PlatformAudit::getCreatedAt);
        return R.ok(auditMapper.selectPage(new Page<>(page, size), qw));
    }
}
