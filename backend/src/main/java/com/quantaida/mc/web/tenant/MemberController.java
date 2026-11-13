package com.quantaida.mc.web.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.NoGenerator;
import com.quantaida.mc.common.R;
import com.quantaida.mc.common.SensitiveUtil;
import com.quantaida.mc.entity.BizEntities.BizBaby;
import com.quantaida.mc.entity.BizEntities.BizMember;
import com.quantaida.mc.entity.BizEntities.BizMemberCoupon;
import com.quantaida.mc.entity.BizEntities.BizWallet;
import com.quantaida.mc.mapper.BizBabyMapper;
import com.quantaida.mc.mapper.BizMemberCouponMapper;
import com.quantaida.mc.mapper.BizMemberMapper;
import com.quantaida.mc.mapper.BizWalletMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant/members")
public class MemberController extends BaseCrudController<BizMember> {
    private final SensitiveUtil sensitiveUtil;
    private final NoGenerator noGenerator;
    private final BizWalletMapper walletMapper;
    private final BizBabyMapper babyMapper;
    private final BizMemberCouponMapper memberCouponMapper;

    public MemberController(BizMemberMapper mapper, SensitiveUtil sensitiveUtil, NoGenerator noGenerator,
                            BizWalletMapper walletMapper, BizBabyMapper babyMapper,
                            BizMemberCouponMapper memberCouponMapper) {
        super(mapper, "member", "name,phone,member_no");
        this.sensitiveUtil = sensitiveUtil;
        this.noGenerator = noGenerator;
        this.walletMapper = walletMapper;
        this.babyMapper = babyMapper;
        this.memberCouponMapper = memberCouponMapper;
    }

    @Override
    protected void beforeCreate(BizMember member) {
        if (!StringUtils.hasText(member.getMemberNo())) {
            member.setMemberNo(noGenerator.gen("M"));
        }
        encrypt(member);
        if (StringUtils.hasText(member.getPhoneHash())) {
            Long same = mapper.selectCount(new LambdaQueryWrapper<BizMember>()
                    .eq(BizMember::getPhoneHash, member.getPhoneHash()));
            if (same > 0) {
                throw new BizException(400, "该手机号已建档，请勿重复");
            }
        }
        if (StringUtils.hasText(member.getUnionid())) {
            Long same = mapper.selectCount(new LambdaQueryWrapper<BizMember>()
                    .eq(BizMember::getUnionid, member.getUnionid()));
            if (same > 0) {
                throw new BizException(400, "该 UnionID 已绑定会员，禁止重复建档");
            }
        }
        if (member.getBalance() == null) {
            member.setBalance(java.math.BigDecimal.ZERO);
        }
        if (member.getPoints() == null) {
            member.setPoints(0);
        }
    }

    @Override
    protected void beforeUpdate(BizMember member) {
        if (StringUtils.hasText(member.getPhone()) && !member.getPhone().contains("*")) {
            encrypt(member);
        }
    }

    private void encrypt(BizMember member) {
        if (StringUtils.hasText(member.getPhone()) && !member.getPhone().contains("*")) {
            String raw = member.getPhone();
            member.setPhone(sensitiveUtil.maskPhone(raw));
            member.setPhoneEnc(sensitiveUtil.encrypt(raw));
            member.setPhoneHash(sensitiveUtil.hash(raw));
        }
        if (StringUtils.hasText(member.getIdCard()) && !member.getIdCard().contains("*")) {
            String raw = member.getIdCard();
            member.setIdCard(sensitiveUtil.maskIdCard(raw));
            member.setIdCardEnc(sensitiveUtil.encrypt(raw));
        }
    }

    @GetMapping("/by-phone")
    public R<BizMember> byPhone(@RequestParam String phone) {
        permissionService.require(perm("read"));
        BizMember member = mapper.selectOne(new LambdaQueryWrapper<BizMember>()
                .eq(BizMember::getPhoneHash, sensitiveUtil.hash(phone)));
        if (member == null) {
            throw new BizException(404, "会员不存在");
        }
        return R.ok(member);
    }

    @GetMapping("/{id}/center")
    public R<Map<String, Object>> center(@PathVariable Long id) {
        permissionService.require(perm("read"));
        BizMember member = mapper.selectById(id);
        if (member == null) {
            assertNotCrossTenant(id);
            throw new BizException(404, "会员不存在");
        }
        BizWallet wallet = walletMapper.selectOne(new LambdaQueryWrapper<BizWallet>()
                .eq(BizWallet::getMemberId, id));
        List<BizBaby> babies = babyMapper.selectList(new LambdaQueryWrapper<BizBaby>()
                .eq(BizBaby::getMemberId, id));
        List<BizMemberCoupon> coupons = memberCouponMapper.selectList(new LambdaQueryWrapper<BizMemberCoupon>()
                .eq(BizMemberCoupon::getMemberId, id));
        return R.ok(Map.of("member", member, "wallet", wallet == null ? Map.of() : wallet,
                "babies", babies, "coupons", coupons));
    }
}
