package com.quantaida.mc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.EventBus;
import com.quantaida.mc.common.Guard;
import com.quantaida.mc.common.NoGenerator;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.BizEntities.BizMember;
import com.quantaida.mc.entity.BizEntities.BizOrder;
import com.quantaida.mc.entity.BizEntities.BizOrderItem;
import com.quantaida.mc.entity.BizEntities.BizPayment;
import com.quantaida.mc.entity.BizEntities.BizPointsFlow;
import com.quantaida.mc.entity.BizEntities.BizRefund;
import com.quantaida.mc.entity.BizEntities.BizService;
import com.quantaida.mc.entity.BizEntities.BizSku;
import com.quantaida.mc.entity.BizEntities.BizStockRecord;
import com.quantaida.mc.mapper.BizMemberMapper;
import com.quantaida.mc.mapper.BizOrderItemMapper;
import com.quantaida.mc.mapper.BizOrderMapper;
import com.quantaida.mc.mapper.BizPaymentMapper;
import com.quantaida.mc.mapper.BizPointsFlowMapper;
import com.quantaida.mc.mapper.BizRefundMapper;
import com.quantaida.mc.mapper.BizServiceMapper;
import com.quantaida.mc.mapper.BizSkuMapper;
import com.quantaida.mc.mapper.BizStockRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final BizOrderMapper orderMapper;
    private final BizOrderItemMapper orderItemMapper;
    private final BizSkuMapper skuMapper;
    private final BizServiceMapper serviceMapper;
    private final BizPaymentMapper paymentMapper;
    private final BizRefundMapper refundMapper;
    private final BizMemberMapper memberMapper;
    private final BizPointsFlowMapper pointsFlowMapper;
    private final BizStockRecordMapper stockRecordMapper;
    private final NoGenerator noGenerator;
    private final Guard guard;
    private final EventBus eventBus;

    public record ItemReq(String itemType, Long itemId, Integer qty) {
    }

    public record CreateReq(Long memberId, Long storeId, String orderType, String source,
                            String idempotencyKey, String remark, List<ItemReq> items) {
    }

    @Transactional
    public BizOrder create(CreateReq req) {
        if (req.items() == null || req.items().isEmpty()) {
            throw new BizException(400, "订单明细不能为空");
        }
        if (req.idempotencyKey() != null) {
            guard.idempotent("order:" + TenantContext.getTenantId() + ":" + req.idempotencyKey());
        }
        BigDecimal total = BigDecimal.ZERO;
        List<BizOrderItem> items = new ArrayList<>();
        for (ItemReq itemReq : req.items()) {
            int qty = itemReq.qty() == null || itemReq.qty() < 1 ? 1 : itemReq.qty();
            BizOrderItem item = new BizOrderItem();
            item.setItemType(itemReq.itemType());
            item.setItemId(itemReq.itemId());
            item.setQty(qty);
            if ("SERVICE".equals(itemReq.itemType())) {
                BizService service = serviceMapper.selectById(itemReq.itemId());
                if (service == null) {
                    throw new BizException(400, "服务项目不存在");
                }
                item.setItemName(service.getName());
                item.setPrice(service.getPrice());
            } else {
                BizSku sku = skuMapper.selectById(itemReq.itemId());
                if (sku == null) {
                    throw new BizException(400, "商品规格不存在");
                }
                item.setItemName("SKU-" + sku.getSkuCode());
                item.setSpec(sku.getSpecJson());
                item.setPrice(sku.getPrice());
            }
            item.setAmount(item.getPrice().multiply(BigDecimal.valueOf(qty)));
            total = total.add(item.getAmount());
            items.add(item);
        }
        BizOrder order = new BizOrder();
        order.setOrderNo(noGenerator.gen("ORD"));
        order.setMemberId(req.memberId());
        order.setStoreId(req.storeId());
        order.setOrderType(req.orderType() == null ? "PRODUCT" : req.orderType());
        order.setTotalAmount(total);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(total);
        order.setPayStatus("UNPAID");
        order.setStatus("PENDING");
        order.setRemark(req.remark());
        order.setSource(req.source());
        order.setIdempotencyKey(req.idempotencyKey());
        orderMapper.insert(order);
        for (BizOrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        eventBus.publish("order.created", Map.of("orderId", order.getId(), "orderNo", order.getOrderNo(),
                "payAmount", order.getPayAmount(), "status", order.getStatus()));
        return order;
    }

    @Transactional
    public BizOrder pay(Long orderId, String channel) {
        BizOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if ("PAID".equals(order.getPayStatus())) {
            return order;
        }
        if (!"UNPAID".equals(order.getPayStatus())) {
            throw new BizException(400, "当前订单状态不可支付");
        }
        List<BizOrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<BizOrderItem>()
                .eq(BizOrderItem::getOrderId, orderId));
        for (BizOrderItem item : items) {
            if (!"SERVICE".equals(item.getItemType())) {
                BizSku sku = skuMapper.selectById(item.getItemId());
                if (sku == null || sku.getStock() < item.getQty()) {
                    throw new BizException(400, "库存不足：" + item.getItemName());
                }
            }
        }
        for (BizOrderItem item : items) {
            if ("SERVICE".equals(item.getItemType())) {
                continue;
            }
            BizSku sku = skuMapper.selectById(item.getItemId());
            int before = sku.getStock();
            sku.setStock(before - item.getQty());
            skuMapper.updateById(sku);
            BizStockRecord record = new BizStockRecord();
            record.setSkuId(sku.getId());
            record.setRecordType("OUT");
            record.setQty(item.getQty());
            record.setBeforeQty(before);
            record.setAfterQty(sku.getStock());
            record.setBizNo(order.getOrderNo());
            record.setRemark("订单支付扣减");
            stockRecordMapper.insert(record);
        }
        BizPayment payment = new BizPayment();
        payment.setPayNo(noGenerator.gen("PAY"));
        payment.setOrderId(orderId);
        payment.setChannel(channel == null ? "WECHAT" : channel);
        payment.setAmount(order.getPayAmount());
        payment.setStatus("SUCCESS");
        payment.setTradeNo("MOCK_" + System.currentTimeMillis());
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.insert(payment);

        order.setPayStatus("PAID");
        order.setStatus("PAID");
        order.setPayChannel(payment.getChannel());
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        if (order.getMemberId() != null && order.getPayAmount() != null) {
            BizMember member = memberMapper.selectById(order.getMemberId());
            if (member != null) {
                int add = order.getPayAmount().intValue();
                member.setPoints((member.getPoints() == null ? 0 : member.getPoints()) + add);
                memberMapper.updateById(member);
                BizPointsFlow flow = new BizPointsFlow();
                flow.setMemberId(member.getId());
                flow.setFlowType("ADD");
                flow.setPoints(add);
                flow.setBalance(member.getPoints());
                flow.setBizNo(order.getOrderNo());
                flow.setRemark("订单消费积分");
                pointsFlowMapper.insert(flow);
            }
        }
        eventBus.publish("order.paid", Map.of("orderId", orderId, "orderNo", order.getOrderNo(),
                "amount", order.getPayAmount(), "channel", payment.getChannel()));
        return order;
    }

    @Transactional
    public BizRefund refund(Long orderId, BigDecimal amount, String reason) {
        BizOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (!"PAID".equals(order.getPayStatus())) {
            throw new BizException(400, "仅已支付订单可退款");
        }
        BigDecimal refundAmount = amount == null ? order.getPayAmount() : amount;
        BizPayment payment = paymentMapper.selectOne(new LambdaQueryWrapper<BizPayment>()
                .eq(BizPayment::getOrderId, orderId).eq(BizPayment::getStatus, "SUCCESS")
                .orderByDesc(BizPayment::getCreatedAt).last("LIMIT 1"));
        BizRefund refund = new BizRefund();
        refund.setRefundNo(noGenerator.gen("REF"));
        refund.setOrderId(orderId);
        refund.setPaymentId(payment == null ? null : payment.getId());
        refund.setAmount(refundAmount);
        refund.setReason(reason);
        refund.setStatus("SUCCESS");
        refund.setRefundedAt(LocalDateTime.now());
        refundMapper.insert(refund);

        List<BizOrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<BizOrderItem>()
                .eq(BizOrderItem::getOrderId, orderId));
        for (BizOrderItem item : items) {
            if ("SERVICE".equals(item.getItemType())) {
                continue;
            }
            BizSku sku = skuMapper.selectById(item.getItemId());
            if (sku != null) {
                int before = sku.getStock();
                sku.setStock(before + item.getQty());
                skuMapper.updateById(sku);
                BizStockRecord record = new BizStockRecord();
                record.setSkuId(sku.getId());
                record.setRecordType("IN");
                record.setQty(item.getQty());
                record.setBeforeQty(before);
                record.setAfterQty(sku.getStock());
                record.setBizNo(order.getOrderNo());
                record.setRemark("退款回滚库存");
                stockRecordMapper.insert(record);
            }
        }
        order.setPayStatus("REFUNDED");
        order.setStatus("REFUNDED");
        orderMapper.updateById(order);
        if (payment != null) {
            payment.setStatus("REFUNDED");
            paymentMapper.updateById(payment);
        }
        eventBus.publish("order.refunded", Map.of("orderId", orderId, "orderNo", order.getOrderNo(),
                "refundNo", refund.getRefundNo(), "amount", refundAmount));
        return refund;
    }

    public BizOrder cancel(Long orderId) {
        BizOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (!"UNPAID".equals(order.getPayStatus())) {
            throw new BizException(400, "仅未支付订单可取消");
        }
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);
        return order;
    }

    public List<BizOrderItem> items(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<BizOrderItem>()
                .eq(BizOrderItem::getOrderId, orderId));
    }
}
