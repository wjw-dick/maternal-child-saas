package com.quantaida.mc.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.NoGenerator;
import com.quantaida.mc.common.R;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.common.UserContext;
import com.quantaida.mc.entity.BizEntities.*;
import com.quantaida.mc.entity.PlatformEntities.PlatformNotice;
import com.quantaida.mc.entity.SystemEntities.SysUser;
import com.quantaida.mc.mapper.*;
import com.quantaida.mc.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务一线端（护理师/育儿师）、员工端、供应商端、渠道商端接口。
 * 数据边界：staffId / supplierId / distributorId 均取自登录账号，不接受前端传入。
 */
@RestController
@RequiredArgsConstructor
public class RoleAppController {
    private final SysUserMapper userMapper;
    private final BizStaffMapper staffMapper;
    private final BizAppointmentMapper appointmentMapper;
    private final BizScheduleMapper scheduleMapper;
    private final BizMemberMapper memberMapper;
    private final BizBabyMapper babyMapper;
    private final PlatformNoticeMapper noticeMapper;
    private final BizSupplierQualificationMapper qualificationMapper;
    private final BizDistributorSettlementMapper settlementMapper;
    private final BizCheckinMapper checkinMapper;
    private final BizServiceRecordMapper recordMapper;
    private final BizServiceMapper serviceMapper;
    private final BizSupplyOrderMapper supplyOrderMapper;
    private final BizSupplyItemMapper supplyItemMapper;
    private final BizSupplierStatementMapper statementMapper;
    private final BizSupplierMapper supplierMapper;
    private final BizDistributorMapper distributorMapper;
    private final BizDistributorReportMapper reportMapper;
    private final BizCommissionFlowMapper commissionFlowMapper;
    private final BizSkuMapper skuMapper;
    private final BizStockRecordMapper stockRecordMapper;
    private final AppointmentService appointmentService;
    private final NoGenerator noGenerator;
    private final JdbcTemplate jdbc;

    private SysUser user() {
        SysUser user = userMapper.selectById(UserContext.userId());
        if (user == null) {
            throw new BizException(401, "账号不存在");
        }
        return user;
    }

    private BizStaff staff() {
        SysUser user = user();
        if (user.getStaffId() == null) {
            throw new BizException(403, "当前账号未绑定员工档案");
        }
        BizStaff staff = staffMapper.selectById(user.getStaffId());
        if (staff == null) {
            throw new BizException(404, "员工档案不存在");
        }
        return staff;
    }

