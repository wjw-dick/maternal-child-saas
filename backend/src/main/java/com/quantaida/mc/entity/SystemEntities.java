package com.quantaida.mc.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.quantaida.mc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

public final class SystemEntities {
    private SystemEntities() {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_user")
    public static class SysUser extends BaseEntity {
        private String username;
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private String password;
        private String nickname;
        private String phone;
        private String email;
        private String avatar;
        private String userType;
        private Long roleId;
        private Long storeId;
        private Long staffId;
        private Long supplierId;
        private Long distributorId;
        private String openid;
        private String unionid;
        private String status;
        private LocalDateTime lastLoginAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_role")
    public static class SysRole extends BaseEntity {
        private String name;
        private String code;
        private String dataScope;
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_menu")
    public static class SysMenu extends BaseEntity {
        private Long parentId;
        private String scope;
        private String name;
        private String code;
        private String menuType;
        private String path;
        private String component;
        private String icon;
        private Integer sortNo;
        private Integer visible;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_role_menu")
    public static class SysRoleMenu extends BaseEntity {
        private Long roleId;
        private Long menuId;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_audit_log")
    public static class SysAuditLog extends BaseEntity {
        private Long userId;
        private String userName;
        private String module;
        private String action;
        private String method;
        private String params;
        private String ip;
        private String result;
        private Long costMs;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_config")
    public static class SysConfig extends BaseEntity {
        private String configKey;
        private String configValue;
        private String groupCode;
        private Integer encrypted;
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_language")
    public static class SysLanguage extends BaseEntity {
        private String code;
        private String name;
        private Integer enabled;
        private Integer isDefault;
        private Integer sortNo;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_i18n")
    public static class SysI18n extends BaseEntity {
        private String langCode;
        private String i18nKey;
        private String i18nValue;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_menu_config")
    public static class SysMenuConfig extends BaseEntity {
        private String terminal;
        private String treeJson;
        private Integer version;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_storage_config")
    public static class SysStorageConfig extends BaseEntity {
        private String driver;
        private String configJson;
        private Integer enabled;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_message_channel")
    public static class SysMessageChannel extends BaseEntity {
        private String channel;
        private Integer enabled;
        private String configJson;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_sms_channel")
    public static class SysSmsChannel extends BaseEntity {
        private String provider;
        private Integer enabled;
        private String configJson;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_message_template")
    public static class SysMessageTemplate extends BaseEntity {
        private String node;
        private String channel;
        private String templateId;
        private String title;
        private String fieldMapping;
        private Integer enabled;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_sso_config")
    public static class SysSsoConfig extends BaseEntity {
        private Integer enabled;
        private String protocol;
        private String baseUrl;
        private String clientId;
        private String clientSecret;
        private String accountMapping;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_attachment")
    public static class SysAttachment extends BaseEntity {
        private String bizType;
        private Long bizId;
        private String name;
        private String url;
        private Long size;
        private String ext;
        private String driver;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_dict_type")
    public static class SysDictType extends BaseEntity {
        private String dictCode;
        private String dictName;
        private String scope;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_dict_item")
    public static class SysDictItem extends BaseEntity {
        private String dictCode;
        private String itemValue;
        private String itemLabel;
        private Integer sortNo;
        private Integer status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("open_app")
    public static class OpenApp extends BaseEntity {
        private String name;
        private String appKey;
        private String appSecret;
        private String scopes;
        private Integer apiQuota;
        private Integer usedApi;
        private String status;
    }
}
