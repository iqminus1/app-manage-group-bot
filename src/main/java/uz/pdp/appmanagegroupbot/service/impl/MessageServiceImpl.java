package uz.pdp.appmanagegroupbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import uz.pdp.appmanagegroupbot.enums.LangFields;
import uz.pdp.appmanagegroupbot.enums.State;
import uz.pdp.appmanagegroupbot.enums.Status;
import uz.pdp.appmanagegroupbot.model.Photo;
import uz.pdp.appmanagegroupbot.model.User;
import uz.pdp.appmanagegroupbot.repository.PhotoRepository;
import uz.pdp.appmanagegroupbot.repository.UserRepository;
import uz.pdp.appmanagegroupbot.service.AdminSendingUpdateService;
import uz.pdp.appmanagegroupbot.service.ButtonService;
import uz.pdp.appmanagegroupbot.service.LangService;
import uz.pdp.appmanagegroupbot.service.MessageService;
import uz.pdp.appmanagegroupbot.service.telegram.Sender;
import uz.pdp.appmanagegroupbot.utils.AppConstants;
import uz.pdp.appmanagegroupbot.utils.CommonUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uz.pdp.appmanagegroupbot.utils.AppConstants.getChatToString;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final CommonUtils commonUtils;
    private final LangService langService;
    private final ButtonService buttonService;
    private final Sender sender;
    private final UserRepository userRepository;
    private final DateTimeFormatter formatter;
    private final PhotoRepository photoRepository;
    private final AdminSendingUpdateService adminSendingUpdateService;

    @Override
    public void process(Message message) {
        if (message.getChat().getType().equals("private")) {
            Long userId = message.getFrom().getId();
            User user = commonUtils.getUser(userId);
            if (user.getAdmin() >= 4) {
                State state = user.getState();
                if (AppConstants.STATE_SEND_UPDATE_LIST.contains(state)) {
                    if (adminSendingUpdateService.process(message, state))
                        sender.sendMessage(userId, langService.getMessage(LangFields.UPDATE_SENT_TEXT, userId));
                    return;
                }
            }
            if (message.hasText()) {
                String text = message.getText();
                if (text.equals("/start")) {
                    start(userId);
                    return;
                }
                switch (user.getState()) {
                    case START -> {
                        if (text.equals(langService.getMessage(LangFields.ABOUT_BOT_BUTTON, userId))) {
                            sender.sendMessage(userId, langService.getMessage(LangFields.ABOUT_BOT_TEXT, userId).formatted(AppConstants.AUTHOR), true);
                        } else if (text.equals(langService.getMessage(LangFields.MONTHLY_TARIFF_TEXT, userId))) {
                            if (user.getContactNumber() == null) {
                                user.setState(State.SENDING_CONTACT_NUMBER);
                                sender.sendMessage(userId, langService.getMessage(LangFields.SEND_YOUR_PHONE_NUMBER_TEXT, userId), buttonService.requestContact(userId));
                                return;
                            }
                            sendingPhoto(userId);
                        } else if (text.equals(langService.getMessage(LangFields.ADMIN_MENU_TEXT, userId))) {
                            adminMenu(userId);
                        }
                    }
                    case SENDING_CONTACT_NUMBER, SENDING_PHOTO -> {
                        if (text.equals(langService.getMessage(LangFields.BACK_BUTTON, userId)))
                            start(userId);
                        else
                            sender.sendMessage(userId, langService.getMessage(LangFields.USE_BUTTONS_TEXT, userId));
                    }
                    case ADMIN_MENU -> {
                        if (text.equals(langService.getMessage(LangFields.BACK_BUTTON, userId)))
                            start(userId);
                        else if (text.equals(langService.getMessage(LangFields.ADMINS_LIST_TEXT, userId))) {
                            showAdmins(userId);
                        } else if (text.equals(langService.getMessage(LangFields.SCREENSHOTS_LIST_TEXT, userId))) {
                            screenshotsList(userId);
                        } else if (text.equals(langService.getMessage(LangFields.SUBSCRIBED_USERS_LIST_TEXT, userId))) {
                            usersList(userId);
                        } else if (text.equals(langService.getMessage(LangFields.SEND_UPDATE_BUTTON, userId))) {
                            sendUpdateTypes(userId);
                        }
                    }
                    case CHOOSE_USERS -> {
                        if (text.equals(langService.getMessage(LangFields.BACK_BUTTON, userId))) {
                            commonUtils.setState(userId, State.ADMIN_MENU);
                            adminMenu(userId);
                        } else if (text.equals(langService.getMessage(LangFields.SEND_UPDATE_TO_ALL_BUTTON, userId))) {
                            commonUtils.setState(userId, State.SENDING_UPDATE_TO_ALL);
                            sender.sendMessage(userId, langService.getMessage(LangFields.SEND_UPDATE_TEXT, userId), buttonService.withString(List.of(langService.getMessage(LangFields.BACK_BUTTON, userId))));
                        } else if (text.equals(langService.getMessage(LangFields.SEND_UPDATE_TO_SUBSCRIBED_BUTTON, userId))) {
                            commonUtils.setState(userId, State.SENDING_UPDATE_TO_SUBSCRIBED);
                            sender.sendMessage(userId, langService.getMessage(LangFields.SEND_UPDATE_TEXT, userId), buttonService.withString(List.of(langService.getMessage(LangFields.BACK_BUTTON, userId))));
                        } else if (text.equals(langService.getMessage(LangFields.SEND_UPDATE_TO_NON_SUBSCRIBED_BUTTON, userId))) {
                            commonUtils.setState(userId, State.SENDING_UPDATE_TO_NON_SUBSCRIBED);
                            sender.sendMessage(userId, langService.getMessage(LangFields.SEND_UPDATE_TEXT, userId), buttonService.withString(List.of(langService.getMessage(LangFields.BACK_BUTTON, userId))));
                        }
                    }
                }
            } else if (message.hasContact()) {
                if (commonUtils.getState(message.getFrom().getId()).equals(State.SENDING_CONTACT_NUMBER))
                    checkContact(message);
            } else if (message.hasDocument()) {
                if (commonUtils.getState(message.getFrom().getId()).equals(State.SENDING_PHOTO)) {
                    saveDocument(message);
                }
            } else if (message.hasPhoto()) {
                if (commonUtils.getState(message.getFrom().getId()).equals(State.SENDING_PHOTO)) {
                    savePhoto(message);
                }
            }
        }
    }

    private void sendUpdateTypes(Long userId) {
        User user = commonUtils.getUser(userId);
        if (user.getAdmin() < 4) {
            return;
        }
        user.setState(State.CHOOSE_USERS);
        String message = langService.getMessage(LangFields.CHOOSE_UPDATE_TEXT, userId);
        ReplyKeyboard replyKeyboard = buttonService.chooseUsers(userId);
        sender.sendMessage(userId, message, replyKeyboard);
    }

    private void usersList(Long userId) {
        List<User> users = userRepository.findAllBySubscribed(true);
        if (users.isEmpty()) {
            sender.sendMessage(userId, langService.getMessage(LangFields.EMPTY_USERS_LIST_TEXT, userId));
            return;
        }

        String header = langService.getMessage(LangFields.HEADER_USERS_LIST_TEXT, userId);
        StringBuilder sb = new StringBuilder(header);
        for (User user : users) {
            Chat chat = sender.getChat(user.getId());
            sb.append(getChatToString(chat)).append("\n");
            if (user.getContactNumber() != null)
                sb.append(user.getContactNumber()).append(" ");

            sb.append(formatter.format(user.getSubscriptionEndTime())).append("\n\n");

        }
        sender.sendMessage(userId, sb.toString());
    }

    private void screenshotsList(Long userId) {
        User user = commonUtils.getUser(userId);
        if (user.getAdmin() < 3) {
            sender.sendMessage(userId, langService.getMessage(LangFields.ADMIN_ACCESS_DENIED, userId).formatted(3, user.getAdmin()));
            return;
        }
        List<Photo> photos = photoRepository.findAllByStatus(Status.DONT_SEE);
        if (photos.isEmpty()) {
            sender.sendMessage(userId, langService.getMessage(LangFields.EMPTY_SCREENSHOTS_LIST_TEXT, userId));
            return;
        }
        for (Photo photo : photos) {
            Long screenshotId = photo.getId();
            InlineKeyboardMarkup keyboard = buttonService.screenshotKeyboard(userId, screenshotId);
            String message = langService.getMessage(LangFields.UN_CHECKED_SCREENSHOT_TEXT, userId);
            String sentAt = formatter.format(photo.getSentAt());
            User senderUser = commonUtils.getUser(photo.getSendUserId());
            if (senderUser.getContactNumber() != null)
                message = message + "\n" + senderUser.getContactNumber();
            message = message
                    + "\n" + getChatToString(sender.getChat(photo.getSendUserId()))
                    + "\n" + langService.getMessage(LangFields.DOWNLOAD_AT, userId) + " " + sentAt + "\n"
                    + langService.getMessage(LangFields.AMOUNT_TEXT, userId) + " " + photo.getPrice();

            sender.sendDocument(userId, message, photo.getPath(), keyboard);
        }
    }

    private void showAdmins(Long userId) {
        User user = commonUtils.getUser(userId);
        if (user.getAdmin() < 3) {
            sender.sendMessage(userId, langService.getMessage(LangFields.ADMIN_ACCESS_DENIED, userId).formatted(3, user.getAdmin()), buttonService.adminMenu(userId));
            return;
        }

        Map<Integer, List<User>> admins = userRepository.findAllByAdminAfter(0).stream().collect(Collectors.groupingBy(User::getAdmin));
        StringBuilder sb = new StringBuilder(langService.getMessage(LangFields.ADMINS_LIST_TEXT, userId) + "\n");
        for (Integer adminLvl : admins.keySet().stream().sorted(Comparator.comparing(Integer::intValue).reversed()).toList()) {
            sb
                    .append("-------------------\n")
                    .append(langService.getMessage(LangFields.ADMIN_LEVEL_TEXT, userId))
                    .append(" - ")
                    .append(adminLvl)
                    .append("\n");
            for (User adminUser : admins.get(adminLvl)) {
                sb
                        .append(getChatToString(sender.getChat(adminUser.getId())));
                if (adminUser.getContactNumber() != null)
                    sb
                            .append(" ")
                            .append(adminUser.getContactNumber());
                sb.append("\n\n");
            }
        }
        sender.sendMessage(userId, sb.toString());
    }

    private void adminMenu(Long userId) {
        User user = commonUtils.getUser(userId);
        if (user.getAdmin() == 0) {
            return;
        }
        user.setState(State.ADMIN_MENU);
        sender.sendMessage(userId, langService.getMessage(LangFields.WELCOME_TO_ADMIN_MENU_TEXT, userId), buttonService.adminMenu(userId));
    }

    private void savePhoto(Message message) {
        Long userId = message.getFrom().getId();

        List<PhotoSize> photo = message.getPhoto();
        if (photo.isEmpty()) {
            return;
        }

        PhotoSize photoSize = photo.stream().max(Comparator.comparing(PhotoSize::getFileSize)).get();
        savePhotoAndSendToStart(photoSize.getFileId(), userId);

    }

    private void savePhotoAndSendToStart(String photoSize, Long userId) {
        String filePath = sender.getFilePath(photoSize);
        photoRepository.save(new Photo(null, userId, filePath, Status.DONT_SEE, LocalDateTime.now(), null, AppConstants.PRICE));

        commonUtils.setState(userId, State.START);
        sender.sendMessage(userId, langService.getMessage(LangFields.PHOTO_SAVED_TEXT, userId), buttonService.start(userId));
    }

    private void sendingPhoto(Long userId) {
        commonUtils.setState(userId, State.SENDING_PHOTO);
        sender.sendMessage(userId, langService.getMessage(LangFields.ADMINS_CARD_INFO, userId).formatted(AppConstants.ADMIN_CARD_NAME, AppConstants.ADMIN_CARD_NUMBER, AppConstants.PRICE), buttonService.withString(List.of(langService.getMessage(LangFields.BACK_BUTTON, userId))), true);
    }

    private void saveDocument(Message message) {
        Long userId = message.getFrom().getId();
        Document document = message.getDocument();
        savePhotoAndSendToStart(document.getFileId(), userId);
    }


    private void checkContact(Message message) {
        Long userId = message.getFrom().getId();
        if (message.getContact().getUserId().equals(message.getChat().getId())) {
            String phoneNumber = message.getContact().getPhoneNumber();
            User user = commonUtils.getUser(userId);
            user.setContactNumber(phoneNumber);
            user.setState(State.START);
            userRepository.save(user);
            sendingPhoto(userId);
            return;
        }
        sender.sendMessage(userId, langService.getMessage(LangFields.SEND_YOUR_PHONE_NUMBER_TEXT, userId), buttonService.requestContact(userId));
    }

    private void start(Long userId) {
        commonUtils.setState(userId, State.START);
        sender.sendPhoto(userId, langService.getMessage(LangFields.START_TEXT, userId).formatted(AppConstants.AUTHOR, sender.getGroupName()), AppConstants.REQUEST_AND_FIRST_PHOTO_PATH, buttonService.start(userId));
    }

}
