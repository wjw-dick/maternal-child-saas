package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizStaff;
import com.quantaida.mc.mapper.BizStaffMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/staff")
public class StaffController extends BaseCrudController<BizStaff> {
    public StaffController(BizStaffMapper mapper) {
        super(mapper, "staff", "name,phone");
    }
}
