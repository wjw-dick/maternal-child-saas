package com.quantaida.mc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.BizEntities.BizActivity;
import com.quantaida.mc.entity.BizEntities.BizProduct;
import com.quantaida.mc.entity.BizEntities.BizService;
import com.quantaida.mc.entity.BizEntities.BizSku;
import com.quantaida.mc.entity.BizEntities.BizStore;
import com.quantaida.mc.entity.CmsEntities.CmsArticle;
import com.quantaida.mc.entity.CmsEntities.CmsBanner;
import com.quantaida.mc.entity.CmsEntities.CmsCase;
import com.quantaida.mc.entity.PlatformEntities.PlatformTenant;
import com.quantaida.mc.entity.SystemEntities.SysConfig;
import com.quantaida.mc.mapper.BizActivityMapper;
import com.quantaida.mc.mapper.BizProductMapper;
import com.quantaida.mc.mapper.BizServiceMapper;
import com.quantaida.mc.mapper.BizSkuMapper;
import com.quantaida.mc.mapper.BizStoreMapper;
import com.quantaida.mc.mapper.CmsArticleMapper;
import com.quantaida.mc.mapper.CmsBannerMapper;
import com.quantaida.mc.mapper.CmsCaseMapper;
import com.quantaida.mc.mapper.PlatformTenantMapper;
import com.quantaida.mc.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublicService {
    private final BizServiceMapper serviceMapper;
    private final BizProductMapper productMapper;
    private final BizSkuMapper skuMapper;
    private final BizStoreMapper storeMapper;
    private final BizActivityMapper activityMapper;
    private final CmsArticleMapper articleMapper;
    private final CmsBannerMapper bannerMapper;
    private final CmsCaseMapper caseMapper;
    private final SysConfigMapper configMapper;
    private final PlatformTenantMapper tenantMapper;

    public Map<String, Object> home(String terminal) {
        Long tid = TenantContext.getTenantId();
        PlatformTenant tenant = tenantMapper.selectById(tid);
        List<CmsBanner> banners = bannerMapper.selectList(new LambdaQueryWrapper<CmsBanner>()
                .eq(CmsBanner::getTerminal, terminal).eq(CmsBanner::getStatus, "ACTIVE")
                .orderByAsc(CmsBanner::getSortNo));
        List<BizService> services = serviceMapper.selectList(new LambdaQueryWrapper<BizService>()
                .eq(BizService::getStatus, "ACTIVE").orderByDesc(BizService::getCreatedAt).last("LIMIT 6"));
        List<BizProduct> products = productMapper.selectList(new LambdaQueryWrapper<BizProduct>()
                .eq(BizProduct::getStatus, "ACTIVE").orderByDesc(BizProduct::getCreatedAt).last("LIMIT 6"));
        List<BizStore> stores = storeMapper.selectList(new LambdaQueryWrapper<BizStore>()
                .eq(BizStore::getStatus, "ACTIVE"));
        List<CmsArticle> articles = articleMapper.selectList(new LambdaQueryWrapper<CmsArticle>()
                .eq(CmsArticle::getStatus, "PUBLISHED").orderByDesc(CmsArticle::getPublishedAt).last("LIMIT 5"));
        List<CmsCase> cases = caseMapper.selectList(new LambdaQueryWrapper<CmsCase>()
                .eq(CmsCase::getStatus, "ACTIVE").orderByAsc(CmsCase::getSortNo).last("LIMIT 6"));
        Map<String, String> config = new java.util.LinkedHashMap<>();
        configMapper.selectList(null).forEach(c -> config.put(c.getConfigKey(), c.getConfigValue()));
        return Map.of("tenant", tenant == null ? Map.of() : tenant, "banners", banners, "services", services,
                "products", products, "stores", stores, "articles", articles, "cases", cases, "config", config);
    }

    public Map<String, Object> productDetail(Long id) {
        BizProduct product = productMapper.selectById(id);
        List<BizSku> skus = skuMapper.selectList(new LambdaQueryWrapper<BizSku>()
                .eq(BizSku::getProductId, id).eq(BizSku::getStatus, "ACTIVE"));
        return Map.of("product", product == null ? Map.of() : product, "skus", skus);
    }

    public List<BizService> services() {
        return serviceMapper.selectList(new LambdaQueryWrapper<BizService>().eq(BizService::getStatus, "ACTIVE"));
    }

    public List<BizProduct> products() {
        return productMapper.selectList(new LambdaQueryWrapper<BizProduct>().eq(BizProduct::getStatus, "ACTIVE"));
    }

    public List<BizStore> stores() {
        return storeMapper.selectList(new LambdaQueryWrapper<BizStore>().eq(BizStore::getStatus, "ACTIVE"));
    }

    public List<CmsArticle> articles() {
        return articleMapper.selectList(new LambdaQueryWrapper<CmsArticle>()
                .eq(CmsArticle::getStatus, "PUBLISHED").orderByDesc(CmsArticle::getPublishedAt));
    }

    public List<BizActivity> activities() {
        return activityMapper.selectList(new LambdaQueryWrapper<BizActivity>().eq(BizActivity::getStatus, "ACTIVE"));
    }

    public List<CmsCase> cases() {
        return caseMapper.selectList(new LambdaQueryWrapper<CmsCase>().eq(CmsCase::getStatus, "ACTIVE"));
    }

    public List<SysConfig> configs() {
        return configMapper.selectList(null);
    }
}
