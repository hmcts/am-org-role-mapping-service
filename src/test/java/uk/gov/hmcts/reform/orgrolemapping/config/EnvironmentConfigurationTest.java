package uk.gov.hmcts.reform.orgrolemapping.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvironmentConfigurationTest {

    @Test
    void testEnvironmentConfiguration_shouldReturnOrmValueWhenBothSupplied() {

        // GIVEN
        String launchDarklyEnvironment = "launchDarklyEnvironment";
        String ormEnvironment = "ormEnvironment";

        // WHEN
        var cut = new EnvironmentConfiguration(launchDarklyEnvironment, ormEnvironment);

        // THEN
        assertEquals(ormEnvironment, cut.getEnvironment());

    }

    @ParameterizedTest
    @NullAndEmptySource
    void testEnvironmentConfiguration_shouldReturnLaunchDarklyValueWhenOrmValueIsNotSupplied(String ormEnvironment) {

        // GIVEN
        String launchDarklyEnvironment = "launchDarklyEnvironment";

        // WHEN
        var cut = new EnvironmentConfiguration(launchDarklyEnvironment, ormEnvironment);

        // THEN
        assertEquals(launchDarklyEnvironment, cut.getEnvironment());

    }

}
