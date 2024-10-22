package uz.pdp.appmanagegroupbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import uz.pdp.appmanagegroupbot.enums.Lang;
import uz.pdp.appmanagegroupbot.enums.LangFields;
import uz.pdp.appmanagegroupbot.service.LangService;
import uz.pdp.appmanagegroupbot.utils.CommonUtils;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LangServiceImpl implements LangService {
    private final MessageSource messageSource;
    private final CommonUtils commonUtils;

    @Override
    public String getMessage(LangFields keyword, Long userId) {
        String lang = commonUtils.getLang(userId);
        try {
            return messageSource.getMessage(keyword.name(), null, new Locale(lang));
        } catch (Exception e) {
            return messageSource.getMessage(keyword.name(), null, new Locale(Lang.UZ.name()));
        }
    }
}
