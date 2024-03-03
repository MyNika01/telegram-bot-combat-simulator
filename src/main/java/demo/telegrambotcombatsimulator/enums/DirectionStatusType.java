package demo.telegrambotcombatsimulator.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DirectionStatusType {

    D_EMPTY(0, "empty", ""),
    HEAD(1, "голову", "\uD83E\uDDE2"),
    BODY(2, "корпус", "\uD83D\uDC55"),
    LEGS(3, "ноги", "\uD83D\uDC5F");

    private final int order;
    private final String translation;
    private final String emoji;

    DirectionStatusType(int order, String translation, String emoji) {
        this.order = order;
        this.translation = translation;
        this.emoji = emoji;
    }

    public static DirectionStatusType getByOrder(int order) {
        for (var data : DirectionStatusType.values()) {
            if (data.getOrder() == order) {
                return data;
            }
        }
        return D_EMPTY;
    }

    //пример getByOrder в stream варианте
    public static DirectionStatusType getByOrderFunc(int order) {
        return Arrays.stream(DirectionStatusType.values())
                .filter(data -> data.getOrder() == order)
                .findFirst().orElse(D_EMPTY);
    }

    @Override
    public String toString() {
        return translation;
    }

    public String toEmoji() {
        return emoji;
    }
}