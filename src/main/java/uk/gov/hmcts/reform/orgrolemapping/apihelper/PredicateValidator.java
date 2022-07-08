package uk.gov.hmcts.reform.orgrolemapping.apihelper;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;

public class PredicateValidator {

    private PredicateValidator() {
    }

    public static final Predicate<List<String>> nullCheckPredicate = CollectionUtils::isNotEmpty;
    public static final BiPredicate<List<String>, RefreshJobEntity> NullCheckBiPredicate =
        (userIds,refreshJobEntity) ->
            (CollectionUtils.isNotEmpty(userIds) && Objects.nonNull(refreshJobEntity));

    public static  Predicate<HttpStatus> httpStatusPredicate(HttpStatus statusCode) {

        return httpstatus -> httpstatus.equals(statusCode);
    }


}
