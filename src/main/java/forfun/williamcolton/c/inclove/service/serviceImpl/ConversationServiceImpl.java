package forfun.williamcolton.c.inclove.service.serviceImpl;

import forfun.williamcolton.c.inclove.dto.conversation.ConversationRespDto;
import forfun.williamcolton.c.inclove.entity.Conversation;
import forfun.williamcolton.c.inclove.mapper.ConversationMapper;
import forfun.williamcolton.c.inclove.service.ConversationService;
import org.springframework.stereotype.Service;

@Service
public class ConversationServiceImpl extends BaseServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    public ConversationRespDto findConversationById(String conversationId) {
        Conversation conversation = getById(conversationId);
        return new ConversationRespDto(conversationId, conversation.getFirstUserId(), conversation.getSecondUserId());
    }

    @Override
    public void openConversation(String userId, String peerUserId) {
        Conversation conversation = null;
        int difference = userId.compareTo(peerUserId);
        if (difference < 0) {
            conversation = new Conversation(userId, peerUserId);
        } else if (difference > 0) {
            conversation = new Conversation(peerUserId, userId);
        } else {
            throw new RuntimeException("八嘎，两个用户id一样");
        }

        save(conversation);
    }

}
