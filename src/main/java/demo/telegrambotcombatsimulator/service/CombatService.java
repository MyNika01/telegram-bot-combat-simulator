package demo.telegrambotcombatsimulator.service;

import demo.telegrambotcombatsimulator.entity.Combat;
import demo.telegrambotcombatsimulator.entity.Player;
import demo.telegrambotcombatsimulator.enums.PlayerStatusType;
import demo.telegrambotcombatsimulator.enums.SessionStatusType;
import demo.telegrambotcombatsimulator.enums.DirectionStatusType;
import demo.telegrambotcombatsimulator.repository.CombatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static demo.telegrambotcombatsimulator.enums.DirectionStatusType.*;
import static demo.telegrambotcombatsimulator.enums.PlayerStatusType.*;
import static demo.telegrambotcombatsimulator.enums.SessionStatusType.*;

@Service
public class CombatService {

    @Autowired
    private CombatRepository combatRepository;

    // Определяем начальное здоровье игроков
    public static final int MAX_HP = 50;

    // Создаём сессию
    public void createSession(String playerName, String sessionFormatOrSecondPlayerName) {

        Combat combat;
        if (sessionFormatOrSecondPlayerName.equals("offline")) {

            // Создаём офлайн сессию
            combat = new Combat(playerName, "Компьютер");

        } else {

            // Создаём онлайн сессию
            // todo Продумать механизм объединения двух игроков в одну сессию
            // todo Обязательно! Проверить/предусмотреть одновременный сейв по одной записи

            combat = new Combat(playerName, sessionFormatOrSecondPlayerName);

        }
        combatRepository.save(combat);

    }

    // Обрабатываем игровое событие (action)
    public void updateSession(String action, String playerName) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        Player firstPlayer = combat.getFirstPlayer();
        Player secondPlayer = combat.getSecondPlayer();

        // Player activePlayer = combat.getPlayerByName(playerName);

        var firstPlayerName = firstPlayer.getName();
        var secondPlayerName = secondPlayer.getName();
        var firstPlayerStatus = firstPlayer.getStatus();
        var secondPlayerStatus = secondPlayer.getStatus();

        if (!combat.getSessionStatus().equals(S_BLOCK)) {

          //  Player activePlayer = combat.getPlayerByName(playerName);

            // В зависимости от события меняем статусы в сессии
            switch (action) {

                // Меняем статус игрока на "Ожидаем выбор направления атаки"
                case "Атака" -> {
                    if (playerName.equals(firstPlayerName)) {
                        firstPlayer.setStatus(WAIT_ATTACK);
                    } else if (playerName.equals(secondPlayerName)) {
                        secondPlayer.setStatus(WAIT_ATTACK);
                    }
                }

                // Меняем статус игрока на "Ожидаем выбор направления защиты"
                case "Защита" -> {
                    if (playerName.equals(firstPlayerName)) {
                        firstPlayer.setStatus(WAIT_DEFENCE);
                    } else if (playerName.equals(secondPlayerName)) {
                        secondPlayer.setStatus(WAIT_DEFENCE);
                    }
                }

                // Указываем направление на Голову для Атаки/Защиты (зависит от последнего значения статуса игрока)
                case "Голова" -> {
                    if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals(WAIT_ATTACK)) {
                        firstPlayer.setAttackDirection(HEAD);
                        firstPlayer.setStatus(P_EMPTY);
                    } else if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals(WAIT_DEFENCE)) {
                        firstPlayer.setDefenseDirection(HEAD);
                        firstPlayer.setStatus(P_EMPTY);
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals(WAIT_ATTACK)) {
                        secondPlayer.setAttackDirection(HEAD);
                        secondPlayer.setStatus(P_EMPTY);
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals(WAIT_DEFENCE)) {
                        secondPlayer.setDefenseDirection(HEAD);
                        secondPlayer.setStatus(P_EMPTY);
                    }
                }

