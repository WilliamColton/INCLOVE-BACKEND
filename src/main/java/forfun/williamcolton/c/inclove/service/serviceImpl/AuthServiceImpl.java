package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import forfun.williamcolton.c.inclove.dto.auth.req.LoginDto;
import forfun.williamcolton.c.inclove.dto.auth.req.RegisterDto;
import forfun.williamcolton.c.inclove.dto.auth.req.VerificationCodeDto;
import forfun.williamcolton.c.inclove.dto.auth.resp.LoginResponseDto;
import forfun.williamcolton.c.inclove.dto.auth.resp.RegisterResponseDto;
import forfun.williamcolton.c.inclove.entity.UserAuth;
import forfun.williamcolton.c.inclove.exception.AuthErrorCode;
import forfun.williamcolton.c.inclove.exception.BusinessException;
import forfun.williamcolton.c.inclove.mapper.UserAuthMapper;
import forfun.williamcolton.c.inclove.service.AuthService;
import forfun.williamcolton.c.inclove.service.EmailService;
import forfun.williamcolton.c.inclove.utils.JwtUtil;
import forfun.williamcolton.c.inclove.utils.VerificationCodeUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthServiceImpl extends BaseServiceImpl<UserAuthMapper, UserAuth> implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthServiceImpl(PasswordEncoder passwordEncoder, EmailService emailService) {
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public LoginResponseDto login(LoginDto loginDto) {
        UserAuth userAuth = getOne(new LambdaQueryWrapper<UserAuth>().eq(UserAuth::getUserId, loginDto.userId()));
        if (userAuth == null || !passwordEncoder.matches(loginDto.rawPassword(), userAuth.getEncodedPassword())) {
            throw new BusinessException(AuthErrorCode.WRONG_CREDENTIALS);
        }
        if (!Boolean.TRUE.equals(userAuth.getVerified())) {
            throw new BusinessException(AuthErrorCode.USER_NOT_VERIFIED);
        }
        String token = JwtUtil.generateToken(userAuth.getUserId());
        return new LoginResponseDto(token);
    }

    @Override
    public RegisterResponseDto register(RegisterDto registerDto) {
        long userCount = count(new LambdaQueryWrapper<UserAuth>().eq(UserAuth::getUserId, registerDto.userId()).or().eq(UserAuth::getEmail, registerDto.email()));
        if (userCount > 0) {
            throw new BusinessException(AuthErrorCode.USER_OR_EMAIL_EXISTS);
        } else {
            var newUserAuth = new UserAuth();
            newUserAuth.setUserId(registerDto.userId());
            newUserAuth.setEmail(registerDto.email());
            newUserAuth.setEncodedPassword(passwordEncoder.encode(registerDto.rawPassword()));
            newUserAuth.setVerified(false);
            String verificationCode = VerificationCodeUtil.buildNumericCode(6);
            newUserAuth.setVerificationCode(verificationCode);
            save(newUserAuth);
            emailService.sendEmail(newUserAuth.getEmail(), "inclove verification code", verificationCode);
            String token = JwtUtil.generateToken(newUserAuth.getUserId());
            return new RegisterResponseDto(token);
        }
    }

    @Override
    public void resendRegisterEmail(String userId) {
        UserAuth userAuth = getOne(new LambdaQueryWrapper<UserAuth>().eq(UserAuth::getUserId, userId));
        if (Objects.nonNull(userAuth)) {
            if (Boolean.TRUE.equals(userAuth.getVerified())) {
                throw new BusinessException(AuthErrorCode.USER_ALREADY_VERIFIED);
            } else {
                emailService.sendEmail(userAuth.getEmail(), "inclove verification code", userAuth.getVerificationCode());
            }
        } else {
            throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);
        }
    }

    @Override
    public void verifyRegisterVerificationCode(VerificationCodeDto verificationCodeDto, String userId) {
        UserAuth userAuth = getOne(new LambdaQueryWrapper<UserAuth>().eq(UserAuth::getUserId, userId));
        if (Objects.nonNull(userAuth)) {
            if (Boolean.TRUE.equals(userAuth.getVerified())) {
                throw new BusinessException(AuthErrorCode.USER_ALREADY_VERIFIED);
            } else {
                if (userAuth.getVerificationCode().equals(verificationCodeDto.verificationCode())) {
                    userAuth.setVerified(true);
                    userAuth.setVerificationCode(null);
                    updateById(userAuth);
                } else {
                    throw new BusinessException(AuthErrorCode.INVALID_VERIFICATION_CODE);
                }
            }
        } else {
            throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);
        }
    }

}
