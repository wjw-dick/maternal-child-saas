package com.quantaida.mc.web.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.AuditService;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.SystemEntities.SysMenuConfig;
import com.quantaida.mc.mapper.SysMenuConfigMapper;
import com.quantaida.mc.security.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 菜单可视化唯一出处（T29）：整树保存、多端、实时生效、清空二次确认。
 */
@RestController
@RequestMapping("/api/tenant/menu-config")
@RequiredArgsConstructor
public class MenuConfigController {
    private final SysMenuConfigMapper menuConfigMapper;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @GetMapping
    public R<List<SysMenuConfig>> list() {
        permissionService.require("menu:read");
        return R.ok(menuConfigMapper.selectList(null));
    }

    @GetMapping("/{terminal}")
    public R<SysMenuConfig> get(@PathVariable String terminal) {
        permissionService.require("menu:read");
        SysMenuConfig config = menuConfigMapper.selectOne(new LambdaQueryWrapper<SysMenuConfig>()
                .eq(SysMenuConfig::getTerminal, terminal));
        return R.ok(config);
    }

    @PutMapping("/{terminal}")
    public R<SysMenuConfig> save(@PathVariable String terminal, @RequestBody Map<String, Object> body) {
        permissionService.require("menu:write");
        String treeJson = String.valueOf(body.getOrDefault("treeJson", "[]"));
        if (treeJson == null || treeJson.length() > 100_000) {
            throw new BizException(400, "菜单树数据非法");
        }
        SysMenuConfig config = menuConfigMapper.selectOne(new LambdaQueryWrapper<SysMenuConfig>()
                .eq(SysMenuConfig::getTerminal, terminal));
        if (config == null) {
            config = new SysMenuConfig();
            config.setTerminal(terminal);
            config.setTreeJson(treeJson);
            config.setVersion(1);
            menuConfigMapper.insert(config);
        } else {
            config.setTreeJson(treeJson);
            config.setVersion((config.getVersion() == null ? 1 : config.getVersion()) + 1);
            menuConfigMapper.updateById(config);
        }
        auditService.log("menu", "save:" + terminal, "SUCCESS");
        return R.ok(config);
    }

    @DeleteMapping("/{terminal}")
    public R<Void> reset(@PathVariable String terminal, @RequestParam(defaultValue = "false") boolean confirm) {
        permissionService.require("menu:write");
        if (!confirm) {
            throw new BizException(400, "清空菜单需二次确认");
        }
        SysMenuConfig config = menuConfigMapper.selectOne(new LambdaQueryWrapper<SysMenuConfig>()
                .eq(SysMenuConfig::getTerminal, terminal));
        if (config != null) {
            menuConfigMapper.deleteById(config.getId());
        }
        auditService.log("menu", "reset:" + terminal, "SUCCESS");
        return R.ok();
    }
}
