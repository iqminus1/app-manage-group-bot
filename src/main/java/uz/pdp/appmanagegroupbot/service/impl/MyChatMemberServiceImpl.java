package uz.pdp.appmanagegroupbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import uz.pdp.appmanagegroupbot.model.User;
import uz.pdp.appmanagegroupbot.repository.UserRepository;
import uz.pdp.appmanagegroupbot.service.MyChatMemberService;
import uz.pdp.appmanagegroupbot.utils.AppConstants;
import uz.pdp.appmanagegroupbot.utils.CommonUtils;

@Service
@RequiredArgsConstructor
public class MyChatMemberServiceImpl implements MyChatMemberService {
    private final CommonUtils commonUtils;
    private final UserRepository userRepository;

    @Override
    public void process(ChatMemberUpdated myChatMember) {
        Chat chat = myChatMember.getChat();
        if (chat.getType().equals("private")) {
            if (myChatMember.getNewChatMember().getStatus().equals(AppConstants.BLOCKED)) {
                Long userId = chat.getId();
                User user = commonUtils.getUser(userId);
                user.setBlocked(true);
                userRepository.save(user);
                commonUtils.removeUser(userId);
            }else if (myChatMember.getNewChatMember().getStatus().equals(AppConstants.UN_BLOCKED)){
                Long userId = chat.getId();
                User user = commonUtils.getUser(userId);
                user.setBlocked(false);
                userRepository.save(user);

            }
        }
    }
}
