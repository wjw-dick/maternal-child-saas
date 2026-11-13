package com.quantaida.mc.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quantaida.mc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

public final class WxEntities {
    private WxEntities() {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("wx_account")
    public static class WxAccount extends BaseEntity {
        private String name;
        private String wxId;
        private String appId;
        private String appSecret;
        private String serverUrl;
        private String token;
        private String aesKey;
        private String authDomain;
        private String jsDomain;
        private String qrUrl;
        private String accountType;
        private Integer verified;
        private Integer apiQuota;
        private Integer apiUsed;
        private String status;
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("wx_mini_config")
    public static class WxMiniConfig extends BaseEntity {
        private String appId;
        private String appSecret;
        private String legalDomain;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("wx_follower")
    public static class WxFollower extends BaseEntity {
        private String openid;
        private String unionid;
        private String nickname;
        private String avatar;
        private String gender;
        private String city;
        private String province;
        private Integer subscribed;
        private String tags;
        private LocalDateTime subscribedAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("wx_tag")
    public static class WxTag extends BaseEntity {
        private Long wxTagId;
        private String name;
        private Integer count;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("wx_message")
    public static class WxMessage extends BaseEntity {
        private String direction;
        private String msgType;
        private String openid;
        private String content;
        private Integer errCode;
        private String errMsg;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("wx_template")
    public static class WxTemplate extends BaseEntity {
        private String wxTemplateId;
        private String title;
        private String industry;
        private String content;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("wx_menu_publish")
    public static class WxMenuPublish extends BaseEntity {
        private String menuJson;
        private String status;
        private String wxResp;
        private LocalDateTime publishedAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("wx_autoreply")
    public static class WxAutoreply extends BaseEntity {
        private String replyType;
        private String keyword;
        private String msgType;
        private String content;
        private String mediaId;
        private Integer enabled;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("wx_material")
    public static class WxMaterial extends BaseEntity {
        private String materialType;
        private String name;
        private String url;
        private String thumbUrl;
        private String mediaId;
        private Long size;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("wx_draft")
    public static class WxDraft extends BaseEntity {
        private String title;
        private String author;
        private String digest;
        private String coverUrl;
        private String content;
        private String multiJson;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("wx_publish_record")
    public static class WxPublishRecord extends BaseEntity {
        private Long draftId;
        private String mediaId;
        private String status;
        private String failReason;
        private LocalDateTime publishedAt;
    }
}
