package uz.pdp.appmanagegroupbot.service.telegram;

import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.groupadministration.*;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.appmanagegroupbot.utils.AppConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class Sender extends DefaultAbsSender {

    public Sender() {
        super(new DefaultBotOptions(), AppConstants.BOT_TOKEN);
    }

    public void sendMessage(Long userId, String text) {
        sendMessage(userId, text, null, false);
    }

    public void sendMessage(Long userId, String text, boolean isMarkdown) {
        sendMessage(userId, text, null, isMarkdown);
    }

    public void sendMessage(Long userId, String text, ReplyKeyboard replyKeyboard) {
        sendMessage(userId, text, replyKeyboard, false);
    }


    public void sendMessage(Long userId, String text, ReplyKeyboard replyKeyboard, boolean isMarkdown) {
        final int MAX_LENGTH = 1024;
        if (text.length() > MAX_LENGTH) {
            int startIndex = 0;

            while (startIndex < text.length() - 1) {
                int endIndex = Math.min(startIndex + MAX_LENGTH, text.length());

                int lastNewLineIndex = text.lastIndexOf("\n\n", endIndex);
                if (lastNewLineIndex != -1 && lastNewLineIndex > startIndex) {
                    endIndex = lastNewLineIndex;
                }

                String part = text.substring(startIndex, endIndex).trim();

                SendMessage message = new SendMessage(userId.toString(), part);
                if (isMarkdown)
                    message.setParseMode("Markdown");

                try {
                    if (replyKeyboard != null) {
                        message.setReplyMarkup(replyKeyboard);
                        execute(message);
                    } else execute(message);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                startIndex = endIndex + 1;
            }
        } else {
            try {
                SendMessage sendMessage = new SendMessage(userId.toString(), text);
                if (replyKeyboard != null)
                    sendMessage.setReplyMarkup(replyKeyboard);
                if (isMarkdown)
                    sendMessage.setParseMode("Markdown");
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void kickUser(Long userId) {
        try {
            execute(new BanChatMember(AppConstants.GROUP_ID.toString(), userId));
            execute(new UnbanChatMember(AppConstants.GROUP_ID.toString(), userId));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptJoinRequest(Long userId, Long groupId) {
        ApproveChatJoinRequest acceptJoinReq = new ApproveChatJoinRequest();
        acceptJoinReq.setUserId(userId);
        acceptJoinReq.setChatId(groupId);
        try {
            execute(acceptJoinReq);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLink(Long groupId) {
        try {
            CreateChatInviteLink createChatInviteLink = new CreateChatInviteLink();
            createChatInviteLink.setName("Link by bot");
            createChatInviteLink.setCreatesJoinRequest(true);
            createChatInviteLink.setChatId(groupId);

            return execute(createChatInviteLink).getInviteLink();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Chat getChat(Long userId) {
        try {
            return execute(new GetChat(userId.toString()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPhoto(Long userId, String caption, String path, ReplyKeyboard keyboard) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setCaption(caption);
        InputFile photo = new InputFile();
        photo.setMedia(new java.io.File(path));
        sendPhoto.setPhoto(photo);
        sendPhoto.setChatId(userId);
        sendPhoto.setReplyMarkup(keyboard);
        sendPhoto.setParseMode("Markdown");
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFilePath(String fileId) {
        GetFile getFile = new GetFile(fileId);
        try {
            File execute = execute(getFile);

            String fileUrl = execute.getFileUrl(AppConstants.BOT_TOKEN);

            String fileName = UUID.randomUUID().toString();
            String[] split = fileUrl.split("\\.");
            String fileExtension = split[split.length - 1];
            String filePath = fileName + "." + fileExtension;

            Path targetPath = Paths.get(AppConstants.FILE_PATH, filePath);

            Files.createDirectories(targetPath.getParent());

            try (InputStream inputStream = new URL(fileUrl).openStream();
                 OutputStream outputStream = Files.newOutputStream(targetPath)) {
                StreamUtils.copy(inputStream, outputStream);
            }

            return targetPath.toString();
        } catch (TelegramApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getGroupName() {
        try {
            Chat execute = execute(new GetChat(AppConstants.GROUP_ID.toString()));
            return execute.getTitle();
        } catch (TelegramApiException e) {
            return "Guruhda muammo";
        }
    }

    public void sendDocument(Long userId, String caption, String path, InlineKeyboardMarkup keyboard) {
        SendDocument document = new SendDocument();
        document.setCaption(caption);
        InputFile photo = new InputFile();
        photo.setMedia(new java.io.File(path));
        document.setDocument(photo);
        document.setChatId(userId);
        document.setReplyMarkup(keyboard);
        try {
            execute(document);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeCaption(Long userId, Integer messageId, String text) {
        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setChatId(userId);
        editMessageCaption.setCaption(text);
        editMessageCaption.setMessageId(messageId);
        try {
            execute(editMessageCaption);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
