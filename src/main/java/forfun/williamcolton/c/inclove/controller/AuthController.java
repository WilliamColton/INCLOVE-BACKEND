package forfun.williamcolton.c.inclove.controller;

import forfun.williamcolton.c.inclove.dto.auth.*;
import forfun.williamcolton.c.inclove.service.AuthService;
import forfun.williamcolton.c.inclove.service.serviceImpl.GoogleAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    public AuthController(AuthService authService, GoogleAuthService googleAuthService) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginDto loginDto) {
        return authService.login(loginDto);
    }

    @PostMapping("/login/google")
    public LoginResponseDto googleLogin(@RequestBody GoogleLoginDto googleLoginDto) {
        return googleAuthService.googleLogin(googleLoginDto);
    }

    @PostMapping("/register")
    public RegisterResponseDto register(@RequestBody RegisterDto registerDto) {
        return authService.register(registerDto);
    }

    @PostMapping("/register/email")
    public RegisterResponseDto resendRegisterEmail(@RequestBody RegisterDto registerDto) {
        return authService.resendRegisterEmail(registerDto);
    }

    @PostMapping("/register/verificationCode")
    public RegisterResponseDto verifyRegisterVerificationCode(VerificationCodeDto verificationCodeDto) {
        return authService.verifyRegisterVerificationCode(verificationCodeDto);
    }

}
