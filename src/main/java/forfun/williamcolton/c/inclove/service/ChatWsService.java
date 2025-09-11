package forfun.williamcolton.c.inclove.service;

import forfun.williamcolton.c.inclove.dto.chat.req.SendMessageDto;
import forfun.williamcolton.c.inclove.dto.chat.resp.AckDto;

public interface ChatWsService {

    void send(SendMessageDto sendMessageDto);

    void doHeartbeat(String userId);

    void ack(AckDto ackDto, String userId);

}
