package com.quantaida.mc.web;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.R;
import com.quantaida.mc.common.SensitiveUtil;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.BizEntities.BizMember;
import com.quantaida.mc.entity.BizEntities.BizOrder;
import com.quantaida.mc.entity.BizEntities.BizProduct;
import com.quantaida.mc.entity.SystemEntities.OpenApp;
import com.quantaida.mc.mapper.BizMemberMapper;
import com.quantaida.mc.mapper.BizOrderMapper;
import com.quantaida.mc.mapper.BizProductMapper;
import com.quantaida.mc.mapper.OpenAppMapper;
import com.quantaida.mc.service.QuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 开放 API：AppKey/Secret 签名 + 配额 + 租户隔离。
 * sign = sha256(appKey + timestamp + nonce + appSecret)
 */
@RestController
@RequestMapping("/api/open")
@RequiredArgsConstructor
public class OpenApiController {
    private final OpenAppMapper appMapper;
    private final BizProductMapper productMapper;
    private final BizOrderMapper orderMapper;
    private final BizMemberMapper memberMapper;
    private final QuotaService quotaService;
    private final SensitiveUtil sensitiveUtil;

    private OpenApp auth(String appKey, String timestamp, String nonce, String sign) {
        if (appKey == null || timestamp == null || nonce == null || sign == null) {
            throw new BizException(401, "缺少签名参数");
        }
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new BizException(401, "时间戳非法");
        }
        if (Math.abs(System.currentTimeMillis() - ts) > 300_000L) {
            throw new BizException(401, "签名已过期");
        }
        OpenApp app = appMapper.selectOne(new LambdaQueryWrapper<OpenApp>()
                .eq(OpenApp::getAppKey, appKey));
        if (app == null || !"ACTIVE".equals(app.getStatus())) {
            throw new BizException(401, "应用不存在或已停用");
        }
        String expected = SecureUtil.sha256(appKey + timestamp + nonce + app.getAppSecret());
        if (!expected.equalsIgnoreCase(sign)) {
            throw new BizException(401, "签名校验失败");
        }
        Long tenantId = TenantContext.getTenantId();
        quotaService.checkApi(tenantId);
        quotaService.addApiUsage(tenantId);
        return app;
    }

    @GetMapping("/products")
    public R<List<BizProduct>> products(@RequestHeader("X-App-Key") String appKey,
                                        @RequestHeader("X-Timestamp") String timestamp,
                                        @RequestHeader("X-Nonce") String nonce,
                                        @RequestHeader("X-Sign") String sign) {
        auth(appKey, timestamp, nonce, sign);
        return R.ok(productMapper.selectList(new LambdaQueryWrapper<BizProduct>()
                .eq(BizProduct::getStatus, "ACTIVE")));
    }

    @GetMapping("/orders/{orderNo}")
    public R<BizOrder> order(@PathVariable String orderNo,
                             @RequestHeader("X-App-Key") String appKey,
                             @RequestHeader("X-Timestamp") String timestamp,
                             @RequestHeader("X-Nonce") String nonce,
                             @RequestHeader("X-Sign") String sign) {
        auth(appKey, timestamp, nonce, sign);
        BizOrder order = orderMapper.selectOne(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        return R.ok(order);
    }

    @GetMapping("/members/{phone}")
    public R<BizMember> member(@PathVariable String phone,
                               @RequestHeader("X-App-Key") String appKey,
                               @RequestHeader("X-Timestamp") String timestamp,
                               @RequestHeader("X-Nonce") String nonce,
                               @RequestHeader("X-Sign") String sign) {
        auth(appKey, timestamp, nonce, sign);
        BizMember member = memberMapper.selectOne(new LambdaQueryWrapper<BizMember>()
                .eq(BizMember::getPhoneHash, sensitiveUtil.hash(phone)));
        if (member == null) {
            throw new BizException(404, "会员不存在");
        }
        return R.ok(member);
    }

    @GetMapping("/error-codes")
    public R<Map<String, Object>> errorCodes() {
        return R.ok(Map.of(
                "0", "success", "401", "签名或鉴权失败", "403", "越权或租户停用",
                "40201", "席位超限", "40202", "公众号超限", "40203", "存储超限",
                "40204", "短信超限", "40205", "API 配额超限", "429", "限流或重复提交"));
    }
}
