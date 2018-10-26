package com.cwssoft.csv2mail.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailUtilsTest {

    @Test
    @DisplayName("isValidEmail - no @")
    void isValidEmail1() {

        boolean result = EmailUtils.isValidEmail("lkjsdflksdf.lkjsdfkljsf.net");
        assertEquals(false, result);
    }
}