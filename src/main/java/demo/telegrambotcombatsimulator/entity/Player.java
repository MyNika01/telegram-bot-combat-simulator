package demo.telegrambotcombatsimulator.entity;

import demo.telegrambotcombatsimulator.enums.PlayerStatusType;
import demo.telegrambotcombatsimulator.enums.DirectionStatusType;

import lombok.Getter;
import lombok.Setter;

import static demo.telegrambotcombatsimulator.enums.DirectionStatusType.D_EMPTY;
import static demo.telegrambotcombatsimulator.enums.PlayerStatusType.P_EMPTY;
import static demo.telegrambotcombatsimulator.service.CombatService.MAX_HP;

@Getter
@Setter
public class Player {

    private String name;
    private int health;
    private DirectionStatusType attackDirection;
    private DirectionStatusType defenseDirection;
    private PlayerStatusType status;
    private String message;

    public Player() {
        this.name = "empty";
        this.health = MAX_HP;
        this.attackDirection = D_EMPTY;
        this.defenseDirection = D_EMPTY;
        this.status = P_EMPTY;
        this.message = "empty";
    }

    public void setMessageAsBattleResult(DirectionStatusType enemyPlayerAttack, DirectionStatusType playerDefense, DirectionStatusType playerAttack, DirectionStatusType enemyPlayerDefense, int playerHealth, int enemyHealth) {

        this.message =
                "Соперник нанёс удар в " + enemyPlayerAttack +
                        " ⚔->" + enemyPlayerAttack.toEmoji() + ", а вы поставили защиту на " + playerDefense +
                        "\uD83D\uDEE1->" + playerDefense.toEmoji() + "\n" +

                        attackResult(enemyPlayerAttack, playerDefense) +

                        "Ваше здоровье " + playerHealth + "/" + MAX_HP + " " + healthToEmoji(playerHealth) + "\n\n" +

                        "Вы нанесли удар в " + playerAttack +
                        " ⚔->" + playerAttack.toEmoji() + ", а соперник поставил защиту на " + enemyPlayerDefense +
                        "\uD83D\uDEE1->" + enemyPlayerDefense.toEmoji() + "\n" +

                        attackResult(playerAttack, enemyPlayerDefense) +

                        "Здоровье соперника " + enemyHealth + "/" + MAX_HP + " " + healthToEmoji(enemyHealth);
    }

    private String healthToEmoji(int health) {

        StringBuilder emoji = new StringBuilder();

        for (int i = 0; i < MAX_HP; i = i + 10) {
            if (i < health) emoji.append("❤");
            else emoji.append("\uD83D\uDC94");
        }

        return emoji.toString();
    }

    private String attackResult (DirectionStatusType attack, DirectionStatusType defense) {

        return attack==defense ? "Блок удара!\uD83D\uDEE1\n" : "Успешный удар!\uD83E\uDE78\n";
    }
}