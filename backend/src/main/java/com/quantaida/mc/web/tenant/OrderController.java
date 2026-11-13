package com.quantaida.mc.web.tenant;

import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.BizEntities.BizOrder;
import com.quantaida.mc.entity.BizEntities.BizOrderItem;
import com.quantaida.mc.mapper.BizOrderMapper;
import com.quantaida.mc.service.OrderService;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tenant/orders")
public class OrderController extends BaseCrudController<BizOrder> {
    private final OrderService orderService;

    public OrderController(BizOrderMapper mapper, OrderService orderService) {
        super(mapper, "order", "orderNo");
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public R<BizOrder> create(@RequestBody OrderService.CreateReq req) {
        permissionService.require("order:write");
        return R.ok(orderService.create(req));
    }

    @PostMapping("/{id}/pay")
    public R<BizOrder> pay(@PathVariable Long id, @RequestParam(required = false) String channel) {
        permissionService.require("order:write");
        return R.ok(orderService.pay(id, channel));
    }

    public record RefundReq(BigDecimal amount, String reason) {
    }

    @PostMapping("/{id}/refund")
    public R<com.quantaida.mc.entity.BizEntities.BizRefund> refund(@PathVariable Long id,
                                                                   @RequestBody(required = false) RefundReq req) {
        permissionService.require("order:write");
        return R.ok(orderService.refund(id, req == null ? null : req.amount(), req == null ? null : req.reason()));
    }

    @PostMapping("/{id}/cancel")
    public R<BizOrder> cancel(@PathVariable Long id) {
        permissionService.require("order:write");
        return R.ok(orderService.cancel(id));
    }

    @GetMapping("/{id}/items")
    public R<List<BizOrderItem>> items(@PathVariable Long id) {
        permissionService.require("order:read");
        return R.ok(orderService.items(id));
    }
}
