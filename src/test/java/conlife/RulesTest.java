package conlife;

import org.junit.Test;

import static org.junit.Assert.*;

public class RulesTest {
    @Test
    public void parseRules() throws Exception {
        Rules rules = Rules.parseRules("B3/S23");
        assertTrue(rules.isBirth(3));
        assertFalse(rules.isBirth(0));
        assertFalse(rules.isBirth(1));
        assertFalse(rules.isBirth(2));
        assertFalse(rules.isBirth(4));
        assertFalse(rules.isBirth(5));
        assertFalse(rules.isBirth(6));
        assertFalse(rules.isBirth(7));
        assertFalse(rules.isBirth(8));

        assertTrue(rules.isSurvive(2));
        assertTrue(rules.isSurvive(3));
        assertFalse(rules.isSurvive(0));
        assertFalse(rules.isSurvive(1));
        assertFalse(rules.isSurvive(4));
        assertFalse(rules.isSurvive(5));
        assertFalse(rules.isSurvive(6));
        assertFalse(rules.isSurvive(7));
        assertFalse(rules.isSurvive(8));
    }
}