package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class AuthServiceImpl extends ServiceImpl<UserAuthMapper, UserAuth> implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthServiceImpl(PasswordEncoder passwordEncoder, EmailService emailService) {
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public LoginResponseDto login(LoginDto loginDto) {
        UserAuth userAuth = getOne(new LambdaQueryWrapper<UserAuth>().eq(UserAuth::getUserId, loginDto.getUserId()));
        if (userAuth == null || !passwordEncoder.matches(loginDto.getRawPassword(), userAuth.getEncodedPassword())) {
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
        long userCount = count(new LambdaQueryWrapper<UserAuth>().eq(UserAuth::getUserId, registerDto.getUserId()).or().eq(UserAuth::getEmail, registerDto.getEmail()));
        if (userCount > 0) {
            throw new BusinessException(AuthErrorCode.USER_OR_EMAIL_EXISTS);
        } else {
            var newUserAuth = new UserAuth();
            newUserAuth.setUserId(registerDto.getUserId());
            newUserAuth.setEmail(registerDto.getEmail());
            newUserAuth.setEncodedPassword(passwordEncoder.encode(registerDto.getRawPassword()));
            newUserAuth.setVerified(false);
            String verificationCode = VerificationCodeUtil.buildNumericCode(6);
            newUserAuth.setVerificationCode(verificationCode);
            save(newUserAuth);
            emailService.sendEmail(newUserAuth.getEmail(), "inclove verification code", verificationCode);
            return new RegisterResponseDto(newUserAuth.getEmail(), newUserAuth.getUserId());
        }
    }

    @Override
    public RegisterResponseDto resendRegisterEmail(RegisterDto registerDto) {
        UserAuth userAuth = getOne(new LambdaQueryWrapper<UserAuth>().eq(UserAuth::getUserId, registerDto.getUserId()));
        if (Objects.nonNull(userAuth)) {
            if (userAuth.getVerified()) {
                throw new BusinessException(AuthErrorCode.USER_ALREADY_VERIFIED);
            } else {
                emailService.sendEmail(userAuth.getEmail(), "inclove verification code", userAuth.getVerificationCode());
                return new RegisterResponseDto(userAuth.getEmail(), userAuth.getUserId());
            }
        } else {
            throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);
        }
    }

    @Override
    public RegisterResponseDto verifyRegisterVerificationCode(VerificationCodeDto verificationCodeDto) {
        UserAuth userAuth = getOne(new LambdaQueryWrapper<UserAuth>().eq(UserAuth::getUserId, verificationCodeDto.getUserId()));
        if (Objects.nonNull(userAuth)) {
            if (userAuth.getVerified()) {
                throw new BusinessException(AuthErrorCode.USER_ALREADY_VERIFIED);
            } else {
                if (userAuth.getVerificationCode().equals(verificationCodeDto.getVerificationCode())) {
                    userAuth.setVerified(true);
                    userAuth.setVerificationCode(null);
                    updateById(userAuth);
                    return new RegisterResponseDto(userAuth.getEmail(), userAuth.getUserId());
                } else {
                    throw new BusinessException(AuthErrorCode.INVALID_VERIFICATION_CODE);
                }
            }
        } else {
            throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);
        }
    }

}
