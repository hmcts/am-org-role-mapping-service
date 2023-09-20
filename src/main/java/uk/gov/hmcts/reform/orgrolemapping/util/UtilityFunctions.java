package uk.gov.hmcts.reform.orgrolemapping.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AppointmentV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType;

import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Slf4j
public final class UtilityFunctions {

    private UtilityFunctions() {
    }

    public static String getAppointmentTypeFromAppointment(final AppointmentV2 appointment) {
        String result = appointment.getAppointmentType(); // fallback

        // load ContractTypeId and convert into v1 constant
        if (appointment.getContractTypeId() != null) {
            switch (appointment.getContractTypeId()) {
                case "0" -> result = AppointmentType.SALARIED;
                case "1" -> result = AppointmentType.FEE_PAID;
                case "2" -> result = AppointmentType.VOLUNTARY;
                case "3", "4", "5", "6", "7", "8", "9" -> result = AppointmentType.SPTW;
                default -> log.warn(
                        "Judicial ContractTypeId not recognised: {} - '{}'",
                        appointment.getContractTypeId(),
                        appointment.getAppointmentType()
                );
            }
        }
        return result;
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
            case "BFA1":
                result = "IA";
                break;
            default:
                break;
        }
        return result;
    }

    public static LocalDateTime localDateToLocalDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    public static ZonedDateTime localDateToZonedDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay(ZoneId.of("UTC")) : null;
    }

    public static List<String> stringListToDistinctList(List<String> input) {

        List<String> output = new ArrayList<>();
        if (input != null) {
            input.stream()
                    .filter(StringUtils::isNoneBlank)
                    .distinct()
                    .forEach(output::add);
        }
        return output;
    }
}
