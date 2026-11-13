package com.quantaida.mc.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.BizEntities.BizActivity;
import com.quantaida.mc.entity.BizEntities.BizProduct;
import com.quantaida.mc.entity.BizEntities.BizService;
import com.quantaida.mc.entity.BizEntities.BizStore;
import com.quantaida.mc.entity.CmsEntities.CmsArticle;
import com.quantaida.mc.entity.CmsEntities.CmsCase;
import com.quantaida.mc.entity.CmsEntities.CmsFormSubmission;
import com.quantaida.mc.entity.SystemEntities.SysI18n;
import com.quantaida.mc.entity.SystemEntities.SysMenuConfig;
import com.quantaida.mc.mapper.BizActivityMapper;
import com.quantaida.mc.mapper.BizServiceMapper;
import com.quantaida.mc.mapper.BizStoreMapper;
import com.quantaida.mc.mapper.CmsArticleMapper;
import com.quantaida.mc.mapper.CmsFormSubmissionMapper;
import com.quantaida.mc.mapper.SysI18nMapper;
import com.quantaida.mc.mapper.SysMenuConfigMapper;
import com.quantaida.mc.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PC 官网 / H5 公开接口（免登录）。
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {
    private final PublicService publicService;
    private final CmsArticleMapper articleMapper;
    private final CmsFormSubmissionMapper formMapper;
    private final BizServiceMapper serviceMapper;
    private final BizStoreMapper storeMapper;
    private final BizActivityMapper activityMapper;
    private final SysI18nMapper i18nMapper;
    private final SysMenuConfigMapper menuConfigMapper;

    @GetMapping("/home")
    public R<Map<String, Object>> home(@RequestParam(defaultValue = "PC") String terminal) {
        return R.ok(publicService.home(terminal));
    }

    @GetMapping("/services")
    public R<List<BizService>> services() {
        return R.ok(publicService.services());
    }

    @GetMapping("/services/{id}")
    public R<BizService> service(@PathVariable Long id) {
        return R.ok(serviceMapper.selectById(id));
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

    @GetMapping("/articles")
    public R<List<CmsArticle>> articles() {
        return R.ok(publicService.articles());
    }

    @GetMapping("/articles/{id}")
    public R<CmsArticle> article(@PathVariable Long id) {
        return R.ok(articleMapper.selectById(id));
    }

    @GetMapping("/cases")
    public R<List<CmsCase>> cases() {
        return R.ok(publicService.cases());
    }

    @GetMapping("/activities")
    public R<List<BizActivity>> activities() {
        return R.ok(publicService.activities());
    }

    @GetMapping("/config")
    public R<Map<String, String>> config() {
        Map<String, String> map = new java.util.LinkedHashMap<>();
        publicService.configs().forEach(c -> map.put(c.getConfigKey(), c.getConfigValue()));
        return R.ok(map);
    }

    @GetMapping("/i18n/{lang}")
    public R<Map<String, String>> i18n(@PathVariable String lang) {
        List<SysI18n> list = i18nMapper.selectList(new LambdaQueryWrapper<SysI18n>()
                .eq(SysI18n::getLangCode, lang));
        Map<String, String> map = new java.util.LinkedHashMap<>();
        list.forEach(i -> map.put(i.getI18nKey(), i.getI18nValue()));
        return R.ok(map);
    }

    @GetMapping("/menus/{terminal}")
    public R<SysMenuConfig> menus(@PathVariable String terminal) {
        return R.ok(menuConfigMapper.selectOne(new LambdaQueryWrapper<SysMenuConfig>()
                .eq(SysMenuConfig::getTerminal, terminal)));
    }

    @PostMapping("/forms")
    public R<Void> submitForm(@RequestBody CmsFormSubmission form) {
        formMapper.insert(form);
        return R.ok();
    }
}
