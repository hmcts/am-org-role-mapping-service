package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Getter
@Setter
@Builder
public class Case {
    private String id;

    @JsonIgnore
    private Long reference;

    @JsonProperty("version")
    private Integer version;

    private String jurisdiction;

    @JsonProperty("case_type_id")
    private String caseTypeId;

    @JsonProperty("created_date")
    private LocalDateTime createdDate;

    @JsonProperty("last_modified")
    private LocalDateTime lastModified;

    @JsonProperty("last_state_modified_date")
    private LocalDateTime lastStateModifiedDate;

    private String state;

    @JsonProperty("case_data")
    private Map<String, JsonNode> data;

    @JsonProperty("data_classification")
    private Map<String, JsonNode> dataClassification;

    /**
     * Attribute passed to UI layer, does not need persistence.
     */
    @JsonProperty("callback_response_status_code")
    private Integer callbackResponseStatusCode;

    /**
     * Attribute passed to UI layer, does not need persistence.
     */
    @JsonProperty("callback_response_status")
    private String callbackResponseStatus;

    /**
     * Attribute passed to UI layer, does not need persistence.
     */
    @JsonProperty("delete_draft_response_status_code")
    private Integer deleteDraftResponseStatusCode;


    /**
     * Attribute passed to UI layer, does not need persistence.
     */
    @JsonProperty("delete_draft_response_status")
    private String deleteDraftResponseStatus;


    @JsonIgnore
    private final Map<String, Object> metadata = new HashMap<>();


    @JsonIgnore
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @JsonIgnore
    public boolean hasCaseReference() {
        return getReference() != null;
    }

}
