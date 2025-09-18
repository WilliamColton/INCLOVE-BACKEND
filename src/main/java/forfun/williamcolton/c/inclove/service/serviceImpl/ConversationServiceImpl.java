package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import forfun.williamcolton.c.inclove.dto.conversation.ConversationRespDto;
import forfun.williamcolton.c.inclove.entity.Conversation;
import forfun.williamcolton.c.inclove.mapper.ConversationMapper;
import forfun.williamcolton.c.inclove.service.ConversationService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class ConversationServiceImpl extends BaseServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    private final ModelMapper modelMapper;

    public ConversationServiceImpl(ModelMapper modelMapper) {
        super();
        this.modelMapper = modelMapper;
    }

    public ConversationRespDto findConversationById(String conversationId) {
        Conversation conversation = getById(conversationId);
        return new ConversationRespDto(conversationId, conversation.getFirstUserId(), conversation.getSecondUserId());
    }

    @Override
    public ConversationRespDto finaConversationByUserIdAndPeerUserId(String peerUserId, String userId) {
        Conversation conversation = null;
        int difference = userId.compareTo(peerUserId);
        if (difference < 0) {
            conversation = getOne(new LambdaQueryWrapper<Conversation>().eq(Conversation::getFirstUserId, userId).eq(Conversation::getSecondUserId, peerUserId));
        } else if (difference > 0) {
            conversation = getOne(new LambdaQueryWrapper<Conversation>().eq(Conversation::getFirstUserId, peerUserId).eq(Conversation::getSecondUserId, userId));
        } else {
            throw new RuntimeException("八嘎，两个用户id一样");
        }
        return modelMapper.map(conversation, ConversationRespDto.class);
    }

    @Override
    public void openConversation(String userId, String peerUserId) {
        Conversation conversation = null;
        int difference = userId.compareTo(peerUserId);
        if (difference < 0) {
            if (getOne(new LambdaQueryWrapper<Conversation>().eq(Conversation::getFirstUserId, userId).eq(Conversation::getSecondUserId, peerUserId)) == null) {
                conversation = new Conversation(userId, peerUserId);
            }
        } else if (difference > 0) {
            if (getOne(new LambdaQueryWrapper<Conversation>().eq(Conversation::getFirstUserId, peerUserId).eq(Conversation::getSecondUserId, userId)) == null) {
                conversation = new Conversation(peerUserId, userId);
            }
        } else {
            throw new RuntimeException("八嘎，两个用户id一样");
        }

        if (conversation != null) {
            save(conversation);
        }
    }

}
