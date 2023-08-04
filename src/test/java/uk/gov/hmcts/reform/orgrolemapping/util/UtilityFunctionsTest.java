package uk.gov.hmcts.reform.orgrolemapping.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtilityFunctionsTest {

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeBBA3() {
        assertEquals("SSCS", UtilityFunctions.getJurisdictionFromServiceCode("BBA3"));
    }

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeAAA6() {
        assertEquals("CIVIL", UtilityFunctions.getJurisdictionFromServiceCode("AAA6"));
    }

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeAAA7() {
        assertEquals("CIVIL", UtilityFunctions.getJurisdictionFromServiceCode("AAA7"));
    }

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeABA5() {
        assertEquals("PRIVATELAW", UtilityFunctions.getJurisdictionFromServiceCode("ABA5"));
    }

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeABA3() {
        assertEquals("PUBLICLAW", UtilityFunctions.getJurisdictionFromServiceCode("ABA3"));
    }

    @Test
    void shouldReturnCorrectJurisdictionFromServiceCodeBHA1() {
        assertEquals("EMPLOYMENT", UtilityFunctions.getJurisdictionFromServiceCode("BHA1"));
    }

    @Test
    void shouldReturnNullFromServiceCodeNotRecognised() {
        assertNull(UtilityFunctions.getJurisdictionFromServiceCode("XXX6"));
    }
}
