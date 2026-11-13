package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizMemberCoupon;
import com.quantaida.mc.mapper.BizMemberCouponMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/member-coupons")
public class MemberCouponController extends BaseCrudController<BizMemberCoupon> {
    public MemberCouponController(BizMemberCouponMapper mapper) {
        super(mapper, "membercoupon", "status");
    }
}
