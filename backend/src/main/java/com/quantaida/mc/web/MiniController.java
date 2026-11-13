package com.quantaida.mc.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.NoGenerator;
import com.quantaida.mc.common.R;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.common.UserContext;
import com.quantaida.mc.entity.BizEntities.*;
import com.quantaida.mc.entity.WxEntities.WxMessage;
import com.quantaida.mc.mapper.*;
import com.quantaida.mc.service.AppointmentService;
import com.quantaida.mc.service.OrderService;
import com.quantaida.mc.service.PublicService;
import com.quantaida.mc.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小程序 / H5 / App 会员端接口（UnionID 同源，仅本人数据可见）。
 */
@RestController
@RequestMapping({"/api/mini", "/api/h5"})
@RequiredArgsConstructor
public class MiniController {
    private final PublicService publicService;
    private final AppointmentService appointmentService;
    private final OrderService orderService;
    private final WalletService walletService;
    private final BizMemberMapper memberMapper;
    private final BizBabyMapper babyMapper;
    private final BizBabyGrowthMapper growthMapper;
    private final BizAppointmentMapper appointmentMapper;
    private final BizOrderMapper orderMapper;
    private final BizRefundMapper refundMapper;
    private final NoGenerator noGenerator;
    private final BizMemberCouponMapper memberCouponMapper;
    private final BizActivityMapper activityMapper;
    private final BizActivitySignupMapper signupMapper;
    private final WxMessageMapper messageMapper;

    @GetMapping("/home")
    public R<Map<String, Object>> home() {
        return R.ok(publicService.home("MINI"));
    }

    @GetMapping("/services")
    public R<List<BizService>> services() {
        return R.ok(publicService.services());
    }

