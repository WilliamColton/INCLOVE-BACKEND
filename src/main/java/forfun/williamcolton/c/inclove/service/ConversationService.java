package forfun.williamcolton.c.inclove.service;

import forfun.williamcolton.c.inclove.dto.conversation.ConversationRespDto;
import forfun.williamcolton.c.inclove.entity.Conversation;

public interface ConversationService extends IBaseService<Conversation> {

    ConversationRespDto findConversationById(String conversationId);

    ConversationRespDto finaConversationByUserIdAndPeerUserId(String peerUserId, String userId);

    void openConversation(String userId, String peerUserId);

}
