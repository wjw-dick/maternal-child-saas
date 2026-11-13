package com.quantaida.mc.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quantaida.mc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class BizEntities {
    private BizEntities() {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_member")
    public static class BizMember extends BaseEntity {
        private String memberNo;
        private String name;
        private String phone;
        private String phoneEnc;
        private String phoneHash;
        private String avatar;
        private String idCard;
        private String idCardEnc;
        private String gender;
        private LocalDate birthday;
        private String level;
        private BigDecimal balance;
        private Integer points;
        private String tags;
        private String source;
        private String openid;
        private String unionid;
        private String status;
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_baby")
    public static class BizBaby extends BaseEntity {
        private Long memberId;
        private String name;
        private String gender;
        private LocalDate birthday;
        private String allergy;
        private String healthNote;
        private String avatar;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_baby_growth")
    public static class BizBabyGrowth extends BaseEntity {
        private Long babyId;
        private LocalDate recordDate;
        private BigDecimal height;
        private BigDecimal weight;
        private String note;
        private String images;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_store")
    public static class BizStore extends BaseEntity {
        private String code;
        private String name;
        private String address;
        private String phone;
        private String businessHours;
        private String serviceRange;
        private String deliveryRange;
        private BigDecimal lng;
        private BigDecimal lat;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_service")
    public static class BizService extends BaseEntity {
        private String code;
        private String name;
        private String category;
        private BigDecimal price;
        private Integer duration;
        private String ageRange;
        private String cover;
        private String description;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_staff")
    public static class BizStaff extends BaseEntity {
        private Long userId;
        private Long storeId;
        private String name;
        private String roleType;
        private String phone;
        private String level;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_staff_license")
    public static class BizStaffLicense extends BaseEntity {
        private Long staffId;
        private String licenseType;
        private String licenseNo;
        private LocalDate expireAt;
        private String fileUrl;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_schedule")
    public static class BizSchedule extends BaseEntity {
        private Long staffId;
        private Long storeId;
        private LocalDate workDate;
        private String timeSlot;
        private Integer capacity;
        private Integer booked;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_product")
    public static class BizProduct extends BaseEntity {
        private String spuCode;
        private String name;
        private String category;
        private String brand;
        private String ageRange;
        private String cover;
        private String images;
        private String detail;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_sku")
    public static class BizSku extends BaseEntity {
        private Long productId;
        private String skuCode;
        private String specJson;
        private BigDecimal price;
        private BigDecimal costPrice;
        private Integer stock;
        private Integer safetyStock;
        private Long supplierId;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_stock_record")
    public static class BizStockRecord extends BaseEntity {
        private Long skuId;
        private String recordType;
        private Integer qty;
        private Integer beforeQty;
        private Integer afterQty;
        private String bizNo;
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_order")
    public static class BizOrder extends BaseEntity {
        private String orderNo;
        private Long memberId;
        private Long storeId;
        private String orderType;
        private BigDecimal totalAmount;
        private BigDecimal discountAmount;
        private BigDecimal payAmount;
        private String payStatus;
        private String status;
        private String payChannel;
        private LocalDateTime payTime;
        private String remark;
        private String source;
        private String idempotencyKey;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_order_item")
    public static class BizOrderItem extends BaseEntity {
        private Long orderId;
        private String itemType;
        private Long itemId;
        private String itemName;
        private String spec;
        private BigDecimal price;
        private Integer qty;
        private BigDecimal amount;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_payment")
    public static class BizPayment extends BaseEntity {
        private String payNo;
        private Long orderId;
        private String channel;
        private BigDecimal amount;
        private String status;
        private String tradeNo;
        private LocalDateTime paidAt;
        private String rawJson;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_refund")
    public static class BizRefund extends BaseEntity {
        private String refundNo;
        private Long orderId;
        private Long paymentId;
        private BigDecimal amount;
        private String reason;
        private String status;
        private LocalDateTime refundedAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_fulfillment")
    public static class BizFulfillment extends BaseEntity {
        private Long orderId;
        private String fulfillType;
        private Long staffId;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime finishedAt;
        private String signUrl;
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_appointment")
    public static class BizAppointment extends BaseEntity {
        private String appointmentNo;
        private Long memberId;
        private Long babyId;
        private Long storeId;
        private Long serviceId;
        private Long staffId;
        private LocalDate appointDate;
        private String timeSlot;
        private String appointType;
        private String address;
        private String contact;
        private String phone;
        private String status;
        private String verifyCode;
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_dispatch")
    public static class BizDispatch extends BaseEntity {
        private Long appointmentId;
        private Long staffId;
        private String status;
        private LocalDateTime acceptedAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_checkin")
    public static class BizCheckin extends BaseEntity {
        private Long appointmentId;
        private Long staffId;
        private String checkinType;
        private BigDecimal lng;
        private BigDecimal lat;
        private String address;
        private String photoUrl;
        private LocalDateTime checkinAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_service_record")
    public static class BizServiceRecord extends BaseEntity {
        private Long appointmentId;
        private Long staffId;
        private String content;
        private BigDecimal babyTemp;
        private BigDecimal babyWeight;
        private String images;
        private String signedUrl;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_wallet")
    public static class BizWallet extends BaseEntity {
        private Long memberId;
        private BigDecimal balance;
        private BigDecimal totalRecharge;
        private BigDecimal totalConsume;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_wallet_flow")
    public static class BizWalletFlow extends BaseEntity {
        private Long memberId;
        private String flowNo;
        private String flowType;
        private BigDecimal amount;
        private BigDecimal balance;
        private String bizNo;
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_points_rule")
    public static class BizPointsRule extends BaseEntity {
        private String name;
        private String ruleType;
        private BigDecimal ratio;
        private Integer enabled;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_points_flow")
    public static class BizPointsFlow extends BaseEntity {
        private Long memberId;
        private String flowType;
        private Integer points;
        private Integer balance;
        private String bizNo;
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_coupon")
    public static class BizCoupon extends BaseEntity {
        private String name;
        private String couponType;
        private BigDecimal faceValue;
        private BigDecimal minAmount;
        private Integer total;
        private Integer issued;
        private LocalDate validFrom;
        private LocalDate validTo;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_member_coupon")
    public static class BizMemberCoupon extends BaseEntity {
        private Long couponId;
        private Long memberId;
        private String status;
        private LocalDateTime usedAt;
        private Long orderId;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_activity")
    public static class BizActivity extends BaseEntity {
        private String title;
        private String cover;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private String address;
        private Integer capacity;
        private Integer joined;
        private BigDecimal price;
        private String status;
        private String content;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_activity_signup")
    public static class BizActivitySignup extends BaseEntity {
        private Long activityId;
        private Long memberId;
        private Long babyId;
        private String phone;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_supplier")
    public static class BizSupplier extends BaseEntity {
        private String code;
        private String name;
        private String contact;
        private String phone;
        private String address;
        private String agreementNo;
        private String rating;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_supplier_qualification")
    public static class BizSupplierQualification extends BaseEntity {
        private Long supplierId;
        private String licenseType;
        private String licenseNo;
        private String fileUrl;
        private LocalDate expireDate;
        private String auditStatus;
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_supply_order")
    public static class BizSupplyOrder extends BaseEntity {
        private String supplyNo;
        private Long supplierId;
        private Long storeId;
        private BigDecimal totalAmount;
        private String status;
        private LocalDateTime expectedAt;
        private LocalDateTime signedAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_supply_item")
    public static class BizSupplyItem extends BaseEntity {
        private Long supplyId;
        private Long skuId;
        private String name;
        private Integer qty;
        private BigDecimal price;
        private BigDecimal amount;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_supplier_statement")
    public static class BizSupplierStatement extends BaseEntity {
        private Long supplierId;
        private String period;
        private BigDecimal amount;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_distributor")
    public static class BizDistributor extends BaseEntity {
        private String code;
        private String name;
        private String contact;
        private String phone;
        private Long parentId;
        private String commissionRule;
        private BigDecimal creditLimit;
        private BigDecimal usedCredit;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_distributor_report")
    public static class BizDistributorReport extends BaseEntity {
        private String reportNo;
        private Long distributorId;
        private Long memberId;
        private Long orderId;
        private BigDecimal amount;
        private BigDecimal commission;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_commission_flow")
    public static class BizCommissionFlow extends BaseEntity {
        private Long distributorId;
        private Long reportId;
        private BigDecimal amount;
        private String flowType;
        private String status;
        private LocalDateTime settledAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("biz_distributor_settlement")
    public static class BizDistributorSettlement extends BaseEntity {
        private String settlementNo;
        private Long distributorId;
        private String period;
        private BigDecimal amount;
        private String status;
        private LocalDateTime paidAt;
    }
}
