package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ParseRequestServiceTest {

  @Mock
  private final  ValidationUtil validationUtil = mock(ValidationUtil.class);

    @InjectMocks
    ParseRequestService sut = new  ParseRequestService(validationUtil);

    @Test
    void shouldValidateUserRequest(){
        UserRequest userRequest = UserRequest.builder()
                .users(Arrays.asList("123e4567-e89b-42d3-a456-556642445678","123e4567-e89b-42d3-a456-556642445698"))
                .build();
        doNothing().when(validationUtil).validateId(any(),any());
        sut.validateUserRequest(userRequest);

    }
}
