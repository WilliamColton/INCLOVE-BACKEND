package forfun.williamcolton.c.inclove.dto.auth.req;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginDto(@NotBlank String idToken) {

}
