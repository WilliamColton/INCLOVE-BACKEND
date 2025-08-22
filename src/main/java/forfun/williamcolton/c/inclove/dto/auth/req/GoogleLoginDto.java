package forfun.williamcolton.c.inclove.dto.auth.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class GoogleLoginDto {

    @NotBlank
    String idToken;

}
