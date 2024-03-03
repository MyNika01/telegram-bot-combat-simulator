package demo.telegrambotcombatsimulator.service;

import demo.telegrambotcombatsimulator.config.BotConfig;
import demo.telegrambotcombatsimulator.entity.User;
import demo.telegrambotcombatsimulator.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.HashMap;
import java.util.List;

import static demo.telegrambotcombatsimulator.enums.PlayerStatusType.*;
import static demo.telegrambotcombatsimulator.enums.SessionStatusType.DO_ACTION;
import static demo.telegrambotcombatsimulator.enums.SessionStatusType.S_BLOCK;

@Component
@Slf4j
@EnableScheduling
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    final UserRepository userRepository;

    final CombatService combatService;

    final QueueService queueService;

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

    // Текст для объяснения правил через "/gamerules"
    static final String RULES_TEXT =
            """
                    Цель игры - снижать здоровье соперника и не допускать потерю собственного здоровья.
                    Победителем будет считаться игрок, который в конце матча останется с ненулевым здоровьем.
                    Игроки начинают матч с 50 здоровья. Матч состоит из раундов.
                    В каждом раунде игрок выбирает направление для атаки (голова/корпус/ноги) и направление для защиты (голова/корпус/ноги).
                    Во время своего выбора игрок не может узнать о выборе соперника.
                    После выбора направлений игрок может сделать выбор повторно или подтвердить свою готовность в раунде.
                    После готовности двух игроков проверяются результаты атак в раунде.
                    Атака игрока считается успешной, если направлена на участок, который остался без защиты.
                    При успешной атаке игрок наносит сопернику урон в 10 единиц здоровья.
                    Раунды повторяются до тех пор, пока какой-то игрок не остается с 0 единиц здоровья.
                                        
                    Одновременно игрок может участвовать только в одном матче.
                                        
                    Начать и выбрать формат игры (Онлайн/Оффлайн) - через команду /play.
                                        
                    Удачи и весёлой игры!
                    """;

    // Константа YES под текстовое наполнение кнопки
    static final String YES = "Да ✅";

    // Константа REG_YES под аргумент для .setCallbackData() в рамках регистрации
    static final String REG_YES = "REG_YES";

    // Константа PLAY_OFFLINE_YES под аргумент для .setCallbackData() в рамках начала игры
    static final String PLAY_OFFLINE_YES = "PLAY_OFFLINE_YES";

    static final String PLAY_ONLINE_YES = "PLAY_ONLINE_YES";

    static final String PLAY_FRIEND_YES = "PLAY_FRIEND_YES";

    // Константа NO под текстовое наполнение кнопки
    static final String NO = "Нет ❌";

    // Константа REG_NO под аргумент для .setCallbackData() в рамках регистрации
    static final String REG_NO = "REG_NO";

    // Константа PLAY_NO под аргумент для .setCallbackData() в рамках начала игры
    static final String PLAY_NO = "PLAY_NO";

    // Константа "Возникла ошибка" под обработку исключений
    static final String ERROR_TEXT = "Error occurred: ";

    static final String ATTACK = "Атака ⚔";

    static final String DEFENSE = "Защита \uD83D\uDEE1";

    static final String HEAD ="Голова \uD83E\uDDE2";

    static final String BODY = "Корпус \uD83D\uDC55";

    static final String LEGS = "Ноги \uD83D\uDC5F";

    static final String CHOOSE_ATTACK = "Выберите направление атаки ⚔";

    static final String CHOOSE_DEFENSE = "Выберите направление защиты \uD83D\uDEE1";

    static final String ONLINE = "Онлайн";

    static final String OFFLINE = "Оффлайн";

    static final String FRIEND = "С другом";

    static final String REFRESH = "Обновить";

    static final String READY = "Готов";

    // Конструктор. Настройка кнопок главного меню бота
    // Столкнулся с проблемой - не понимал, что такое бины и почему на это жалуется ide
    // Вылечил тем, что раскинул аннотации по классам (@Component, @Service, @Repository)
    // https://javarush.com/forum/1459 https://qna.habr.com/q/1296162 https://javarush.com/quests/lectures/questspring.level01.lecture36
    @Autowired
    public TelegramBot(BotConfig config, CombatService combatService, UserRepository userRepository, QueueService queueService) {

        this.config = config;
        this.combatService = combatService;
        this.userRepository = userRepository;
        this.queueService = queueService;

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
            log.error("Error at setting bot's command list: " + e.getMessage());
        }
    }

    /**
     * Проставляем getBotName и getToken
     */
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    // Не разобрался, почему warning на getBotToken
    @Override
    public String getBotToken() {
        return config.getToken();
    }

    // Коды смайликов искать тут https://emojipedia.org/red-heart
    // Прописываем поведение на каждое ожидаемое событие (update) от пользователя
    @Override
    public void onUpdateReceived(Update update) {

        // Флоу разбора обычного сообщения - апдейт содержит сообщение и сообщение содержит текст (можно ли оставить одну проверку текста?)
        if (update.hasMessage() && update.getMessage().hasText()) {

            var msg = update.getMessage();
            var chat = msg.getChat();
            long chatId = msg.getChatId();
            String messageText = msg.getText();
            String userName = chat.getUserName();

            //тут нужно проверить NPE, так как userName может не прийти
            try {
                if (userName.isEmpty()) userName = chat.getFirstName() + " " + msg.getChatId().toString();
            } catch (Exception e) {
                log.error(ERROR_TEXT + e.getMessage());
                userName = chat.getFirstName() + msg.getChatId().toString();
            }

            switch (messageText) {

                // Регистрируем пользователя, отвечаем hello-сообщением и сообщением-подсказкой
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    prepareAndSendMessage(chatId, "Нажмите /help или откройте меню бота для помощи");
                    break;

                // Переводим пользователя на выбор и создание игровой сессии
                case "/play":
                    playCommandReceived(chatId, userName);
                    break;

                // Отвечаем пользователю help-сообщением
                case "/help":
                    prepareAndSendMessage(chatId, HELP_TEXT);
                    break;

                // Запускаем флоу удаления пользователя из базы данных
                case "/deletedata":
                    deleteData(chatId);
                    break;

                // Показываем пользователю его данные из базы данных
                case "/mydata":
                    showUserData(chatId);
                    break;

                // Показываем пользователю правила игры
                case "/gamerules":
                    prepareAndSendMessage(chatId, RULES_TEXT);
                    break;

                // Обрабатываем событие "Пользователь отправил сообщение - Атака"
                case ATTACK, DEFENSE:
                    sendHeadBodyFootChooseAndSetPlayerDirection(chatId, messageText, userName);
                    break;

                // Обрабатываем событие "Пользователь отправил сообщение - Голова"
                case HEAD, BODY, LEGS:
                    setPlayerDirectionAndPrepareAttackDefenseChoose(chatId, messageText, userName);
                    break;

                // Обрабатываем событие "Пользователь отправил сообщение - Готов"
                case READY:
                    sendOpponentWaitAndBlockUserChoose(chatId, userName);
                    break;

                // Обрабатываем событие "Пользователь нажал Обновить"
                case REFRESH:
                    sendCombatResultOrRefresh(chatId, userName);
                    break;

                default:

                    // Пробуем пригласить друга в сессию
                    if (messageText.charAt(0) == '@') {

                        String friendName = messageText.substring(1);

                        // todo Учесть активные сессии у друга - удалить их!
                        if (friendName.equals(userName)) {

                            prepareAndSendMessage(chatId, "Нельзя отправлять приглашение самому себе");
                            break;

                        } else if (userRepository.isExistByUserName(friendName)) {

                            if (combatService.hasUserActiveSession(friendName)) {

                                prepareAndSendMessage(chatId, "Другу необходимо завершить активную сессию или нажать /play для её удаления");

                            } else {

                                String text = "\nДелайте первый ход (пользуйтесь кнопками встроенной клавиатуры)\n";
                                String textToUser = "Битва начинается! Ваш соперник - @" + friendName + text;
                                String textToFriend = "Битва начинается! Ваш соперник - @" + userName + text;

                                combatService.createSession(userName, friendName);
                                prepareAndSendMessage(chatId, textToUser);
                                sendAttackDefenseChoose(chatId);

                                long friendChatId = userRepository.findByUserName(friendName).get().chatId();
                                prepareAndSendMessage(friendChatId, textToFriend);
                                sendAttackDefenseChoose(friendChatId);
                            }

                            break;

                        } else
                            prepareAndSendMessage(chatId, "Другу необходимо нажать /start для завершения регистрации в боте");

                        // Отбивка на незнакомое сообщение/команду
                    } else prepareAndSendMessage(chatId, "Вы отправили неизвестную или временно недоступную команду");
            }
        }

        // Флоу обработки событий, полученных через Inline кнопки (те, что встроены в сообщение)
        else if (update.hasCallbackQuery()) {

            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String userName = update.getCallbackQuery().getMessage().getChat().getUserName();

            switch (callbackData) {

                // Обрабатываем событие "Пользователь нажал кнопку - Да. Был вопрос (Do you really want to delete your data?)"
                case REG_YES:
                    userRepository.deleteByChatId(chatId);
                    String textREG_YES = "Ваши данные удалены";
                    executeEditMessageText(textREG_YES, chatId, messageId);
                    break;

                // Обрабатываем событие "Пользователь нажал кнопку - Нет. Был вопрос (Do you really want to delete your data?)"
                case REG_NO:
                    String textREG_NO = "Ваши данные не изменены\n" +
                            "Нажмите /mydata для проверки сохранённой информации";
                    executeEditMessageText(textREG_NO, chatId, messageId);
                    break;

                // Обрабатываем событие "Пользователь нажал кнопку - Онлайн. Был вопрос (Выберите режим игры)"
                // todo переименовать Онлайн и Оффлайн на Против игрока и Против компьютера(?)
                case ONLINE, FRIEND, OFFLINE:
                    String text = "Выбран режим игры: " + callbackData;
                    executeEditMessageText(text, chatId, messageId);
                    checkReadiness(chatId, callbackData);
                    break;

                // Обрабатываем событие "Пользователь нажал кнопку - Да. Был вопрос (Готовы начать битву?)"
                case PLAY_OFFLINE_YES:

                    String textPLAY_OFF_YES = "Ваш соперник - КоМпЬюТеР\n" +
                            "Делайте первый ход (пользуйтесь кнопками встроенной клавиатуры)";

                    // Заботимся о том, чтобы не было дублирования сессии
                    if (combatService.hasUserActiveSession(userName)) {

                        do combatService.deleteUserSession(userName);
                        while (combatService.hasUserActiveSession(userName));

                        textPLAY_OFF_YES = "Ваш соперник - КоМпЬюТеР\n" +
                                "Делайте первый ход (пользуйтесь кнопками встроенной клавиатуры)\n" +
                                "Ваши прошлые сессии удалены. Больше не ломайте бота!";
                    }

                    executeEditMessageText(textPLAY_OFF_YES, chatId, messageId);
                    combatService.createSession(update.getCallbackQuery().getMessage().getChat().getUserName(), "offline");
                    sendAttackDefenseChoose(chatId);
                    break;

                case PLAY_ONLINE_YES:

                    String PLAY_ON_YES = "Ищем соперника...\n" +
                            "Вы получите сообщение о начале битвы. Будьте готовы!";

                    executeEditMessageText(PLAY_ON_YES, chatId, messageId);

                    //добавляем юзера в очередь на поиск матча
                    queueService.addToPlayersQueue(chatId, userName);

                    break;

                case PLAY_FRIEND_YES:

                    String PLAY_FR_YES = "В ответ укажите никнейм друга (через @)";

                    executeEditMessageText(PLAY_FR_YES, chatId, messageId);

                    //добавляем юзера в очередь на поиск матча
                    queueService.addToPlayersQueue(chatId, userName);

                    break;

                // Обрабатываем событие "Пользователь нажал кнопку - Нет. Был вопрос (Готовы начать битву?)"
                case PLAY_NO:
                    String textPLAY_NO = "Возвращаемся к выбору режима игры";
                    executeEditMessageText(textPLAY_NO, chatId, messageId);
                    playCommandReceived(chatId, userName);
                    break;

                // Обрабатываем событие "Пользователь нажал кнопку - Да. Был вопрос (Удалить прошлую сессию?)"
                case "NEW_SESSION_YES":

                    String textNEW_SESSION_YES = "Прошлая сессия удалена";
                    executeEditMessageText(textNEW_SESSION_YES, chatId, messageId);

                    // Заботимся о том, чтобы не было дублирования сессии
                    do combatService.deleteUserSession(userName);
                    while (combatService.hasUserActiveSession(userName));

                    playCommandReceived(chatId, userName);
                    break;

                // Обрабатываем событие "Пользователь нажал кнопку - Нет. Был вопрос (Удалить прошлую сессию?)"
                case "NEW_SESSION_NO":
                    String textNEW_SESSION_NO = "Возвращаем Вас на прошлую сессию";
                    executeEditMessageText(textNEW_SESSION_NO, chatId, messageId);
                    // проработать все статусы - вспомнить откуда и зачем?
                    // sendCombatResultOrRefresh(chatId, userName);
                    sendActualGameMessage(chatId, userName);
                    break;

                // Отбивка на незнакомое сообщение/команду
                default:
                    prepareAndSendMessage(chatId, "Sorry, CallbackQuery was not recognized");
            }
        }
    }

    // Формируем приветственное сообщение, отправляем пользователю
    private void startCommandReceived(long chatId, String name) {

        String answer = "Привет, " + name + ", рад нашей встрече!" + " \uD83D\uDC7E";
        log.info("Replied to user " + name);

        prepareAndSendMessage(chatId, answer);
    }

    // Регистрируем пользователя
    private void registerUser(Message msg) {

        // Сохраняем юзера в бд, только если он отсутствует в бд
        if (userRepository.findByChatId(msg.getChatId()).isEmpty()) {

            var chatId = msg.getChatId();
            var chat = msg.getChat();
            String userName = chat.getUserName();

            //тут нужно проверить NPE, потому что userName может прийти пустой
            try {
                if (userName.isEmpty()) userName = chat.getFirstName() + " " + msg.getChatId().toString();
            } catch (Exception e) {
                log.error(ERROR_TEXT + e.getMessage());
                userName = chat.getFirstName() + msg.getChatId().toString();
            }

            User user = new User(chatId, chat.getFirstName(), chat.getLastName(), userName);

            userRepository.save(user);
            log.info("User saved: " + user);
        }
    }

    // Проверяем наличие юзера в бд. Предлагаем выбрать режим игры или завершить/продолжить новую
    private void playCommandReceived(long chatId, String userName) {

        if (userRepository.findByChatId(chatId).isPresent()) {

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));

            if (!combatService.hasUserActiveSession(userName)) {

                message.setText("Выберите режим игры");

                HashMap<String, String> buttons = new HashMap<>();
                buttons.put(ONLINE, ONLINE);
                buttons.put(FRIEND, FRIEND);
                buttons.put(OFFLINE, OFFLINE);
                setInlineKeyboard(message, buttons);

                executeMessage(message);

            } else {

                HashMap<String, String> buttons = new HashMap<>();
                buttons.put(YES, "NEW_SESSION_YES");
                buttons.put(NO, "NEW_SESSION_NO");
                setInlineKeyboard(message, buttons);

                message.setText("У Вас имеется активная сессия. Желаете завершить её и начать новую?");

                executeMessage(message);
            }
        }
        // Если пользователь не найден в бд, то сообщаем ему об этом
        else {
            sendZeroRegistrationMessage(chatId);
        }
    }

    private void checkReadiness(long chatId, String format) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Готовы начать битву?");

        HashMap<String, String> buttons = new HashMap<>();

        switch (format) {

            case OFFLINE -> buttons.put(YES, PLAY_OFFLINE_YES);
            case ONLINE -> buttons.put(YES, PLAY_ONLINE_YES);
            case FRIEND -> buttons.put(YES, PLAY_FRIEND_YES);
        }

        buttons.put(NO, PLAY_NO);

        setInlineKeyboard(message, buttons); // Возможно стоит отправку сообщения с клавиатурой в единую функцию объединить

        executeMessage(message);
    }

    // Отправляем сообщение с двумя кнопками - Атака и Защита
    public void sendAttackDefenseChoose(long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите направления для атаки ⚔ и защиты \uD83D\uDEE1");

        ArrayList<String> keys = new ArrayList<>();
        keys.add(ATTACK);
        keys.add(DEFENSE);

        setReplyKeyboard(message, keys);

        executeMessage(message);
    }

    // Передаём в сервис контроля игровых сессий выбор пользователя - Атака или Защита
    // Отправляем пользователю сообщение с тремя кнопками - Голова, корпус и Ноги
    private void sendHeadBodyFootChooseAndSetPlayerDirection(long chatId, String answer, String userName) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        if (answer.equals(ATTACK)) {
            message.setText(CHOOSE_ATTACK);
            combatService.updateSession(ATTACK, userName);
        } else if (answer.equals(DEFENSE)) {
            message.setText(CHOOSE_DEFENSE);
            combatService.updateSession(DEFENSE, userName);
        }

        ArrayList<String> keys = new ArrayList<>();
        keys.add(HEAD);
        keys.add(BODY);
        keys.add(LEGS);

        setReplyKeyboard(message, keys);
        executeMessage(message);
    }

    // Передаём в сервис контроля игровых сессий выбор пользователя - Голова, корпус или Ноги
    // Если пользователь уже выбрал Атака и Защиту, то готовим сообщение с тремя кнопками - Атака, Защита и Готов
    // В противном случае готовим кнопки - Атака и Защита
    private void setPlayerDirectionAndPrepareAttackDefenseChoose(long chatId, String answer, String userName) {

        combatService.updateSession(answer, userName);

        if (combatService.getPlayerStatus(userName).equals(P_READY)) {
            sendAttackDefenseReadyChoose(chatId, userName);
        } else
            sendAttackDefenseChoose(chatId);
    }

    // Отправляем пользователю сообщение с тремя кнопками - Атака, Защита и Готов
    private void sendAttackDefenseReadyChoose(long chatId, String userName) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Нажмите \"Готов\", если сделали окончательный выбор");

        ArrayList<String> keys = new ArrayList<>();
        keys.add(ATTACK);
        keys.add(DEFENSE);
        keys.add(READY);

        setReplyKeyboard(message, keys);

        executeMessage(message);

        message.setText(combatService.getLastPlayerChoose(userName));
        executeMessage(message);
    }

    // Обрабатываем конец раунда (когда пользователь подтвердил готовность)
    private void sendOpponentWaitAndBlockUserChoose(long chatId, String userName) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        // Ставим игроку статус block, чтобы он не мог изменить свой выбор в раунде
        combatService.updateSession("block", userName);

        // Уже оба игрока подтвердили готовность? Если да, то статус сессии должен быть "doAction" (==true)
        if (combatService.getSessionStatus(userName).equals(DO_ACTION)) {

            // Активируем подсчёт раунда, получаем результат раунда по игроку, отправляем результат игроку
            message.setText(combatService.combatResult(userName));
            executeMessage(message);

            // Это был финальный раунд? Если нет, то игрок остался в сессии
            if (combatService.hasUserActiveSession(userName)) {

                // Отдаём пользователю игровые кнопки для продолжения
                sendAttackDefenseChoose(chatId);
            }

            // Это был финальный раунд? Если да, то игрок уже удалён из сессии
            else {

                // Предлагаем пользователю сыграть снова
                message.setText("Нажмите /play, чтобы сыграть снова!");

                ArrayList<String> keys = new ArrayList<>();
                keys.add("/play");

                setReplyKeyboard(message, keys);
                executeMessage(message);
            }
        }

        // Это был финальный раунд? Если нет, то игрок остался в сессии
        if (combatService.hasUserActiveSession(userName)) {

            // Пользователь находится в состоянии ожидания? Если да, то у него должен быть статус "block"
            if (combatService.getPlayerStatus(userName).equals(P_BLOCK)) {

                // Предлагаем пользователю нажимать на кнопку, чтобы получить результаты раунда (после готовности соперника)
                message.setText("Ваш выбор зафиксирован, ожидаем ход соперника");

                ArrayList<String> keys = new ArrayList<>();
                keys.add(REFRESH);

                setReplyKeyboard(message, keys);
                executeMessage(message);
            }
        }
    }

    // Обрабатываем ожидание нового раунда (когда ты - готов, а соперник - не готов)
    private void sendCombatResultOrRefresh(long chatId, String userName) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        // Готовы ли результаты раунда? Если да, то статус игрока будет "resultReady"
        if (combatService.getPlayerStatus(userName).equals(RESULT_READY)) {

            // Отправляем пользователю результаты последнего раунда
            message.setText(combatService.getPlayerMessageAndRefreshStatus(userName));
            executeMessage(message);

            // Был ли раунд финальным? Если да, то статус сессии должен быть "block"
            if (combatService.getSessionStatus(userName).equals(S_BLOCK)) {

                // Предлагаем пользователю сыграть снова
                message.setText("Нажмите /play, чтобы сыграть снова!");

                ArrayList<String> keys = new ArrayList<>();
                keys.add("/play");

                setReplyKeyboard(message, keys);
                executeMessage(message);

                // Полностью удаляем игровую сессию
                combatService.updateSession("delete", userName);
            }

            // Был ли раунд финальным? Если нет, то статус сессии ожидаем не "block"
            else {

                // Продолжаем игру
                sendAttackDefenseChoose(chatId);
            }
        }

        // Готовы ли результаты раунда? Если нет, то статус игрока будет не "resultReady"
        else {

            // Предлагаем пользователю нажимать на кнопку, чтобы получить результаты раунда (после готовности соперника)
            message.setText("Соперник скрылся! Приготовьтесь к внезапной атаке!");

            ArrayList<String> keys = new ArrayList<>();
            keys.add(REFRESH);

            setReplyKeyboard(message, keys);
            executeMessage(message);
        }
    }

    // todo Не обрабатывать команды пользователя, если они не совпадают с типом ожидаемого события. Сообщить об этом пользователю
    private void sendActualGameMessage(long chatId, String userName) {

        var playerStatus = combatService.getPlayerStatus(userName);

        if (playerStatus.equals(P_EMPTY) || playerStatus.equals(WAIT_DEFENCE) || playerStatus.equals(WAIT_ATTACK)) {

        }
    }

    private void sendGameInvite(long chatId, String userName) {

    }

    // Удаляем пользователя из базы данных
    private void deleteData(long chatId) {

        // Если пользователь существует в бд, то спрашиваем: "Удаляем или не удаляем данные?"
        if (userRepository.findByChatId(chatId).isPresent()) {

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Вы действительно хотите удалить данные?");

            HashMap<String, String> buttons = new HashMap<>();
            buttons.put(YES, REG_YES);
            buttons.put(NO, REG_NO);
            setInlineKeyboard(message, buttons); // Прикрепляем кнопки Да/Нет

            executeMessage(message);
        }

        // Если пользователь не существует в бд, то сообщаем ему об этом
        else sendZeroRegistrationMessage(chatId);
    }

    // Флоу поиска данных юзера через бд
    private void showUserData(long chatId) {

        // Если пользователь имеется в бд, то показываем его данные
        if (userRepository.findByChatId(chatId).isPresent()) {

            var user = userRepository.findByChatId(chatId); // todo Тут читать про optional https://habr.com/ru/articles/658457/
            String answer = user.get().toString(); // todo Подумать - стоит ли добавлять проверку? Содержимое optional может быть пустым в моём случае?
            prepareAndSendMessage(chatId, answer);
        }

        // Если пользователь не существует в бд, то сообщаем ему об этом
        else sendZeroRegistrationMessage(chatId);
    }

    // todo Разработать функцию, которая принимает на вход параметры для ReplyKeyboard и отправляет сообщение
    private void sendMessageWithReplyKeyboard(long chatId, String textToSend) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ArrayList<String> keys = new ArrayList<>();
        keys.add(ONLINE);

        setReplyKeyboard(message, keys);

        executeMessage(message);
    }

    // Отправить сообщение для пользователя без регистрации
    private void sendZeroRegistrationMessage(long chatId) {

        String answer = "Вы не зарегистрированы, данные Вашего пользователя не найдены\n" +
                "Чтобы получить доступ ко всем командам, нажмите /start для повторной регистрации";
        prepareAndSendMessage(chatId, answer);
    }

    // Готовим сообщение по ChatId и Text. Выполняем отправку через executeMessage
    private void prepareAndSendMessage(long chatId, String textToSend) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    // Устанавливаем сообщению клавиатуру с двумя Inline-кнопками
    public void setInlineKeyboard(SendMessage message, HashMap<String, String> buttons) {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        for (String buttonText : buttons.keySet()) {
            String callbackData = buttons.get(buttonText);
            var newButton = new InlineKeyboardButton();
            newButton.setText(buttonText);
            newButton.setCallbackData(callbackData);
            rowInLine.add(newButton);
        }

        rowsInLine.add(rowInLine);
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);
    }

    // Устанавливаем сообщению клавиатуру с Reply-кнопками
    public void setReplyKeyboard(SendMessage message, ArrayList<String> buttons) {

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        for (String button : buttons) {
            row.add(button);
        }

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

    // Редактируем текст уже отправленного сообщения
    private void executeEditMessageText(String text, long chatId, long messageId) {

        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);

        // Тут нужно exception, так как взаимодействие с api telegram
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    // Функция осуществления/отправки текстового сообщения
    private void executeMessage(SendMessage message) {

        // Тут нужно exception, так как взаимодействие с api telegram
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    @Scheduled(cron = "*/5 * * * * *")
    private void messageSender() {

        long chatId = queueService.getAndDeleteFirstFromMessagesQueue();
        sendAttackDefenseChoose(chatId);
    }
}