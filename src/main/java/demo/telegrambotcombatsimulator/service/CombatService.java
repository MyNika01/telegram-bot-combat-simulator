package demo.telegrambotcombatsimulator.service;

import demo.telegrambotcombatsimulator.entity.Combat;
import demo.telegrambotcombatsimulator.repository.CombatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CombatService {

    @Autowired
    private CombatRepository combatRepository;

    // Определяем начальное здоровье игроков
    final int MAX_HP = 50;

    // Создаём офлайн сессию
    public void createOfflineSession(String playerName) {

        Combat combat = new Combat();

        // todo Часть из этого можно вынести в конструктор combat
        combat.setFirstPlayerName(playerName);
        combat.setSecondPlayerName("Компьютер");
        combat.setFirstPlayerHealth(MAX_HP);
        combat.setSecondPlayerHealth(MAX_HP);
        combat.setFirstPlayerAttackDirection("empty");
        combat.setSecondPlayerAttackDirection("empty");
        combat.setFirstPlayerDefenseDirection("empty");
        combat.setSecondPlayerDefenseDirection("empty");
        combat.setFirstPlayerStatus("empty");
        combat.setSecondPlayerStatus("empty");
        combat.setFirstPlayerMessage("empty");
        combat.setSecondPlayerMessage("empty");
        combat.setSessionStatus("actionPrepare");

        combatRepository.save(combat);
    }

    // Создаём онлайн сессию
    public void createOnlineSession(String firstPlayerName) {

        // todo Продумать механизм объединения двух игроков в одну сессию
        // todo Обязательно! Проверить/предусмотреть одновременный сейв по одной записи

        Combat combat = new Combat();

        combat.setFirstPlayerName(firstPlayerName);
        combat.setSecondPlayerName("Неизвестный соперник");
        combat.setFirstPlayerHealth(MAX_HP);
        combat.setSecondPlayerHealth(MAX_HP);
        combat.setFirstPlayerAttackDirection("empty");
        combat.setSecondPlayerAttackDirection("empty");
        combat.setFirstPlayerDefenseDirection("empty");
        combat.setSecondPlayerDefenseDirection("empty");
        combat.setFirstPlayerStatus("empty");
        combat.setSecondPlayerStatus("empty");
        combat.setSessionStatus("opponentSearchStage");

        combatRepository.save(combat);
    }

    // Обрабатываем игровое событие (action)
    public void updateSession(String action, String playerName) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        var firstPlayerName = combat.getFirstPlayerName();
        var secondPlayerName = combat.getSecondPlayerName();
        var firstPlayerStatus = combat.getFirstPlayerStatus();
        var secondPlayerStatus = combat.getSecondPlayerStatus();


        if (!combat.getSessionStatus().equals("block")) {

            // В зависимости от события меняем статусы в сессии
            switch (action) {

                // Меняем статус игрока на "Ожидаем выбор направления атаки"
                case "Атака" -> {
                    if (playerName.equals(firstPlayerName)) {
                        combat.setFirstPlayerStatus("waitAttack");
                        break;
                    } else if (playerName.equals(secondPlayerName)) {
                        combat.setSecondPlayerStatus("waitAttack");
                        break;
                    }
                }

                // Меняем статус игрока на "Ожидаем выбор направления защиты"
                case "Защита" -> {
                    if (playerName.equals(firstPlayerName)) {
                        combat.setFirstPlayerStatus("waitDefense");
                        break;
                    } else if (playerName.equals(secondPlayerName)) {
                        combat.setSecondPlayerStatus("waitDefense");
                        break;
                    }
                }

                // Указываем направление на Голову для Атаки/Защиты (зависит от последнего значения статуса игрока)
                case "Голова" -> {
                    if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals("waitAttack")) {
                        combat.setFirstPlayerAttackDirection("head");
                        combat.setFirstPlayerStatus("empty");
                        break;
                    } else if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals("waitDefense")) {
                        combat.setFirstPlayerDefenseDirection("head");
                        combat.setFirstPlayerStatus("empty");
                        break;
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals("waitAttack")) {
                        combat.setSecondPlayerAttackDirection("head");
                        combat.setSecondPlayerStatus("empty");
                        break;
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals("waitDefense")) {
                        combat.setSecondPlayerDefenseDirection("head");
                        combat.setSecondPlayerStatus("empty");
                        break;
                    }
                }

                // Указываем направление на Тело для Атаки/Защиты (зависит от последнего значения статуса игрока)
                case "Туловище" -> {
                    if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals("waitAttack")) {
                        combat.setFirstPlayerAttackDirection("body");
                        combat.setFirstPlayerStatus("empty");
                        break;
                    } else if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals("waitDefense")) {
                        combat.setFirstPlayerDefenseDirection("body");
                        combat.setFirstPlayerStatus("empty");
                        break;
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals("waitAttack")) {
                        combat.setSecondPlayerAttackDirection("body");
                        combat.setSecondPlayerStatus("empty");
                        break;
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals("waitDefense")) {
                        combat.setSecondPlayerDefenseDirection("body");
                        combat.setSecondPlayerStatus("empty");
                        break;
                    }
                }

                // Указываем направление на Ноги для Атаки/Защиты (зависит от последнего значения статуса игрока)
                case "Ноги" -> {
                    if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals("waitAttack")) {
                        combat.setFirstPlayerAttackDirection("legs");
                        combat.setFirstPlayerStatus("empty");
                        break;
                    } else if (playerName.equals(firstPlayerName) && firstPlayerStatus.equals("waitDefense")) {
                        combat.setFirstPlayerDefenseDirection("legs");
                        combat.setFirstPlayerStatus("empty");
                        break;
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals("waitAttack")) {
                        combat.setSecondPlayerAttackDirection("legs");
                        combat.setSecondPlayerStatus("empty");
                        break;
                    } else if (playerName.equals(secondPlayerName) && secondPlayerStatus.equals("waitDefense")) {
                        combat.setSecondPlayerDefenseDirection("legs");
                        combat.setSecondPlayerStatus("empty");
                        break;
                    }
                }

                // Блокируем статус игрока, так как он сообщил о своей готовности. В этом случае нельзя менять выбор направлений!
                case "block" -> {
                    if (playerName.equals(firstPlayerName)) {
                        combat.setFirstPlayerStatus("block");
                        break;
                    } else if (playerName.equals(secondPlayerName)) {
                        combat.setSecondPlayerStatus("block");
                        break;
                    }
                }
            }

            // Если пользователь сделал выбор Атаки и Защиты, то ставим статус ready
            // Этот статус позволяет определить, что пользователю нужно отправить кнопку Готов
            if ((combat.getFirstPlayerAttackDirection().equals("head") ||
                    combat.getFirstPlayerAttackDirection().equals("body") ||
                    combat.getFirstPlayerAttackDirection().equals("legs")) &&
                    (combat.getFirstPlayerDefenseDirection().equals("head") ||
                            combat.getFirstPlayerDefenseDirection().equals("body") ||
                            combat.getFirstPlayerDefenseDirection().equals("legs")) &&
                    combat.getFirstPlayerStatus().equals("empty")
            ) {
                combat.setFirstPlayerStatus("ready");
            }

            // Аналогичная проверка для второго игрока
            if ((combat.getSecondPlayerAttackDirection().equals("head") ||
                    combat.getSecondPlayerAttackDirection().equals("body") ||
                    combat.getSecondPlayerAttackDirection().equals("legs")) &&
                    (combat.getSecondPlayerDefenseDirection().equals("head") ||
                            combat.getSecondPlayerDefenseDirection().equals("body") ||
                            combat.getSecondPlayerDefenseDirection().equals("legs")) &&
                    combat.getSecondPlayerStatus().equals("empty")
            ) {
                combat.setSecondPlayerStatus("ready");
            }

            // Если противник - Компьютер, то генерируем направления и ставим статус второму игроку
            if (secondPlayerName.equals("Компьютер") && combat.getSecondPlayerStatus().equals("empty")) {
                generateAndSetComputerDirection(combat);
            }

            // Если противник - Компьютер, то генерируем направления и ставим статус второму игроку
            if (combat.getFirstPlayerStatus().equals("block") && combat.getSecondPlayerStatus().equals("block")) {
                combat.setSessionStatus("doAction");
            }

            combatRepository.save(combat);
        }

        // Если получили событие "Удалить сессию"
        else if (action.equals("delete")) {

            combatRepository.deleteByPlayerName(playerName);
        }
    }

    // Через случайный выбор определяем игровые действия компьютера и обновляем статус второго игрока
    public void generateAndSetComputerDirection(Combat combat) {

        // todo Протестировать рандом!
        int max = 3, min = 1;
        int attackDirection = (int) ((Math.random() * (max - min)) + min);
        int defenseDirection = (int) ((Math.random() * (max - min)) + min);

        switch (attackDirection) {
            case 1 -> combat.setSecondPlayerAttackDirection("head");
            case 2 -> combat.setSecondPlayerAttackDirection("body");
            case 3 -> combat.setSecondPlayerAttackDirection("legs");
        }

        switch (defenseDirection) {
            case 1 -> combat.setSecondPlayerDefenseDirection("head");
            case 2 -> combat.setSecondPlayerDefenseDirection("body");
            case 3 -> combat.setSecondPlayerDefenseDirection("legs");
        }

        combat.setSecondPlayerStatus("block");
    }

    // Подсчитываем результаты раунда
    // todo Вынести часто используемые параметры в начало
    public String combatResult (String playerName) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        // Важно определить какой из игроков попал сюда первым, потому что
        // этот игрок сразу получит результат раунда, а второму
        // игроку нужно будет забрать результаты после подсчёта
        // Поэтому задаём оставшемуся игроку статус "Забери результат"
        if (combat.getFirstPlayerName().equals(playerName)) {
            combat.setFirstPlayerStatus("empty");
            combat.setSecondPlayerStatus("resultReady");
        } else {
            combat.setSecondPlayerStatus("empty");
            combat.setFirstPlayerStatus("resultReady");
        }

        // Если второй игрок компьютер, то считаем, что он уже забрал свой результат
        // todo Можно подумать - вынести ненужные операции, если второй игрок Компьютер
        if (combat.getSecondPlayerName().equals("Компьютер")) {
            combat.setSecondPlayerStatus("empty");
        }

        String fpAttack = combat.getFirstPlayerAttackDirection();
        String fpDefense = combat.getFirstPlayerDefenseDirection();
        String spAttack = combat.getSecondPlayerAttackDirection();
        String spDefense = combat.getSecondPlayerDefenseDirection();

        // Наносим урон игроку, если по нему попал соперник
        if (!fpAttack.equals(spDefense)) combat.setSecondPlayerHealth(combat.getSecondPlayerHealth() - 10);
        if (!spAttack.equals(fpDefense)) combat.setFirstPlayerHealth(combat.getFirstPlayerHealth() - 10);

        // Обнуляем направления
        combat.setFirstPlayerAttackDirection("empty");
        combat.setFirstPlayerDefenseDirection("empty");
        combat.setSecondPlayerAttackDirection("empty");
        combat.setSecondPlayerDefenseDirection("empty");

        // Формируем сообщение с результатом для первого игрока
        combat.setFirstPlayerMessage(
                "Соперник нанёс удар в "+transToRus(spAttack)+", а вы поставили защиту на "+transToRus(fpDefense)+"\n"+
                        "Ваше здоровье "+combat.getFirstPlayerHealth()+"/50\n"+
                        "Вы нанесли удар в "+transToRus(fpAttack)+", а соперник поставил защиту на "+transToRus(spDefense)+"\n"+
                        "Здоровье соперника "+combat.getSecondPlayerHealth()+"/50");

        // Формируем сообщение с результатом для второго игрока
        combat.setSecondPlayerMessage(
                "Соперник нанёс удар в "+transToRus(fpAttack)+", а вы поставили защиту на "+transToRus(spDefense)+"\n"+
                        "Ваше здоровье "+combat.getSecondPlayerHealth()+"/50\n"+
                        "Вы нанесли удар в "+transToRus(spAttack)+", а соперник поставил защиту на "+transToRus(fpDefense)+"\n"+
                        "Здоровье соперника "+combat.getFirstPlayerHealth()+"/50");

        // Возвращаем статус сессии на Подготовка
        combat.setSessionStatus("actionPrepare");

        // Проверяем закончился ли раунд поражением хотя бы одного игрока
        if (combat.getFirstPlayerHealth() == 0 || combat.getSecondPlayerHealth() == 0) {

            // Блокируем изменения в сессии для игрока
            combat.setSessionStatus("block");

            // Удаляем игрока из сессии, так как он больше не сможет в ней принимать участие
            if (combat.getFirstPlayerName().equals(playerName)) {
                combat.setFirstPlayerName("deleted");
            } else {
                combat.setSecondPlayerName("deleted");
            }

            // Формируем финальное сообщение в зависимости от результата
            if (combat.getFirstPlayerHealth() == 0 && combat.getSecondPlayerHealth() != 0) {

                combat.setFirstPlayerMessage(combat.getFirstPlayerMessage() + "\n" +
                        "Ваше сражение окончено - соперник одержал победу");

                combat.setSecondPlayerMessage(combat.getSecondPlayerMessage() + "\n" +
                        "Ваше сражение окончено - соперник разгромлен!");
            }

            // Формируем финальное сообщение в зависимости от результата
            if (combat.getSecondPlayerHealth() == 0 && combat.getFirstPlayerHealth() != 0) {

                combat.setSecondPlayerMessage(combat.getSecondPlayerMessage() + "\n" +
                        "Ваше сражение окончено - соперник одержал победу");

                combat.setFirstPlayerMessage(combat.getFirstPlayerMessage() + "\n" +
                        "Ваше сражение окончено - соперник разгромлен!");
            }

            // Формируем финальное сообщение в зависимости от результата
            if (combat.getSecondPlayerHealth() == 0 && combat.getFirstPlayerHealth() == 0) {

                combat.setFirstPlayerMessage(combat.getFirstPlayerMessage() + "\n" +
                        "Ваше сражение окончено - никого не осталось в живых");

                combat.setSecondPlayerMessage(combat.getSecondPlayerMessage() + "\n" +
                        "Ваше сражение окончено - никого не осталось в живых");
            }
        }

        // Если в сессии остался только компьютер, то удаляем такую сессию
        if (combat.getSecondPlayerName().equals("Компьютер") && combat.getFirstPlayerName().equals("deleted"))
            combatRepository.deleteByPlayerName(playerName);
        else combatRepository.save(combat);

        // В зависимости от игрока возвращаем нужное сообщение с результатом
        if (combat.getFirstPlayerName().equals(playerName) || combat.getFirstPlayerName().equals("deleted")) {
            return combat.getFirstPlayerMessage();

        } else {
            return combat.getSecondPlayerMessage();
        }
    }

    // Получить статус сессии
    public String getSessionStatus(String playerName) {
        return combatRepository.getByPlayerName(playerName).getSessionStatus();
    }

    // Получить статус игрока
    public String getPlayerStatus(String playerName) {
        if (combatRepository.getByPlayerName(playerName).getFirstPlayerName().equals(playerName))
            return combatRepository.getByPlayerName(playerName).getFirstPlayerStatus();
        else
            return combatRepository.getByPlayerName(playerName).getSecondPlayerStatus();
    }

    // Получить сообщение с результатом раунда и обнулить статус игрока
    public String getPlayerMessageAndRefreshStatus(String playerName) {

        setPlayerStatus(playerName, "empty");

        if (combatRepository.getByPlayerName(playerName).getFirstPlayerName().equals(playerName))
            return combatRepository.getByPlayerName(playerName).getFirstPlayerMessage();
        else
            return combatRepository.getByPlayerName(playerName).getSecondPlayerMessage();
    }

    // Поставить статус игроку
    public void setPlayerStatus(String playerName, String playerStatus) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        if (combat.getFirstPlayerName().equals(playerName))
            combat.setFirstPlayerStatus(playerStatus);
        else
            combat.setSecondPlayerStatus(playerStatus);

        combatRepository.save(combat);
    }

    // Перевести ключевые слова с английского на русский
    private String transToRus (String engWord) {
        switch (engWord) {
            case "head" -> {
                return "голову";
            }
            case "body" -> {
                return "тело";
            }
            case "legs" -> {
                return "ноги";
            }
            default -> {
                return engWord;
            }
        }
    }

    // Проверяем существует ли активная сессия у пользователя
    public boolean hasUserActiveSession (String playerName) {
        return combatRepository.findByPlayerName(playerName).isPresent();
    }

    // Удаляем сессию пользователя
    public void deleteUserSession (String playerName) {
        combatRepository.deleteByPlayerName(playerName);
    }

    // Отдаём результат последнего раунда пользователю
    public String getLastPlayerChoose (String playerName) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        if (combat.getFirstPlayerName().equals(playerName)) {

            return "Ваш последний выбор: атака - в " + transToRus(combat.getFirstPlayerAttackDirection()) +
                    ", защита - на " + transToRus(combat.getFirstPlayerDefenseDirection());
        }
        else {

            return "Ваш последний выбор: атака - в " + transToRus(combat.getSecondPlayerAttackDirection()) +
                    ", защита - на " + transToRus(combat.getSecondPlayerDefenseDirection());
        }
    }
}
