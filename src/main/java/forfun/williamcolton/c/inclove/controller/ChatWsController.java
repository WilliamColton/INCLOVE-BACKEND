package forfun.williamcolton.c.inclove.controller;

import forfun.williamcolton.c.inclove.dto.chat.req.SendMessageDto;
import forfun.williamcolton.c.inclove.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final SimpMessagingTemplate template;
    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void send(SendMessageDto sendMessageDto, Principal p) {
        try {
            template.convertAndSendToUser(p.getName(), "/queue/conversations", sendMessageDto);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        messageService.saveMessage();
    }
}
