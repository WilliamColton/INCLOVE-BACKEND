package forfun.williamcolton.c.inclove.dto.auth.req;

import jakarta.validation.constraints.NotBlank;

public record VerificationCodeDto(@NotBlank String verificationCode) {

}
