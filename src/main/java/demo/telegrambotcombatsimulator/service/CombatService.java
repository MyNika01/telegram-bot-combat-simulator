package demo.telegrambotcombatsimulator.service;

import demo.telegrambotcombatsimulator.entity.Combat;
import demo.telegrambotcombatsimulator.entity.Player;
import demo.telegrambotcombatsimulator.enums.DirectionStatusType;
import demo.telegrambotcombatsimulator.enums.PlayerStatusType;
import demo.telegrambotcombatsimulator.enums.SessionStatusType;
import demo.telegrambotcombatsimulator.repository.CombatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static demo.telegrambotcombatsimulator.enums.DirectionStatusType.*;
import static demo.telegrambotcombatsimulator.enums.PlayerStatusType.*;
import static demo.telegrambotcombatsimulator.enums.SessionStatusType.*;
import static demo.telegrambotcombatsimulator.service.TelegramBot.ATTACK;
import static demo.telegrambotcombatsimulator.service.TelegramBot.DEFENSE;

@Service
public class CombatService {

    @Autowired
    private CombatRepository combatRepository;

    // Определяем начальное здоровье игроков
    public static final int MAX_HP = 50;

    // Создаём сессию. На вход может прийти playerName+sessionFormat или playerName+SecondPlayerName
    public void createSession(String playerName, String sessionFormatOrSecondPlayerName) {

        Combat combat;
        if (sessionFormatOrSecondPlayerName.equals("offline")) {

            // Создаём офлайн сессию для одного игрока и компьютера
            // todo вынести "Компьютер" в константу
            combat = new Combat(playerName, "Компьютер");

        } else {

            // Создаём онлайн сессию из двух игроков
            combat = new Combat(playerName, sessionFormatOrSecondPlayerName);

        }
        combatRepository.save(combat);

    }

    // Обрабатываем игровое событие action от игрока playerName
    public void updateSession(String action, String playerName) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        var firstPlayer = combat.getFirstPlayer();
        var secondPlayer = combat.getSecondPlayer();

        // определяем игрока, который прислал событие
        var activePlayer = playerName.equals(firstPlayer.getName()) ? firstPlayer : secondPlayer;
        var activePlayerStatus = activePlayer.getStatus();

        // проваливаемся сюда, если сессия не была заблокирована
        if (!combat.getSessionStatus().equals(S_BLOCK)) {

            // В зависимости от события меняем статусы в сессии
            switch (action) {

                // Меняем статус игрока на "Ожидаем выбор направления атаки"
                case ATTACK -> activePlayer.setStatus(WAIT_ATTACK);

                // Меняем статус игрока на "Ожидаем выбор направления защиты"
                case DEFENSE -> activePlayer.setStatus(WAIT_DEFENCE);

                // Указываем направление на Голову для Атаки/Защиты
                // Указываем направление на Корпус для Атаки/Защиты
                // Указываем направление на Ноги для Атаки/Защиты
                case TelegramBot.HEAD, TelegramBot.LEGS, TelegramBot.BODY -> {

                    if (activePlayerStatus.equals(WAIT_ATTACK)) {
                        activePlayer.setAttackDirection(stringToDirectionStatusType(action));
                    } else if (activePlayerStatus.equals(WAIT_DEFENCE)) {
                        activePlayer.setDefenseDirection(stringToDirectionStatusType(action));
                    }
                    activePlayer.setStatus(P_EMPTY);
                }

                // Блокируем статус игрока, так как он сообщил о своей готовности. В этом случае нельзя менять выбор направлений!
                case "block" -> activePlayer.setStatus(P_BLOCK); // todo Проверить, что это где-то используется, кроме перевода в DO_ACTION
            }

            // Если у игрока выбраны направления, то делаем ему статус P_READY. Это означается, что игрок получит кнопку подтверждения готовности
            if ((activePlayer.getAttackDirection().equals(HEAD) || activePlayer.getAttackDirection().equals(BODY) || activePlayer.getAttackDirection().equals(LEGS)) &&
                    (activePlayer.getDefenseDirection().equals(HEAD) || activePlayer.getDefenseDirection().equals(BODY) || activePlayer.getDefenseDirection().equals(LEGS)) &&
                    activePlayer.getStatus().equals(P_EMPTY)
            ) activePlayer.setStatus(P_READY);

            // Если противник - Компьютер, то генерируем направления и ставим статус второму игроку
            if (secondPlayer.getName().equals("Компьютер") && secondPlayer.getStatus().equals(P_EMPTY)) {
                generateAndSetComputerDirection(secondPlayer);
            }

            // Если оба игрока готовы, то переводим сессию в DO_ACTION. Означает, что можно провести подсчёт хода
            if (firstPlayer.getStatus().equals(P_BLOCK) && secondPlayer.getStatus().equals(P_BLOCK)) {
                combat.setSessionStatus(DO_ACTION);
            }

            combatRepository.save(combat);
        }

