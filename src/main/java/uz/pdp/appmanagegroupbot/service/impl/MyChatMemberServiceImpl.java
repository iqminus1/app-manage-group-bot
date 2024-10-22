package uz.pdp.appmanagegroupbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import uz.pdp.appmanagegroupbot.model.Group;
import uz.pdp.appmanagegroupbot.repository.GroupRepository;
import uz.pdp.appmanagegroupbot.service.MyChatMemberService;
import uz.pdp.appmanagegroupbot.service.telegram.Sender;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyChatMemberServiceImpl implements MyChatMemberService {
    private final GroupRepository groupRepository;
    private final Sender sender;

    @Override
    public void process(ChatMemberUpdated myChatMember) {
        Long groupId = myChatMember.getChat().getId();
        List<Group> groups = groupRepository.findAll();

        String newStatus = myChatMember.getNewChatMember().getStatus();

        boolean hasPermission = ChatMemberOwner.STATUS.equals(newStatus) || ChatMemberAdministrator.STATUS.equals(newStatus);
        if (groups.isEmpty()) {
            if (hasPermission) {
                groupRepository.save(Group.builder().groupId(groupId).build());
            }
        } else if (groups.size() == 1) {
            Group group = groups.get(0);
            if (hasPermission) {
                if (group.getGroupId() != null) {
                    sender.leaveChat(groupId);
                } else {
                    group.setGroupId(groupId);
                    groupRepository.save(group);
                }
            } else if (ChatMemberMember.STATUS.equals(newStatus) || ChatMemberLeft.STATUS.equals(newStatus)) {
                if (group.getGroupId() != null && group.getGroupId().equals(groupId)) {
                    group.setGroupId(null);
                    groupRepository.save(group);
                }
            }
        }
    }
}