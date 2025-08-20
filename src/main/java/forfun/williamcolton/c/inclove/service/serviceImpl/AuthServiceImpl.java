package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import forfun.williamcolton.c.inclove.dto.auth.*;
import forfun.williamcolton.c.inclove.dto.email.EmailSendResponse;
import forfun.williamcolton.c.inclove.entity.UserAuth;
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
        UserAuth userAuth = getOne(new QueryWrapper<UserAuth>().eq("user_id", loginDto.getUserId()));
        if (Objects.nonNull(userAuth)) {
            if (passwordEncoder.matches(loginDto.getRawPassword(), userAuth.getEncodedPassword())) {
                if (!userAuth.getVerified()) {
                    throw new BusinessException(2009, "this user has not been verified yet");
                }
                String token = JwtUtil.generateToken(userAuth.getUserId());
                return new LoginResponseDto(token);
            } else {
                throw new BusinessException(1900, "wrong account or password");
            }
        } else {
            throw new BusinessException(1900, "the user does not exist");
        }
    }

    @Override
    public RegisterResponseDto register(RegisterDto registerDto) {
        long userCount = count(new QueryWrapper<UserAuth>().eq("user_id", registerDto.getUserId()).or().eq("email", registerDto.getEmail()));
        if (userCount > 0) {
            throw new BusinessException(1901, "the user id or email already exists");
        } else {
            var newUserAuth = new UserAuth();
            newUserAuth.setUserId(registerDto.getUserId());
            newUserAuth.setEmail(registerDto.getEmail());
            newUserAuth.setEncodedPassword(passwordEncoder.encode(registerDto.getRawPassword()));
            newUserAuth.setVerified(false);
            String verificationCode = VerificationCodeUtil.buildNumericCode(6);
            newUserAuth.setVerificationCode(verificationCode);
            emailService.sendEmail(newUserAuth.getEmail(), "inclove verification code", verificationCode);
            save(newUserAuth);
            return new RegisterResponseDto(newUserAuth.getEmail(), newUserAuth.getUserId());
        }
    }

    @Override
    public RegisterResponseDto resendRegisterEmail(RegisterDto registerDto) {
        UserAuth userAuth = getOne(new QueryWrapper<UserAuth>().eq("user_id", registerDto.getUserId()));
        if (Objects.nonNull(userAuth)) {
            if (userAuth.getVerified()) {
                throw new BusinessException(2006, "This user has been verified");
            } else {
                emailService.sendEmail(userAuth.getEmail(), "inclove verification code", userAuth.getVerificationCode());
                return new RegisterResponseDto(userAuth.getEmail(), userAuth.getUserId());
            }
        } else {
            throw new BusinessException(2007, "the user does not exist");
        }
    }

    @Override
    public RegisterResponseDto verifyRegisterVerificationCode(VerificationCodeDto verificationCodeDto) {
        UserAuth userAuth = getOne(new QueryWrapper<UserAuth>().eq("user_id", verificationCodeDto.getUserId()));
        if (Objects.nonNull(userAuth)) {
            if (userAuth.getVerified()) {
                throw new BusinessException(2010, "this user has been verified");
            } else {
                if (userAuth.getVerificationCode().equals(verificationCodeDto.getVerificationCode())) {
                    userAuth.setVerified(true);
                    updateById(userAuth);
                    return new RegisterResponseDto(userAuth.getEmail(), userAuth.getUserId());
                }else {
                    throw new BusinessException(2011,"Invalid verification code");
                }
            }
        } else {
            throw new BusinessException(2007, "the user does not exist");
        }
    }

}
