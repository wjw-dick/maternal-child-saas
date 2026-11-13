package com.quantaida.mc.web.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.R;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.SystemEntities.SysMenuConfig;
import com.quantaida.mc.entity.WxEntities.*;
import com.quantaida.mc.mapper.*;
import com.quantaida.mc.security.PermissionService;
import com.quantaida.mc.service.QuotaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信公众号 11 子页 API（账号/统计/标签/粉丝/消息/模版/菜单/自动回复/素材/草稿/发表）。
 * 真实微信 API 调用需在 T40 配置凭证后接入；当前以可验证的本地台账 + 发布闸门实现。
 */
@RestController
@RequestMapping("/api/tenant/wx")
@RequiredArgsConstructor
public class WxController {
    private final WxAccountMapper accountMapper;
    private final WxMiniConfigMapper miniConfigMapper;
    private final WxFollowerMapper followerMapper;
    private final WxTagMapper tagMapper;
    private final WxMessageMapper messageMapper;
    private final WxTemplateMapper templateMapper;
    private final WxMenuPublishMapper menuPublishMapper;
    private final WxAutoreplyMapper autoreplyMapper;
    private final WxMaterialMapper materialMapper;
    private final WxDraftMapper draftMapper;
    private final WxPublishRecordMapper publishRecordMapper;
    private final SysMenuConfigMapper menuConfigMapper;
    private final PermissionService permissionService;
    private final QuotaService quotaService;
    private final ObjectMapper objectMapper;

    // ---------- 账号与凭证 ----------
    @GetMapping("/accounts")
    public R<List<WxAccount>> accounts() {
        permissionService.require("wx:account:read");
        List<WxAccount> list = accountMapper.selectList(null);
        list.forEach(a -> a.setAppSecret(mask(a.getAppSecret())));
        return R.ok(list);
    }

    @PostMapping("/accounts")
    public R<WxAccount> saveAccount(@RequestBody WxAccount account) {
        permissionService.require("wx:account:write");
        if (account.getId() == null) {
            quotaService.checkMpAccount(TenantContext.getTenantId());
            accountMapper.insert(account);
        } else {
            WxAccount exists = accountMapper.selectById(account.getId());
            if (exists == null) throw new BizException(404, "账号不存在");
            if (!StringUtils.hasText(account.getAppSecret()) || "******".equals(account.getAppSecret())) {
                account.setAppSecret(exists.getAppSecret());
            }
            accountMapper.updateById(account);
        }
        return R.ok(account);
    }

    @DeleteMapping("/accounts/{id}")
    public R<Void> deleteAccount(@PathVariable Long id) {
        permissionService.require("wx:account:write");
        accountMapper.deleteById(id);
        return R.ok();
    }

    @PostMapping("/accounts/{id}/reset-quota")
    public R<WxAccount> resetQuota(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean confirm) {
        permissionService.require("wx:account:write");
        if (!confirm) throw new BizException(400, "清空 API 配额需二次确认");
        WxAccount account = accountMapper.selectById(id);
        account.setApiUsed(0);
        accountMapper.updateById(account);
        return R.ok(account);
    }

    @GetMapping("/mini-config")
    public R<WxMiniConfig> miniConfig() {
        permissionService.require("wx:account:read");
        WxMiniConfig config = miniConfigMapper.selectOne(new LambdaQueryWrapper<>());
        if (config == null) config = new WxMiniConfig();
        config.setAppSecret(mask(config.getAppSecret()));
        return R.ok(config);
    }

    @PostMapping("/mini-config")
    public R<WxMiniConfig> saveMini(@RequestBody WxMiniConfig patch) {
        permissionService.require("wx:account:write");
        WxMiniConfig config = miniConfigMapper.selectOne(new LambdaQueryWrapper<>());
        if (config == null) {
            miniConfigMapper.insert(patch);
            return R.ok(patch);
        }
        if (StringUtils.hasText(patch.getAppSecret()) && !"******".equals(patch.getAppSecret())) {
            config.setAppSecret(patch.getAppSecret());
        }
        if (patch.getAppId() != null) config.setAppId(patch.getAppId());
        if (patch.getLegalDomain() != null) config.setLegalDomain(patch.getLegalDomain());
        miniConfigMapper.updateById(config);
        return R.ok(config);
    }

    private String mask(String value) {
        return StringUtils.hasText(value) ? "******" : "";
    }

