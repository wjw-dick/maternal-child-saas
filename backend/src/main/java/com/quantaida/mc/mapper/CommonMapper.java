package com.quantaida.mc.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CommonMapper {

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT tenant_id FROM ${table} WHERE id = #{id}")
    Long findTenantId(@Param("table") String table, @Param("id") Long id);

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT COUNT(*) FROM ${table} WHERE tenant_id = #{tenantId} AND deleted = 0")
    long countByTenant(@Param("table") String table, @Param("tenantId") Long tenantId);

    @InterceptorIgnore(tenantLine = "true")
    @Insert("INSERT INTO sys_role(tenant_id,name,code,data_scope,remark,deleted,created_at,updated_at) " +
            "SELECT #{tenantId},name,code,data_scope,remark,0,NOW(),NOW() FROM sys_role WHERE tenant_id=0 AND deleted=0")
    int copyRoleTemplates(@Param("tenantId") Long tenantId);

    @InterceptorIgnore(tenantLine = "true")
    @Insert("INSERT INTO sys_menu(tenant_id,parent_id,scope,name,code,menu_type,path,component,icon,sort_no,visible,status,deleted,created_at,updated_at) " +
            "SELECT #{tenantId},parent_id,scope,name,code,menu_type,path,component,icon,sort_no,visible,status,0,NOW(),NOW() FROM sys_menu WHERE tenant_id=0 AND scope='TENANT' AND deleted=0")
    int copyMenuTemplates(@Param("tenantId") Long tenantId);

    @InterceptorIgnore(tenantLine = "true")
    @Insert("INSERT INTO sys_config(tenant_id,config_key,config_value,group_code,encrypted,remark,created_at,updated_at) " +
            "SELECT #{tenantId},config_key,config_value,group_code,0,remark,NOW(),NOW() FROM sys_config WHERE tenant_id=0")
    int copyConfigTemplates(@Param("tenantId") Long tenantId);

    @InterceptorIgnore(tenantLine = "true")
    @Insert("INSERT INTO sys_language(tenant_id,code,name,enabled,is_default,sort_no,created_at,updated_at) " +
            "SELECT #{tenantId},code,name,enabled,is_default,sort_no,NOW(),NOW() FROM sys_language WHERE tenant_id=0")
    int copyLanguageTemplates(@Param("tenantId") Long tenantId);

    @InterceptorIgnore(tenantLine = "true")
    @Insert("INSERT INTO sys_role_menu(tenant_id,role_id,menu_id) " +
            "SELECT #{tenantId}, r.id, m.id FROM sys_role r JOIN sys_menu m ON m.tenant_id=#{tenantId} " +
            "WHERE r.tenant_id=#{tenantId} AND r.code='TENANT_ADMIN' AND m.deleted=0")
    int grantAdminAllMenus(@Param("tenantId") Long tenantId);
}
