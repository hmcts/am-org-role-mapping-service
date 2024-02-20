//package uk.gov.hmcts.reform.orgrolemapping.domain.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.jdbc.Sql;
//import org.springframework.transaction.annotation.Transactional;
//import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
//import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
//import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
//import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.when;
//
//@Transactional
//public class CaseDefinitionServiceIntegrationTest extends BaseTestIntegration {
//
//    @Autowired
//    private CaseDefinitionService caseDefinitionService;
//
//    @Autowired
//    private ProfileRefreshQueueRepository profileRefreshQueueRepository;
//
//    @Autowired
//    private AccessTypesRepository accessTypesRepository;
//
//    @Test
//    @Sql(scripts = {"classpath:sql/insert_access_types.sql"})
//    void shouldUpdateLocalDefinitions() throws JsonProcessingException {
//
//        RestructuredAccessTypes ccdDefintions = buildRestructuredAccessTypes();
//
//        when(caseDefinitionService.retrieveCCDAccessTypeDefinitions()).thenReturn(ccdDefintions);
//
//        caseDefinitionService.findAndUpdateCaseDefinitionChanges();
//
//        assertEquals()
//
//    }
//
//    private RestructuredAccessTypes buildRestructuredAccessTypes(){
//        return RestructuredAccessTypes.builder()
//                .build();
//    }
//
//
//}
