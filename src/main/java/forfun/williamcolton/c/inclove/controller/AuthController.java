package forfun.williamcolton.c.inclove.controller;

import forfun.williamcolton.c.inclove.dto.auth.req.GoogleLoginDto;
import forfun.williamcolton.c.inclove.dto.auth.req.LoginDto;
import forfun.williamcolton.c.inclove.dto.auth.req.RegisterDto;
import forfun.williamcolton.c.inclove.dto.auth.req.VerificationCodeDto;
import forfun.williamcolton.c.inclove.dto.auth.resp.LoginResponseDto;
import forfun.williamcolton.c.inclove.dto.auth.resp.RegisterResponseDto;
import forfun.williamcolton.c.inclove.service.AuthService;
import forfun.williamcolton.c.inclove.service.serviceImpl.GoogleAuthServiceImpl;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthServiceImpl googleAuthServiceImpl;

    public AuthController(AuthService authService, GoogleAuthServiceImpl googleAuthServiceImpl) {
        this.authService = authService;
        this.googleAuthServiceImpl = googleAuthServiceImpl;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginDto loginDto) {
        return authService.login(loginDto);
    }

    @PostMapping("/login/google")
    public LoginResponseDto googleLogin(@Valid @RequestBody GoogleLoginDto googleLoginDto) {
        return googleAuthServiceImpl.googleLogin(googleLoginDto);
    }

    @PostMapping("/register")
    public RegisterResponseDto register(@Valid @RequestBody RegisterDto registerDto) {
        return authService.register(registerDto);
    }

    @PostMapping("/register/email")
    public RegisterResponseDto resendRegisterEmail(@Valid @RequestBody RegisterDto registerDto) {
        return authService.resendRegisterEmail(registerDto);
    }

    @PostMapping("/register/verificationCode")
    public RegisterResponseDto verifyRegisterVerificationCode(@Valid @RequestBody VerificationCodeDto verificationCodeDto) {
        return authService.verifyRegisterVerificationCode(verificationCodeDto);
    }

    @GetMapping("/me")
    public String getCurrentUserId(Authentication authentication) {
        return (String) authentication.getPrincipal();
    }

}
