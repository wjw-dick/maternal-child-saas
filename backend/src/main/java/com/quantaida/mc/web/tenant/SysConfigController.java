package com.quantaida.mc.web.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.AuditService;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.SystemEntities.SysConfig;
import com.quantaida.mc.mapper.SysConfigMapper;
import com.quantaida.mc.security.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant/config")
@RequiredArgsConstructor
public class SysConfigController {
    private final SysConfigMapper configMapper;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @GetMapping
    public R<Map<String, String>> all() {
        permissionService.require("config:read");
        Map<String, String> map = new LinkedHashMap<>();
        configMapper.selectList(null).forEach(c -> map.put(c.getConfigKey(), c.getConfigValue()));
        return R.ok(map);
    }

    @PutMapping
    public R<Void> save(@RequestBody Map<String, String> body) {
        permissionService.require("config:write");
        body.forEach((key, value) -> {
            SysConfig config = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, key));
            if (config == null) {
                config = new SysConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                config.setGroupCode("custom");
                config.setEncrypted(0);
                configMapper.insert(config);
            } else {
                config.setConfigValue(value);
                configMapper.updateById(config);
            }
        });
        auditService.log("config", "save:" + body.keySet(), "SUCCESS");
        return R.ok();
    }
}
