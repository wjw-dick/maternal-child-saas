package com.quantaida.mc.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.NoGenerator;
import com.quantaida.mc.common.R;
import com.quantaida.mc.common.SensitiveUtil;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.common.UserContext;
import com.quantaida.mc.entity.BizEntities.*;
import com.quantaida.mc.entity.SystemEntities.SysUser;
import com.quantaida.mc.mapper.*;
import com.quantaida.mc.service.AppointmentService;
import com.quantaida.mc.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 员工端（店长/前台/库管/财务）接口。
 *
 * <p>数据边界：门店范围取自登录账号 storeId，不接受前端传入；所有查询由租户插件隔离。</p>
 */
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final SysUserMapper userMapper;
    private final BizAppointmentMapper appointmentMapper;
    private final BizMemberMapper memberMapper;
    private final BizBabyMapper babyMapper;
    private final BizSkuMapper skuMapper;
    private final BizStockRecordMapper stockRecordMapper;
    private final BizOrderMapper orderMapper;
    private final BizScheduleMapper scheduleMapper;
    private final AppointmentService appointmentService;
    private final WalletService walletService;
    private final SensitiveUtil sensitiveUtil;
    private final NoGenerator noGenerator;
    private final JdbcTemplate jdbc;

    private SysUser user() {
        SysUser user = userMapper.selectById(UserContext.userId());
        if (user == null) {
            throw new BizException(401, "账号不存在");
        }
        return user;
    }

    private Long storeId() {
        return user().getStoreId();
    }

    // ================= 今日预约 / 核销 =================
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        Long storeId = storeId();
        LocalDate today = LocalDate.now();
        List<BizAppointment> todayList = appointmentMapper.selectList(new LambdaQueryWrapper<BizAppointment>()
                .eq(BizAppointment::getAppointDate, today)
                .eq(storeId != null, BizAppointment::getStoreId, storeId)
                .orderByAsc(BizAppointment::getTimeSlot));
        long pending = todayList.stream().filter(a -> "PENDING".equals(a.getStatus())
                || "CONFIRMED".equals(a.getStatus())).count();
        long completed = todayList.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        return R.ok(Map.of(
                "storeId", storeId == null ? 0L : storeId,
                "todayTotal", todayList.size(),
                "pendingVerify", pending,
                "completed", completed,
                "todayOrders", orderMapper.selectCount(new LambdaQueryWrapper<BizOrder>()
                        .ge(BizOrder::getCreatedAt, today.atStartOfDay())),
                "lowStock", skuMapper.selectCount(new LambdaQueryWrapper<BizSku>()
                        .apply("stock <= safety_stock"))));
    }

    @GetMapping("/appointments")
    public R<List<BizAppointment>> appointments(@RequestParam(required = false) String date) {
        Long storeId = storeId();
        LocalDate day = date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
        return R.ok(appointmentMapper.selectList(new LambdaQueryWrapper<BizAppointment>()
                .eq(BizAppointment::getAppointDate, day)
                .eq(storeId != null, BizAppointment::getStoreId, storeId)
                .orderByAsc(BizAppointment::getTimeSlot)));
    }

    public record VerifyReq(String code) {
    }

    @PostMapping("/appointments/verify")
    public R<BizAppointment> verify(@RequestBody VerifyReq req) {
        if (req.code() == null || req.code().isBlank()) {
            throw new BizException(400, "请输入核销码");
        }
        BizAppointment appointment = appointmentService.verify(req.code().trim());
        Long storeId = storeId();
        if (storeId != null && appointment.getStoreId() != null
                && !storeId.equals(appointment.getStoreId())) {
            throw new BizException(403, "该预约不属于本门店");
        }
        return R.ok(appointment);
    }

    // ================= 会员开卡 / 储值 =================
    @GetMapping("/members")
    public R<List<BizMember>> members(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BizMember> q = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            q.and(w -> w.eq(BizMember::getMemberNo, kw)
                    .or().like(BizMember::getName, kw)
                    .or().eq(BizMember::getPhoneHash, sensitiveUtil.hash(kw)));
        }
        q.orderByDesc(BizMember::getCreatedAt).last("LIMIT 30");
        return R.ok(memberMapper.selectList(q));
    }

    public record OpenCardReq(String name, String phone, String babyName, String babyBirthday, String remark) {
    }

    @PostMapping("/members")
    public R<Map<String, Object>> openCard(@RequestBody OpenCardReq req) {
        if (req.name() == null || req.name().isBlank()) {
            throw new BizException(400, "请输入会员姓名");
        }
        if (req.phone() == null || req.phone().length() < 7) {
            throw new BizException(400, "请输入有效手机号");
        }
        String phoneHash = sensitiveUtil.hash(req.phone());
        if (memberMapper.selectCount(new LambdaQueryWrapper<BizMember>()
                .eq(BizMember::getPhoneHash, phoneHash)) > 0) {
            throw new BizException(400, "该手机号已开卡，请直接查询会员");
        }
        BizMember member = new BizMember();
        member.setMemberNo(noGenerator.gen("MB"));
        member.setName(req.name().trim());
        member.setPhone(req.phone().trim());
        member.setPhoneEnc(sensitiveUtil.encrypt(req.phone().trim()));
        member.setPhoneHash(phoneHash);
        member.setLevel("NORMAL");
        member.setBalance(BigDecimal.ZERO);
        member.setPoints(0);
        member.setSource("STORE");
        member.setStatus("ACTIVE");
        member.setRemark(req.remark());
        memberMapper.insert(member);
        BizWallet wallet = walletService.getOrCreate(member.getId());

        if (req.babyName() != null && !req.babyName().isBlank()) {
            BizBaby baby = new BizBaby();
            baby.setMemberId(member.getId());
            baby.setName(req.babyName().trim());
            if (req.babyBirthday() != null && !req.babyBirthday().isBlank()) {
                baby.setBirthday(LocalDate.parse(req.babyBirthday()));
            }
            babyMapper.insert(baby);
        }
        return R.ok(Map.of("member", member, "wallet", wallet));
    }

    @GetMapping("/members/{id}/wallet")
    public R<Map<String, Object>> wallet(@PathVariable Long id) {
        return R.ok(Map.of("member", requireMember(id),
                "wallet", walletService.getOrCreate(id),
                "flows", walletService.flows(id)));
    }

    public record AmountReq(BigDecimal amount, String remark) {
    }

    @PostMapping("/members/{id}/recharge")
    public R<BizWallet> recharge(@PathVariable Long id, @RequestBody AmountReq req) {
        requireMember(id);
        return R.ok(walletService.recharge(id, req.amount(),
                req.remark() == null ? "员工端充值" : req.remark()));
    }

    @PostMapping("/members/{id}/consume")
    public R<BizWallet> consume(@PathVariable Long id, @RequestBody AmountReq req) {
        requireMember(id);
        return R.ok(walletService.consume(id, req.amount(),
                req.remark() == null ? "员工端消费" : req.remark()));
    }

    private BizMember requireMember(Long id) {
        BizMember member = memberMapper.selectById(id);
        if (member == null) {
            throw new BizException(404, "会员不存在");
        }
        return member;
    }

    // ================= 商品库存 =================
    @GetMapping("/skus")
    public R<List<BizSku>> skus(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BizSku> q = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            q.and(w -> w.like(BizSku::getSkuCode, kw).or().eq(BizSku::getId, kw.matches("\\d+") ? Long.valueOf(kw) : -1L));
        }
        q.orderByAsc(BizSku::getSkuCode).last("LIMIT 100");
        return R.ok(skuMapper.selectList(q));
    }

    /** recordType: IN 入库 / OUT 出库 / ADJUST 盘点调整（qty 为调整后的实际库存）。 */
    public record StockReq(Long skuId, Integer qty, String recordType, String remark) {
    }

    @PostMapping("/stock/adjust")
    public R<BizStockRecord> adjustStock(@RequestBody StockReq req) {
        if (req.skuId() == null || req.qty() == null || req.qty() < 0) {
            throw new BizException(400, "库存数量不合法");
        }
        BizSku sku = skuMapper.selectById(req.skuId());
        if (sku == null) {
            throw new BizException(404, "SKU 不存在");
        }
        String type = req.recordType() == null ? "ADJUST" : req.recordType();
        int before = sku.getStock() == null ? 0 : sku.getStock();
        int after;
        switch (type) {
            case "IN" -> after = before + req.qty();
            case "OUT" -> {
                if (req.qty() > before) {
                    throw new BizException(400, "出库数量超过当前库存");
                }
                after = before - req.qty();
            }
            default -> after = req.qty();
        }
        sku.setStock(after);
        skuMapper.updateById(sku);
        BizStockRecord record = new BizStockRecord();
        record.setSkuId(sku.getId());
        record.setRecordType(type);
        record.setQty(type.equals("ADJUST") ? Math.abs(after - before) : req.qty());
        record.setBeforeQty(before);
        record.setAfterQty(after);
        record.setBizNo(noGenerator.gen("ST"));
        record.setRemark(req.remark() == null ? "员工端库存操作" : req.remark());
        stockRecordMapper.insert(record);
        return R.ok(record);
    }

    // ================= 订单处理 =================
    @GetMapping("/orders")
    public R<List<BizOrder>> orders(@RequestParam(required = false) String date) {
        LocalDate day = date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
        return R.ok(orderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                .ge(BizOrder::getCreatedAt, day.atStartOfDay())
                .lt(BizOrder::getCreatedAt, day.plusDays(1).atStartOfDay())
                .orderByDesc(BizOrder::getCreatedAt)));
    }

    @PostMapping("/orders/{id}/complete")
    public R<BizOrder> completeOrder(@PathVariable Long id) {
        BizOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new BizException(400, "仅已支付订单可完成");
        }
        order.setStatus("COMPLETED");
        orderMapper.updateById(order);
        return R.ok(order);
    }

    // ================= 排班 / 简报 =================
    @GetMapping("/schedules")
    public R<List<BizSchedule>> schedules() {
        Long storeId = storeId();
        return R.ok(scheduleMapper.selectList(new LambdaQueryWrapper<BizSchedule>()
                .ge(BizSchedule::getWorkDate, LocalDate.now().minusDays(1))
                .eq(storeId != null, BizSchedule::getStoreId, storeId)
                .orderByAsc(BizSchedule::getWorkDate)
                .last("LIMIT 30")));
    }

    @GetMapping("/brief")
    public R<Map<String, Object>> brief() {
        Long storeId = storeId();
        LocalDate today = LocalDate.now();
        Long tenantId = TenantContext.getTenantId();
        Long appointments = jdbc.queryForObject(
                "select count(*) from biz_appointment where tenant_id=? and appoint_date=? and deleted=0"
                        + (storeId == null ? "" : " and store_id=" + storeId),
                Long.class, tenantId, today);
        Long completed = jdbc.queryForObject(
                "select count(*) from biz_appointment where tenant_id=? and appoint_date=? and status='COMPLETED' and deleted=0"
                        + (storeId == null ? "" : " and store_id=" + storeId),
                Long.class, tenantId, today);
        BigDecimal revenue = jdbc.queryForObject(
                "select coalesce(sum(pay_amount),0) from biz_order where tenant_id=? and deleted=0"
                        + " and status in ('PAID','COMPLETED') and created_at>=? and created_at<?",
                BigDecimal.class, tenantId, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        Long newMembers = jdbc.queryForObject(
                "select count(*) from biz_member where tenant_id=? and deleted=0 and created_at>=?",
                Long.class, tenantId, today.atStartOfDay());
        return R.ok(Map.of(
                "date", today.toString(),
                "appointments", appointments,
                "completed", completed,
                "revenue", revenue == null ? BigDecimal.ZERO : revenue,
                "newMembers", newMembers,
                "generatedAt", LocalDateTime.now().toString()));
    }
}
