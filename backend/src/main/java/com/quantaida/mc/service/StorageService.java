package com.quantaida.mc.service;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.entity.SystemEntities.SysAttachment;
import com.quantaida.mc.entity.SystemEntities.SysStorageConfig;
import com.quantaida.mc.mapper.SysAttachmentMapper;
import com.quantaida.mc.mapper.SysStorageConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 对象存储抽象：本地驱动完整可用；云端驱动预留统一入口，切换后新写走新驱动。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
    private static final List<String> ALLOWED = List.of("jpg", "jpeg", "png", "gif", "webp", "pdf",
            "doc", "docx", "xls", "xlsx", "mp4", "mp3", "amr", "txt", "zip");

    private final SysStorageConfigMapper storageConfigMapper;
    private final SysAttachmentMapper attachmentMapper;
    private final QuotaService quotaService;

    @Value("${app.storage.local-root:./data/storage}")
    private String defaultRoot;
    @Value("${app.storage.public-prefix:/files}")
    private String publicPrefix;

    public Map<String, Object> upload(MultipartFile file, String bizType, Long bizId) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "请选择文件");
        }
        String original = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file";
        String ext = FileUtil.extName(original);
        if (ext == null || !ALLOWED.contains(ext.toLowerCase())) {
            throw new BizException(400, "不支持的文件类型：" + ext);
        }
        Long tenantId = TenantContext.getTenantId();
        quotaService.checkStorage(tenantId, file.getSize());

        SysStorageConfig config = storageConfigMapper.selectOne(new LambdaQueryWrapper<>());
        String driver = config == null || !StringUtils.hasText(config.getDriver()) ? "LOCAL" : config.getDriver();
        if (!"LOCAL".equals(driver)) {
            throw new BizException(50010, "当前存储驱动 " + driver + " 未装配 SDK，请切换本地存储或安装对应驱动");
        }
        String root = defaultRoot;
        String prefix = publicPrefix;
        if (config != null && StringUtils.hasText(config.getConfigJson())) {
            String json = config.getConfigJson();
            root = extract(json, "root", root);
            prefix = extract(json, "publicPrefix", prefix);
        }
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext.toLowerCase();
        String relative = "tenants/" + tenantId + "/" + (StringUtils.hasText(bizType) ? bizType : "common")
                + "/" + date + "/" + fileName;
        try {
            Path target = Paths.get(root).toAbsolutePath().normalize().resolve(relative);
            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
        } catch (Exception e) {
            log.error("文件写入失败", e);
            throw new BizException(500, "文件上传失败");
        }
        String url = prefix + "/" + relative;
        SysAttachment attachment = new SysAttachment();
        attachment.setBizType(bizType);
        attachment.setBizId(bizId);
        attachment.setName(original);
        attachment.setUrl(url);
        attachment.setSize(file.getSize());
        attachment.setExt(ext);
        attachment.setDriver(driver);
        attachmentMapper.insert(attachment);
        quotaService.addStorageUsage(tenantId, file.getSize());
        return Map.of("id", attachment.getId(), "url", url, "name", original, "size", file.getSize());
    }

    private String extract(String json, String key, String fallback) {
        String token = "\"" + key + "\"";
        int i = json.indexOf(token);
        if (i < 0) {
            return fallback;
        }
        int start = json.indexOf('"', json.indexOf(':', i) + 1);
        int end = start < 0 ? -1 : json.indexOf('"', start + 1);
        return start > 0 && end > start ? json.substring(start + 1, end) : fallback;
    }
}
