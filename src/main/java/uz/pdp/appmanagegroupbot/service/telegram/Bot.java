package uz.pdp.appmanagegroupbot.service.telegram;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.pdp.appmanagegroupbot.service.ProcessService;
import uz.pdp.appmanagegroupbot.utils.AppConstants;

@Service
public class Bot extends TelegramLongPollingBot {
    private final ProcessService processService;

    public Bot(ProcessService processService) {
        super(new DefaultBotOptions(), AppConstants.BOT_TOKEN);
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);
            this.processService = processService;
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        processService.process(update);
    }

    @Override
    public String getBotUsername() {
        return AppConstants.BOT_USERNAME;
    }
}
