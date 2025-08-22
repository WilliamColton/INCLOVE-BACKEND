package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import forfun.williamcolton.c.inclove.dto.auth.req.GoogleLoginDto;
import forfun.williamcolton.c.inclove.dto.auth.resp.LoginResponseDto;
import forfun.williamcolton.c.inclove.entity.UserAuth;
import forfun.williamcolton.c.inclove.exception.AuthErrorCode;
import forfun.williamcolton.c.inclove.exception.BusinessException;
import forfun.williamcolton.c.inclove.mapper.UserAuthMapper;
import forfun.williamcolton.c.inclove.utils.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class GoogleAuthServiceImpl extends ServiceImpl<UserAuthMapper, UserAuth> {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public GoogleAuthServiceImpl(GoogleIdTokenVerifier googleIdTokenVerifier) {
        this.googleIdTokenVerifier = googleIdTokenVerifier;
    }

    public LoginResponseDto googleLogin(GoogleLoginDto googleLoginDto) {
        GoogleIdToken idToken = null;

        try {
            idToken = googleIdTokenVerifier.verify(googleLoginDto.getIdToken());
        } catch (Exception e) {
            throw new BusinessException(AuthErrorCode.GOOGLE_VERIFICATION_FAILED);
        }

        if (Objects.nonNull(idToken)) {
            Payload payload = idToken.getPayload();
            String userId = payload.getSubject();
            String email = payload.getEmail();
            boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());

            if (!emailVerified) {
                throw new BusinessException(AuthErrorCode.GOOGLE_EMAIL_NOT_VERIFIED);
            }

            UserAuth userAuth = getOne(new LambdaQueryWrapper<UserAuth>().eq(UserAuth::getUserId, userId));
            if (Objects.isNull(userAuth)) {
                    var emailOwnerOpt = lambdaQuery().eq(UserAuth::getEmail, email).oneOpt();
                    if (emailOwnerOpt.isPresent() && !userId.equals(emailOwnerOpt.get().getUserId())) {
                        throw new BusinessException(AuthErrorCode.OAUTH_ACCOUNT_CONFLICT);
                    }

                    var newUserAuth = new UserAuth();
                newUserAuth.setVerified(true);
                newUserAuth.setUserId(userId);
                newUserAuth.setEmail(payload.getEmail());
                newUserAuth.setEncodedPassword(null);
                save(newUserAuth);
            }

            String token = JwtUtil.generateToken(userId);
            return new LoginResponseDto(token);
        } else {
            throw new BusinessException(AuthErrorCode.GOOGLE_INVALID_ID_TOKEN);
        }
    }

}
