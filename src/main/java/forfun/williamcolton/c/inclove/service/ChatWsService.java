package forfun.williamcolton.c.inclove.service;

import forfun.williamcolton.c.inclove.dto.chat.req.SendMessageDto;
import forfun.williamcolton.c.inclove.dto.chat.req.UserStatusDto;
import forfun.williamcolton.c.inclove.dto.chat.resp.AckDto;

public interface ChatWsService {

    void send(SendMessageDto sendMessageDto, String userId);

    void doHeartbeat(UserStatusDto userStatusDto, String userId);

    void ack(AckDto ackDto, String userId);

}
