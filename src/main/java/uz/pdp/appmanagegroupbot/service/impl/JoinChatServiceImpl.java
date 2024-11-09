package uz.pdp.appmanagegroupbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import uz.pdp.appmanagegroupbot.enums.LangFields;
import uz.pdp.appmanagegroupbot.enums.State;
import uz.pdp.appmanagegroupbot.model.User;
import uz.pdp.appmanagegroupbot.repository.UserRepository;
import uz.pdp.appmanagegroupbot.service.ButtonService;
import uz.pdp.appmanagegroupbot.service.JoinChatService;
import uz.pdp.appmanagegroupbot.service.LangService;
import uz.pdp.appmanagegroupbot.service.telegram.Sender;
import uz.pdp.appmanagegroupbot.utils.AppConstants;
import uz.pdp.appmanagegroupbot.utils.CommonUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JoinChatServiceImpl implements JoinChatService {
    private final Sender sender;
    private final LangService langService;
    private final CommonUtils commonUtils;
    private final UserRepository userRepository;
    private final ButtonService buttonService;


    @Override
    public void process(ChatJoinRequest chatJoinRequest) {
        Long groupId = chatJoinRequest.getChat().getId();
        if (!groupId.equals(AppConstants.GROUP_ID))
            return;

        Long userId = chatJoinRequest.getUser().getId();
        String name = chatJoinRequest.getChat().getTitle();
        User user = commonUtils.getUser(userId);
        if (user.getSubscriptionEndTime() != null) {
            if (user.getSubscriptionEndTime().isAfter(LocalDateTime.now())) {
                sender.acceptJoinRequest(userId, groupId);
                user.setSubscribed(true);
                userRepository.save(user);
                return;
            }
        }
        user.setState(State.START);
        sender.sendPhoto(userId, langService.getMessage(LangFields.NOT_PAID_TEXT, userId).formatted(name), AppConstants.REQUEST_AND_FIRST_PHOTO_PATH, buttonService.start(userId));
        if (user.isBlocked()) {
            user.setBlocked(false);
            userRepository.save(user);
        }
    }
}
