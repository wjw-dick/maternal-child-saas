package com.quantaida.mc.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.R;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.WxEntities.WxFollower;
import com.quantaida.mc.entity.WxEntities.WxMessage;
import com.quantaida.mc.mapper.WxFollowerMapper;
import com.quantaida.mc.mapper.WxMessageMapper;
import com.quantaida.mc.mapper.WxAutoreplyMapper;
import com.quantaida.mc.entity.WxEntities.WxAutoreply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信服务器回调（免鉴权，按 tenant_code / 子域名落库；停用租户只落库不执行）。
 */
@Slf4j
@RestController
@RequestMapping("/api/wx/callback")
@RequiredArgsConstructor
public class WxCallbackController {
    private static final Pattern OPENID = Pattern.compile("<FromUserName><!\\[CDATA\\[(.*?)]]></FromUserName>");
    private static final Pattern CONTENT = Pattern.compile("<Content><!\\[CDATA\\[(.*?)]]></Content>");

    private final WxMessageMapper messageMapper;
    private final WxFollowerMapper followerMapper;
    private final WxAutoreplyMapper autoreplyMapper;

    @PostMapping
    public String receive(@RequestBody String xml) {
        Long tenantId = TenantContext.getTenantId();
        String openid = extract(OPENID, xml);
        String content = extract(CONTENT, xml);
        WxMessage message = new WxMessage();
        message.setDirection("IN");
        message.setMsgType("TEXT");
        message.setOpenid(openid);
        message.setContent(content);
        message.setErrCode(0);
        message.setErrMsg("ok");
        messageMapper.insert(message);

        if (openid != null && followerMapper.selectCount(new LambdaQueryWrapper<WxFollower>()
                .eq(WxFollower::getOpenid, openid)) == 0) {
            WxFollower follower = new WxFollower();
            follower.setOpenid(openid);
            follower.setSubscribed(1);
            follower.setSubscribedAt(LocalDateTime.now());
            followerMapper.insert(follower);
        }
        if (content != null) {
            WxAutoreply reply = autoreplyMapper.selectOne(new LambdaQueryWrapper<WxAutoreply>()
                    .eq(WxAutoreply::getKeyword, content).eq(WxAutoreply::getEnabled, 1).last("LIMIT 1"));
            if (reply != null) {
                WxMessage out = new WxMessage();
                out.setDirection("OUT");
                out.setMsgType(reply.getMsgType());
                out.setOpenid(openid);
                out.setContent(reply.getContent());
                out.setErrCode(0);
                out.setErrMsg("ok");
                messageMapper.insert(out);
            }
        }
        log.info("微信回调 tenant={} openid={} content={}", tenantId, openid, content);
        return "success";
    }

    private String extract(Pattern pattern, String xml) {
        Matcher matcher = pattern.matcher(xml);
        return matcher.find() ? matcher.group(1) : null;
    }
}
