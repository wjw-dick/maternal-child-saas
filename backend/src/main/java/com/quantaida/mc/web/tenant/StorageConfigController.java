package com.quantaida.mc.web.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.AuditService;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.SystemEntities.SysStorageConfig;
import com.quantaida.mc.mapper.SysStorageConfigMapper;
import com.quantaida.mc.security.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant/storage-config")
@RequiredArgsConstructor
public class StorageConfigController {
    private final SysStorageConfigMapper storageConfigMapper;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @GetMapping
    public R<SysStorageConfig> get() {
        permissionService.require("storage:read");
        SysStorageConfig config = storageConfigMapper.selectOne(new LambdaQueryWrapper<>());
        if (config == null) {
            config = new SysStorageConfig();
            config.setDriver("LOCAL");
        }
        return R.ok(config);
    }

    @GetMapping("/drivers")
    public R<List<String>> drivers() {
        return R.ok(List.of("LOCAL", "S3", "OSS", "COS", "MINIO"));
    }

    @PostMapping("/test")
    public R<Map<String, Object>> test(@RequestBody Map<String, String> body) {
        permissionService.require("storage:write");
        String driver = body.getOrDefault("driver", "LOCAL");
        if (!List.of("LOCAL", "S3", "OSS", "COS", "MINIO").contains(driver)) {
            throw new BizException(400, "非法驱动");
        }
        if (!"LOCAL".equals(driver)) {
            return R.ok(Map.of("ok", false, "message", driver + " 需安装对应 SDK 后测试连接"));
        }
        String root = body.getOrDefault("root", "./data/storage");
        try {
            Path path = Paths.get(root).toAbsolutePath().normalize();
            Files.createDirectories(path);
            Path probe = path.resolve(".write-test");
            Files.writeString(probe, "ok");
            Files.deleteIfExists(probe);
            return R.ok(Map.of("ok", true, "message", "本地存储连接成功"));
        } catch (Exception e) {
            return R.ok(Map.of("ok", false, "message", "目录不可写：" + e.getMessage()));
        }
    }

    @PutMapping
    public R<SysStorageConfig> save(@RequestBody Map<String, String> body,
                                    @RequestParam(defaultValue = "false") boolean confirmSwitch) {
        permissionService.require("storage:write");
        String driver = body.getOrDefault("driver", "LOCAL");
        SysStorageConfig config = storageConfigMapper.selectOne(new LambdaQueryWrapper<>());
        if (config == null) {
            config = new SysStorageConfig();
            config.setDriver(driver);
            config.setConfigJson(body.getOrDefault("configJson", "{}"));
            config.setEnabled(1);
            storageConfigMapper.insert(config);
        } else {
            if (!driver.equals(config.getDriver()) && !confirmSwitch) {
                throw new BizException(400, "切换存储驱动需二次确认");
            }
            config.setDriver(driver);
            config.setConfigJson(body.getOrDefault("configJson", "{}"));
            config.setEnabled(1);
            storageConfigMapper.updateById(config);
        }
        auditService.log("storage", "save:" + driver, "SUCCESS");
        return R.ok(config);
    }
}
