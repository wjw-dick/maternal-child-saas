package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.CmsEntities.CmsArticle;
import com.quantaida.mc.mapper.CmsArticleMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/article")
public class ArticleController extends BaseCrudController<CmsArticle> {
    public ArticleController(CmsArticleMapper mapper) {
        super(mapper, "article", "title");
    }
}
