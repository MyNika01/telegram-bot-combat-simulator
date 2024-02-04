package demo.telegrambotcombatsimulator.entity;

import demo.telegrambotcombatsimulator.enums.SessionStatusType;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.UUID;

import static demo.telegrambotcombatsimulator.enums.SessionStatusType.S_EMPTY;

@Getter
@Setter
@Document("combatData")
public class Combat {

    @MongoId
    private String sessionId;
    private SessionStatusType sessionStatus;
    private Player firstPlayer;
    private Player secondPlayer;

    public Combat() {
        this.sessionId = UUID.randomUUID().toString();
        this.sessionStatus = S_EMPTY;
        this.firstPlayer = new Player();
        this.secondPlayer = new Player();
    }

    public Combat(String playerName) {
        this();
        this.sessionStatus = SessionStatusType.OPPONENT_SEARCH;
        this.firstPlayer.setName(playerName);
    }

    public Combat(String firstPlayerName, String secondPlayerName) {
        this(firstPlayerName);
        this.sessionStatus = SessionStatusType.ACTION_PREPARE;
        this.secondPlayer.setName(secondPlayerName);
    }
}
