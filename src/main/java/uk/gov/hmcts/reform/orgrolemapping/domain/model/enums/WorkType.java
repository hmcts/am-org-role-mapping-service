package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums;

public enum WorkType {
    ACCESS_REQUESTS("access_requests"),
    AMENDMENTS("amendments"),
    APPLICATIONS("applications"),
    BAIL_WORK("bail_work"),
    DECISION_MAKING_WORK("decision_making_work"),
    ERROR_MANAGEMENT("error_management"),
    HEARING_WORK("hearing_work"),
    INTERMEDIATE_TRACK_DECISION_MAKING_WORK("intermediate_track_decision_making_work"),
    INTERMEDIATE_TRACK_HEARING_WORK("intermediate_track_hearing_work"),
    MULTI_TRACK_DECISION_MAKING_WORK("multi_track_decision_making_work"),
    MULTI_TRACK_HEARING_WORK("multi_track_hearing_work"),
    POST_HEARING("post_hearing"),
    PRE_HEARING("pre_hearing"),
    PRIORITY("priority"),
    QUERY_WORK("query_work"),
    REVIEW_CASE("review_case"),
    ROUTINE_WORK("routine_work"),
    STF_24W_ACCESS_REQUESTS("stf_24w_access_requests"),
    STF_24W_APPLICATIONS("stf_24w_applications"),
    STF_24W_DECISION_MAKING_WORK("stf_24w_decision_making_work"),
    STF_24W_HEARING_WORK("stf_24w_hearing_work"),
    STF_24W_ROUTINE_WORK("stf_24w_routine_work"),
    STF_24W_UPPER_TRIBUNAL("stf_24w_upper_tribunal"),
    STOPPED_APPLICATIONS("stopped_applications"),
    UPPER_TRIBUNAL("upper_tribunal"),
    WELSH_TRANSLATION_WORK("welsh_translation_work");

    private final String value;

    WorkType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;   
    }

    public static String joinValues(WorkType... workTypes) {
        String[] values = new String[workTypes.length];
        for (int i = 0; i < workTypes.length; i++) {
            values[i] = workTypes[i].getValue();
        }
        return String.join(", ", values);
    }
}
