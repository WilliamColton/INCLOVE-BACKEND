package forfun.williamcolton.c.inclove.controller;

import forfun.williamcolton.c.inclove.dto.chat.req.SendMessageDto;
import forfun.williamcolton.c.inclove.dto.chat.req.UserStatusDto;
import forfun.williamcolton.c.inclove.dto.chat.resp.AckDto;
import forfun.williamcolton.c.inclove.service.ChatWsService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    public final ChatWsService chatWsService;

    @MessageMapping("/chat.send")
    public void send(SendMessageDto sendMessageDto, Principal p) {
        chatWsService.send(sendMessageDto, p.getName());
    }

    @MessageMapping("/chat.ack")
    public void ack(AckDto ackDto, Principal p) {
        chatWsService.ack(ackDto, p.getName());
    }

    @MessageMapping("/user.heartbeat")
    public void doHeartbeat(UserStatusDto userStatusDto, Principal p) {
        if (p != null) {
            chatWsService.doHeartbeat(userStatusDto, p.getName());
        }
    }

}
