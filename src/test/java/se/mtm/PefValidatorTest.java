package se.mtm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PefValidatorTest {

    @DisplayName("Test that an incorrect page number returns -1")
    @Test
    void testIncorrectPageNumber() {
        PefValidator pefValidator = new PefValidator();
        assertEquals(-1, pefValidator.getPageNumber("dsjiads"), "Handle incorrect number");
        assertEquals(-1, pefValidator.getPageNumber("#dsqjiads"), "Handle incorrect number");
    }

    @DisplayName("Test that correct numbers return their values")
    @Test
    void testCorrectPageNumber() {
        PefValidator pefValidator = new PefValidator();
        assertEquals(1, pefValidator.getPageNumber("#a"), "Can handle one number");
        assertEquals(5, pefValidator.getPageNumber("#e"), "Can handle another number");
        assertEquals(10, pefValidator.getPageNumber("#aj"), "Can handle more than one number");
        assertEquals(1000, pefValidator.getPageNumber("#ajjj"), "Can handle thousends");
        assertEquals(1337, pefValidator.getPageNumber("#accg"), "Can be leet.");
    }

    @DisplayName("Test that an incorrect roman numerals returns -1")
    @Test
    void testIncorrectRomanNumerals() {
        PefValidator pefValidator = new PefValidator();
        assertEquals(-1, pefValidator.getPageNumber("_ii"), "Handle incorrect number");
        assertEquals(-1, pefValidator.getPageNumber("__i"), "Handle incorrect number");
    }

    @DisplayName("Test that we can handle roman numerals")
    @Test
    void testCorrectRomanNumerals() {
        PefValidator pefValidator = new PefValidator();
        assertEquals(1, pefValidator.getPageNumber("_i"), "Can handle one");
        assertEquals(5, pefValidator.getPageNumber("_v"), "Can handle five");
        assertEquals(10, pefValidator.getPageNumber("_x"), "Can handle more than one number");
        assertEquals(50, pefValidator.getPageNumber("_l"), "Can handle fifty");
        assertEquals(100, pefValidator.getPageNumber("_c"), "Can handle hundreds.");
        assertEquals(500, pefValidator.getPageNumber("_d"), "Can handle five hundreds.");
        assertEquals(1000, pefValidator.getPageNumber("_m"), "Can handle thousands.");
        assertEquals(1337, pefValidator.getPageNumber("__mcccxxxvii"), "Can be leet.");
        assertEquals(1771, pefValidator.getPageNumber("__mdcclxxi"), "Can handle an important year");
        assertEquals(2019, pefValidator.getPageNumber("__mmxix"), "Can handle this year");
    }
}