    @GetMapping("/services/{id}")
    public R<BizService> service(@PathVariable Long id) {
        return R.ok(publicService.services().stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null));
    }

    @GetMapping("/products")
    public R<List<BizProduct>> products() {
        return R.ok(publicService.products());
    }

    @GetMapping("/products/{id}")
    public R<Map<String, Object>> product(@PathVariable Long id) {
        return R.ok(publicService.productDetail(id));
    }

    @GetMapping("/stores")
    public R<List<BizStore>> stores() {
        return R.ok(publicService.stores());
    }

    @GetMapping("/activities")
    public R<List<BizActivity>> activities() {
        return R.ok(publicService.activities());
    }

    @PostMapping("/appointments")
    public R<BizAppointment> appoint(@RequestBody AppointmentService.CreateReq req) {
        Long memberId = UserContext.memberId();
        BizMember member = memberMapper.selectById(memberId);
        AppointmentService.CreateReq safe = new AppointmentService.CreateReq(memberId, req.babyId(), req.storeId(),
                req.serviceId(), req.staffId(), req.appointDate(), req.timeSlot(), req.appointType(), req.address(),
                req.contact() == null && member != null ? member.getName() : req.contact(),
                req.phone() == null && member != null ? member.getPhone() : req.phone(),
                req.remark(), req.idempotencyKey());
        return R.ok(appointmentService.create(safe));
    }

    @GetMapping("/appointments/mine")
    public R<List<BizAppointment>> myAppointments() {
        return R.ok(appointmentMapper.selectList(new LambdaQueryWrapper<BizAppointment>()
                .eq(BizAppointment::getMemberId, UserContext.memberId())
                .orderByDesc(BizAppointment::getCreatedAt)));
    }

    @PostMapping("/orders")
    public R<BizOrder> createOrder(@RequestBody OrderService.CreateReq req) {
        OrderService.CreateReq safe = new OrderService.CreateReq(UserContext.memberId(), req.storeId(),
                req.orderType(), "MINI", req.idempotencyKey(), req.remark(), req.items());
        return R.ok(orderService.create(safe));
    }

    @GetMapping("/orders/mine")
    public R<List<BizOrder>> myOrders() {
        return R.ok(orderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getMemberId, UserContext.memberId()).orderByDesc(BizOrder::getCreatedAt)));
    }

    @PostMapping("/orders/{id}/pay")
    public R<BizOrder> pay(@PathVariable Long id, @RequestParam(defaultValue = "WECHAT") String channel) {
        BizOrder order = orderMapper.selectById(id);
        if (order == null || !UserContext.memberId().equals(order.getMemberId())) {
            throw new BizException(403, "无权操作该订单");
        }
        return R.ok(orderService.pay(id, channel));
    }

    @PostMapping("/orders/{id}/aftersale")
    public R<BizRefund> aftersale(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        BizOrder order = orderMapper.selectById(id);
        if (order == null || !UserContext.memberId().equals(order.getMemberId())) {
            throw new BizException(403, "无权操作该订单");
        }
        if (!"PAID".equals(order.getPayStatus())) {
            throw new BizException(400, "仅已支付订单可申请售后");
        }
        BizRefund refund = new BizRefund();
        refund.setRefundNo(noGenerator.gen("AS"));
        refund.setOrderId(id);
        refund.setAmount(order.getPayAmount());
        refund.setReason(body == null ? "用户申请售后" : body.getOrDefault("reason", "用户申请售后"));
        refund.setStatus("PENDING");
        refundMapper.insert(refund);
        return R.ok(refund);
    }

    @GetMapping("/orders/{id}/aftersale")
    public R<List<BizRefund>> aftersales(@PathVariable Long id) {
        BizOrder order = orderMapper.selectById(id);
        if (order == null || !UserContext.memberId().equals(order.getMemberId())) {
            throw new BizException(403, "无权查看该订单");
        }
        return R.ok(refundMapper.selectList(new LambdaQueryWrapper<BizRefund>()
                .eq(BizRefund::getOrderId, id).orderByDesc(BizRefund::getCreatedAt)));
    }

    @GetMapping("/member/center")
    public R<Map<String, Object>> center() {
        Long memberId = UserContext.memberId();
        BizMember member = memberMapper.selectById(memberId);
        return R.ok(Map.of("member", member == null ? Map.of() : member,
                "wallet", walletService.getOrCreate(memberId),
                "babies", babyMapper.selectList(new LambdaQueryWrapper<BizBaby>()
                        .eq(BizBaby::getMemberId, memberId)),
                "coupons", memberCouponMapper.selectList(new LambdaQueryWrapper<BizMemberCoupon>()
                        .eq(BizMemberCoupon::getMemberId, memberId))));
    }

    @GetMapping("/babies")
    public R<List<BizBaby>> babies() {
        return R.ok(babyMapper.selectList(new LambdaQueryWrapper<BizBaby>()
                .eq(BizBaby::getMemberId, UserContext.memberId())));
    }

    @PostMapping("/babies")
    public R<BizBaby> addBaby(@RequestBody BizBaby baby) {
        baby.setId(null);
        baby.setMemberId(UserContext.memberId());
        babyMapper.insert(baby);
        return R.ok(baby);
    }

    @PostMapping("/babies/{id}/growth")
    public R<BizBabyGrowth> addGrowth(@PathVariable Long id, @RequestBody BizBabyGrowth growth) {
        BizBaby baby = babyMapper.selectById(id);
        if (baby == null || !UserContext.memberId().equals(baby.getMemberId())) {
            throw new BizException(403, "无权操作该宝宝档案");
        }
        growth.setId(null);
        growth.setBabyId(id);
        growthMapper.insert(growth);
        return R.ok(growth);
    }

    @GetMapping("/babies/{id}/growth")
    public R<List<BizBabyGrowth>> growth(@PathVariable Long id) {
        return R.ok(growthMapper.selectList(new LambdaQueryWrapper<BizBabyGrowth>()
                .eq(BizBabyGrowth::getBabyId, id).orderByDesc(BizBabyGrowth::getRecordDate)));
    }

    @PostMapping("/activities/{id}/signup")
    public R<BizActivitySignup> signup(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        BizActivity activity = activityMapper.selectById(id);
        if (activity == null) throw new BizException(404, "活动不存在");
        if (activity.getCapacity() != null && activity.getJoined() != null
                && activity.getJoined() >= activity.getCapacity()) {
            throw new BizException(400, "活动名额已满");
        }
        BizActivitySignup signup = new BizActivitySignup();
        signup.setActivityId(id);
        signup.setMemberId(UserContext.memberId());
        signup.setPhone(body == null ? null : body.get("phone"));
        signup.setStatus("SIGNED");
        signupMapper.insert(signup);
        activity.setJoined((activity.getJoined() == null ? 0 : activity.getJoined()) + 1);
        activityMapper.updateById(activity);
        return R.ok(signup);
    }

    @GetMapping("/messages")
    public R<List<WxMessage>> messages() {
        BizMember member = memberMapper.selectById(UserContext.memberId());
        if (member == null) return R.ok(List.of());
        return R.ok(messageMapper.selectList(new LambdaQueryWrapper<WxMessage>()
                .eq(WxMessage::getOpenid, member.getOpenid()).orderByDesc(WxMessage::getCreatedAt)));
    }

    @PutMapping("/profile")
    public R<BizMember> profile(@RequestBody Map<String, String> body) {
        BizMember member = memberMapper.selectById(UserContext.memberId());
        if (member == null) throw new BizException(404, "会员不存在");
        if (body.get("name") != null) member.setName(body.get("name"));
        if (body.get("avatar") != null) member.setAvatar(body.get("avatar"));
        memberMapper.updateById(member);
        return R.ok(member);
    }
}
