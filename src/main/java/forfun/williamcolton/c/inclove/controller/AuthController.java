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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public void resendRegisterEmail(Authentication authentication) {
        authService.resendRegisterEmail((String) authentication.getPrincipal());
    }

    @PostMapping("/register/verificationCode")
    public void verifyRegisterVerificationCode(@Valid @RequestBody VerificationCodeDto verificationCodeDto, Authentication authentication) {
        authService.verifyRegisterVerificationCode(verificationCodeDto, (String) authentication.getPrincipal());
    }

}
