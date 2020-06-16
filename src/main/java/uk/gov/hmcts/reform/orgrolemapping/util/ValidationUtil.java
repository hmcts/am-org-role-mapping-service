package uk.gov.hmcts.reform.orgrolemapping.util;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.INPUT_CASE_ID_PATTERN;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.inject.Named;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;

@Named
@Singleton
@Slf4j
public class ValidationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationUtil.class);

    private ValidationUtil() {
    }

    /**
     * Validate a number string using  algorithm.
     *
     * @param numberString =null
     * @return
     */
    public static boolean validate(String numberString) {
        validateInputParams(INPUT_CASE_ID_PATTERN, numberString);
        return (numberString != null && numberString.length() == 16);
    }

    public static void validateInputParams(String pattern, String... inputString) {
        for (String input : inputString) {
            if (StringUtils.isEmpty(input)) {
                throw new BadRequestException("The input parameter is Null/Empty");
            } else if (!Pattern.matches(pattern, input)) {
                throw new BadRequestException("The input parameter: \"" + input + "\", does not comply with the "
                                              + "required pattern");
            }
        }
    }

    public static void validateLists(List<?>... inputList) {
        for (List<?> list : inputList) {
            if (CollectionUtils.isEmpty(list)) {
                throw new BadRequestException("The List is empty");
            }
        }
    }

    public static boolean validateTTL(String strDate) {
        if (strDate.length() < 24) {
            return false;
        }
        String timeZone = strDate.substring(20);

        if (timeZone.chars().allMatch(Character::isDigit)) {
            SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
            sdfrmt.setLenient(false);
            try {
                Date javaDate = sdfrmt.parse(strDate);
                LOG.info("TTL {}", javaDate);
            } catch (ParseException e) {
                return false;
            }
            return true;
        }
        return false;
    }
}
