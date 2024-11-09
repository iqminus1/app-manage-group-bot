package uz.pdp.appmanagegroupbot.scheduled;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.pdp.appmanagegroupbot.enums.LangFields;
import uz.pdp.appmanagegroupbot.model.User;
import uz.pdp.appmanagegroupbot.repository.UserRepository;
import uz.pdp.appmanagegroupbot.service.LangService;
import uz.pdp.appmanagegroupbot.service.telegram.Sender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledProcess {
    private final UserRepository userRepository;
    private final LangService langService;
    private final Sender sender;

    @Scheduled(cron = "0 0 12 * * ?")
    public void scheduledProcess() {
        List<User> users = userRepository.findAllBySubscribedAndSubscriptionEndTimeIsBetween(true, LocalDateTime.now().minusDays(3), LocalDateTime.now().plusDays(4));
        LocalDateTime today = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0));
        for (User user : users) {
            long between = ChronoUnit.DAYS.between(today, user.getSubscriptionEndTime());
            Long userId = user.getId();
            if (between > 0)
                sender.sendMessage(userId, langService.getMessage(LangFields.SCHEDULED_EXPIRE_ON_DAYS_TEXT, userId).formatted(between, langService.getMessage(LangFields.SUBSCRIBE_BUTTON, userId)), true);
            else {
                if (user.getSubscriptionEndTime().isAfter(LocalDateTime.now())) {
                    sender.sendMessage(userId, langService.getMessage(LangFields.SCHEDULED_EXPIRE_TODAY_TEXT, userId).formatted(langService.getMessage(LangFields.SUBSCRIBE_BUTTON, userId)), true);
                } else {
                    sender.kickUser(userId);
                    user.setSubscribed(false);
                    userRepository.save(user);
                    sender.sendMessage(userId, langService.getMessage(LangFields.SCHEDULED_KICKED_FROM_GROUP_TEXT, userId).formatted(sender.getGroupName()), true);

                }
            }
        }
    }
}