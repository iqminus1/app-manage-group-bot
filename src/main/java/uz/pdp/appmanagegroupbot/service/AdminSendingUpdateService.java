package uz.pdp.appmanagegroupbot.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.appmanagegroupbot.enums.State;

public interface AdminSendingUpdateService {
    void process(Message message);
}
