package forfun.williamcolton.c.inclove.dto.auth.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class RegisterDto {

    @NotBlank
    @Email
    String email;
    @NotBlank
    String userId;
    @NotBlank
    String rawPassword;

}
