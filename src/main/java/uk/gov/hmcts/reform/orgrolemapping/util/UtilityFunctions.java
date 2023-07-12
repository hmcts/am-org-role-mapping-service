package uk.gov.hmcts.reform.orgrolemapping.util;

import javax.inject.Singleton;

@Singleton
public final class UtilityFunctions {

    private UtilityFunctions() {
    }

    public static String getJurisdictionFromServiceCode(final String serviceCode) {
        String result = null;
        switch (serviceCode) {
            case "BBA3":
                result = "SSCS";
                break;
            case "AAA6":
            case "AAA7":
                result = "CIVIL";
                break;
            case "ABA5":
                result = "PRIVATELAW";
                break;
            case "ABA3":
                result = "PUBLICLAW";
                break;
            case "BHA1":
                result = "EMPLOYMENT";
                break;
        }
        return result;
    }
}
