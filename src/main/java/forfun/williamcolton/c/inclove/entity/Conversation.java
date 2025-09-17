package forfun.williamcolton.c.inclove.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
public class Conversation {

    String id;
    @NotNull
    String firstUserId;
    @NotNull
    String SecondUserId;

}
