package uk.gov.hmcts.reform.orgrolemapping.befta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.befta.BeftaMain;
import uk.gov.hmcts.befta.DefaultBeftaTestDataLoader;
import uk.gov.hmcts.befta.auth.UserTokenProviderConfig;
import uk.gov.hmcts.befta.data.UserData;
import uk.gov.hmcts.befta.util.BeftaUtils;
import uk.gov.hmcts.befta.util.EnvironmentVariableUtils;
import uk.gov.hmcts.befta.util.JsonUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class OrmTestDataLoader extends DefaultBeftaTestDataLoader {

    @Override
    public void doLoadTestData() {

    }

    @Override
    public boolean isTestDataLoadedForCurrentRound() {
        return false;
    }

    @Override
    public void loadDataIfNotLoadedVeryRecently() {
        RestAssured.useRelaxedHTTPSValidation();
        try {
            createUsersInIdam();
        } catch (Exception e) {
            // Write details to log then swallow this error as this failure is not critical.  i.e. allow it to continue
            // so any errors related to the deployment under test will re-occur during smoke / functional test run
            // which has better fault reporting.
            BeftaUtils.defaultLog("Error creating test users in IDAM.", e);
        }
    }

    private void createUsersInIdam() {
        String idamTsUrl = EnvironmentVariableUtils.getOptionalVariable("IDAM_TESTING_SUPPORT_URL");

        if (StringUtils.isBlank(idamTsUrl)) {
            BeftaUtils.defaultLog("Skipping user creation in IDAM as no URL configured.");
        } else {
            File idamUsersDir = BeftaUtils.getFileFromResource("idamUsers");
            if (idamUsersDir.exists() && idamUsersDir.isDirectory()) {
                UserData amBeftaUser1 = getAmBeftaUser1();

                FileFilter fileFilter = file -> !file.isDirectory() && file.getName().endsWith(".json");
                for (final File jsonFile : Objects.requireNonNull(idamUsersDir.listFiles(fileFilter))) {
                    createUserFromFile(jsonFile, idamTsUrl, amBeftaUser1.getAccessToken());
                }
            }
        }
    }

    private void createUserFromFile(File jsonFile, String idamTsUrl, String accessToken) {

        JsonNode requestJson;

        try {
            requestJson = JsonUtils.readObjectFromJsonFile(jsonFile.getPath(), JsonNode.class);

            // adjust for environment variables
            updateNodeValueFromEnvironmentVariable(requestJson, "password");
            updateNodeValueFromEnvironmentVariable(requestJson.findValue("user"), "email");

        } catch (IOException e) {
            throw new RuntimeException("Error loading user data from: " + jsonFile.getPath(), e);
        }

        Response response = RestAssured
                .given(new RequestSpecBuilder().setBaseUri(idamTsUrl).build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(requestJson)
                .contentType(ContentType.JSON)
                .when()
                .post("/test/idam/users");

        if (response.getStatusCode() == HttpStatus.CREATED.value()) {
            BeftaUtils.defaultLog("IDAM user created from: " + jsonFile.getPath());

        } else if (response.getStatusCode() == HttpStatus.CONFLICT.value()) {
            BeftaUtils.defaultLog("IDAM user already exists for: " + jsonFile.getPath());

        } else {
            BeftaUtils.defaultLog("Error when creating IDAM user from: " + jsonFile.getPath());
            String message = "Call to create IDAM user failed with response body: " + response.body().prettyPrint();
            message += "\nand http code: " + response.statusCode();
            throw new RuntimeException(message);
        }
    }

    private UserData getAmBeftaUser1() {

        UserData amBeftaUser1;

        try {
            // reuse user from FTAs
            JsonNode userJson = JsonUtils.readObjectFromJsonFile(
                BeftaUtils.getFileFromResource("features/common/users/AmBeftaUser1.td.json").getPath(),
                JsonNode.class
            );

            // adjust for environment variables
            amBeftaUser1 = new UserData(
                EnvironmentVariableUtils.resolvePossibleVariable(userJson.findValue("username").asText()),
                EnvironmentVariableUtils.resolvePossibleVariable(userJson.findValue("password").asText())
            );

        } catch (IOException e) {
            throw new RuntimeException("Error loading user data for AmBeftaUser1", e);
        }

        try {
            BeftaMain.getAdapter().authenticate(amBeftaUser1, UserTokenProviderConfig.DEFAULT_INSTANCE.getClientId());

        } catch (ExecutionException e) {
            throw new RuntimeException("Authenticating as AmBeftaUser1 failed.", e);
        }

        return amBeftaUser1;
    }

    private void updateNodeValueFromEnvironmentVariable(JsonNode parentNode, String fieldName) {
        String value = EnvironmentVariableUtils.resolvePossibleVariable(parentNode.findValue(fieldName).asText());
        ((ObjectNode) parentNode).put(fieldName, value);
    }

}
