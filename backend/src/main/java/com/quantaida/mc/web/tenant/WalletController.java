package com.quantaida.mc.web.tenant;

import com.quantaida.mc.common.R;
import com.quantaida.mc.entity.BizEntities.BizWallet;
import com.quantaida.mc.entity.BizEntities.BizWalletFlow;
import com.quantaida.mc.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tenant/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    public record AmountReq(Long memberId, BigDecimal amount, String remark) {
    }

    @GetMapping("/{memberId}")
    public R<BizWallet> detail(@PathVariable Long memberId) {
        return R.ok(walletService.getOrCreate(memberId));
    }

    @GetMapping("/{memberId}/flows")
    public R<List<BizWalletFlow>> flows(@PathVariable Long memberId) {
        return R.ok(walletService.flows(memberId));
    }

    @PostMapping("/recharge")
    public R<BizWallet> recharge(@RequestBody AmountReq req) {
        return R.ok(walletService.recharge(req.memberId(), req.amount(), req.remark()));
    }

    @PostMapping("/consume")
    public R<BizWallet> consume(@RequestBody AmountReq req) {
        return R.ok(walletService.consume(req.memberId(), req.amount(), req.remark()));
    }
}
