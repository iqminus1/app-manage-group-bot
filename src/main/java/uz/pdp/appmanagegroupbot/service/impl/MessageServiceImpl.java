package uz.pdp.appmanagegroupbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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

import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

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
    private final DecimalFormat decimalFormat;

    @Override
    public void process(Message message) {
        if (message.getChat().getType().equals("private")) {
            Long userId = message.getFrom().getId();
            if (!AppConstants.IDS.contains(userId))
                return;
            User user = commonUtils.getUser(userId);
            if (user.getAdmin() >= 4) {
                if (user.getState().equals(State.SENDING_UPDATE)) {
                    adminSendingUpdateService.process(message);
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
                        if (text.equals(langService.getMessage(LangFields.ABOUT_COURSES_BUTTON, userId))) {
                            aboutCourses(user, userId);
                        } else if (text.equals(langService.getMessage(LangFields.ADMIN_MENU_TEXT, userId))) {
                            adminMenu(userId);
                        } else if (text.equals(langService.getMessage(LangFields.ABOUT_TEACHERS_BUTTON, userId))) {
                            aboutTeachers(userId);
                        } else if (text.equals(langService.getMessage(LangFields.ABOUT_FILIAL_BUTTON, userId))) {
                            aboutFilials(user, userId);
                        } else if (text.equals(langService.getMessage(LangFields.ABOUT_CHANNEL_BUTTON, userId))) {
                            aboutChannel(user, userId);
                        }
                    }
                    case SENDING_CONTACT_NUMBER -> {
                        if (text.equals(langService.getMessage(LangFields.BACK_BUTTON, userId)))
                            aboutChannel(user, userId);
                        else
                            sender.sendMessage(userId, langService.getMessage(LangFields.USE_BUTTONS_TEXT, userId));
                    }
                    case SENDING_PHOTO -> {
                        if (text.equals(langService.getMessage(LangFields.BACK_BUTTON, userId)))
                            aboutChannel(user, userId);
                        else
                            sender.sendMessage(userId, langService.getMessage(LangFields.SEND_PHOTO_OR_DOCUMENT_TEXT, userId));

                    }
                    case ADMIN_MENU -> {
                        if (text.equals(langService.getMessage(LangFields.BACK_BUTTON, userId)))
                            start(userId);
                        else if (text.equals(langService.getMessage(LangFields.SCREENSHOTS_LIST_TEXT, userId))) {
                            screenshotsList(userId);
                        } else if (text.equals(langService.getMessage(LangFields.SUBSCRIBED_USERS_LIST_TEXT, userId))) {
                            usersList(userId);
                        } else if (text.equals(langService.getMessage(LangFields.SEND_UPDATE_BUTTON, userId))) {
                            sendUpdateTypes(userId);
                        } else if (text.equals(langService.getMessage(LangFields.REPORT_BUTTON, userId))) {
                            sendReport(userId);
                        }
                    }
                    case SELECTING_COURSES, SELECTING_FILIAL -> {
                        if (text.equals(langService.getMessage(LangFields.BACK_BUTTON, userId))) {
                            start(userId);
                        }
                    }
                    case ABOUT_CHANNEL -> {
                        if (text.equals(langService.getMessage(LangFields.BACK_BUTTON, userId))) {
                            start(userId);
                        } else if (text.equals(langService.getMessage(LangFields.SUBSCRIBE_BUTTON, userId))) {
                            if (user.getContactNumber() == null) {
                                user.setState(State.SENDING_CONTACT_NUMBER);
                                sender.sendMessage(userId, langService.getMessage(LangFields.SEND_YOUR_PHONE_NUMBER_TEXT, userId), buttonService.requestContact(userId));
                            } else
                                sendingPhoto(userId);
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

    private void aboutChannel(User user, Long userId) {
        user.setState(State.ABOUT_CHANNEL);
        String message = langService.getMessage(LangFields.ABOUT_CHANNEL_TEXT, userId).formatted(sender.getGroupName(), AppConstants.PRICE);
        ReplyKeyboard channel = buttonService.getChannel(userId);
        sender.sendMessage(userId, message, channel, true);
    }

    private void aboutFilials(User user, Long userId) {
        user.setState(State.SELECTING_FILIAL);
        ReplyKeyboard filials = buttonService.getFilials(userId);
        String message = langService.getMessage(LangFields.ABOUT_FILIAL_TEXT, userId);
        sender.sendMessage(userId, message, filials, true);
    }

    private void aboutTeachers(Long userId) {
        String teacher1 = langService.getMessage(LangFields.TEACHER_1_TEXT, userId);
        String teacher2 = langService.getMessage(LangFields.TEACHER_2_TEXT, userId);
        String teacher3 = langService.getMessage(LangFields.TEACHER_3_TEXT, userId);
        InlineKeyboardMarkup chatCallback = buttonService.getChatCallback(727977552L);
        SendPhoto sendPhoto = new SendPhoto(userId.toString(), new InputFile(new File(AppConstants.REQUEST_AND_FIRST_PHOTO_PATH)));
        sendPhoto.setCaption(teacher1);
        sendPhoto.setReplyMarkup(chatCallback);
        sendPhoto.setHasSpoiler(true);
        try {
            sender.execute(sendPhoto);
            sendPhoto.setCaption(teacher2);
            sender.execute(sendPhoto);
            sendPhoto.setCaption(teacher3);
            sender.execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void aboutCourses(User user, Long userId) {
        user.setState(State.SELECTING_COURSES);
        String message = langService.getMessage(LangFields.ABOUT_COURSES_TEXT, userId);
        ReplyKeyboard replyKeyboard = buttonService.aboutCourses(userId);
        sender.sendMessage(userId, message, replyKeyboard, true);
    }

    private void sendReport(Long userId) {
        long count = userRepository.count();
        long acceptedCount = photoRepository.countByStatus(Status.ACCEPT);
        long blocked = userRepository.countByBlocked(true);
        long uniqueUsers = photoRepository.countUniqueUsers(Status.ACCEPT);
        String formatted = langService.getMessage(LangFields.REPORT_TEXT, userId).formatted(count, blocked, acceptedCount, uniqueUsers);
        SendMessage sendMessage = new SendMessage(userId.toString(), formatted);
        sendMessage.setParseMode(ParseMode.HTML);
        sender.sendMessage(sendMessage);
    }

    private void sendUpdateTypes(Long userId) {
        User user = commonUtils.getUser(userId);
        if (user.getAdmin() < 2) {
            return;
        }
        user.setState(State.SENDING_UPDATE);
        String message = langService.getMessage(LangFields.SEND_UPDATE_TEXT, userId);
        ReplyKeyboard back = buttonService.back(userId);
        sender.sendMessage(userId, message, back);
    }

    private void usersList(Long userId) {
        int pageNumber = 0;
        boolean hasNext;
        Sort sort = Sort.by(Sort.Order.asc("subscriptionEndTime"));
        do {
            Pageable pageable = PageRequest.of(pageNumber, 500, sort);
            Page<User> usersPage = userRepository.findAllBySubscribed(true, pageable);
            List<User> users = usersPage.getContent();
            if (users.isEmpty()) {
                sender.sendMessage(userId, langService.getMessage(LangFields.EMPTY_USERS_LIST_TEXT, userId));
                return;
            }

            String header = langService.getMessage(LangFields.HEADER_USERS_LIST_TEXT, userId);
            sender.sendMessage(userId, header);
            for (User user : users) {
                StringBuilder sb = new StringBuilder();
                InlineKeyboardMarkup keyboard = buttonService.getChatCallback(user.getId());
                Chat chat = sender.getChat(user.getId());
                sb.append(langService.getMessage(LangFields.USER_ID_TEXT, userId));
                int userIdIndex = sb.indexOf("%s");
                sb.replace(userIdIndex, userIdIndex + 2, userId.toString());
                if (chat.getFirstName() != null) {
                    sb.append("\n").append(langService.getMessage(LangFields.USER_FIRST_NAME_TEXT, userId));
                    int i = sb.indexOf("%s");
                    sb.replace(i, i + 2, chat.getFirstName());
                }
                if (chat.getLastName() != null) {
                    sb.append("\n").append(langService.getMessage(LangFields.USER_LAST_NAME_TEXT, userId));
                    int i = sb.indexOf("%s");
                    sb.replace(i, i + 2, chat.getLastName());
                }
                if (chat.getUserName() != null) {
                    sb.append("\n").append(langService.getMessage(LangFields.USER_USERNAME_TEXT, userId));
                    int i = sb.indexOf("%s");
                    sb.replace(i, i + 2, chat.getUserName());
                }
                sb.append("\n").append(langService.getMessage(LangFields.USER_SUBSCRIBED_EXPIRE_TEXT, userId));
                int endTimeIndex = sb.indexOf("%s");
                sb.replace(endTimeIndex, endTimeIndex + 2, user.getSubscriptionEndTime().format(formatter));

                SendMessage sendMessage = new SendMessage(userId.toString(), sb.toString());
                sendMessage.setParseMode(ParseMode.HTML);
                sendMessage.setReplyMarkup(keyboard);
                sender.sendMessage(sendMessage);
            }
            pageNumber++;
            hasNext = usersPage.hasNext();
        } while (hasNext);

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
            InlineKeyboardMarkup keyboard = buttonService.screenshotKeyboard(userId, screenshotId, photo.getSendUserId());
            String sentAt = formatter.format(photo.getSentAt());
            User senderUser = commonUtils.getUser(photo.getSendUserId());
            String message = langService.getMessage(LangFields.UN_CHECKED_SCREENSHOT_TEXT, userId).formatted(senderUser.getContactNumber(), sentAt, decimalFormat.format(photo.getPrice()));
            sender.sendDocument(userId, message, photo.getPath(), keyboard, ParseMode.HTML);
        }
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
        sender.sendMessage(userId, langService.getMessage(LangFields.ADMINS_CARD_INFO, userId).formatted(AppConstants.ADMIN_CARD_NUMBER, AppConstants.ADMIN_CARD_NAME, decimalFormat.format(AppConstants.PRICE)), buttonService.withString(List.of(langService.getMessage(LangFields.BACK_BUTTON, userId))), true);
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
            if (phoneNumber.charAt(0) != '+')
                phoneNumber = "+" + phoneNumber;
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
        User user = commonUtils.getUser(userId);
        user.setState(State.START);
        if (user.isBlocked()) {
            user.setBlocked(false);
            userRepository.save(user);
        }
        sender.sendPhoto(userId, langService.getMessage(LangFields.START_TEXT, userId).formatted(sender.getGroupName()), AppConstants.REQUEST_AND_FIRST_PHOTO_PATH, buttonService.start(userId));
    }

}
