package uk.gov.hmcts.reform.orgrolemapping.apihelper;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;

public class PredicateValidator {

    private PredicateValidator() {
    }

    public static final Predicate<HttpStatus> httpPredicates = statsCode -> statsCode.equals(HttpStatus.CREATED);
    public static final Predicate<List<Object>> objectPredicates = List::isEmpty;
    public static final Predicate<List<String>> userRequestListPredicate = CollectionUtils::isNotEmpty;
    public static final BiPredicate<List<String>, RefreshJobEntity> refreshJobBiPredicate =
        (userIds,refreshJobEntity) ->
            (CollectionUtils.isNotEmpty(userIds) && Objects.nonNull(refreshJobEntity));

    public static Predicate<UserType> nameContains(final String userTypeName) {

        return userType -> userType.name().contains(userTypeName);
    }

}
