package demo.telegrambotcombatsimulator.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DirectionStatusType {
    D_EMPTY(0),
    HEAD(1),
    BODY(2),
    LEGS(3);

    private final int order;

    DirectionStatusType(int order) {
        this.order = order;
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
}