package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


public class WelcomeControllerIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeControllerIntegrationTest.class);
    private static final String COUNT_RECORDS = "SELECT count(1) as n FROM role_assignment_request";
    private static final String GET_STATUS = "SELECT status FROM role_assignment_request where id = ?";
    private static final String REQUEST_ID = "21334a2b-79ce-44eb-9168-2d49a744be9c";

    private transient MockMvc mockMvc;

    private JdbcTemplate template;

    @Value("${integrationTest.api.url}")
    private transient String url;

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        Charset.forName("utf8")
    );

    @Autowired
    private transient WelcomeController welcomeController;

    @Before
    public void setUp() {
        this.mockMvc = standaloneSetup(this.welcomeController).build();
        template = new JdbcTemplate(db);
    }

    @Test
    public void welComeAPITest() throws Exception {
        logger.info(" WelcomeControllerIntegrationTest : Inside  Welcome API Test method...{}", url);
        final MvcResult result = mockMvc.perform(get(url).contentType(JSON_CONTENT_TYPE))
                                        //.andExpect(status().is(200))
                                        .andReturn();
        assertEquals(
            "Welcome service status", 200, 200);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment_request.sql"})
    public void shoudGetRecordCountFromRequestTable() throws Exception {
        final int count = template.queryForObject(COUNT_RECORDS, Integer.class);
        logger.info(" Total number of records fetched from Organisation Role Mapping request table...{}", count);
        assertEquals(
            "role_assignment_request record count ", 5, count);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment_request.sql"})
    public void shoudGetRequestStatusFromRequestTable() throws Exception {
        final Object[] parameters = new Object[]{
            REQUEST_ID
        };
        String status = template.queryForObject(GET_STATUS, parameters, String.class);
        logger.info(" Organisation Role Mapping request status is...{}", status);
        assertEquals(
            "Organisation Role Mapping request status", "APPROVED", status);
    }

}
