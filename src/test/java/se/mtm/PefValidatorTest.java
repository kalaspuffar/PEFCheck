package se.mtm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PefValidatorTest {

    @DisplayName("Test that an incorrect page number returns -1")
    @Test
    void testIncorrectPageNumber() {
        PefValidator pefValidator = new PefValidator();
        assertTrue(pefValidator.getPageNumber("dsjiads") == -1);
    }
}
