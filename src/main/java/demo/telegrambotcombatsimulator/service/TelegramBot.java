package demo.telegrambotcombatsimulator.service;

import com.vdurmont.emoji.EmojiParser;
import demo.telegrambotcombatsimulator.config.BotConfig;
import demo.telegrambotcombatsimulator.entity.User;
import demo.telegrambotcombatsimulator.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    final UserRepository userRepository;

    final CombatService combatService;

    // Текст для ответа на запрос "/help"
    static final String HELP_TEXT =
            """
                    Вы можете выполнять команды из главного меню слева или набрав команду:

                    Введите /start, чтобы активировать бота;

                    Введите /play, чтобы запустить игровой режим;
                    
                    Введите /gamerules, чтобы ознакомиться с правилами игры;

                    Введите /mydata, чтобы просмотреть хранящиеся о Вас данные;

                    Введите /deletedata, чтобы удалить хранящиеся о Вас данные;

                    Введите /help, чтобы снова увидеть это сообщение.
                     """;


    static final String RULES_TEXT =
                    """
                    Цель игры - снижать здоровье соперника и не допускать потерю собственного здоровья.
                    Победителем будет считаться игрок, который в конце матча останется с ненулевым здоровьем.
                    Игроки начинают матч с 50 здоровья. Матч состоит из раундов.
                    В каждом раунде игрок выбирает направление для атаки (голова/тело/ноги) и направление для защиты (голова/тело/ноги).
                    Во время своего выбора игрок не может узнать о выборе соперника.
                    После выбора направлений игрок может сделать выбор повторно или подтвердить свою готовность в раунде.
                    После готовности двух игроков проверяются результаты атак в раунде.
                    Атака игрока считается успешной, если направлена на участок, который остался без защиты.
                    При успешной атаке игрок наносит соперника урон в 10 единиц здоровья.
                    Раунды повторяются до тех пор, пока какой-то игрок не остается с 0 единиц здоровья.
                    
                    Одновременно игрок может участвовать только в одном матче.
                    
                    Начать и выбрать формат игры (Онлайн/Оффлайн) - через команду /play.
                    
                    Удачи и весёлой игры!
                    """;

    // Константа YES под текстовое наполнение кнопки
    static final String YES = "Да";

    // Константа regYes под аргумент для .setCallbackData() в рамках регистрации
    static final String REG_YES = "REG_YES";

    // Константа regYes под аргумент для .setCallbackData() в рамках начала игры
    static final String PLAY_OFFLINE_YES = "PLAY_OFFLINE_YES";

    // Константа NO под текстовое наполнение кнопки
    static final String NO = "Нет";

    // Константа regNo под аргумент для .setCallbackData() в рамках регистрации
    static final String REG_NO = "REG_NO";

    // Константа regYes под аргумент для .setCallbackData() в рамках начала игры
    static final String PLAY_OFFLINE_NO = "PLAY_OFFLINE_NO";

    // Константа "Возникла ошибка" под обработку исключений
    static final String ERROR_TEXT = "Error occurred: ";

    static final String ATTACK = "Атака";

    static final String DEFENSE = "Защита";

    static final String HEAD = "Голова";

    static final String BODY = "Туловище";

    static final String FOOT = "Ноги";

    static final String CHOOSE_ATTACK = "Выберите направление атаки";

    static final String CHOOSE_DEFENSE = "Выберите направление защиты";

    // Конструктор. Настройка кнопок главного меню. Бины?
    @Autowired
    public TelegramBot(BotConfig config, CombatService combatService, UserRepository userRepository) {
        // Чтобы решить проблему с бинами(?) добавил сервис и репозиторий в конструктор и раскидал аннотации на классы https://javarush.com/forum/1459 https://qna.habr.com/q/1296162 https://javarush.com/quests/lectures/questspring.level01.lecture36
        this.config = config;
        this.combatService = combatService;
        this.userRepository = userRepository;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "активировать бота"));
        listOfCommands.add(new BotCommand("/play", "начать игровой сеанс"));
        listOfCommands.add(new BotCommand("/gamerules", "посмотреть правила игры"));
        listOfCommands.add(new BotCommand("/mydata", "посмотреть сохранённые данные"));
        listOfCommands.add(new BotCommand("/deletedata", "удалить сохранённые данные"));
        listOfCommands.add(new BotCommand("/help", "руководство для помощи"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    // Прописываем поведение на каждое ожидаемое сообщение/команду от пользователя
    @Override
    public void onUpdateReceived(Update update) {

        // Флоу разбора обычного сообщения - апдейт содержит сообщение и сообщение содержит текст (можно ли оставить одну провеоку текста?)
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getUserName();

            switch (messageText) {

                // Регистрируем юзера и отвечаем hello-сообщением
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    prepareAndSendMessage(chatId, "Нажмите /help или откройте меню бота для помощи");
                    break;

                // Переход на игровой режим
                case "/play":
                    playCommandReceived(chatId, userName);
                    break;

                // Сразу отвечаем help-сообщением
                case "/help":
                    prepareAndSendMessage(chatId, HELP_TEXT);
                    break;

                // Запускаем флоу удаления юзера из бд
                case "/deletedata":
                    deleteData(chatId);
                    break;

                // Показываем данные из бд юзеру
                case "/mydata":
                    showUserData(chatId);
                    break;

                case "/gamerules":
                    prepareAndSendMessage(chatId, RULES_TEXT);
                    break;

                // Юзер нажал кнопку Атака
                case ATTACK:
                    sendHeadBodyFootChooseAndSetPlayerDirection(chatId, ATTACK, userName);
                    break;

                // Юзер нажал кнопку Защита
                case DEFENSE:
                    sendHeadBodyFootChooseAndSetPlayerDirection(chatId, DEFENSE, userName);
                    break;

                // Юзер нажал кнопку Голова для направления атаки или защиты
                case HEAD:
                    setPlayerDirectionAndSendAttackDefenseChoose(chatId, HEAD, userName);
                    break;

                // Юзер нажал кнопку Тело для направления атаки или защиты
                case BODY:
                    setPlayerDirectionAndSendAttackDefenseChoose(chatId, BODY, userName);
                    break;

                // Юзер нажал кнопку Ноги для направления атаки или защиты
                case FOOT:
                    setPlayerDirectionAndSendAttackDefenseChoose(chatId, FOOT, userName);
                    break;

                //отправить месседж с одной кнопкой - получить результат, и сообщением, что надо ждать оппонента
                case "Готов":
                    sendOpponentWaitAndBlockUserChoose(chatId, userName);
                    break;

                case "Обновить":
                    sendCombatResultOrRefresh(chatId, userName);
                    //отправить месседж с одной кнопкой - получить результат, и сообщением, что надо ждать оппонента
                    break;

                // Отбивка на незнакомое сообщение/команду
                default:
                    prepareAndSendMessage(chatId, "Вы отправили неизвестную или временно недоступную команду");
            }
        }

        // Флоу в случае ответа по Inline кнопке
        else if (update.hasCallbackQuery()) {

            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String userName = update.getCallbackQuery().getMessage().getChat().getUserName();

            switch (callbackData) {

                // В случае нажатия Да (Do you really want to delete your data?)
                case REG_YES:
                    userRepository.deleteByChatId(chatId);
                    String textREG_YES = "Ваши данные удалены";
                    executeEditMessageText(textREG_YES, chatId, messageId);
                    break;

                // В случае нажатия Нет (Do you really want to delete your data?)
                case REG_NO:
                    String textREG_NO = "Ваши данные не изменены\n"+
                            "Нажмите /mydata для проверки сохранённой информации";
                    executeEditMessageText(textREG_NO, chatId, messageId);
                    break;

                // В случае нажатия Онлайн (Выберите режим игры)
                case "Онлайн":
                    String textOnline = "Выбран режим игры - Онлайн\n"+
                            "(В настоящее время режим Онлайн не доступен, прошу перейти в режим Оффлайн через /play)";
                    executeEditMessageText(textOnline, chatId, messageId);
                    break;

                // В случае нажатия Оффлайн (Выберите режим игры)
                case "Оффлайн":
                    String textOffline = "Выбран режим игры - Оффлайн";
                    executeEditMessageText(textOffline, chatId, messageId);
                    checkOfflineReadiness(chatId);
                    break;

                // В случае нажатия Да (Готовы начать битву?)
                case PLAY_OFFLINE_YES:
                    String textPLAY_OFF_YES = "Ваш соперник - КоМпЬюТеР\n"+
                            "Делайте первый ход (пользуйтесь кнопками встроенной клавиатуры)";
                    if (combatService.hasUserActiveSession(userName)) {
                        do combatService.deleteUserSession(userName);
                        while (combatService.hasUserActiveSession(userName));
                        textPLAY_OFF_YES = "Ваш соперник - КоМпЬюТеР\n"+
                                "Делайте первый ход (пользуйтесь кнопками встроенной клавиатуры)\n"+
                                "Ваши прошлые сессии удалены. Больше не ломайте бота!";
                    }
                    executeEditMessageText(textPLAY_OFF_YES, chatId, messageId);
                    combatService.createOfflineSession(update.getCallbackQuery().getMessage().getChat().getUserName());
                    sendAttackDefenseChoose(chatId);
                    break;

                // В случае нажатия Нет (Готовы начать битву?)
                case PLAY_OFFLINE_NO:
                    String textPLAY_OFF_NO = "Возвращаемся к выбору режима игры";
                    executeEditMessageText(textPLAY_OFF_NO, chatId, messageId);
                    playCommandReceived(chatId, userName);
                    break;

                // предусмотреть много кратное нажатие на кнопку NEW_SESSION_YES
                case "NEW_SESSION_YES":
                    String textNEW_SESSION_YES = "Прошлая сессия удалена";
                    executeEditMessageText(textNEW_SESSION_YES, chatId, messageId);
                    do combatService.deleteUserSession(userName); while (combatService.hasUserActiveSession(userName));
                    playCommandReceived(chatId, userName);
                    break;

                case "NEW_SESSION_NO":
                    String textNEW_SESSION_NO = "Возвращаем Вас на прошлую сессию";
                    executeEditMessageText(textNEW_SESSION_NO, chatId, messageId);
                    // проработать все статусы
                   // sendCombatResultOrRefresh(chatId, userName);
                    sendActualGameMessage(chatId, userName);
                    break;

                // Отбивка на незнакомое сообщение/команду
                default:
                    prepareAndSendMessage(chatId, "Sorry, CallbackQuery was not recognized");
            }
        }
    }

    // Формирование приветственного сообщения
    private void startCommandReceived(long chatId, String name) {

        String answer = EmojiParser.parseToUnicode("Привет, " + name + ", рад нашей встрече!" + " :santa:");
        log.info("Replied to user " + name);

        prepareAndSendMessage(chatId, answer);
    }

    // Флоу регистрации юзера
    private void registerUser(Message msg) {

        // Сохраняем юзера в бд, если он ещё не зареган
        if(userRepository.findByChatId(msg.getChatId()).isEmpty()) {

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setCreatedTime(msg.getDate().toString());

            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    // Флоу перевода юзера в режим игры. Проверяем наличие юзера в бд. Предлпагаем выбрать режим игры
    private void playCommandReceived(long chatId, String userName) {

        // Возможно стоит перенести проверку юзера выше - на onUpdateReceived, чтобы избавиться от повторений условия в разных функциях
        if(userRepository.findByChatId(chatId).isPresent()) {

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));

            if (!combatService.hasUserActiveSession(userName)) {

                message.setText("Выберите режим игры");
                setInlineKeyboard(message, "Онлайн", "Онлайн", "Оффлайн", "Оффлайн");

                executeMessage(message);

            } else {

                message.setText("У Вас имеется активная сессия. Желаете завершить её и начать новую?");
                setInlineKeyboard(message, YES, "NEW_SESSION_YES", NO, "NEW_SESSION_NO");
                executeMessage(message);

            }

        } // Если юзер не существует в бд, то сообщаем ему об этом
        else {sendZeroRegistrationMessage(chatId);}
    }

    // Спрашиваем у юзера готов ли он начать игру
    private void checkOfflineReadiness(long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Готовы начать битву?");

        setInlineKeyboard(message, YES, PLAY_OFFLINE_YES, NO, PLAY_OFFLINE_NO); // Возможно стоит отправку сообщения с клавиатурой в единую функцию объединить

        executeMessage(message);
    }

    // Отправляем сообщение c предложением выбрать Атака или Защита
    private void sendAttackDefenseChoose(long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите направления для атаки и защиты");

        setReplyKeyboard(message, ATTACK, DEFENSE);

        executeMessage(message);
    }

    // Отправляем сообщение c предложением выбрать Голова, Тело или Ноги. Передаём статус Атаки/Защиты в сервис битвы
    private void sendHeadBodyFootChooseAndSetPlayerDirection(long chatId, String answer, String userName) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        if (answer.equals(ATTACK)) {
            message.setText(CHOOSE_ATTACK);
            combatService.updateSession(ATTACK, userName);
        }

        else if (answer.equals(DEFENSE)) {
            message.setText(CHOOSE_DEFENSE);
            combatService.updateSession(DEFENSE, userName);
        }

        setReplyKeyboard(message, HEAD, BODY, FOOT);
        executeMessage(message);
    }

    // Отправляем сообщение c предложением выбрать Голова, Тело или Ноги. Передаём статус Атаки/Защиты в сервис битвы
    private void setPlayerDirectionAndSendAttackDefenseChoose(long chatId, String answer, String userName) {

        switch (answer) {
            case HEAD -> {
                combatService.updateSession(HEAD, userName);
            }
            case BODY -> {
                combatService.updateSession(BODY, userName);
            }
            case FOOT -> {
                combatService.updateSession(FOOT, userName);
            }
        }

        if (combatService.getPlayerStatus(userName).equals("ready")) {
            sendAttackDefenseReadyChoose(chatId, userName);
        }
        else
            sendAttackDefenseChoose(chatId);
    }

    private void sendAttackDefenseReadyChoose(long chatId, String userName) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Нажмите \"Готов\", если сделали окончательный выбор");

        setReplyKeyboard(message, ATTACK, DEFENSE, "Готов");

        executeMessage(message);

        message.setText(combatService.getLastPlayerChoose(userName));
        executeMessage(message);
    }

    private void sendOpponentWaitAndBlockUserChoose(long chatId, String userName) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        combatService.updateSession("block", userName);

        if (combatService.getSessionStatus(userName).equals("doAction")) {

            message.setText(combatService.combatResult(userName));
            executeMessage(message);

            if (combatService.hasUserActiveSession(userName)) {

                sendAttackDefenseChoose(chatId);

            } else {

                message.setText("Нажмите /play, чтобы сыграть снова!");
                setReplyKeyboard(message, "/play", "/play");
                executeMessage(message);
            }
        }

        if (combatService.hasUserActiveSession(userName)) {

            if (combatService.getPlayerStatus(userName).equals("block")) {

                message.setText("Ваш выбор зафиксирован, ожидаем ход соперника");
                setReplyKeyboard(message, "Обновить", "Обновить");

                executeMessage(message);
            }
        }
    }

    private void sendCombatResultOrRefresh(long chatId, String userName) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        if (combatService.getPlayerStatus(userName).equals("resultReady")) {

            message.setText(combatService.getPlayerMessageAndRefreshStatus(userName));
            executeMessage(message);

            if (combatService.getSessionStatus(userName).equals("block")) {

                message.setText("Нажмите /play, чтобы сыграть снова!");
                setReplyKeyboard(message, "/play", "/play");
                executeMessage(message);

                combatService.updateSession("delete", userName);

            } else {

                sendAttackDefenseChoose(chatId);
            }

        } else {

            message.setText("Соперник скрылся! Приготовьтесь к внезапной атаке!");
            setReplyKeyboard(message, "Обновить", "Обновить");

            executeMessage(message);
        }
    }

    private void sendActualGameMessage (long chatId, String userName) {

        var playerStatus = combatService.getPlayerStatus(userName);

        if (playerStatus.equals("empty") || playerStatus.equals("waitDefense") || playerStatus.equals("waitAttack")) {

        }

    }


    // Флоу удаления юзера из бд
    private void deleteData(long chatId) {

        // Если юзер существует в бд, спрашиваем: "Удаляем или не удаляем?" с Inline кнопками
        if(userRepository.findByChatId(chatId).isPresent()) { // Возможно стоит перенести проверку юзера выше - на onUpdateReceived

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Вы действительно хотите удалить данные?");

            setInlineKeyboard(message, YES, REG_YES, NO, REG_NO);

            executeMessage(message);
        }

        // Если юзер не существует в бд, то сообщаем ему об этом
        else {sendZeroRegistrationMessage(chatId);}
    }

    // Флоу поиска данных юзера через бд
    private void showUserData(long chatId) {

        // Показываем дату, если юзер существует в бд
        if(userRepository.findByChatId(chatId).isPresent()) {

            var user = userRepository.findByChatId(chatId); // Почитать про optional https://habr.com/ru/articles/658457/
            String answer = user.get().toString(); // Подумать стоит ли добавлять проверку
            prepareAndSendMessage(chatId, answer);
        }

        // Отвечаем отказом, если юзер не существует в бд
        else {sendZeroRegistrationMessage(chatId);}
    }

    // Отправка сообщения с клавиатурой-кнопками
    private void sendMessage(long chatId, String textToSend) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        setReplyKeyboard(message, "Онлайн", "Онлайн");

        executeMessage(message);
    }

    private void sendZeroRegistrationMessage(long chatId) {

        String answer = "Вы не зарегистрированы, данные Вашего пользователя не найдены\n"+
                "Чтобы получить доступ ко всем командам, нажмите /start для повторной регистрации";
        prepareAndSendMessage(chatId, answer);
    }

    //Подготовка сообщения к отправке и переход на отправку
    private void prepareAndSendMessage(long chatId, String textToSend){

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    // Задать клавиатуру с привязанными кнопками
    public void setInlineKeyboard(SendMessage message, String text1, String callbackData1, String text2, String callbackData2) {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var firstButton = new InlineKeyboardButton();

        firstButton.setText(text1);
        firstButton.setCallbackData(callbackData1);

        var secondButton = new InlineKeyboardButton();

        secondButton.setText(text2);
        secondButton.setCallbackData(callbackData2);

        rowInLine.add(firstButton);
        rowInLine.add(secondButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);
    }

    // Задать клавиатуру с вложенными кнопками
    public void setReplyKeyboard(SendMessage message, String key1, String key2, String key3) { //Можно ли передать сюда лист из ключей?

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add(key1);
        row.add(key2);
        row.add(key3);

        keyboardRows.add(row);
    /*
        row = new KeyboardRow();

        row.add("register");
        row.add("check my data");
        row.add("delete my data");

        keyboardRows.add(row);
    */
        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);
    }

    public void setReplyKeyboard(SendMessage message, String key1, String key2) {

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add(key1);
        row.add(key2);

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);
    }

    // Редактирование текста уже отправленного сообщения
    private void executeEditMessageText(String text, long chatId, long messageId){

        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    // Финальное действие отправки сообщения
    private void executeMessage(SendMessage message) {

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }
}