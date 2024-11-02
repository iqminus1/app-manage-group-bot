package uz.pdp.appmanagegroupbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.pdp.appmanagegroupbot.enums.LangFields;
import uz.pdp.appmanagegroupbot.enums.State;
import uz.pdp.appmanagegroupbot.enums.Status;
import uz.pdp.appmanagegroupbot.model.Photo;
import uz.pdp.appmanagegroupbot.model.User;
import uz.pdp.appmanagegroupbot.repository.PhotoRepository;
import uz.pdp.appmanagegroupbot.repository.UserRepository;
import uz.pdp.appmanagegroupbot.service.CallbackService;
import uz.pdp.appmanagegroupbot.service.LangService;
import uz.pdp.appmanagegroupbot.service.telegram.Sender;
import uz.pdp.appmanagegroupbot.utils.AppConstants;
import uz.pdp.appmanagegroupbot.utils.CommonUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static uz.pdp.appmanagegroupbot.utils.AppConstants.getChatToString;
import static uz.pdp.appmanagegroupbot.utils.AppConstants.setSubscriptionTime;

@Service
@RequiredArgsConstructor
public class CallbackServiceImpl implements CallbackService {
    private final CommonUtils commonUtils;
    private final Sender sender;
    private final UserRepository userRepository;
    private final LangService langService;
    private final PhotoRepository photoRepository;
    private final DateTimeFormatter dateTimeFormatter;

    @Override
    public void process(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (commonUtils.getState(callbackQuery.getFrom().getId()).equals(State.ADMIN_MENU)) {
            if (data.startsWith(AppConstants.ACCEPT_SCREENSHOT_DATA)) {
                acceptScreenshot(callbackQuery);
            } else if (data.startsWith(AppConstants.REJECT_SCREENSHOT_DATA))
                rejectScreenshot(callbackQuery);
        }
    }

    private void rejectScreenshot(CallbackQuery callbackQuery) {
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long userId = callbackQuery.getFrom().getId();
        long photoId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Photo screenshot = updatePhoto(photoId, Status.REJECT);
        String message = langService.getMessage(LangFields.REJECTED_SCREENSHOT_TEXT, userId);
        String format = dateTimeFormatter.format(screenshot.getChangedStatus());
        message = message + "\n" + getChatToString(sender.getChat(userId)) + "\n" + langService.getMessage(LangFields.REJECTED_AT, userId) + " " + format;
        sender.changeCaption(userId, messageId, message);

        sender.sendMessage(screenshot.getSendUserId(), langService.getMessage(LangFields.SCREENSHOT_IS_INVALID_TEXT, screenshot.getSendUserId()));
    }

    private void acceptScreenshot(CallbackQuery callbackQuery) {
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long userId = callbackQuery.getFrom().getId();
        long photoId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Photo screenshot = updatePhoto(photoId, Status.ACCEPT);
        String message = langService.getMessage(LangFields.ACCEPTED_SCREENSHOT_TEXT, userId);
        String format = dateTimeFormatter.format(screenshot.getChangedStatus());
        message = message + "\n" + getChatToString(sender.getChat(userId)) + "\n" + langService.getMessage(LangFields.ACCEPTED_AT, userId) + " " + format;
        sender.changeCaption(userId, messageId, message);


        User user = commonUtils.getUser(screenshot.getSendUserId());
        setSubscriptionTime(user, 1);
        userRepository.save(user);

        sender.sendMessage(user.getId(), langService.getMessage(LangFields.SCREENSHOT_IS_VALID_TEXT, screenshot.getSendUserId()) + " -> " + sender.getLink(AppConstants.GROUP_ID));
    }


    private Photo updatePhoto(long photoId, Status status) {
        Photo photo = photoRepository.findById(photoId).orElseThrow();
        photo.setStatus(status);
        photo.setChangedStatus(LocalDateTime.now());
        return photoRepository.save(photo);
    }
}
