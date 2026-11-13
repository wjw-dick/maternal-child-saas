package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.SystemEntities.SysMenu;
import com.quantaida.mc.mapper.SysMenuMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/menus")
public class MenuController extends BaseCrudController<SysMenu> {
    public MenuController(SysMenuMapper mapper) {
        super(mapper, "menu", "name,code");
    }
}
