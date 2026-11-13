package com.quantaida.mc.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quantaida.mc.common.AuditService;
import com.quantaida.mc.common.BaseEntity;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.R;
import com.quantaida.mc.common.TenantContext;
import com.quantaida.mc.mapper.CommonMapper;
import com.quantaida.mc.security.PermissionService;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Map;

/**
 * 通用租户 CRUD：自动校验权限、租户越权与审计。
 */
public abstract class BaseCrudController<T extends BaseEntity> {
    protected final BaseMapper<T> mapper;
    protected final String permPrefix;
    private final String keywordColumns;
    private final String tableName;
    private final Class<T> entityClass;

    @Autowired
    protected PermissionService permissionService;
    @Autowired
    protected AuditService auditService;
    @Autowired
    protected CommonMapper commonMapper;

    @SuppressWarnings("unchecked")
    protected BaseCrudController(BaseMapper<T> mapper, String permPrefix, String keywordColumns) {
        this.mapper = mapper;
        this.permPrefix = permPrefix;
        this.keywordColumns = keywordColumns;
        this.entityClass = (Class<T>) ResolvableType.forClass(getClass()).getSuperType().resolveGeneric(0);
        TableName tn = this.entityClass.getAnnotation(TableName.class);
        this.tableName = tn == null ? "" : tn.value();
    }

    protected String perm(String action) {
        return permPrefix + ":" + action;
    }

    @GetMapping
    public R<Page<T>> page(@RequestParam(defaultValue = "1") long page,
                           @RequestParam(defaultValue = "10") long size,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Map<String, String> params) {
        permissionService.require(perm("read"));
        QueryWrapper<T> qw = new QueryWrapper<>();
        applyFilters(qw, params);
        if (StringUtils.hasText(keyword) && StringUtils.hasText(keywordColumns)) {
            qw.and(w -> {
                String[] cols = keywordColumns.split(",");
                for (int i = 0; i < cols.length; i++) {
                    if (i == 0) {
                        w.like(cols[i].trim(), keyword);
                    } else {
                        w.or().like(cols[i].trim(), keyword);
                    }
                }
            });
        }
        qw.orderByDesc("created_at");
        return R.ok(mapper.selectPage(new Page<>(page, size), qw));
    }

    @GetMapping("/all")
    public R<java.util.List<T>> all() {
        permissionService.require(perm("read"));
        QueryWrapper<T> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at").last("LIMIT 500");
        return R.ok(mapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public R<T> detail(@PathVariable Long id) {
        permissionService.require(perm("read"));
        T entity = mapper.selectById(id);
        if (entity == null) {
            assertNotCrossTenant(id);
            throw new BizException(404, "数据不存在");
        }
        return R.ok(entity);
    }

    @PostMapping
    public R<T> create(@RequestBody T entity) {
        permissionService.require(perm("write"));
        beforeCreate(entity);
        entity.setId(null);
        mapper.insert(entity);
        auditService.log(permPrefix, "create", "SUCCESS");
        return R.ok(entity);
    }

    @PutMapping("/{id}")
    public R<T> update(@PathVariable Long id, @RequestBody T entity) {
        permissionService.require(perm("write"));
        T exists = mapper.selectById(id);
        if (exists == null) {
            assertNotCrossTenant(id);
            throw new BizException(404, "数据不存在");
        }
        entity.setId(id);
        beforeUpdate(entity);
        mapper.updateById(entity);
        auditService.log(permPrefix, "update", "SUCCESS");
        return R.ok(mapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        permissionService.require(perm("write"));
        T exists = mapper.selectById(id);
        if (exists == null) {
            assertNotCrossTenant(id);
            throw new BizException(404, "数据不存在");
        }
        mapper.deleteById(id);
        auditService.log(permPrefix, "delete", "SUCCESS");
        return R.ok();
    }

    protected void applyFilters(QueryWrapper<T> qw, Map<String, String> params) {
    }

    protected void beforeCreate(T entity) {
    }

    protected void beforeUpdate(T entity) {
    }

    /** 跨租户直连资源 ID：403 + 安全审计（checklist §一.2） */
    protected void assertNotCrossTenant(Long id) {
        if (tableName.startsWith("platform_")) {
            return;
        }
        Long owner = commonMapper.findTenantId(tableName, id);
        if (owner != null && !owner.equals(TenantContext.getTenantId())) {
            auditService.log(permPrefix, "cross-tenant-access", "DENIED");
            throw new BizException(403, "无权访问其他租户数据");
        }
    }

    protected String keywordColumns() {
        return keywordColumns;
    }

    protected java.util.List<String> split(String s) {
        return StringUtils.hasText(s) ? Arrays.asList(s.split(",")) : java.util.List.of();
    }
}
