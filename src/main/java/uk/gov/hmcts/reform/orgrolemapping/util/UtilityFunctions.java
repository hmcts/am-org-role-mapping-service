package uk.gov.hmcts.reform.orgrolemapping.util;

import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AppointmentV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType;

import jakarta.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
        String result;
        switch (serviceCode) {
            case "BBA3" -> result = "SSCS";
            case "AAA6", "AAA7" -> result = "CIVIL";
            case "ABA5" -> result = "PRIVATELAW";
            case "ABA3" -> result = "PUBLICLAW";
            case "BHA1" -> result = "EMPLOYMENT";
            case "BFA1" -> result = "IA";
            default -> result = null;
        }
        return result;
    }

    public static List<String> getUserIdsFromJudicialAccessProfileMap(
            Map<String, Set<UserAccessProfile>> userAccessProfiles
    ) {
        return userAccessProfiles.values().stream()
            .flatMap(accessProfileSet -> accessProfileSet.stream().map(userAccessProfile ->
                ((JudicialAccessProfile)userAccessProfile).getUserId()
            ))
            .distinct()
            .toList();
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

    public static List<List<String>> splitListIntoBatches(List<String> inputList, String batchSize) {
        final int defaultBatchSize = getBatchSize(batchSize, inputList.size());
        AtomicInteger counter = new AtomicInteger();

        return inputList.stream()
            .collect(Collectors.groupingBy(gr -> counter.getAndIncrement() / defaultBatchSize))
            .values().stream().toList();
    }

    private static int getBatchSize(String batchSize, int defaultSize) {
        if (batchSize == null) {
            return defaultSize;
        }

        Integer value = Ints.tryParse(batchSize);
        if (value == null) {
            return defaultSize;
        }

        return value;
    }

}
