package demo.telegrambotcombatsimulator.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.UUID;

@Getter
@Setter
@Document("combatData")
public class Combat {

    @MongoId
    private String sessionId;

    private String firstPlayerName;

    private String secondPlayerName;

    private int firstPlayerHealth;

    private int secondPlayerHealth;

    private String firstPlayerAttackDirection;

    private String secondPlayerAttackDirection;

    private String firstPlayerDefenseDirection;

    private String secondPlayerDefenseDirection;

    private String firstPlayerStatus;

    private String secondPlayerStatus;

    private String firstPlayerMessage;

    private String secondPlayerMessage;

    private String sessionStatus;

    public Combat() {
        this.sessionStatus = "opponentSearchStage";
        this.sessionId = UUID.randomUUID().toString();
    }
}
