package com.quantaida.mc.web;

import com.quantaida.mc.common.R;
import com.quantaida.mc.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class CommonAuthController {
    private final AuthService authService;

    public record ChangePasswordReq(String oldPassword, String newPassword) {
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        return R.ok(authService.me());
    }

    @PostMapping("/change-password")
    public R<Void> changePassword(@RequestBody ChangePasswordReq req) {
        authService.changePassword(req.oldPassword(), req.newPassword());
        return R.ok();
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }
}
