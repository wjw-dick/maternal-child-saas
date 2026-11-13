package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.SystemEntities.SysUser;
import com.quantaida.mc.mapper.SysUserMapper;
import com.quantaida.mc.service.AuthService;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/users")
public class UserController extends BaseCrudController<SysUser> {
    private final AuthService authService;

    public UserController(SysUserMapper mapper, AuthService authService) {
        super(mapper, "user", "username,nickname");
        this.authService = authService;
    }

    @Override
    protected void beforeCreate(SysUser user) {
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(authService.encode("admin123"));
        } else {
            user.setPassword(authService.encode(user.getPassword()));
        }
        if (!StringUtils.hasText(user.getUserType())) {
            user.setUserType("TENANT");
        }
        if (!StringUtils.hasText(user.getStatus())) {
            user.setStatus("ACTIVE");
        }
    }

    @Override
    protected void beforeUpdate(SysUser user) {
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(null);
        } else {
            user.setPassword(authService.encode(user.getPassword()));
        }
    }
}
