package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

abstract class BaseIrmSchedulerTestIntegration extends BaseSchedulerTestIntegration {

    public static final UUID STUB_ID_GET_USER_ID
            = UUID.fromString("03df5228-b5ab-4e0b-ad90-5ed565d60705");

    protected void stubGetIdamUser(List<String> fileNames, String userId, EndStatus endStatus) {
        stubGetIdamUser(
                "{ \"users\": " + jsonHelper.readJsonArrayFromFiles(fileNames)
                        + " }", userId, endStatus
        );
    }

    private void stubGetIdamUser(String body, String userId, EndStatus endStatus) {
        HttpHeaders headers = new HttpHeaders()
                .plus(new HttpHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

        int httpStatus = EndStatus.FAILED.equals(endStatus)
                ? HttpStatus.UNAUTHORIZED.value() : HttpStatus.OK.value();

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching(
                "/api/v2/users/" + userId))
                .withId(STUB_ID_GET_USER_ID)
                .willReturn(aResponse()
                        .withStatus(httpStatus)
                        .withHeaders(headers)
                        .withBody(body)));
    }
}
