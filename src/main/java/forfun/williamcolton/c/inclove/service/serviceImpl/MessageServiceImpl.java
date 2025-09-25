package forfun.williamcolton.c.inclove.service.serviceImpl;

import forfun.williamcolton.c.inclove.entity.Message;
import forfun.williamcolton.c.inclove.mapper.MessageMapper;
import forfun.williamcolton.c.inclove.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends BaseServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public void saveMessage(Message message) {
        save(message);
    }

}
