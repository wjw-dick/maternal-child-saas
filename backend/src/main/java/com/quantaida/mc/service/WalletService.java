package com.quantaida.mc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.mc.common.BizException;
import com.quantaida.mc.common.NoGenerator;
import com.quantaida.mc.entity.BizEntities.BizMember;
import com.quantaida.mc.entity.BizEntities.BizWallet;
import com.quantaida.mc.entity.BizEntities.BizWalletFlow;
import com.quantaida.mc.mapper.BizMemberMapper;
import com.quantaida.mc.mapper.BizWalletFlowMapper;
import com.quantaida.mc.mapper.BizWalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final BizWalletMapper walletMapper;
    private final BizWalletFlowMapper flowMapper;
    private final BizMemberMapper memberMapper;
    private final NoGenerator noGenerator;

    @Transactional
    public BizWallet recharge(Long memberId, BigDecimal amount, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(400, "充值金额必须大于 0");
        }
        BizWallet wallet = getOrCreate(memberId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setTotalRecharge(wallet.getTotalRecharge().add(amount));
        walletMapper.updateById(wallet);
        writeFlow(memberId, "RECHARGE", amount, wallet.getBalance(), remark);
        BizMember member = memberMapper.selectById(memberId);
        if (member != null) {
            member.setBalance(wallet.getBalance());
            memberMapper.updateById(member);
        }
        return wallet;
    }

    @Transactional
    public BizWallet consume(Long memberId, BigDecimal amount, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(400, "消费金额必须大于 0");
        }
        BizWallet wallet = getOrCreate(memberId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BizException(400, "储值余额不足");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setTotalConsume(wallet.getTotalConsume().add(amount));
        walletMapper.updateById(wallet);
        writeFlow(memberId, "CONSUME", amount.negate(), wallet.getBalance(), remark);
        BizMember member = memberMapper.selectById(memberId);
        if (member != null) {
            member.setBalance(wallet.getBalance());
            memberMapper.updateById(member);
        }
        return wallet;
    }

    public BizWallet getOrCreate(Long memberId) {
        BizWallet wallet = walletMapper.selectOne(new LambdaQueryWrapper<BizWallet>()
                .eq(BizWallet::getMemberId, memberId));
        if (wallet == null) {
            wallet = new BizWallet();
            wallet.setMemberId(memberId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setTotalRecharge(BigDecimal.ZERO);
            wallet.setTotalConsume(BigDecimal.ZERO);
            walletMapper.insert(wallet);
        }
        return wallet;
    }

    private void writeFlow(Long memberId, String type, BigDecimal amount, BigDecimal balance, String remark) {
        BizWalletFlow flow = new BizWalletFlow();
        flow.setMemberId(memberId);
        flow.setFlowNo(noGenerator.gen("WF"));
        flow.setFlowType(type);
        flow.setAmount(amount);
        flow.setBalance(balance);
        flow.setRemark(remark);
        flowMapper.insert(flow);
    }

    public List<BizWalletFlow> flows(Long memberId) {
        return flowMapper.selectList(new LambdaQueryWrapper<BizWalletFlow>()
                .eq(BizWalletFlow::getMemberId, memberId).orderByDesc(BizWalletFlow::getCreatedAt));
    }
}
