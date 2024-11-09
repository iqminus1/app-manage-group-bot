package uz.pdp.appmanagegroupbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.appmanagegroupbot.enums.LangFields;
import uz.pdp.appmanagegroupbot.service.ButtonService;
import uz.pdp.appmanagegroupbot.service.LangService;
import uz.pdp.appmanagegroupbot.utils.AppConstants;
import uz.pdp.appmanagegroupbot.utils.CommonUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ButtonServiceImpl implements ButtonService {
    private final LangService langService;
    private final CommonUtils commonUtils;

    @Override
    public ReplyKeyboard withString(List<String> list, int rowSize) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        int i = 1;
        for (String text : list) {
            row.add(new KeyboardButton(text));
            if (i == rowSize) {
                rows.add(row);
                row = new KeyboardRow();
                i = 0;
            }
            i++;
        }
        markup.setKeyboard(rows);
        return markup;
    }

    @Override
    public InlineKeyboardMarkup callbackKeyboard(List<Map<String, String>> textData) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (Map<String, String> map : textData) {

            for (String text : map.keySet()) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setCallbackData(map.get(text));
                button.setText(text);
                row.add(button);
            }

            rows.add(row);
            row = new ArrayList<>();

        }
        markup.setKeyboard(rows);
        return markup;
    }


    @Override
    public ReplyKeyboard start(Long userId) {
        String aboutChannel = langService.getMessage(LangFields.ABOUT_CHANNEL_BUTTON, userId);
        String aboutFilial = langService.getMessage(LangFields.ABOUT_FILIAL_BUTTON, userId);
        String aboutTeachers = langService.getMessage(LangFields.ABOUT_TEACHERS_BUTTON, userId);
        String aboutCourses = langService.getMessage(LangFields.ABOUT_COURSES_BUTTON, userId);
        List<String> strings = new LinkedList<>();
        if (commonUtils.getUser(userId).getAdmin() > 0)
            strings.add(langService.getMessage(LangFields.ADMIN_MENU_TEXT, userId));
        strings.add(aboutFilial);
        strings.add(aboutChannel);
        strings.add(aboutTeachers);
        strings.add(aboutCourses);
        return withString(strings);
    }

    @Override
    public ReplyKeyboard requestContact(Long userId) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
        //contact req
        KeyboardRow requestRow = new KeyboardRow();
        KeyboardButton request = new KeyboardButton(langService.getMessage(LangFields.BUTTON_REQUEST_CONTACT_TEXT, userId));
        request.setRequestContact(true);
        requestRow.add(request);

        KeyboardRow backRow = new KeyboardRow();
        backRow.add(new KeyboardButton(langService.getMessage(LangFields.BACK_BUTTON, userId)));

        rows.add(requestRow);
        rows.add(backRow);
        markup.setKeyboard(rows);
        return markup;
    }

    @Override
    public ReplyKeyboard adminMenu(Long userId) {
        List<String> list = new LinkedList<>();
        int adminLvl = commonUtils.getUser(userId).getAdmin();
        if (adminLvl >= 3)
            list.add(langService.getMessage(LangFields.SEND_UPDATE_BUTTON, userId));

        if (adminLvl >= 2)
            list.add(langService.getMessage(LangFields.SCREENSHOTS_LIST_TEXT, userId));

        if (adminLvl >= 1)
            list.add(langService.getMessage(LangFields.SUBSCRIBED_USERS_LIST_TEXT, userId));

        list.add(langService.getMessage(LangFields.REPORT_BUTTON, userId));
        list.add(langService.getMessage(LangFields.BACK_BUTTON, userId));
        return withString(list);
    }

    @Override
    public InlineKeyboardMarkup screenshotKeyboard(Long userId, Long screenshotId, Long senderId) {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put(langService.getMessage(LangFields.ACCEPT_SCREENSHOT_TEXT, userId),
                AppConstants.ACCEPT_SCREENSHOT_DATA + screenshotId);
        map.put(langService.getMessage(LangFields.REJECT_SCREENSHOT_TEXT, userId),
                AppConstants.REJECT_SCREENSHOT_DATA + screenshotId);
        list.add(map);
        InlineKeyboardMarkup markup = callbackKeyboard(list);
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(langService.getMessage(LangFields.CHAT_BUTTON, userId));
        inlineKeyboardButton.setUrl("tg://user?id=" + senderId);
        keyboard.add(List.of(inlineKeyboardButton));
        return markup;
    }

    @Override
    public InlineKeyboardMarkup getChatCallback(Long userId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(langService.getMessage(LangFields.USER_PROFILE_TEXT, userId));
        button.setUrl("tg://user?id=" + userId);
        rows.add(List.of(button));

        markup.setKeyboard(rows);
        return markup;

    }

    @Override
    public ReplyKeyboard aboutCourses(Long userId) {
        List<String> list = new LinkedList<>();
        list.add("General English");
        list.add("IELTS");
        list.add("CEFR");
        list.add(langService.getMessage(LangFields.BACK_BUTTON, userId));
        return withString(list);
    }

    @Override
    public ReplyKeyboard getFilials(Long userId) {
        List<String> list = new LinkedList<>();
        String message = langService.getMessage(LangFields.BACK_BUTTON, userId);
        list.add("Sergeli filial lokatsiyasini olish");
        list.add("Chilonzor filial lokatsiyasini olish");
        list.add(message);
        return withString(list);
    }

    @Override
    public ReplyKeyboard getChannel(Long userId) {
        String message = langService.getMessage(LangFields.SUBSCRIBE_BUTTON, userId);
        String message1 = langService.getMessage(LangFields.BACK_BUTTON, userId);
        List<String> list = new LinkedList<>();
        list.add(message);
        list.add(message1);
        return withString(list);
    }

    @Override
    public ReplyKeyboard back(Long userId) {
        return withString(List.of(langService.getMessage(LangFields.BACK_BUTTON, userId)));
    }
}
