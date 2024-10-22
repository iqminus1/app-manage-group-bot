package uz.pdp.appmanagegroupbot.service;

import uz.pdp.appmanagegroupbot.enums.LangFields;

public interface LangService {
    String getMessage(LangFields keyword, Long userId);
}
