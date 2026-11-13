package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizCoupon;
import com.quantaida.mc.mapper.BizCouponMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/coupon")
public class CouponController extends BaseCrudController<BizCoupon> {
    public CouponController(BizCouponMapper mapper) {
        super(mapper, "coupon", "name");
    }
}
