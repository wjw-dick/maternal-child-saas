package com.quantaida.mc.web.platform;

import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.PlatformEntities.PlatformNotice;
import com.quantaida.mc.mapper.PlatformNoticeMapper;
import com.quantaida.mc.service.TenantService;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/platform/notices")
public class PlatformNoticeController extends BaseCrudController<PlatformNotice> {
    private final TenantService tenantService;

    public PlatformNoticeController(PlatformNoticeMapper mapper, TenantService tenantService) {
        super(mapper, "platform:notice", "title");
        this.tenantService = tenantService;
    }

    @PostMapping("/{id}/publish")
    public R<PlatformNotice> publish(@PathVariable Long id) {
        PlatformNotice notice = mapper.selectById(id);
        if (notice == null) {
            throw new com.quantaida.mc.common.BizException(404, "公告不存在");
        }
        notice.setStatus("PUBLISHED");
        notice.setPublishedAt(LocalDateTime.now());
        mapper.updateById(notice);
        tenantService.platformAudit("notice:publish", "notice:" + id, "SUCCESS", notice.getTitle());
        return R.ok(notice);
    }
}
