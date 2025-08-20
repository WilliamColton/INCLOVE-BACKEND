package forfun.williamcolton.c.inclove.dto.auth;

import lombok.Value;

@Value
public class VerificationCodeDto {

    String userId;
    String verificationCode;

}
