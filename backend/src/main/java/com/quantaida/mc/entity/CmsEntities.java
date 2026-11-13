package com.quantaida.mc.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.quantaida.mc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

public final class CmsEntities {
    private CmsEntities() {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("cms_article")
    public static class CmsArticle extends BaseEntity {
        private String title;
        private String titleEn;
        private String content;
        private String contentEn;
        private String cover;
        private String seoTitle;
        private String seoKeywords;
        private String seoDesc;
        private String category;
        private String status;
        private LocalDateTime publishedAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("cms_banner")
    public static class CmsBanner extends BaseEntity {
        private String terminal;
        private String title;
        private String image;
        private String link;
        private Integer sortNo;
        private String status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("cms_form_submission")
    public static class CmsFormSubmission extends BaseEntity {
        private String formType;
        private String name;
        private String phone;
        private String content;
        private String source;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @TableName("cms_case")
    public static class CmsCase extends BaseEntity {
        private String title;
        private String titleEn;
        private String cover;
        private String content;
        private Integer sortNo;
        private String status;
    }
}
