package forfun.williamcolton.c.inclove.service;

import com.baomidou.mybatisplus.extension.service.IService;
import forfun.williamcolton.c.inclove.dto.auth.req.LoginDto;
import forfun.williamcolton.c.inclove.dto.auth.req.RegisterDto;
import forfun.williamcolton.c.inclove.dto.auth.req.VerificationCodeDto;
import forfun.williamcolton.c.inclove.dto.auth.resp.LoginResponseDto;
import forfun.williamcolton.c.inclove.dto.auth.resp.RegisterResponseDto;
import forfun.williamcolton.c.inclove.entity.UserAuth;

public interface AuthService extends IService<UserAuth> {
    LoginResponseDto login(LoginDto loginDto);

    RegisterResponseDto register(RegisterDto registerDto);

    RegisterResponseDto resendRegisterEmail(RegisterDto registerDto);

    RegisterResponseDto verifyRegisterVerificationCode(VerificationCodeDto verificationCodeDto);
}
