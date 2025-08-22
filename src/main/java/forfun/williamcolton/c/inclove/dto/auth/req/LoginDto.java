package forfun.williamcolton.c.inclove.dto.auth.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDto(@NotBlank @Email String email, String userId, @NotBlank String rawPassword) {

}
