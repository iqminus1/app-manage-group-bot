package uz.pdp.appmanagegroupbot.service;

import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;

public interface MyChatMemberService {
    void process(ChatMemberUpdated myChatMember);
}
