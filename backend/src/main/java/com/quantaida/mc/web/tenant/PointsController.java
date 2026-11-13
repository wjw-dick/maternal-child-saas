package com.quantaida.mc.web.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.BizEntities.BizMember;
import com.quantaida.mc.entity.BizEntities.BizPointsFlow;
import com.quantaida.mc.mapper.BizMemberMapper;
import com.quantaida.mc.mapper.BizPointsFlowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会员积分：调整积分并落流水，供后台积分规则/流水页使用。
 */
@RestController
@RequestMapping("/api/tenant/points")
@RequiredArgsConstructor
public class PointsController {
    private final BizMemberMapper memberMapper;
    private final BizPointsFlowMapper flowMapper;

    public record AdjustReq(Long memberId, Integer points, String remark, String bizNo) {
    }

    @PostMapping("/adjust")
    public R<BizPointsFlow> adjust(@RequestBody AdjustReq req) {
        if (req.memberId() == null || req.points() == null || req.points() == 0) {
            throw new BizException(400, "请选择会员并填写非零积分");
        }
        BizMember member = memberMapper.selectById(req.memberId());
        if (member == null) {
            throw new BizException(404, "会员不存在");
        }
        int balance = (member.getPoints() == null ? 0 : member.getPoints()) + req.points();
        if (balance < 0) {
            throw new BizException(400, "积分余额不足");
        }
        member.setPoints(balance);
        memberMapper.updateById(member);

        BizPointsFlow flow = new BizPointsFlow();
        flow.setMemberId(member.getId());
        flow.setFlowType(req.points() > 0 ? "ADJUST_ADD" : "ADJUST_SUB");
        flow.setPoints(req.points());
        flow.setBalance(balance);
        flow.setBizNo(req.bizNo());
        flow.setRemark(req.remark() == null ? "后台调整积分" : req.remark());
        flowMapper.insert(flow);
        return R.ok(flow);
    }

    @GetMapping("/members/{memberId}/flows")
    public R<List<BizPointsFlow>> memberFlows(@PathVariable Long memberId) {
        return R.ok(flowMapper.selectList(new LambdaQueryWrapper<BizPointsFlow>()
                .eq(BizPointsFlow::getMemberId, memberId)
                .orderByDesc(BizPointsFlow::getCreatedAt)));
    }

    @GetMapping("/members/{memberId}/summary")
    public R<Map<String, Object>> summary(@PathVariable Long memberId) {
        BizMember member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException(404, "会员不存在");
        }
        return R.ok(Map.of("memberId", memberId, "points", member.getPoints() == null ? 0 : member.getPoints()));
    }
}