    // ================= 一线端 / 员工端 =================
    @GetMapping("/api/staff/workbench")
    public R<Map<String, Object>> workbench() {
        BizStaff staff = staff();
        List<BizAppointment> today = appointmentMapper.selectList(new LambdaQueryWrapper<BizAppointment>()
                .eq(BizAppointment::getStaffId, staff.getId())
                .eq(BizAppointment::getAppointDate, LocalDate.now())
                .orderByAsc(BizAppointment::getTimeSlot));
        List<BizAppointment> pending = appointmentMapper.selectList(new LambdaQueryWrapper<BizAppointment>()
                .eq(BizAppointment::getStaffId, staff.getId())
                .in(BizAppointment::getStatus, List.of("PENDING", "CONFIRMED"))
                .orderByAsc(BizAppointment::getAppointDate));
        List<BizSchedule> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getStaffId, staff.getId())
                .ge(BizSchedule::getWorkDate, LocalDate.now().minusDays(1))
                .orderByAsc(BizSchedule::getWorkDate));
        return R.ok(Map.of("staff", staff, "today", today, "pending", pending, "schedules", schedules));
    }

    @PostMapping("/api/staff/appointments/{id}/accept")
    public R<Void> accept(@PathVariable Long id) {
        BizStaff staff = staff();
        appointmentService.accept(id, staff.getId());
        return R.ok();
    }

    @PostMapping("/api/staff/checkin")
    public R<BizCheckin> checkin(@RequestBody BizCheckin checkin) {
        BizStaff staff = staff();
        checkin.setId(null);
        checkin.setStaffId(staff.getId());
        checkin.setCheckinAt(LocalDateTime.now());
        checkinMapper.insert(checkin);
        BizAppointment appointment = appointmentMapper.selectById(checkin.getAppointmentId());
        if (appointment != null) {
            appointment.setStatus("SERVING");
            appointmentMapper.updateById(appointment);
        }
        return R.ok(checkin);
    }

    @PostMapping("/api/staff/records")
    public R<BizServiceRecord> record(@RequestBody BizServiceRecord record) {
        BizStaff staff = staff();
        record.setId(null);
        record.setStaffId(staff.getId());
        recordMapper.insert(record);
        return R.ok(record);
    }

    @GetMapping("/api/staff/records/{appointmentId}")
    public R<List<BizServiceRecord>> records(@PathVariable Long appointmentId) {
        return R.ok(recordMapper.selectList(new LambdaQueryWrapper<BizServiceRecord>()
                .eq(BizServiceRecord::getAppointmentId, appointmentId)));
    }

    @PostMapping("/api/staff/appointments/{id}/complete")
    public R<BizAppointment> complete(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        BizAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) throw new BizException(404, "预约不存在");
        appointment.setStatus("COMPLETED");
        appointmentMapper.updateById(appointment);
        if (body != null && body.get("signUrl") != null) {
            BizServiceRecord record = new BizServiceRecord();
            record.setAppointmentId(id);
            record.setStaffId(staff().getId());
            record.setSignedUrl(body.get("signUrl"));
            record.setContent("客户已完成电子签名确认");
            recordMapper.insert(record);
        }
        return R.ok(appointment);
    }

    @GetMapping("/api/staff/income")
    public R<Map<String, Object>> income() {
        BizStaff staff = staff();
        BigDecimal amount = jdbc.queryForObject(
                "select coalesce(sum(s.price),0) from biz_appointment a join biz_service s on s.id=a.service_id " +
                        "where a.tenant_id=? and a.staff_id=? and a.status='COMPLETED' and a.deleted=0",
                BigDecimal.class, TenantContext.getTenantId(), staff.getId());
        Long count = jdbc.queryForObject(
                "select count(*) from biz_appointment where tenant_id=? and staff_id=? and status='COMPLETED' and deleted=0",
                Long.class, TenantContext.getTenantId(), staff.getId());
        return R.ok(Map.of("amount", amount == null ? BigDecimal.ZERO : amount, "completed", count == null ? 0 : count));
    }

    @GetMapping("/api/staff/schedules")
    public R<List<BizSchedule>> schedules() {
        return R.ok(scheduleMapper.selectList(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getStaffId, staff().getId()).orderByDesc(BizSchedule::getWorkDate)));
    }

    @GetMapping("/api/staff/appointments/{id}")
    public R<Map<String, Object>> appointmentDetail(@PathVariable Long id) {
        BizStaff staff = staff();
        BizAppointment appointment = requireOwnAppointment(id, staff.getId());
        BizMember member = appointment.getMemberId() == null ? null
                : memberMapper.selectById(appointment.getMemberId());
        BizBaby baby = appointment.getBabyId() == null ? null
                : babyMapper.selectById(appointment.getBabyId());
        BizService service = appointment.getServiceId() == null ? null
                : serviceMapper.selectById(appointment.getServiceId());
        return R.ok(Map.of(
                "appointment", appointment,
                "member", member == null ? Map.of() : member,
                "baby", baby == null ? Map.of() : baby,
                "service", service == null ? Map.of() : service,
                "checkins", checkinMapper.selectList(new LambdaQueryWrapper<BizCheckin>()
                        .eq(BizCheckin::getAppointmentId, id).orderByDesc(BizCheckin::getCheckinAt)),
                "records", recordMapper.selectList(new LambdaQueryWrapper<BizServiceRecord>()
                        .eq(BizServiceRecord::getAppointmentId, id).orderByDesc(BizServiceRecord::getCreatedAt))));
    }

    private BizAppointment requireOwnAppointment(Long id, Long staffId) {
        BizAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        if (appointment.getStaffId() != null && !staffId.equals(appointment.getStaffId())) {
            throw new BizException(403, "该预约不属于当前护理师");
        }
        return appointment;
    }

    @PostMapping("/api/staff/appointments/{id}/checkin")
    public R<BizCheckin> checkinAppointment(@PathVariable Long id, @RequestBody BizCheckin body) {
        BizStaff staff = staff();
        BizAppointment appointment = requireOwnAppointment(id, staff.getId());
        BizCheckin checkin = new BizCheckin();
        checkin.setAppointmentId(id);
        checkin.setStaffId(staff.getId());
        checkin.setCheckinType(body.getCheckinType() == null ? "GPS" : body.getCheckinType());
        checkin.setLng(body.getLng());
        checkin.setLat(body.getLat());
        checkin.setAddress(body.getAddress());
        checkin.setPhotoUrl(body.getPhotoUrl());
        checkin.setCheckinAt(LocalDateTime.now());
        checkinMapper.insert(checkin);
        appointment.setStatus("SERVING");
        appointmentMapper.updateById(appointment);
        return R.ok(checkin);
    }

    @PostMapping("/api/staff/appointments/{id}/records")
    public R<BizServiceRecord> recordAppointment(@PathVariable Long id, @RequestBody BizServiceRecord body) {
        BizStaff staff = staff();
        BizAppointment appointment = requireOwnAppointment(id, staff.getId());
        BizServiceRecord record = new BizServiceRecord();
        record.setAppointmentId(id);
        record.setStaffId(staff.getId());
        record.setContent(body.getContent());
        record.setBabyTemp(body.getBabyTemp());
        record.setBabyWeight(body.getBabyWeight());
        record.setImages(body.getImages());
        record.setSignedUrl(body.getSignedUrl());
        recordMapper.insert(record);
        if (appointment.getStatus() == null || "PENDING".equals(appointment.getStatus())
                || "CONFIRMED".equals(appointment.getStatus())) {
            appointment.setStatus("SERVING");
            appointmentMapper.updateById(appointment);
        }
        return R.ok(record);
    }

    @GetMapping("/api/staff/income/detail")
    public R<List<Map<String, Object>>> incomeDetail() {
        BizStaff staff = staff();
        return R.ok(jdbc.query(
                "select a.appointment_no, a.appoint_date, a.time_slot, s.name, s.price, s.duration "
                        + "from biz_appointment a join biz_service s on s.id=a.service_id "
                        + "where a.tenant_id=? and a.staff_id=? and a.status='COMPLETED' and a.deleted=0 "
                        + "order by a.appoint_date desc limit 50",
                (rs, i) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("appointmentNo", rs.getString("appointment_no"));
                    row.put("appointDate", rs.getString("appoint_date"));
                    row.put("timeSlot", rs.getString("time_slot"));
                    row.put("serviceName", rs.getString("name"));
                    row.put("price", rs.getBigDecimal("price"));
                    row.put("duration", rs.getInt("duration"));
                    return row;
                },
                TenantContext.getTenantId(), staff.getId()));
    }

    @GetMapping("/api/staff/notices")
    public R<List<PlatformNotice>> notices() {
        return R.ok(noticeMapper.selectList(new LambdaQueryWrapper<PlatformNotice>()
                .eq(PlatformNotice::getStatus, "PUBLISHED")
                .orderByDesc(PlatformNotice::getPublishedAt)
                .last("LIMIT 20")));
    }

    @GetMapping("/api/staff/services")
    public R<List<BizService>> staffServices() {
        return R.ok(serviceMapper.selectList(new LambdaQueryWrapper<BizService>()
                .eq(BizService::getStatus, "ACTIVE")));
    }

    @GetMapping("/api/staff/profile")
    public R<Map<String, Object>> staffProfile() {
        return R.ok(Map.of("staff", staff(), "user", user()));
    }

    // ================= 供应商端 =================
    private BizSupplier supplier() {
        SysUser user = user();
        if (user.getSupplierId() == null) throw new BizException(403, "当前账号未绑定供应商");
        BizSupplier supplier = supplierMapper.selectById(user.getSupplierId());
        if (supplier == null) throw new BizException(404, "供应商档案不存在");
        return supplier;
    }

    @GetMapping("/api/supplier/supply-orders")
    public R<List<BizSupplyOrder>> supplyOrders() {
        return R.ok(supplyOrderMapper.selectList(new LambdaQueryWrapper<BizSupplyOrder>()
                .eq(BizSupplyOrder::getSupplierId, supplier().getId())
                .orderByDesc(BizSupplyOrder::getCreatedAt)));
    }

    @GetMapping("/api/supplier/supply-orders/{id}/items")
    public R<List<BizSupplyItem>> supplyItems(@PathVariable Long id) {
        return R.ok(supplyItemMapper.selectList(new LambdaQueryWrapper<BizSupplyItem>()
                .eq(BizSupplyItem::getSupplyId, id)));
    }

    @PostMapping("/api/supplier/supply-orders/{id}/ship")
    public R<BizSupplyOrder> ship(@PathVariable Long id) {
        BizSupplyOrder order = supplyOrderMapper.selectById(id);
        if (order == null || !supplier().getId().equals(order.getSupplierId())) {
            throw new BizException(403, "无权操作该供货单");
        }
        if ("SIGNED".equals(order.getStatus())) {
            throw new BizException(400, "已签收的供货单不能重复发货");
        }
        order.setStatus("SHIPPED");
        supplyOrderMapper.updateById(order);
        return R.ok(order);
    }

    @PostMapping("/api/supplier/supply-orders/{id}/sign")
    public R<BizSupplyOrder> sign(@PathVariable Long id) {
        BizSupplyOrder order = supplyOrderMapper.selectById(id);
        if (order == null || !supplier().getId().equals(order.getSupplierId())) {
            throw new BizException(403, "无权操作该供货单");
        }
        if ("SIGNED".equals(order.getStatus())) {
            return R.ok(order);
        }
        order.setStatus("SIGNED");
        order.setSignedAt(LocalDateTime.now());
        supplyOrderMapper.updateById(order);
        List<BizSupplyItem> items = supplyItemMapper.selectList(new LambdaQueryWrapper<BizSupplyItem>()
                .eq(BizSupplyItem::getSupplyId, id));
        for (BizSupplyItem item : items) {
            BizSku sku = skuMapper.selectById(item.getSkuId());
            if (sku != null) {
                int before = sku.getStock() == null ? 0 : sku.getStock();
                sku.setStock(before + item.getQty());
                skuMapper.updateById(sku);
                BizStockRecord record = new BizStockRecord();
                record.setSkuId(sku.getId());
                record.setRecordType("IN");
                record.setQty(item.getQty());
                record.setBeforeQty(before);
                record.setAfterQty(sku.getStock());
                record.setBizNo(order.getSupplyNo());
                record.setRemark("供货签收入库");
                stockRecordMapper.insert(record);
            }
        }
        return R.ok(order);
    }

    @GetMapping("/api/supplier/statements")
    public R<List<BizSupplierStatement>> statements() {
        return R.ok(statementMapper.selectList(new LambdaQueryWrapper<BizSupplierStatement>()
                .eq(BizSupplierStatement::getSupplierId, supplier().getId())
                .orderByDesc(BizSupplierStatement::getCreatedAt)));
    }

    @GetMapping("/api/supplier/qualifications")
    public R<List<BizSupplierQualification>> qualifications() {
        return R.ok(qualificationMapper.selectList(new LambdaQueryWrapper<BizSupplierQualification>()
                .eq(BizSupplierQualification::getSupplierId, supplier().getId())
                .orderByDesc(BizSupplierQualification::getCreatedAt)));
    }

    @PostMapping("/api/supplier/qualifications")
    public R<BizSupplierQualification> addQualification(@RequestBody BizSupplierQualification body) {
        if (!StringUtils.hasText(body.getLicenseType()) || !StringUtils.hasText(body.getFileUrl())) {
            throw new BizException(400, "请填写资质类型并上传证件");
        }
        BizSupplierQualification q = new BizSupplierQualification();
        q.setSupplierId(supplier().getId());
        q.setLicenseType(body.getLicenseType());
        q.setLicenseNo(body.getLicenseNo());
        q.setFileUrl(body.getFileUrl());
        q.setExpireDate(body.getExpireDate());
        q.setAuditStatus("PENDING");
        q.setRemark(body.getRemark());
        qualificationMapper.insert(q);
        return R.ok(q);
    }

    @GetMapping("/api/supplier/notices")
    public R<List<PlatformNotice>> supplierNotices() {
        return R.ok(noticeMapper.selectList(new LambdaQueryWrapper<PlatformNotice>()
                .eq(PlatformNotice::getStatus, "PUBLISHED")
                .and(w -> w.eq(PlatformNotice::getTargetType, "ALL")
                        .or().eq(PlatformNotice::getTargetType, "SUPPLIER"))
                .orderByDesc(PlatformNotice::getPublishedAt)
                .last("LIMIT 20")));
    }

    @GetMapping("/api/supplier/profile")
    public R<Map<String, Object>> supplierProfile() {
        return R.ok(Map.of("supplier", supplier(), "user", user()));
    }

    // ================= 渠道商端 =================
    private BizDistributor distributor() {
        SysUser user = user();
        if (user.getDistributorId() == null) throw new BizException(403, "当前账号未绑定渠道商");
        BizDistributor distributor = distributorMapper.selectById(user.getDistributorId());
        if (distributor == null) throw new BizException(404, "渠道商档案不存在");
        return distributor;
    }

    public record ReportReq(Long memberId, Long orderId, BigDecimal amount) {
    }

    @PostMapping("/api/distributor/reports")
    public R<BizDistributorReport> report(@RequestBody ReportReq req) {
        BizDistributor distributor = distributor();
        if (req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(400, "报单金额必须大于 0");
        }
        BigDecimal rate = BigDecimal.valueOf(0.10);
        String rule = distributor.getCommissionRule();
        if (rule != null && rule.startsWith("PERCENT:")) {
            try {
                rate = new BigDecimal(rule.substring(8)).divide(BigDecimal.valueOf(100));
            } catch (Exception ignored) {
                // 保持默认比例
            }
        }
        BigDecimal commission = req.amount().multiply(rate).setScale(2, java.math.RoundingMode.HALF_UP);
        BizDistributorReport report = new BizDistributorReport();
        report.setReportNo(noGenerator.gen("RP"));
        report.setDistributorId(distributor.getId());
        report.setMemberId(req.memberId());
        report.setOrderId(req.orderId());
        report.setAmount(req.amount());
        report.setCommission(commission);
        report.setStatus("PENDING");
        reportMapper.insert(report);

        BizCommissionFlow flow = new BizCommissionFlow();
        flow.setDistributorId(distributor.getId());
        flow.setReportId(report.getId());
        flow.setAmount(commission);
        flow.setFlowType("COMMISSION");
        flow.setStatus("PENDING");
        commissionFlowMapper.insert(flow);
        return R.ok(report);
    }

    @GetMapping("/api/distributor/reports")
    public R<List<BizDistributorReport>> reports() {
        return R.ok(reportMapper.selectList(new LambdaQueryWrapper<BizDistributorReport>()
                .eq(BizDistributorReport::getDistributorId, distributor().getId())
                .orderByDesc(BizDistributorReport::getCreatedAt)));
    }

    @GetMapping("/api/distributor/commissions")
    public R<List<BizCommissionFlow>> commissions() {
        return R.ok(commissionFlowMapper.selectList(new LambdaQueryWrapper<BizCommissionFlow>()
                .eq(BizCommissionFlow::getDistributorId, distributor().getId())
                .orderByDesc(BizCommissionFlow::getCreatedAt)));
    }

    @GetMapping("/api/distributor/credit")
    public R<Map<String, Object>> credit() {
        BizDistributor distributor = distributor();
        return R.ok(Map.of("creditLimit", distributor.getCreditLimit(),
                "usedCredit", distributor.getUsedCredit(),
                "available", distributor.getCreditLimit() == null ? BigDecimal.ZERO
                        : distributor.getCreditLimit().subtract(distributor.getUsedCredit() == null
                        ? BigDecimal.ZERO : distributor.getUsedCredit())));
    }

    @GetMapping("/api/distributor/subordinates")
    public R<List<BizDistributor>> subordinates() {
        return R.ok(distributorMapper.selectList(new LambdaQueryWrapper<BizDistributor>()
                .eq(BizDistributor::getParentId, distributor().getId())));
    }

    @GetMapping("/api/distributor/settlements")
    public R<List<BizDistributorSettlement>> settlements() {
        return R.ok(settlementMapper.selectList(new LambdaQueryWrapper<BizDistributorSettlement>()
                .eq(BizDistributorSettlement::getDistributorId, distributor().getId())
                .orderByDesc(BizDistributorSettlement::getCreatedAt)));
    }

    @GetMapping("/api/distributor/orders")
    public R<List<Map<String, Object>>> myOrders() {
        BizDistributor distributor = distributor();
        return R.ok(jdbc.query(
                "select o.order_no, o.order_type, o.pay_amount, o.status, o.pay_status, o.created_at, r.report_no "
                        + "from biz_distributor_report r join biz_order o on o.id=r.order_id "
                        + "where r.tenant_id=? and r.distributor_id=? and r.deleted=0 "
                        + "order by o.created_at desc limit 50",
                (rs, i) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("orderNo", rs.getString("order_no"));
                    row.put("orderType", rs.getString("order_type"));
                    row.put("payAmount", rs.getBigDecimal("pay_amount"));
                    row.put("status", rs.getString("status"));
                    row.put("payStatus", rs.getString("pay_status"));
                    row.put("createdAt", rs.getString("created_at"));
                    row.put("reportNo", rs.getString("report_no"));
                    return row;
                },
                TenantContext.getTenantId(), distributor.getId()));
    }

    @GetMapping("/api/distributor/notices")
    public R<List<PlatformNotice>> distributorNotices() {
        return R.ok(noticeMapper.selectList(new LambdaQueryWrapper<PlatformNotice>()
                .eq(PlatformNotice::getStatus, "PUBLISHED")
                .and(w -> w.eq(PlatformNotice::getTargetType, "ALL")
                        .or().eq(PlatformNotice::getTargetType, "DISTRIBUTOR"))
                .orderByDesc(PlatformNotice::getPublishedAt)
                .last("LIMIT 20")));
    }

    @GetMapping("/api/distributor/profile")
    public R<Map<String, Object>> distributorProfile() {
        return R.ok(Map.of("distributor", distributor(), "user", user()));
    }
}
