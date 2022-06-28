package uk.gov.hmcts.reform.orgrolemapping.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Named
@Singleton
public class ValidationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationUtil.class);

    private ValidationUtil() {
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

    public static void validateDateTime(String strDate, String timeParam) {
        LOG.debug("validateDateTime");
        if (strDate.length() < 16) {
            throw new BadRequestException(String.format(
                    "Incorrect date format %s",
                    strDate
            ));
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_PATTERN);
        simpleDateFormat.setLenient(false);
        Date javaDate;
        try {
            javaDate = simpleDateFormat.parse(strDate);
            if (LOG.isInfoEnabled() && javaDate != null) {
                LOG.debug(javaDate.toString());
            }
        } catch (ParseException e) {
            throw new BadRequestException(String.format(
                    "Incorrect date format %s",
                    strDate
            ));
        }
        assert javaDate != null;
        if (javaDate.before(new Date())) {
            throw new BadRequestException(String.format(
                    "The parameter '%s' cannot be prior to current date", timeParam
            ));
        }
    }

    public static void compareDateOrder(String beginTime, String endTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_PATTERN);
        Date beginTimeP = sdf.parse(beginTime);
        Date endTimeP = sdf.parse(endTime);
        Date createTimeP = new Date();

        if (beginTimeP.before(createTimeP)) {
            throw new BadRequestException(
                    String.format("The begin time: %s takes place before the current time: %s",
                            beginTime, createTimeP
                    ));
        } else if (endTimeP.before(createTimeP)) {
            throw new BadRequestException(
                    String.format("The end time: %s takes place before the current time: %s", endTime, createTimeP));
        } else if (endTimeP.before(beginTimeP)) {
            throw new BadRequestException(
                    String.format("The end time: %s takes place before the begin time: %s", endTime, beginTime));
        }
    }

    public static void validateId(String pattern, String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            throw new BadRequestException("An input parameter is Null/Empty");
        } else if (!Pattern.matches(pattern, inputString)) {
            throw new BadRequestException(
                    String.format("The input parameter: \"%s\", does not comply with the required pattern",
                            inputString));
        }
    }

    public static void compareRoleCategory(String roleCategoryFromJob) {
        var valid = false;
        for (RoleCategory roleCategory : RoleCategory.values()) {
            if (roleCategory.name().equalsIgnoreCase(roleCategoryFromJob)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new BadRequestException("The roleCategory parameter supplied is not valid");
        }
    }

}
