package forfun.williamcolton.c.inclove.service.serviceImpl;

import forfun.williamcolton.c.inclove.entity.Conversation;
import forfun.williamcolton.c.inclove.mapper.ConversationMapper;
import forfun.williamcolton.c.inclove.service.ConversationService;
import org.springframework.stereotype.Service;

@Service
public class ConversationServiceImpl extends BaseServiceImpl<ConversationMapper, Conversation> implements ConversationService {
}
