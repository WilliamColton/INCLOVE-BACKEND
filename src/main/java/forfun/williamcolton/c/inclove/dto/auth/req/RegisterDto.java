package forfun.williamcolton.c.inclove.dto.auth.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterDto(@NotBlank @Email String email, @NotBlank String userId, @NotBlank String rawPassword) {

}
