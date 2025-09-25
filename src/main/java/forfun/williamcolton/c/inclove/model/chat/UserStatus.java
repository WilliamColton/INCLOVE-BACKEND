package forfun.williamcolton.c.inclove.model.chat;

import java.time.LocalDateTime;

public record UserStatus(Boolean isOnline, Boolean isTyping, LocalDateTime lastActiveTime) {
}
