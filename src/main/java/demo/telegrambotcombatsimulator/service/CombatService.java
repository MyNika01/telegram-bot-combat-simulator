package demo.telegrambotcombatsimulator.service;

import demo.telegrambotcombatsimulator.entity.Combat;
import demo.telegrambotcombatsimulator.repository.CombatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CombatService {

    @Autowired
    private CombatRepository combatRepository;

    final int MAX_HP = 50;

    public void createOfflineSession(String playerName) {

        //надо сначала проверить имеется ли активная сессия, тогда вернуться в активную сессию

        Combat combat = new Combat();

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

    public void createOnlineSession(String firstPlayerName) {

        //надо сначала проверить имеется ли свободная сессия, тогда нобъединить юзеров в один матч
        //функция поиска сессии
        //предусмотреть одновременный сейв по одной записи

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

    public void updateSession(String action, String playerName) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        var firstPlayerName = combat.getFirstPlayerName();
        var secondPlayerName = combat.getSecondPlayerName();
        var firstPlayerStatus = combat.getFirstPlayerStatus();
        var secondPlayerStatus = combat.getSecondPlayerStatus();

        if (!combat.getSessionStatus().equals("block")) {

            switch (action) {

                case "Атака" -> {
                    if (playerName.equals(firstPlayerName)) {
                        combat.setFirstPlayerStatus("waitAttack");
                        break;
                    } else if (playerName.equals(secondPlayerName)) {
                        combat.setSecondPlayerStatus("waitAttack");
                        break;
                    }
                }

                case "Защита" -> {
                    if (playerName.equals(firstPlayerName)) {
                        combat.setFirstPlayerStatus("waitDefense");
                        break;
                    } else if (playerName.equals(secondPlayerName)) {
                        combat.setSecondPlayerStatus("waitDefense");
                        break;
                    }
                }

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


            if ((combat.getSecondPlayerAttackDirection().equals("head") ||
                    combat.getSecondPlayerAttackDirection().equals("body") ||
                    combat.getSecondPlayerAttackDirection().equals("legs")) &&
                    (combat.getSecondPlayerDefenseDirection().equals("head") ||
                            combat.getSecondPlayerDefenseDirection().equals("body") ||
                            combat.getSecondPlayerDefenseDirection().equals("legs")) &&
                    combat.getSecondPlayerStatus().equals("empty") //решение проблемы, что игрок не можем перевыбрать свою атаку/защиту
            ) {
                combat.setSecondPlayerStatus("ready");
            }

            if (secondPlayerName.equals("Компьютер") && combat.getSecondPlayerStatus().equals("empty")) {
                generateAndSetComputerDirection(combat);
            }

            if (combat.getFirstPlayerStatus().equals("block") && combat.getSecondPlayerStatus().equals("block")) {
                combat.setSessionStatus("doAction");
            }

            combatRepository.save(combat);

        } else if (action.equals("delete")) {

       //     if (playerName.equals(firstPlayerName)) {
       //         combat.setFirstPlayerName("delete");
       //     } else {
       //         combat.setSecondPlayerStatus("delete");
       //     }

       //     if (combat.getFirstPlayerName().equals("delete") && combat.getSecondPlayerStatus().equals("delete"))

            combatRepository.deleteByPlayerName(playerName);
        }
    }

    public void generateAndSetComputerDirection(Combat combat) {

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

    public String combatResult (String playerName) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        if (combat.getFirstPlayerName().equals(playerName)) {
            combat.setFirstPlayerStatus("empty");
            combat.setSecondPlayerStatus("resultReady");
        } else {
            combat.setSecondPlayerStatus("empty");
            combat.setFirstPlayerStatus("resultReady");
        }

        if (combat.getSecondPlayerName().equals("Компьютер")) {
            combat.setSecondPlayerStatus("empty");
        }

        String fpAttack = combat.getFirstPlayerAttackDirection();
        String fpDefense = combat.getFirstPlayerDefenseDirection();
        String spAttack = combat.getSecondPlayerAttackDirection();
        String spDefense = combat.getSecondPlayerDefenseDirection();

        if (!fpAttack.equals(spDefense)) combat.setSecondPlayerHealth(combat.getSecondPlayerHealth() - 10);
        if (!spAttack.equals(fpDefense)) combat.setFirstPlayerHealth(combat.getFirstPlayerHealth() - 10);

        combat.setFirstPlayerAttackDirection("empty");
        combat.setFirstPlayerDefenseDirection("empty");
        combat.setSecondPlayerAttackDirection("empty");
        combat.setSecondPlayerDefenseDirection("empty");

        combat.setFirstPlayerMessage(
                "Соперник нанёс удар в "+transToRus(spAttack)+", а вы поставили защиту на "+transToRus(fpDefense)+"\n"+
                        "Ваше здоровье "+combat.getFirstPlayerHealth()+"/50\n"+
                        "Вы нанесли удар в "+transToRus(fpAttack)+", а соперник поставил защиту на "+transToRus(spDefense)+"\n"+
                        "Здоровье соперника "+combat.getSecondPlayerHealth()+"/50");

        combat.setSecondPlayerMessage(
                "Соперник нанёс удар в "+transToRus(fpAttack)+", а вы поставили защиту на "+transToRus(spDefense)+"\n"+
                        "Ваше здоровье "+combat.getSecondPlayerHealth()+"/50\n"+
                        "Вы нанесли удар в "+transToRus(spAttack)+", а соперник поставил защиту на "+transToRus(fpDefense)+"\n"+
                        "Здоровье соперника "+combat.getFirstPlayerHealth()+"/50");

        combat.setSessionStatus("actionPrepare");

        if (combat.getFirstPlayerHealth() == 0 || combat.getSecondPlayerHealth() == 0) {

            combat.setSessionStatus("block");

            if (combat.getFirstPlayerName().equals(playerName)) {
                combat.setFirstPlayerName("deleted");
            } else {
                combat.setSecondPlayerName("deleted");
            }

            if (combat.getFirstPlayerHealth() == 0 && combat.getSecondPlayerHealth() != 0) {

                combat.setFirstPlayerMessage(combat.getFirstPlayerMessage() + "\n" +
                        "Ваше сражение окончено - соперник одержал победу");

                combat.setSecondPlayerMessage(combat.getSecondPlayerMessage() + "\n" +
                        "Ваше сражение окончено - соперник разгромлен!");
            }

            if (combat.getSecondPlayerHealth() == 0 && combat.getFirstPlayerHealth() != 0) {

                combat.setSecondPlayerMessage(combat.getSecondPlayerMessage() + "\n" +
                        "Ваше сражение окончено - соперник одержал победу");

                combat.setFirstPlayerMessage(combat.getFirstPlayerMessage() + "\n" +
                        "Ваше сражение окончено - соперник разгромлен!");
            }

            if (combat.getSecondPlayerHealth() == 0 && combat.getFirstPlayerHealth() == 0) {

                combat.setFirstPlayerMessage(combat.getFirstPlayerMessage() + "\n" +
                        "Ваше сражение окончено - никого не осталось в живых");

                combat.setSecondPlayerMessage(combat.getSecondPlayerMessage() + "\n" +
                        "Ваше сражение окончено - никого не осталось в живых");
            }
        }

        if (combat.getSecondPlayerName().equals("Компьютер") && combat.getFirstPlayerName().equals("deleted"))
            combatRepository.deleteByPlayerName(playerName);
        else combatRepository.save(combat);

        if (combat.getFirstPlayerName().equals(playerName) || combat.getFirstPlayerName().equals("deleted")) {
            return combat.getFirstPlayerMessage();

        } else {
            return combat.getSecondPlayerMessage();
        }
    }

    public String getSessionStatus(String playerName) {
        return combatRepository.getByPlayerName(playerName).getSessionStatus();
    }

    public String getPlayerStatus(String playerName) {
        if (combatRepository.getByPlayerName(playerName).getFirstPlayerName().equals(playerName))
            return combatRepository.getByPlayerName(playerName).getFirstPlayerStatus();
        else
            return combatRepository.getByPlayerName(playerName).getSecondPlayerStatus();
    }

    public String getPlayerMessageAndRefreshStatus(String playerName) {

        setPlayerStatus(playerName, "empty");

        if (combatRepository.getByPlayerName(playerName).getFirstPlayerName().equals(playerName))
            return combatRepository.getByPlayerName(playerName).getFirstPlayerMessage();
        else
            return combatRepository.getByPlayerName(playerName).getSecondPlayerMessage();
    }

    public void setPlayerStatus(String playerName, String playerStatus) {

        Combat combat = combatRepository.getByPlayerName(playerName);

        if (combat.getFirstPlayerName().equals(playerName))
            combat.setFirstPlayerStatus(playerStatus);
        else
            combat.setSecondPlayerStatus(playerStatus);

        combatRepository.save(combat);
    }

    public void setPlayerName(String oldPlayerName, String newPlayerName) {

        Combat combat = combatRepository.getByPlayerName(oldPlayerName);

        if (combat.getFirstPlayerName().equals(oldPlayerName))
            combat.setFirstPlayerStatus(newPlayerName);
        else
            combat.setSecondPlayerStatus(oldPlayerName);

        combatRepository.save(combat);
    }

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

    public boolean hasUserActiveSession (String playerName) {
        return combatRepository.findByPlayerName(playerName).isPresent();
    }

    public void deleteUserSession (String playerName) {
        combatRepository.deleteByPlayerName(playerName);
    }

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
