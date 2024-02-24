package demo.telegrambotcombatsimulator.service;

import demo.telegrambotcombatsimulator.entity.Player;
import demo.telegrambotcombatsimulator.enums.DirectionStatusType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CombatServiceTest {

    private final CombatService combatService = spy(CombatService.class);

    @Test
    void test_generateAndSetComputerDirection() { //todo входной парам инт, второй - то что ожидаю. Как загрузить параметры сюда?

        Player playerIn = new Player();

        int max = 3, min = 1;
        when(combatService.getRandomInt(max, min)).thenReturn(2);

        combatService.generateAndSetComputerDirection(playerIn);

        assertEquals(DirectionStatusType.BODY, playerIn.getAttackDirection());
        assertEquals(DirectionStatusType.BODY, playerIn.getDefenseDirection());
    }
}