    // ---------- 数据统计 ----------
    @GetMapping("/stats")
    public R<Map<String, Object>> stats(@RequestParam(defaultValue = "7") int days) {
        permissionService.require("wx:stats:read");
        LocalDateTime since = LocalDateTime.now().minusDays(Math.max(1, Math.min(days, 30)));
        long total = followerMapper.selectCount(null);
        long subscribed = followerMapper.selectCount(new LambdaQueryWrapper<WxFollower>()
                .eq(WxFollower::getSubscribed, 1));
        long newUsers = followerMapper.selectCount(new LambdaQueryWrapper<WxFollower>()
                .ge(WxFollower::getCreatedAt, since));
        long inMsg = messageMapper.selectCount(new LambdaQueryWrapper<WxMessage>()
                .eq(WxMessage::getDirection, "IN").ge(WxMessage::getCreatedAt, since));
        long outMsg = messageMapper.selectCount(new LambdaQueryWrapper<WxMessage>()
                .eq(WxMessage::getDirection, "OUT").ge(WxMessage::getCreatedAt, since));
        WxAccount account = accountMapper.selectOne(new LambdaQueryWrapper<WxAccount>().last("LIMIT 1"));
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long count = followerMapper.selectCount(new LambdaQueryWrapper<WxFollower>()
                    .ge(WxFollower::getCreatedAt, date.atStartOfDay())
                    .lt(WxFollower::getCreatedAt, date.plusDays(1).atStartOfDay()));
            trend.add(Map.of("date", date.toString(), "value", count));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("subscribed", subscribed);
        data.put("newUsers", newUsers);
        data.put("inMsg", inMsg);
        data.put("outMsg", outMsg);
        data.put("apiUsed", account == null ? 0 : account.getApiUsed());
        data.put("apiQuota", account == null ? 0 : account.getApiQuota());
        data.put("trend", trend);
        return R.ok(data);
    }

    // ---------- 标签 ----------
    @GetMapping("/tags")
    public R<List<WxTag>> tags() {
        permissionService.require("wx:follower:read");
        return R.ok(tagMapper.selectList(null));
    }

    @PostMapping("/tags")
    public R<WxTag> createTag(@RequestBody Map<String, String> body) {
        permissionService.require("wx:follower:write");
        String name = body.get("name");
        if (!StringUtils.hasText(name)) throw new BizException(400, "标签名称必填");
        if (tagMapper.selectCount(null) >= 100) throw new BizException(400, "标签数量已达上限");
        WxTag tag = new WxTag();
        tag.setName(name);
        tag.setWxTagId(System.currentTimeMillis() % 100000);
        tag.setCount(0);
        tagMapper.insert(tag);
        return R.ok(tag);
    }

    @DeleteMapping("/tags/{id}")
    public R<Void> deleteTag(@PathVariable Long id) {
        permissionService.require("wx:follower:write");
        tagMapper.deleteById(id);
        return R.ok();
    }

    @PostMapping("/tags/sync")
    public R<List<WxTag>> syncTags() {
        permissionService.require("wx:follower:write");
        return R.ok(tagMapper.selectList(null));
    }

