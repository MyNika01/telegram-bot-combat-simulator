package demo.telegrambotcombatsimulator.service;

import demo.telegrambotcombatsimulator.entity.Player;
import demo.telegrambotcombatsimulator.enums.DirectionStatusType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CombatServiceTest {

    private final CombatService combatService = spy(CombatService.class);

    @ParameterizedTest
    @CsvSource({
            "1, HEAD",
            "2, BODY",
            "3, LEGS"
    })
    void test_generateAndSetComputerDirection(int directionInt, DirectionStatusType directionEnum) {

        Player playerIn = new Player();

        int min = 1, max = 3;
        when(combatService.getRandomInt(min, max)).thenReturn(directionInt);

        combatService.generateAndSetComputerDirection(playerIn);

        assertEquals(directionEnum, playerIn.getAttackDirection());
        assertEquals(directionEnum, playerIn.getDefenseDirection());
    }
}