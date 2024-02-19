package demo.telegrambotcombatsimulator.enums;

import lombok.Getter;

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
            if (data.getOrder() == order){
                return data;
            }
        }
        return D_EMPTY;
    }
}