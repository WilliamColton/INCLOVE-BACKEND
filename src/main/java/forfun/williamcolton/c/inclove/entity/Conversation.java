package forfun.williamcolton.c.inclove.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
public class Conversation {

    private String id;
    @NotNull
    private String firstUserId;
    @NotNull
    private String secondUserId;

}