                // Указываем направление на Тело для Атаки/Защиты (зависит от последнего значения статуса игрока)
                case "Туловище" -> {
                    if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals(WAIT_ATTACK)) {
                        firstPlayer.setAttackDirection(BODY);
                        firstPlayer.setStatus(P_EMPTY);
                    } else if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals(WAIT_DEFENCE)) {
                        firstPlayer.setDefenseDirection(BODY);
                        firstPlayer.setStatus(P_EMPTY);
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals(WAIT_ATTACK)) {
                        secondPlayer.setAttackDirection(BODY);
                        secondPlayer.setStatus(P_EMPTY);
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals(WAIT_DEFENCE)) {
                        secondPlayer.setDefenseDirection(BODY);
                        secondPlayer.setStatus(P_EMPTY);
                    }
                }

                // Указываем направление на Ноги для Атаки/Защиты (зависит от последнего значения статуса игрока)
                case "Ноги" -> {
                    if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals(WAIT_ATTACK)) {

                        firstPlayer.setAttackDirection(LEGS);
                        firstPlayer.setStatus(P_EMPTY);
                    } else if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals(WAIT_DEFENCE)) {
                        firstPlayer.setDefenseDirection(LEGS);
                        firstPlayer.setStatus(P_EMPTY);
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals(WAIT_ATTACK)) {
                        secondPlayer.setAttackDirection(LEGS);
                        secondPlayer.setStatus(P_EMPTY);
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals(WAIT_DEFENCE)) {
                        secondPlayer.setDefenseDirection(LEGS);
                        secondPlayer.setStatus(P_EMPTY);
                    }
                }

                // Блокируем статус игрока, так как он сообщил о своей готовности. В этом случае нельзя менять выбор направлений!
                case "block" -> {
                    if (playerName.equals(firstPlayerName)) {
                        firstPlayer.setStatus(P_BLOCK);
                    } else if (playerName.equals(secondPlayerName)) {
                        secondPlayer.setStatus(P_BLOCK);
                    }
                }
            }

            // Если пользователь сделал выбор Атаки и Защиты, то ставим статус ready
            // Этот статус позволяет определить, что пользователю нужно отправить кнопку Готов
            if ((firstPlayer.getAttackDirection().equals(HEAD) ||
                    firstPlayer.getAttackDirection().equals(BODY) ||
                    firstPlayer.getAttackDirection().equals(LEGS)) &&
                    (firstPlayer.getDefenseDirection().equals(HEAD) ||
                            firstPlayer.getDefenseDirection().equals(BODY) ||
                            firstPlayer.getDefenseDirection().equals(LEGS)) &&
                    firstPlayer.getStatus().equals(P_EMPTY)
            ) {
                firstPlayer.setStatus(P_READY);
            }

            // Аналогичная проверка для второго игрока
            if ((secondPlayer.getAttackDirection().equals(HEAD) ||
                    secondPlayer.getAttackDirection().equals(BODY) ||
                    secondPlayer.getAttackDirection().equals(LEGS)) &&
                    (secondPlayer.getDefenseDirection().equals(HEAD) ||
                            secondPlayer.getDefenseDirection().equals(BODY) ||
                            secondPlayer.getDefenseDirection().equals(LEGS)) &&
                    secondPlayer.getStatus().equals(P_EMPTY)
            ) {
                secondPlayer.setStatus(P_READY);
            }

            // Если противник - Компьютер, то генерируем направления и ставим статус второму игроку
            if (secondPlayerName.equals("Компьютер") && secondPlayer.getStatus().equals(P_EMPTY)) {
                generateAndSetComputerDirection(secondPlayer);
            }

            // Если противник - Компьютер, то генерируем направления и ставим статус второму игроку
            if (firstPlayer.getStatus().equals(P_BLOCK) && secondPlayer.getStatus().equals(P_BLOCK)) {
                combat.setSessionStatus(DO_ACTION);
            }

            combat.setFirstPlayer(firstPlayer);
            combat.setSecondPlayer(secondPlayer);

            combatRepository.save(combat);
        }

        // Если получили событие "Удалить сессию"
        else if (action.equals("delete")) {

            combatRepository.deleteByPlayerName(playerName);
        }
    }

    // Через случайный выбор определяем игровые действия компьютера и обновляем статус второго игрока
    public Player generateAndSetComputerDirection(Player player) {

        // todo Протестировать рандом!
        int max = 3, min = 1;
        int attackDirection = getRandomInt(max, min);
        int defenseDirection = getRandomInt(max, min);

        switch (attackDirection) {
            case 1 -> player.setAttackDirection(HEAD);
            case 2 -> player.setAttackDirection(BODY);
            case 3 -> player.setAttackDirection(LEGS);
        }

        switch (defenseDirection) {
            case 1 -> player.setDefenseDirection(HEAD);
            case 2 -> player.setDefenseDirection(BODY);
            case 3 -> player.setDefenseDirection(LEGS);
        }

        player.setStatus(P_BLOCK);
        return player;
    }

    protected int getRandomInt(int max, int min) {
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

        // Важно определить какой из игроков попал сюда первым, потому что
        // этот игрок сразу получит результат раунда, а второму
        // игроку нужно будет забрать результаты после подсчёта
        // Поэтому задаём оставшемуся игроку статус "Забери результат"
        if (firstPlayerName.equals(playerName)) {
            firstPlayer.setStatus(P_EMPTY);
            secondPlayer.setStatus(RESULT_READY);
        } else {
            secondPlayer.setStatus(P_EMPTY);
            firstPlayer.setStatus(RESULT_READY);
        }

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

        // Обнуляем направления
        firstPlayer.setAttackDirection(D_EMPTY);
        firstPlayer.setDefenseDirection(D_EMPTY);
        secondPlayer.setAttackDirection(D_EMPTY);
        secondPlayer.setDefenseDirection(D_EMPTY);

        // Формируем сообщение с результатом для первого игрока
        firstPlayer.setMessage(
                "Соперник нанёс удар в " + transToRus(spAttack) + ", а вы поставили защиту на " + transToRus(fpDefense) + "\n" +
                        "Ваше здоровье " + fpHealth + "/" + MAX_HP + "\n" +
                        "Вы нанесли удар в " + transToRus(fpAttack) + ", а соперник поставил защиту на " + transToRus(spDefense) + "\n" +
                        "Здоровье соперника " + spHealth + "/" + MAX_HP);

        // Формируем сообщение с результатом для второго игрока
        secondPlayer.setMessage(
                "Соперник нанёс удар в " + transToRus(fpAttack) + ", а вы поставили защиту на " + transToRus(spDefense) + "\n" +
                        "Ваше здоровье " + spHealth + "/" + MAX_HP + "\n" +
                        "Вы нанесли удар в " + transToRus(spAttack) + ", а соперник поставил защиту на " + transToRus(fpDefense) + "\n" +
                        "Здоровье соперника " + fpHealth + "/" + MAX_HP);

        // Возвращаем статус сессии на Подготовка
        combat.setSessionStatus(ACTION_PREPARE);

        // Проверяем закончился ли раунд поражением хотя бы одного игрока
        if (fpHealth == 0 || spHealth == 0) {

            // Блокируем изменения в сессии для игрока
            combat.setSessionStatus(S_BLOCK);

            // Удаляем игрока из сессии, так как он больше не сможет в ней принимать участие
            if (firstPlayerName.equals(playerName)) {
                firstPlayer.setName("deleted");
            } else {
                secondPlayer.setName("deleted");
            }

            // Формируем финальное сообщение в зависимости от результата
            if (fpHealth == 0 && spHealth != 0) {

                firstPlayer.setMessage(firstPlayer.getMessage() + "\n" +
                        "Ваше сражение окончено - соперник одержал победу");

                secondPlayer.setMessage(secondPlayer.getMessage() + "\n" +
                        "Ваше сражение окончено - соперник разгромлен!");
            }

            // Формируем финальное сообщение в зависимости от результата
            if (spHealth == 0 && fpHealth != 0) {

                secondPlayer.setMessage(secondPlayer.getMessage() + "\n" +
                        "Ваше сражение окончено - соперник одержал победу");

                firstPlayer.setMessage(firstPlayer.getMessage() + "\n" +
                        "Ваше сражение окончено - соперник разгромлен!");
            }

            // Формируем финальное сообщение в зависимости от результата
            if (spHealth == 0 && fpHealth == 0) {

                firstPlayer.setMessage(firstPlayer.getMessage() + "\n" +
                        "Ваше сражение окончено - никого не осталось в живых");

                secondPlayer.setMessage(secondPlayer.getMessage() + "\n" +
                        "Ваше сражение окончено - никого не осталось в живых");
            }
        }

        combat.setFirstPlayer(firstPlayer);
        combat.setSecondPlayer(secondPlayer);

        // Если в сессии остался только компьютер, то удаляем такую сессию
        if (secondPlayer.getName().equals("Компьютер") && firstPlayer.getName().equals("deleted"))
            combatRepository.deleteByPlayerName(playerName);
        else combatRepository.save(combat);

        // В зависимости от игрока возвращаем нужное сообщение с результатом
        if (firstPlayer.getName().equals(playerName) || firstPlayer.getName().equals("deleted")) {
            return firstPlayer.getMessage();
        } else {
            return secondPlayer.getMessage();
        }
    }

    // Получить статус сессии
    public SessionStatusType getSessionStatus(String playerName) {
        return combatRepository.getByPlayerName(playerName).getSessionStatus();
    }

    // Получить статус игрока
    public PlayerStatusType getPlayerStatus(String playerName) {
        if (combatRepository.getByPlayerName(playerName).getFirstPlayer().getName().equals(playerName))
            return combatRepository.getByPlayerName(playerName).getFirstPlayer().getStatus();
        else
            return combatRepository.getByPlayerName(playerName).getSecondPlayer().getStatus();
    }

    // Получить сообщение с результатом раунда и обнулить статус игрока
    public String getPlayerMessageAndRefreshStatus(String playerName) {

        setPlayerStatus(playerName, P_EMPTY);

        if (combatRepository.getByPlayerName(playerName).getFirstPlayer().getName().equals(playerName))
            return combatRepository.getByPlayerName(playerName).getFirstPlayer().getMessage();
        else
            return combatRepository.getByPlayerName(playerName).getSecondPlayer().getMessage();
    }

    // Поставить статус игроку
    public void setPlayerStatus(String playerName, PlayerStatusType status) {

        Combat combat = combatRepository.getByPlayerName(playerName);
        Player firstPlayer = combat.getFirstPlayer();
        Player secondPlayer = combat.getSecondPlayer();

        if (firstPlayer.getName().equals(playerName))
            firstPlayer.setStatus(status);
        else
            secondPlayer.setStatus(status);

        combatRepository.save(combat);
    }

    // Перевести ключевые слова с английского на русский
    private String transToRus(DirectionStatusType engWord) {
        switch (engWord) {
            case HEAD -> {
                return "голову";
            }
            case BODY -> {
                return "тело";
            }
            case LEGS -> {
                return "ноги";
            }
            default -> {
                return "";
            }
        }
    }

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
        Player firstPlayer = combat.getFirstPlayer();
        Player secondPlayer = combat.getSecondPlayer();

        if (firstPlayer.getName().equals(playerName)) {

            return "Ваш последний выбор: атака - в " + transToRus(firstPlayer.getAttackDirection()) +
                    ", защита - на " + transToRus(firstPlayer.getDefenseDirection());
        } else {

            return "Ваш последний выбор: атака - в " + transToRus(secondPlayer.getAttackDirection()) +
                    ", защита - на " + transToRus(secondPlayer.getDefenseDirection());
        }
    }
}