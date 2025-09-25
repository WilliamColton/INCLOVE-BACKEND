package forfun.williamcolton.c.inclove.controller;

import forfun.williamcolton.c.inclove.dto.conversation.ConversationRespDto;
import forfun.williamcolton.c.inclove.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    // idea:可以在启动会话的时候让两个用户id根据字典序决定谁是first，谁是second
    @PostMapping
    public void openConversation(@RequestParam String peerUserId, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        conversationService.openConversation(userId, peerUserId);
    }

    @GetMapping("/userIdAndPeerUserId")
    public ConversationRespDto findConversationByUserIdAndPeerUserId(@RequestParam String peerUserId, Principal p) {
        return conversationService.finaConversationByUserIdAndPeerUserId(peerUserId, p.getName());
    }

    @GetMapping("/id")
    public ConversationRespDto findConversationById(@RequestParam String conversationId) {
        return conversationService.findConversationById(conversationId);
    }

}
