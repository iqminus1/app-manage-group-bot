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
        String monthly = langService.getMessage(LangFields.MONTHLY_TARIFF_TEXT, userId);
        String aboutBot = langService.getMessage(LangFields.ABOUT_BOT_BUTTON, userId);
        List<String> strings = new LinkedList<>();
        if (commonUtils.getUser(userId).getAdmin() > 0)
            strings.add(langService.getMessage(LangFields.ADMIN_MENU_TEXT, userId));
        strings.add(monthly);
        strings.add(aboutBot);
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
        if (adminLvl >= 4)
            list.add(langService.getMessage(LangFields.SEND_UPDATE_BUTTON, userId));
        if (adminLvl >= 3)
            list.add(langService.getMessage(LangFields.ADMINS_LIST_TEXT, userId));

        if (adminLvl >= 2)
            list.add(langService.getMessage(LangFields.SCREENSHOTS_LIST_TEXT, userId));

        if (adminLvl >= 1)
            list.add(langService.getMessage(LangFields.SUBSCRIBED_USERS_LIST_TEXT, userId));

        list.add(langService.getMessage(LangFields.BACK_BUTTON, userId));
        return withString(list);
    }

    @Override
    public InlineKeyboardMarkup screenshotKeyboard(Long userId, Long screenshotId) {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put(langService.getMessage(LangFields.ACCEPT_SCREENSHOT_TEXT, userId),
                AppConstants.ACCEPT_SCREENSHOT_DATA + screenshotId);
        map.put(langService.getMessage(LangFields.REJECT_SCREENSHOT_TEXT, userId),
                AppConstants.REJECT_SCREENSHOT_DATA + screenshotId);
        list.add(map);
        return callbackKeyboard(list);
    }

    @Override
    public ReplyKeyboard chooseUsers(Long userId) {
        String all = langService.getMessage(LangFields.SEND_UPDATE_TO_ALL_BUTTON, userId);
        String subscribed = langService.getMessage(LangFields.SEND_UPDATE_TO_SUBSCRIBED_BUTTON, userId);
        String nonSubscribed = langService.getMessage(LangFields.SEND_UPDATE_TO_NON_SUBSCRIBED_BUTTON, userId);
        String back = langService.getMessage(LangFields.BACK_BUTTON, userId);

        List<String> strings = new LinkedList<>();
        strings.add(all);
        strings.add(subscribed);
        strings.add(nonSubscribed);
        strings.add(back);
        return withString(strings);
    }
}
