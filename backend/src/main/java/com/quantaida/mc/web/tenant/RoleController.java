package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.SystemEntities.SysRole;
import com.quantaida.mc.mapper.SysRoleMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/roles")
public class RoleController extends BaseCrudController<SysRole> {
    public RoleController(SysRoleMapper mapper) {
        super(mapper, "role", "name,code");
    }
}
