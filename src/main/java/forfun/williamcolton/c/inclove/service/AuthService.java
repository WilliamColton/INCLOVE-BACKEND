package forfun.williamcolton.c.inclove.service;

import forfun.williamcolton.c.inclove.dto.auth.req.LoginDto;
import forfun.williamcolton.c.inclove.dto.auth.req.RegisterDto;
import forfun.williamcolton.c.inclove.dto.auth.req.VerificationCodeDto;
import forfun.williamcolton.c.inclove.dto.auth.resp.LoginResponseDto;
import forfun.williamcolton.c.inclove.dto.auth.resp.RegisterResponseDto;
import forfun.williamcolton.c.inclove.entity.UserAuth;

public interface AuthService extends IBaseService<UserAuth> {
    LoginResponseDto login(LoginDto loginDto);

    RegisterResponseDto register(RegisterDto registerDto);

    void resendRegisterEmail(String userId);

    void verifyRegisterVerificationCode(VerificationCodeDto verificationCodeDto, String userId);
}
