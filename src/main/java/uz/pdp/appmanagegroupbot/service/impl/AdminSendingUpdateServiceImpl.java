package uz.pdp.appmanagegroupbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.appmanagegroupbot.enums.LangFields;
import uz.pdp.appmanagegroupbot.enums.State;
import uz.pdp.appmanagegroupbot.model.User;
import uz.pdp.appmanagegroupbot.repository.UserRepository;
import uz.pdp.appmanagegroupbot.service.AdminSendingUpdateService;
import uz.pdp.appmanagegroupbot.service.ButtonService;
import uz.pdp.appmanagegroupbot.service.LangService;
import uz.pdp.appmanagegroupbot.service.telegram.Sender;
import uz.pdp.appmanagegroupbot.utils.AppConstants;
import uz.pdp.appmanagegroupbot.utils.CommonUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSendingUpdateServiceImpl implements AdminSendingUpdateService {
    private final UserRepository userRepository;
    private final LangService langService;
    private final CommonUtils commonUtils;
    private final ButtonService buttonService;
    private final Sender sender;

    @Override
    @Async
    public void process(Message message) {
        Long userId = message.getFrom().getId();
        if (message.hasText()) {
            String text = message.getText();
            if (text.equals(langService.getMessage(LangFields.BACK_BUTTON, userId))) {
                commonUtils.setState(userId, State.ADMIN_MENU);
                sender.sendMessage(userId, langService.getMessage(LangFields.ADMIN_MENU_TEXT, userId), buttonService.adminMenu(userId));
                return;
            } else if (text.equals("/start")) {
                commonUtils.setState(userId, State.START);
                sender.sendPhoto(userId, langService.getMessage(LangFields.START_TEXT, userId).formatted(sender.getGroupName()), AppConstants.REQUEST_AND_FIRST_PHOTO_PATH, buttonService.start(userId));
                return;
            }
        }
        int pageNumber = 0;
        boolean hasNext;
        Sort sort = Sort.by(Sort.Order.asc("id"));
        commonUtils.setState(userId, State.ADMIN_MENU);
        sender.sendMessage(userId, langService.getMessage(LangFields.UPDATE_SENDING_TEXT, userId), buttonService.adminMenu(userId));
        do {
            Pageable pageable = PageRequest.of(pageNumber, 500, sort);
            Page<User> usersPage = userRepository.findAllByBlocked(pageable, false);
            List<User> users = usersPage.getContent();
            CopyMessage copyMessage = new CopyMessage();
            copyMessage.setMessageId(message.getMessageId());
            copyMessage.setFromChatId(userId);
            for (User user : users) {
                copyMessage.setChatId(user.getId());
                try {
                    sender.execute(copyMessage);
                } catch (TelegramApiException ignored) {
                }

            }
            pageNumber++;
            hasNext = usersPage.hasNext();
        } while (hasNext);
        sender.sendMessage(userId, langService.getMessage(LangFields.UPDATE_SENT_TEXT, userId));
    }
}
