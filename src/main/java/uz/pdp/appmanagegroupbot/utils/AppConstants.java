package uz.pdp.appmanagegroupbot.utils;

import uz.pdp.appmanagegroupbot.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface AppConstants {
    String BOT_USERNAME = "SelfDevelopment_uz_bot";
    String BOT_TOKEN = "7637751901:AAH-CWlyqsGZ_OXt5Cwq_dIkDjV7i3tI0RU";
    String REQUEST_AND_FIRST_PHOTO_PATH = "C:\\Users\\User\\Desktop\\projects\\app-manage-group-bot\\files/request-and-first-photo.jpg";
    String FILE_PATH = "C:\\Users\\User\\Desktop\\projects\\app-manage-group-bot\\files/payments/";
    String ADMIN_CARD_NUMBER = "9860 0201 1596 7091";
    String ADMIN_CARD_NAME = "Qodirov Abdulaziz";
    Long PRICE = 15000L;
    String SET_ADMIN_CODE = "78554";
    String ACCEPT_SCREENSHOT_DATA = "acceptPhoto:";
    String REJECT_SCREENSHOT_DATA = "rejectPhoto:";
    Long GROUP_ID = -1002314027417L;
    String LINK = "https://t.me/+hbR_WzqU6pdjZmRh";
    String BLOCKED = "kicked";
    String UN_BLOCKED = "member";
    List<Long> IDS = List.of(727977552L, 595732024L, 739640202L, 1234636600L, 1589555027L, 5182943798L);

    static User setSubscriptionTime(User user, Integer month) {
        if (user.getSubscriptionEndTime().isBefore(LocalDateTime.now())) {
            user.setSubscriptionEndTime(LocalDateTime.now().plusMonths(month));
        } else
            user.setSubscriptionEndTime(user.getSubscriptionEndTime().plusMonths(month));
        return user;
    }

}
