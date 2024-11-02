package uz.pdp.appmanagegroupbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import uz.pdp.appmanagegroupbot.enums.LangFields;
import uz.pdp.appmanagegroupbot.enums.State;
import uz.pdp.appmanagegroupbot.model.User;
import uz.pdp.appmanagegroupbot.repository.UserRepository;
import uz.pdp.appmanagegroupbot.service.AdminSendingUpdateService;
import uz.pdp.appmanagegroupbot.service.ButtonService;
import uz.pdp.appmanagegroupbot.service.LangService;
import uz.pdp.appmanagegroupbot.service.telegram.Sender;
import uz.pdp.appmanagegroupbot.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSendingUpdateServiceImpl implements AdminSendingUpdateService {
    private final UserRepository userRepository;
    private final LangService langService;
    private final CommonUtils commonUtils;
    private final ButtonService buttonService;
    private final Sender sender;

    @Override
    @SneakyThrows
    public boolean process(Message message, State state) {
        List<User> users = switch (state) {
            case SENDING_UPDATE_TO_ALL -> userRepository.findAll();
            case SENDING_UPDATE_TO_SUBSCRIBED -> userRepository.findAllBySubscribed(true);
            case SENDING_UPDATE_TO_NON_SUBSCRIBED -> userRepository.findAllBySubscribed(false);
            default -> new ArrayList<>();
        };
        Long userId = message.getFrom().getId();
        if (message.hasText()) {
            String text = message.getText();
            if (text.equals(langService.getMessage(LangFields.BACK_BUTTON, userId))) {
                commonUtils.setState(userId, State.CHOOSE_USERS);
                sender.sendMessage(userId, langService.getMessage(LangFields.CHOOSE_UPDATE_TEXT, userId), buttonService.chooseUsers(userId));
                return false;
            }
        }
        sender.sendMessage(userId, langService.getMessage(LangFields.UPDATE_SENDING_TEXT, userId));
        if (message.hasText()) {
            SendMessage sendMessage = new SendMessage();
            if (message.hasEntities()) {
                sendMessage.setEntities(message.getEntities());
            }
            String text = message.getText();
            if (message.hasReplyMarkup())
                sendMessage.setReplyMarkup(message.getReplyMarkup());
            sendMessage.setText(text);
            for (User user : users) {
                sendMessage.setChatId(user.getId());
                sender.execute(sendMessage);

            }
        } else if (message.hasPhoto()) {
            List<PhotoSize> photo = message.getPhoto();
            PhotoSize photoSize = photo.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElseThrow();
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setCaption(message.getCaption());
            sendPhoto.setHasSpoiler(message.getHasMediaSpoiler());
            sendPhoto.setPhoto(new InputFile(photoSize.getFileId()));
            sendPhoto.setCaptionEntities(message.getCaptionEntities());
            if (message.hasReplyMarkup())
                sendPhoto.setReplyMarkup(message.getReplyMarkup());
            for (User user : users) {
                sendPhoto.setChatId(user.getId());
                sender.execute(sendPhoto);

            }
        } else if (message.hasContact()) {
            SendContact sendContact = new SendContact();
            Contact contact = message.getContact();
            sendContact.setFirstName(contact.getFirstName());
            sendContact.setLastName(contact.getLastName());
            sendContact.setVCard(contact.getVCard());
            sendContact.setPhoneNumber(contact.getPhoneNumber());
            sendContact.setProtectContent(message.getHasProtectedContent());
            if (message.hasReplyMarkup())
                sendContact.setReplyMarkup(message.getReplyMarkup());
            for (User user : users) {
                sendContact.setChatId(user.getId());
                sender.execute(sendContact);
            }
        } else if (message.hasDocument()) {
            SendDocument document = new SendDocument();
            document.setDocument(new InputFile(message.getDocument().getFileId()));
            document.setCaption(message.getCaption());
            document.setCaptionEntities(message.getCaptionEntities());
            if (message.hasReplyMarkup())
                document.setReplyMarkup(message.getReplyMarkup());
            if (message.getDocument().getThumbnail() != null)
                document.setThumbnail(new InputFile(message.getDocument().getThumbnail().getFileId()));
            for (User user : users) {
                document.setChatId(user.getId());
                sender.execute(document);
            }
        } else if (message.hasVideo()) {
            SendVideo sendVideo = new SendVideo();
            Video video = message.getVideo();
            sendVideo.setVideo(new InputFile(video.getFileId()));
            sendVideo.setCaption(message.getCaption());
            sendVideo.setCaptionEntities(message.getCaptionEntities());
            sendVideo.setDuration(video.getDuration());
            sendVideo.setHasSpoiler(message.getHasMediaSpoiler());
            sendVideo.setHeight(video.getHeight());
            sendVideo.setWidth(video.getWidth());
            sendVideo.setProtectContent(message.getHasProtectedContent());
            if (message.hasReplyMarkup())
                sendVideo.setReplyMarkup(message.getReplyMarkup());
            if (video.getThumbnail() != null)
                sendVideo.setThumbnail(new InputFile(video.getThumbnail().getFileId()));
            for (User user : users) {
                sendVideo.setChatId(user.getId());
                sender.execute(sendVideo);
            }
        } else if (message.hasVideoNote()) {
            VideoNote videoNote = message.getVideoNote();
            SendVideoNote sendVideoNote = new SendVideoNote();
            sendVideoNote.setDuration(videoNote.getDuration());
            if (videoNote.getThumbnail() != null)
                sendVideoNote.setThumbnail(new InputFile(videoNote.getThumbnail().getFileId()));
            sendVideoNote.setProtectContent(message.getHasProtectedContent());
            sendVideoNote.setLength(videoNote.getLength());
            sendVideoNote.setVideoNote(new InputFile(videoNote.getFileId()));
            if (message.hasReplyMarkup())
                sendVideoNote.setReplyMarkup(message.getReplyMarkup());
            for (User user : users) {
                sendVideoNote.setChatId(user.getId());
                sender.execute(sendVideoNote);
            }
        } else if (message.hasAnimation()) {
            Animation animation = message.getAnimation();
            SendAnimation sendAnimation = new SendAnimation();
            sendAnimation.setCaption(message.getCaption());
            sendAnimation.setCaptionEntities(message.getCaptionEntities());
            sendAnimation.setAnimation(new InputFile(animation.getFileId()));
            sendAnimation.setDuration(animation.getDuration());
            sendAnimation.setWidth(animation.getWidth());
            sendAnimation.setHeight(animation.getHeight());
            sendAnimation.setHasSpoiler(message.getHasMediaSpoiler());
            if (message.hasReplyMarkup())
                sendAnimation.setReplyMarkup(message.getReplyMarkup());
            if (animation.getThumbnail() != null)
                sendAnimation.setThumbnail(new InputFile(animation.getThumbnail().getFileId()));
            sendAnimation.setProtectContent(message.getHasProtectedContent());
            for (User user : users) {
                sendAnimation.setChatId(user.getId());
                sender.execute(sendAnimation);
            }
        } else if (message.hasAudio()) {
            Audio audio = message.getAudio();
            SendAudio sendAudio = new SendAudio();
            sendAudio.setCaption(message.getCaption());
            sendAudio.setAudio(new InputFile(audio.getFileId()));
            sendAudio.setDuration(audio.getDuration());
            sendAudio.setCaptionEntities(message.getCaptionEntities());
            sendAudio.setProtectContent(message.getHasProtectedContent());
            if (message.hasReplyMarkup())
                sendAudio.setReplyMarkup(message.getReplyMarkup());
            if (audio.getThumbnail() != null)
                sendAudio.setThumbnail(new InputFile(audio.getThumbnail().getFileId()));
            sendAudio.setTitle(audio.getTitle());
            for (User user : users) {
                sendAudio.setChatId(user.getId());
                sender.execute(sendAudio);
            }
        } else if (message.hasVoice()) {
            Voice voice = message.getVoice();
            SendVoice sendVoice = new SendVoice();
            sendVoice.setCaption(message.getCaption());
            sendVoice.setCaptionEntities(message.getCaptionEntities());
            sendVoice.setDuration(voice.getDuration());
            sendVoice.setProtectContent(message.getHasProtectedContent());
            sendVoice.setVoice(new InputFile(voice.getFileId()));
            if (message.hasReplyMarkup())
                sendVoice.setReplyMarkup(message.getReplyMarkup());
            for (User user : users) {
                sendVoice.setChatId(user.getId());
                sender.execute(sendVoice);
            }
        } else if (message.hasSticker()) {
            Sticker sticker = message.getSticker();
            SendSticker sendSticker = new SendSticker();
            sendSticker.setProtectContent(message.getHasProtectedContent());
            sendSticker.setSticker(new InputFile(sticker.getFileId()));
            sendSticker.setEmoji(sticker.getEmoji());
            if (message.hasReplyMarkup())
                sendSticker.setReplyMarkup(message.getReplyMarkup());
            for (User user : users) {
                sendSticker.setChatId(user.getId());
                sender.execute(sendSticker);
            }
        } else if (message.hasPoll()) {
            sender.sendMessage(userId, langService.getMessage(LangFields.POLL_DONT_WORK_TEXT, userId));
            return false;
        } else if (message.hasLocation()) {
            Location location = message.getLocation();
            if (location.getLivePeriod() != null) {
                sender.sendMessage(userId, langService.getMessage(LangFields.LIVE_LOCATION_DONT_WORK_TEXT, userId));
                return false;
            }
            SendLocation sendLocation = new SendLocation();
            sendLocation.setProtectContent(message.getHasProtectedContent());
            if (message.hasReplyMarkup())
                sendLocation.setReplyMarkup(message.getReplyMarkup());
            sendLocation.setLatitude(location.getLatitude());
            sendLocation.setLongitude(location.getLongitude());
            sendLocation.setProximityAlertRadius(location.getProximityAlertRadius());
            for (User user : users) {
                sendLocation.setChatId(user.getId());
                sender.execute(sendLocation);
            }
        } else if (message.hasDice()) {
            Dice dice = message.getDice();
            Integer value1 = dice.getValue();
            SendDice sendDice = new SendDice();
            sendDice.setProtectContent(message.getHasProtectedContent());
            sendDice.setEmoji(dice.getEmoji());
            sendDice.setReplyMarkup(message.getReplyMarkup());
            for (User user : users) {
                sendDice.setChatId(user.getId());
                Message execute = sender.execute(sendDice);
                Integer value = execute.getDice().getValue();
                if (value1 < value) {
                    sender.sendMessage(user.getId(), langService.getMessage(LangFields.WIN_DICE_TEXT, userId).formatted(value1, value));
                } else if (value1.equals(value)) {
                    sender.sendMessage(user.getId(), langService.getMessage(LangFields.EQUAL_DICE_TEXT, userId));
                } else
                    sender.sendMessage(user.getId(), langService.getMessage(LangFields.LOSE_DICE_TEXT, userId).formatted(value1, value));
            }
        }
        return true;
    }
}
