package uk.gov.hmcts.reform.orgrolemapping.befta;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class FunctionalTestUtils {

    private FunctionalTestUtils() {
    }

    public static String getSaSToken(String resourceUri, String keyName, String key) throws Exception {
        var epoch = System.currentTimeMillis() / 1000L;
        var week = 60 * 60 * 24 * 7;
        var expiry = Long.toString(epoch + week);

        var stringToSign = URLEncoder.encode(resourceUri, StandardCharsets.UTF_8) + "\n" + expiry;
        var signature = getHmac256(key, stringToSign);
        return "SharedAccessSignature sr="
                + URLEncoder.encode(resourceUri, StandardCharsets.UTF_8) + "&sig="
                + URLEncoder.encode(signature, StandardCharsets.UTF_8) + "&se=" + expiry + "&skn=" + keyName;
    }

    static String getHmac256(String key, String input) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256Hmac.init(secretKey);
        Base64.Encoder encoder = Base64.getEncoder();

        return new String(encoder.encode(sha256Hmac.doFinal(input.getBytes(StandardCharsets.UTF_8))));
    }

    public static List<String> getUserIdFromFile(String fileName) {
        try (InputStream inputStream =
                     FunctionalTestUtils.class.getClassLoader().getResourceAsStream(fileName)) {
            assert inputStream != null;
            ObjectMapper objectMapper = new ObjectMapper();
            UserRequest userRequest = objectMapper.readValue(inputStream, UserRequest.class);
            return userRequest.getUserIds();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
