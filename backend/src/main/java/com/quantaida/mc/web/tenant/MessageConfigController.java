package com.quantaida.mc.web.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.quantaida.mc.common.AuditService;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.EventBus;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.SystemEntities.SysMessageChannel;
import com.quantaida.mc.entity.SystemEntities.SysMessageTemplate;
import com.quantaida.mc.entity.SystemEntities.SysSmsChannel;
import com.quantaida.mc.mapper.SysMessageChannelMapper;
import com.quantaida.mc.mapper.SysMessageTemplateMapper;
import com.quantaida.mc.mapper.SysSmsChannelMapper;
import com.quantaida.mc.security.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 消息配置：4 通道 + 6 短信渠道（全局互斥）+ 模板表。
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/message-config")
@RequiredArgsConstructor
public class MessageConfigController {
    private final SysMessageChannelMapper channelMapper;
    private final SysSmsChannelMapper smsMapper;
    private final SysMessageTemplateMapper templateMapper;
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final EventBus eventBus;

    @GetMapping
    public R<Map<String, Object>> overview() {
        permissionService.require("message:read");
        List<SysMessageChannel> channels = channelMapper.selectList(null);
        List<SysSmsChannel> sms = smsMapper.selectList(null);
        List<SysMessageTemplate> templates = templateMapper.selectList(null);
        return R.ok(Map.of("channels", channels, "sms", sms, "templates", templates));
    }

    @PutMapping("/channel")
    public R<SysMessageChannel> saveChannel(@RequestBody SysMessageChannel patch) {
        permissionService.require("message:write");
        SysMessageChannel channel = channelMapper.selectOne(new LambdaQueryWrapper<SysMessageChannel>()
                .eq(SysMessageChannel::getChannel, patch.getChannel()));
        if (channel == null) {
            throw new BizException(404, "通道不存在");
        }
        if (patch.getEnabled() != null) channel.setEnabled(patch.getEnabled());
        if (patch.getConfigJson() != null && !patch.getConfigJson().isBlank()) {
            channel.setConfigJson(patch.getConfigJson());
        }
        channelMapper.updateById(channel);
        auditService.log("message", "channel:" + channel.getChannel(), "SUCCESS");
        return R.ok(channel);
    }

    @PutMapping("/sms")
    @Transactional
    public R<SysSmsChannel> saveSms(@RequestBody SysSmsChannel patch) {
        permissionService.require("message:write");
        SysSmsChannel provider = smsMapper.selectOne(new LambdaQueryWrapper<SysSmsChannel>()
                .eq(SysSmsChannel::getProvider, patch.getProvider()));
        if (provider == null) {
            throw new BizException(404, "短信渠道不存在");
        }
        if (patch.getConfigJson() != null && !patch.getConfigJson().isBlank()) {
            provider.setConfigJson(patch.getConfigJson());
        }
        if (patch.getEnabled() != null && patch.getEnabled() == 1) {
            smsMapper.update(null, new LambdaUpdateWrapper<SysSmsChannel>()
                    .set(SysSmsChannel::getEnabled, 0));
            provider.setEnabled(1);
        } else if (patch.getEnabled() != null) {
            provider.setEnabled(0);
        }
        smsMapper.updateById(provider);
        auditService.log("message", "sms:" + provider.getProvider(), "SUCCESS");
        return R.ok(provider);
    }

    @PostMapping("/test")
    public R<Map<String, Object>> test(@RequestBody Map<String, String> body) {
        permissionService.require("message:write");
        String type = body.getOrDefault("type", "SMS");
        if ("SMS".equals(type)) {
            Long enabled = smsMapper.selectCount(new LambdaQueryWrapper<SysSmsChannel>()
                    .eq(SysSmsChannel::getEnabled, 1));
            if (enabled == 0) {
                log.warn("短信渠道未启用，验证码降级为日志输出，目标={}", body.get("target"));
                return R.ok(Map.of("ok", false, "degraded", true, "message", "短信渠道未启用，已降级为日志（请配置渠道）"));
            }
            eventBus.publish("notify.sms", Map.of("target", body.get("target") == null ? "" : body.get("target"),
                    "template", body.getOrDefault("template", "TEST"), "degraded", false));
        } else {
            Long enabled = channelMapper.selectCount(new LambdaQueryWrapper<SysMessageChannel>()
                    .eq(SysMessageChannel::getChannel, type).eq(SysMessageChannel::getEnabled, 1));
            if (enabled == 0) {
                log.warn("消息通道 {} 未启用，已降级写日志", type);
                return R.ok(Map.of("ok", false, "degraded", true, "message", "通道未启用，已降级为站内日志"));
            }
            eventBus.publish("notify.channel", Map.of("channel", type,
                    "target", body.get("target") == null ? "" : body.get("target"),
                    "template", body.getOrDefault("template", "TEST")));
        }
        return R.ok(Map.of("ok", true, "degraded", false, "message", "测试消息已发送"));
    }
}
