package forfun.williamcolton.c.inclove.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {

    @PostMapping
    public void openConversation(@RequestParam String peerUserId, Authentication authentication) {
        authentication.getPrincipal();
    }

}
