package com.quantaida.mc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quantaida.mc.entity.SystemEntities.SysMenu;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT m.code FROM sys_menu m JOIN sys_role_menu rm ON rm.menu_id = m.id " +
            "WHERE rm.role_id = #{roleId} AND m.code IS NOT NULL AND m.deleted = 0 AND rm.deleted = 0")
    List<String> selectPermsByRole(Long roleId);
}
