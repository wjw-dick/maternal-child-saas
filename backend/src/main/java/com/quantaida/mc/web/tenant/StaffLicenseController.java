package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizStaffLicense;
import com.quantaida.mc.mapper.BizStaffLicenseMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/staff-licenses")
public class StaffLicenseController extends BaseCrudController<BizStaffLicense> {
    public StaffLicenseController(BizStaffLicenseMapper mapper) {
        super(mapper, "license", "licenseNo");
    }
}
