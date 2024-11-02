package uz.pdp.appmanagegroupbot.utils;

import org.telegram.telegrambots.meta.api.objects.Chat;
import uz.pdp.appmanagegroupbot.enums.State;
import uz.pdp.appmanagegroupbot.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface AppConstants {
    String BOT_USERNAME = "SelfDevelopment_uz_bot";
    String BOT_TOKEN = "7637751901:AAH-CWlyqsGZ_OXt5Cwq_dIkDjV7i3tI0RU";
    String REQUEST_AND_FIRST_PHOTO_PATH = "C:\\Users\\User\\Desktop\\projects\\app-manage-group-bot\\files/request-and-first-photo.jpg";
    String FILE_PATH = "C:\\Users\\User\\Desktop\\projects\\app-manage-group-bot\\files/";
    String AUTHOR = "@gplw_user";
    String ADMIN_CARD_NUMBER = "9860 0201 1596 7091";
    String ADMIN_CARD_NAME = "Qodirov Abdulaziz";
    Long PRICE = 15000L;
    String SET_ADMIN_CODE = "78554";
    String ACCEPT_SCREENSHOT_DATA = "acceptPhoto:";
    String REJECT_SCREENSHOT_DATA = "rejectPhoto:";
    Long GROUP_ID = -1002314027417L;
    List<State> STATE_SEND_UPDATE_LIST = List.of(State.SENDING_UPDATE_TO_ALL, State.SENDING_UPDATE_TO_SUBSCRIBED, State.SENDING_UPDATE_TO_NON_SUBSCRIBED);

    static User setSubscriptionTime(User user, Integer month) {
        if (user.getSubscriptionEndTime().isBefore(LocalDateTime.now())) {
            user.setSubscriptionEndTime(LocalDateTime.now().plusMonths(month));
        } else
            user.setSubscriptionEndTime(user.getSubscriptionEndTime().plusMonths(month));
        return user;
    }

    static String getChatToString(Chat chat) {
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(chat.getId());
        if (chat.getUserName() != null) {
            sb.append(" @").append(chat.getUserName());
        }
        if (chat.getFirstName() != null) {
            sb.append(" ").append(chat.getFirstName());
        }
        return sb.toString();
    }

}