        // Если получили событие "Удалить сессию"
        else if (action.equals("delete")) {

            combatRepository.deleteByPlayerName(playerName);
        }
    }

    // Рандомом определяем выбор атаки/защиты и обновляем статус игрока (это всегда компьютер)
    public void generateAndSetComputerDirection(Player player) {
        // todo Протестировать рандом!
        int max = 3, min = 1;
        player.setAttackDirection(DirectionStatusType.getByOrder(getRandomInt(min, max)));
        player.setDefenseDirection(DirectionStatusType.getByOrder(getRandomInt(min, max)));
        player.setStatus(P_BLOCK);
    }

    // Функция рандома
    public int getRandomInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    // Подсчитываем результаты раунда
    // todo Вынести часто используемые параметры в начало
    public String combatResult(String playerName) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        Player firstPlayer = combat.getFirstPlayer();
        Player secondPlayer = combat.getSecondPlayer();

        var firstPlayerName = firstPlayer.getName();
        var secondPlayerName = secondPlayer.getName();

        // Важно определить какой из игроков попал сюда, потому что
        // этот игрок сразу получит результат раунда, а второму
        // игроку нужно будет забрать результаты после подсчёта
        // Поэтому задаём оставшемуся игроку статус "Забери результат"
        var activePlayer = playerName.equals(firstPlayerName) ? firstPlayer : secondPlayer;
        var passivePlayer = playerName.equals(firstPlayerName) ? secondPlayer : firstPlayer;
        activePlayer.setStatus(P_EMPTY);
        passivePlayer.setStatus(RESULT_READY);

        // Если второй игрок компьютер, то считаем, что он уже забрал свой результат
        // todo Можно подумать - вынести ненужные операции, если второй игрок Компьютер
        if (secondPlayerName.equals("Компьютер")) {
            secondPlayer.setStatus(P_EMPTY);
        }

        var fpAttack = firstPlayer.getAttackDirection();
        var fpDefense = firstPlayer.getDefenseDirection();
        var spAttack = secondPlayer.getAttackDirection();
        var spDefense = secondPlayer.getDefenseDirection();

        // Наносим урон игроку, если по нему попал соперник
        if (!fpAttack.equals(spDefense)) secondPlayer.setHealth(secondPlayer.getHealth() - 10);
        if (!spAttack.equals(fpDefense)) firstPlayer.setHealth(firstPlayer.getHealth() - 10);

        var fpHealth = firstPlayer.getHealth();
        var spHealth = secondPlayer.getHealth();

        // Формируем сообщение с результатом для первого игрока
        firstPlayer.setMessageAsBattleResult(spAttack, fpDefense, fpAttack, spDefense, fpHealth, spHealth);

        // Формируем сообщение с результатом для второго игрока
        secondPlayer.setMessageAsBattleResult(fpAttack, spDefense, spAttack, fpDefense, spHealth, fpHealth);

        // Обнуляем направления
        firstPlayer.setAttackDirection(D_EMPTY);
        firstPlayer.setDefenseDirection(D_EMPTY);
        secondPlayer.setAttackDirection(D_EMPTY);
        secondPlayer.setDefenseDirection(D_EMPTY);

        // Возвращаем статус сессии на Подготовка
        combat.setSessionStatus(ACTION_PREPARE);

        // Проверяем закончился ли раунд поражением хотя бы одного игрока
        if (fpHealth == 0 || spHealth == 0) {

            // Блокируем изменения в сессии для игрока
            combat.setSessionStatus(S_BLOCK);

            // Удаляем игрока из сессии, так как он больше не сможет в ней принимать участие
            activePlayer.setName("deleted");

            // Формируем финальное сообщение в зависимости от результата
            if (fpHealth == 0 && spHealth != 0) {

                firstPlayer.setMessage(firstPlayer.getMessage() + "\n\n" +
                        "‼\uFE0FВаше сражение окончено - соперник одержал победу‼\uFE0F");

                secondPlayer.setMessage(secondPlayer.getMessage() + "\n\n" +
                        "‼\uFE0FВаше сражение окончено - соперник разгромлен‼\uFE0F");
            }

            // Формируем финальное сообщение в зависимости от результата
            if (spHealth == 0 && fpHealth != 0) {

                firstPlayer.setMessage(firstPlayer.getMessage() + "\n\n" +
                        "‼\uFE0FВаше сражение окончено - соперник разгромлен‼\uFE0F");

                secondPlayer.setMessage(secondPlayer.getMessage() + "\n\n" +
                        "‼\uFE0FВаше сражение окончено - соперник одержал победу‼\uFE0F");
            }

            // Формируем финальное сообщение в зависимости от результата
            if (spHealth == 0 && fpHealth == 0) {

                firstPlayer.setMessage(firstPlayer.getMessage() + "\n\n" +
                        "‼\uFE0FВаше сражение окончено - никого не осталось в живых‼\uFE0F");

                secondPlayer.setMessage(secondPlayer.getMessage() + "\n\n" +
                        "‼\uFE0FВаше сражение окончено - никого не осталось в живых‼\uFE0F");
            }
        }

        // Если в сессии остался только компьютер, то удаляем такую сессию
        if (secondPlayer.getName().equals("Компьютер") && firstPlayer.getName().equals("deleted"))
            combatRepository.deleteByPlayerName(playerName);
        else combatRepository.save(combat);

        // В зависимости от игрока возвращаем нужное сообщение с результатом
        return activePlayer.getMessage();

    }

    // Получить статус сессии
    public SessionStatusType getSessionStatus(String playerName) {
        return combatRepository.getByPlayerName(playerName).getSessionStatus();
    }

    // Получить статус игрока
    public PlayerStatusType getPlayerStatus(String playerName) {

        Combat combat = combatRepository.getByPlayerName(playerName);
        var activePlayer = combat.getFirstPlayer().getName().equals(playerName) ? combat.getFirstPlayer() : combat.getSecondPlayer();

        return activePlayer.getStatus();
    }

    // Получить сообщение с результатом раунда и обнулить статус игрока
    public String getPlayerMessageAndRefreshStatus(String playerName) {

        Combat combat = combatRepository.getByPlayerName(playerName);
        var activePlayer = combat.getFirstPlayer().getName().equals(playerName) ? combat.getFirstPlayer() : combat.getSecondPlayer();

        activePlayer.setStatus(P_EMPTY);
        combatRepository.save(combat);

        return activePlayer.getMessage();
    }

    // Поставить статус игроку
    /*
    public Combat setPlayerStatus(String playerName, PlayerStatusType status) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        var activePlayer = playerName.equals(combat.getFirstPlayer().getName()) ? combat.getFirstPlayer() : combat.getSecondPlayer();
        activePlayer.setStatus(status);

        return combatRepository.save(combat);
    }
    */

    // Проверяем существует ли активная сессия у пользователя
    public boolean hasUserActiveSession(String playerName) {
        return combatRepository.findByPlayerName(playerName).isPresent();
    }

    // Удаляем сессию пользователя
    public void deleteUserSession(String playerName) {
        combatRepository.deleteByPlayerName(playerName);
    }

    // Отдаём результат последнего раунда пользователю
    public String getLastPlayerChoose(String playerName) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        var activePlayer = playerName.equals(combat.getFirstPlayer().getName()) ? combat.getFirstPlayer() : combat.getSecondPlayer();

        return "Ваш последний выбор: ⚔->" + activePlayer.getAttackDirection().toEmoji() +
                ", \uD83D\uDEE1->" + activePlayer.getDefenseDirection().toEmoji();
    }

    // Переводим направление атаки/защиты из строки в enum
    public DirectionStatusType stringToDirectionStatusType (String s) {

        if (s.equals(TelegramBot.HEAD)) return HEAD;
        else if (s.equals(TelegramBot.BODY)) return BODY;
        else return LEGS;

    }
}