    // ---------- 粉丝 ----------
    @GetMapping("/followers")
    public R<IPage<WxFollower>> followers(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String keyword) {
        permissionService.require("wx:follower:read");
        LambdaQueryWrapper<WxFollower> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(WxFollower::getNickname, keyword).or().like(WxFollower::getOpenid, keyword));
        }
        qw.orderByDesc(WxFollower::getCreatedAt);
        return R.ok(followerMapper.selectPage(new Page<>(page, size), qw));
    }

    @PostMapping("/followers/{id}/tags")
    public R<WxFollower> tagFollower(@PathVariable Long id, @RequestBody Map<String, String> body) {
        permissionService.require("wx:follower:write");
        WxFollower follower = followerMapper.selectById(id);
        if (follower == null) throw new BizException(404, "粉丝不存在");
        follower.setTags(body.get("tags"));
        followerMapper.updateById(follower);
        return R.ok(follower);
    }

    @PostMapping("/followers/sync")
    public R<Map<String, Object>> syncFollowers() {
        permissionService.require("wx:follower:write");
        return R.ok(Map.of("synced", followerMapper.selectCount(null), "message", "已同步本地粉丝台账"));
    }

    // ---------- 消息 ----------
    @GetMapping("/messages")
    public R<IPage<WxMessage>> messages(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size,
                                        @RequestParam(required = false) String msgType,
                                        @RequestParam(required = false) String start,
                                        @RequestParam(required = false) String end) {
        permissionService.require("wx:message:read");
        LambdaQueryWrapper<WxMessage> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(msgType)) qw.eq(WxMessage::getMsgType, msgType);
        if (StringUtils.hasText(start)) qw.ge(WxMessage::getCreatedAt, LocalDate.parse(start).atStartOfDay());
        if (StringUtils.hasText(end)) qw.lt(WxMessage::getCreatedAt, LocalDate.parse(end).plusDays(1).atStartOfDay());
        qw.orderByDesc(WxMessage::getCreatedAt);
        return R.ok(messageMapper.selectPage(new Page<>(page, size), qw));
    }

    // ---------- 模版消息（仅同步/查看） ----------
    @GetMapping("/templates")
    public R<List<WxTemplate>> templates() {
        permissionService.require("wx:template:read");
        return R.ok(templateMapper.selectList(null));
    }

    @PostMapping("/templates/sync")
    public R<List<WxTemplate>> syncTemplates() {
        permissionService.require("wx:template:write");
        if (templateMapper.selectCount(null) == 0) {
            String[][] seeds = {{"预约成功通知", "母婴护理"}, {"订单发货通知", "母婴护理"}, {"服务完成提醒", "母婴护理"}};
            for (String[] seed : seeds) {
                WxTemplate template = new WxTemplate();
                template.setWxTemplateId("TPL_" + System.currentTimeMillis() + templateMapper.selectCount(null));
                template.setTitle(seed[0]);
                template.setIndustry(seed[1]);
                template.setContent("{\"thing1\":\"项目\",\"time2\":\"时间\",\"phrase3\":\"状态\"}");
                templateMapper.insert(template);
            }
        }
        return R.ok(templateMapper.selectList(null));
    }

    // ---------- 菜单发布（发布闸门） ----------
    @GetMapping("/menu")
    public R<Map<String, Object>> menu() {
        permissionService.require("wx:menu:read");
        SysMenuConfig config = menuConfigMapper.selectOne(new LambdaQueryWrapper<SysMenuConfig>()
                .eq(SysMenuConfig::getTerminal, "WECHAT"));
        List<WxMenuPublish> records = menuPublishMapper.selectList(new LambdaQueryWrapper<WxMenuPublish>()
                .orderByDesc(WxMenuPublish::getCreatedAt).last("LIMIT 10"));
        return R.ok(Map.of("config", config == null ? Map.of() : config, "records", records));
    }

    @PostMapping("/menu/publish")
    public R<WxMenuPublish> publishMenu(@RequestBody(required = false) Map<String, String> body) {
        permissionService.require("wx:menu:write");
        String json = body == null ? null : body.get("menuJson");
        if (!StringUtils.hasText(json)) {
            SysMenuConfig config = menuConfigMapper.selectOne(new LambdaQueryWrapper<SysMenuConfig>()
                    .eq(SysMenuConfig::getTerminal, "WECHAT"));
            json = config == null ? null : config.getTreeJson();
        }
        if (!StringUtils.hasText(json)) throw new BizException(400, "请先在菜单可视化中配置公众号菜单");
        try {
            var root = objectMapper.readTree(json);
            var buttons = root.has("button") ? root.get("button") : root;
            if (buttons.isArray() && buttons.size() > 3) {
                throw new BizException(400, "一级菜单最多 3 个");
            }
            if (buttons.isArray()) {
                for (var button : buttons) {
                    if (button.has("sub_button") && button.get("sub_button").size() > 5) {
                        throw new BizException(400, "二级菜单最多 5 个");
                    }
                }
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(400, "菜单 JSON 格式非法");
        }
        WxMenuPublish record = new WxMenuPublish();
        record.setMenuJson(json);
        record.setStatus("SUCCESS");
        record.setWxResp("发布成功（重新进入公众号可见）");
        record.setPublishedAt(LocalDateTime.now());
        menuPublishMapper.insert(record);
        WxMessage message = new WxMessage();
        message.setDirection("OUT");
        message.setMsgType("MENU_PUBLISH");
        message.setContent("公众号菜单已发布");
        message.setErrCode(0);
        message.setErrMsg("ok");
        messageMapper.insert(message);
        return R.ok(record);
    }

    @DeleteMapping("/menu")
    public R<Void> clearMenu(@RequestParam(defaultValue = "false") boolean confirm) {
        permissionService.require("wx:menu:write");
        if (!confirm) throw new BizException(400, "清空公众号菜单需二次确认");
        WxMenuPublish record = new WxMenuPublish();
        record.setStatus("CLEARED");
        record.setWxResp("已清空（重新进入公众号可见）");
        record.setPublishedAt(LocalDateTime.now());
        menuPublishMapper.insert(record);
        return R.ok();
    }

    // ---------- 自动回复 ----------
    @GetMapping("/autoreplies")
    public R<List<WxAutoreply>> autoreplies() {
        permissionService.require("wx:autoreply:read");
        return R.ok(autoreplyMapper.selectList(null));
    }

    @PostMapping("/autoreplies")
    public R<WxAutoreply> saveAutoreply(@RequestBody WxAutoreply reply) {
        permissionService.require("wx:autoreply:write");
        if (!List.of("TEXT", "IMAGE", "NEWS", "AUDIO", "VIDEO", "CARD").contains(reply.getMsgType())) {
            throw new BizException(400, "不支持的回复类型（音乐请使用 AUDIO）");
        }
        if (reply.getId() == null) autoreplyMapper.insert(reply);
        else autoreplyMapper.updateById(reply);
        return R.ok(reply);
    }

    @DeleteMapping("/autoreplies/{id}")
    public R<Void> deleteAutoreply(@PathVariable Long id) {
        permissionService.require("wx:autoreply:write");
        autoreplyMapper.deleteById(id);
        return R.ok();
    }

    // ---------- 素材 ----------
    @GetMapping("/materials")
    public R<List<WxMaterial>> materials(@RequestParam(required = false) String type) {
        permissionService.require("wx:material:read");
        LambdaQueryWrapper<WxMaterial> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) qw.eq(WxMaterial::getMaterialType, type);
        return R.ok(materialMapper.selectList(qw));
    }

    @PostMapping("/materials")
    public R<WxMaterial> saveMaterial(@RequestBody WxMaterial material) {
        permissionService.require("wx:material:write");
        if ("VIDEO".equals(material.getMaterialType()) && !StringUtils.hasText(material.getThumbUrl())) {
            throw new BizException(400, "视频素材必须上传封面 thumb");
        }
        materialMapper.insert(material);
        return R.ok(material);
    }

    @DeleteMapping("/materials/{id}")
    public R<Void> deleteMaterial(@PathVariable Long id) {
        permissionService.require("wx:material:write");
        materialMapper.deleteById(id);
        return R.ok();
    }

    // ---------- 图文草稿 / 发表记录 ----------
    @GetMapping("/drafts")
    public R<List<WxDraft>> drafts() {
        permissionService.require("wx:material:read");
        return R.ok(draftMapper.selectList(new LambdaQueryWrapper<WxDraft>()
                .orderByDesc(WxDraft::getCreatedAt)));
    }

    @PostMapping("/drafts")
    public R<WxDraft> saveDraft(@RequestBody WxDraft draft) {
        permissionService.require("wx:material:write");
        if (draft.getId() == null) draftMapper.insert(draft);
        else draftMapper.updateById(draft);
        return R.ok(draft);
    }

    @PostMapping("/drafts/{id}/publish")
    public R<WxPublishRecord> publishDraft(@PathVariable Long id) {
        permissionService.require("wx:material:write");
        WxDraft draft = draftMapper.selectById(id);
        if (draft == null) throw new BizException(404, "草稿不存在");
        draft.setStatus("PUBLISHED");
        draftMapper.updateById(draft);
        WxPublishRecord record = new WxPublishRecord();
        record.setDraftId(id);
        record.setMediaId("MEDIA_" + System.currentTimeMillis());
        record.setStatus("SUCCESS");
        record.setPublishedAt(LocalDateTime.now());
        publishRecordMapper.insert(record);
        return R.ok(record);
    }

    @GetMapping("/publish-records")
    public R<List<WxPublishRecord>> publishRecords() {
        permissionService.require("wx:publish:read");
        return R.ok(publishRecordMapper.selectList(new LambdaQueryWrapper<WxPublishRecord>()
                .orderByDesc(WxPublishRecord::getCreatedAt)));
    }
}
