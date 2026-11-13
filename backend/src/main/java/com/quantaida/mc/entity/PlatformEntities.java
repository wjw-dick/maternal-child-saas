package com.quantaida.mc.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quantaida.mc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PlatformEntities {
    private PlatformEntities() {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("platform_tenant")
    public static class PlatformTenant extends BaseEntity {
        private String tenantCode;
        private String name;
        private String industry;
        private String contact;
        private String phone;
        private Long planId;
        private String status;
        private LocalDateTime trialEndAt;
        private LocalDateTime expireAt;
        private Integer seatLimit;
        private Integer mpAccountLimit;
        private Integer storageQuotaGb;
        private Integer smsQuota;
        private Integer apiQuota;
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("platform_plan")
    public static class PlatformPlan extends BaseEntity {
        private String code;
        private String name;
        private BigDecimal price;
        private String billingCycle;
        private Integer endCount;
        private Integer seatLimit;
        private Integer mpAccountLimit;
        private Integer storageQuotaGb;
        private Integer smsQuota;
        private Integer apiQuota;
        private Integer trialDays;
        private String status;
        private String features;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("platform_tenant_quota_usage")
    public static class PlatformQuotaUsage extends BaseEntity {
        private Long tenantId;
        private Integer usedSeats;
        private Integer usedMp;
        private Long usedStorageMb;
        private Integer usedSms;
        private Integer usedApi;
        private LocalDate statDate;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("platform_bill")
    public static class PlatformBill extends BaseEntity {
        private Long tenantId;
        private String period;
        private String item;
        private Integer quantity;
        private BigDecimal amount;
        private String status;
        private LocalDateTime paidAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("platform_audit")
    public static class PlatformAudit extends BaseEntity {
        private String operatorType;
        private Long operatorId;
        private String operatorName;
        private Long tenantId;
        private String action;
        private String target;
        private String result;
        private String ip;
        private String detail;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("platform_tenant_admin")
    public static class PlatformTenantAdmin extends BaseEntity {
        private Long tenantId;
        private Long userId;
        private Integer isPrimary;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("platform_notice")
    public static class PlatformNotice extends BaseEntity {
        private String title;
        private String content;
        private String targetType;
        private String targetTenantIds;
        private String status;
        private LocalDateTime publishedAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("platform_notice_read")
    public static class PlatformNoticeRead extends BaseEntity {
        private Long noticeId;
        private Long tenantId;
        private Long userId;
        private LocalDateTime readAt;
    }
}
