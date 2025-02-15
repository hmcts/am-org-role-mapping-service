package uk.gov.hmcts.reform.orgrolemapping.drool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfigRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.OrgMappingController.CREATE_ORG_MAPPING_URI;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_XUI;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.getHttpHeaders;
import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.TEST_ENVIRONMENT;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.cloneAndExpandReplaceMap;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.getSidamIdsList;

@TestPropertySource(properties = {
    "refresh.BulkAssignment.includeJudicialBookings=true",
    "refresh.judicial.filterSoftDeletedUsers=true",
    // turn off service bus
    "amqp.crd.enabled=false",
    "amqp.jrd.enabled=false",
    // set environment ready for flag checks
    "launchdarkly.sdk.environment=" + TEST_ENVIRONMENT,
    "orm.environment=" + TEST_ENVIRONMENT,
    "testing.support.enabled=true"
})
public class BaseDroolTestIntegration extends BaseTestIntegration {

    static final String TEST_ENVIRONMENT = "local";

    static final String EMPTY_ROLE_ASSIGNMENT_TEMPLATE = "Common/Empty__RasAssignmentRequest";

    protected final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    protected final ObjectMapper mapper = JacksonUtils.MAPPER;

    protected MockMvc mockMvc;

    @Autowired
    private FlagConfigRepository flagConfigRepository;

    @Inject
    private WebApplicationContext wac;

    @MockBean
    private SecurityUtils securityUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @MockBean
    private FeatureConditionEvaluator featureConditionEvaluation;


    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);

        doReturn(true).when(featureConditionEvaluation).preHandle(any(),any(),any());

        wiremockFixtures.resetRequests();
        wiremockFixtures.stubIdamSystemUser();
    }

    protected String readJsonArrayFromFile(String fileName, List<TestScenario> testScenarios) {
        if (StringUtils.isEmpty(fileName)) {
            return "[]";
        }

        return "[" + testScenarios.stream()
            .map(testScenario -> readJsonFromFile(fileName, testScenario.getReplaceMap()))
            .collect(Collectors.joining(",")) + "]";
    }

    protected String readJsonFromFile(String fileName, Map<String, String> replaceMap) {
        Map<String, String> replaceMapClone = cloneAndExpandReplaceMap(replaceMap);

        String json;
        try {
            json = readJsonFromFile(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<String, String> entry: replaceMapClone.entrySet()) {
            if (entry.getValue() == null) {
                // i.e. replace `"[[NULL_VALUE]]"` with `null`: rather than `"null"`
                json = json.replace("\"" + entry.getKey() + "\"", "null");
            } else if (entry.getKey().endsWith("_BOOLEAN]]")) {
                // i.e. replace `"[[REPLACE_BOOLEAN]]"` with `true` or `false`: rather than `"true"` or `"false"`
                json = json.replace("\"" + entry.getKey() + "\"", entry.getValue());
            } else {
                json = json.replace(entry.getKey(), entry.getValue());
            }
        }

        return json;
    }

    protected String readJsonFromFile(String fileName) throws IOException {
        Object json;

        try (InputStream is = WiremockFixtures.class.getResourceAsStream(String.format("/%s.json", fileName))) {
            json = mapper.readValue(is, Object.class);
        }

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    protected void setAllFlags(List<FeatureFlagEnum> turnOffFlags) {
        for (FeatureFlagEnum featureFlagEnum : FeatureFlagEnum.values()) {
            var flagConfig = flagConfigRepository.findByFlagNameAndEnv(featureFlagEnum.getValue(), TEST_ENVIRONMENT);
            if (flagConfig != null) {
                flagConfig.setStatus(turnOffFlags == null || !turnOffFlags.contains(featureFlagEnum));
                flagConfigRepository.save(flagConfig);
            }
        }
    }

    protected void triggerCreateOrmMappingApi(UserType userType, List<TestScenario> testScenarios) throws Exception {

        mockMvc.perform(post(CREATE_ORG_MAPPING_URI)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders(S2S_XUI))
                .param("userType", userType.name())
                .content(mapper.writeValueAsBytes(UserRequest.builder()
                    .userIds(getSidamIdsList(testScenarios)).build())))
            .andExpect(status().is(200))
            .andReturn();
    }

}
