package com.quantaida.mc.web;

import com.quantaida.mc.common.R;
import com.quantaida.mc.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping({"/api/tenant/files", "/api/staff/files", "/api/supplier/files",
        "/api/distributor/files", "/api/mini/files", "/api/h5/files"})
@RequiredArgsConstructor
public class FileController {
    private final StorageService storageService;

    @PostMapping
    public R<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam(required = false) String bizType,
                                         @RequestParam(required = false) Long bizId) {
        return R.ok(storageService.upload(file, bizType, bizId));
    }
}
