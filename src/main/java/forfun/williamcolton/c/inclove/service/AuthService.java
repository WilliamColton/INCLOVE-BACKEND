package forfun.williamcolton.c.inclove.service;

import com.baomidou.mybatisplus.extension.service.IService;
import forfun.williamcolton.c.inclove.dto.auth.*;
import forfun.williamcolton.c.inclove.entity.UserAuth;
import forfun.williamcolton.c.inclove.utils.VerificationCodeUtil;

public interface AuthService extends IService<UserAuth> {
    LoginResponseDto login(LoginDto loginDto);

    RegisterResponseDto register(RegisterDto registerDto);

    RegisterResponseDto resendRegisterEmail(RegisterDto registerDto);

    RegisterResponseDto verifyRegisterVerificationCode(VerificationCodeDto verificationCodeDto);
}
