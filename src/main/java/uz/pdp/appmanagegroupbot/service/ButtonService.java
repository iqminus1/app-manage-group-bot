package uz.pdp.appmanagegroupbot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;
import java.util.Map;

public interface ButtonService {

    default ReplyKeyboard withString(List<String> list) {
        return withString(list, 1);
    }

    ReplyKeyboard withString(List<String> list, int rowSize);

    InlineKeyboardMarkup callbackKeyboard(List<Map<String, String>> textData);

    ReplyKeyboard start(Long userId);

    ReplyKeyboard requestContact(Long userId);

    ReplyKeyboard adminMenu(Long userId);

    InlineKeyboardMarkup screenshotKeyboard(Long userId, Long screenshotId, Long senderId);

    InlineKeyboardMarkup getChatCallback(Long userId);

    ReplyKeyboard aboutCourses(Long userId);

    ReplyKeyboard getFilials(Long userId);

    ReplyKeyboard getChannel(Long userId);

    ReplyKeyboard back(Long userId);
}
