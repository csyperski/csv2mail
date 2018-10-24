package com.cwssoft.csv2mail.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BodyTemplateTest {

    @Test
    @DisplayName("Prefix - Basic Test")
    void getPrefix1() {

        String body = "abc${x-repeat-start}middle${x-repeat-end}xyz";

        BodyTemplate instance = new BodyTemplate(body);
        String result = instance.getPrefix(body);

        assertEquals("abc", result);

    }

    @Test
    @DisplayName("Postfix - Basic Test")
    void getPostfix1() {

        String body = "abc${x-repeat-start}middle${x-repeat-end}xyz";

        BodyTemplate instance = new BodyTemplate(body);
        String result = instance.getPostfix(body);

        assertEquals("xyz", result);

    }
}