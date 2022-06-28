package uk.gov.hmcts.reform.orgrolemapping.util;

import java.net.URLEncoder;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class GetSasToken {
    //Todo : This class is reserved for local testing with Azure service bus to publish a message. Need to remove later.

    private GetSasToken() {
        //not called
    }

    public static void main(String[] args) throws Exception  {
        System.out.println("Arrays of String :: " + args);
        if (args.length == 3) {
           var sas = getSaSToken(args[0], args[1], args[2]);
            System.out.println("Signature:: " + sas);
        } else {
            var usage = "Usage: java GetSASToken <Service Bus URI> <Key Name> <Key Value>";
            System.out.println(usage);
        }
    }

    private static String getSaSToken(String resourceUri, String keyName, String key) throws Exception {
        var epoch = System.currentTimeMillis() / 1000L;
        var week = 60 * 60 * 24 * 7;
        var expiry = Long.toString(epoch + week);

        var stringToSign = URLEncoder.encode(resourceUri, "UTF-8") + "\n" + expiry;
        var signature = getHmac256(key, stringToSign);
        return "SharedAccessSignature sr="
                + URLEncoder.encode(resourceUri, "UTF-8") + "&sig="
                + URLEncoder.encode(signature, "UTF-8") + "&se=" + expiry + "&skn=" + keyName;
    }

    public static String getHmac256(String key, String input) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256Hmac.init(secretKey);
        Base64.Encoder encoder = Base64.getEncoder();

        return new String(encoder.encode(sha256Hmac.doFinal(input.getBytes("UTF-8"))));
    }
}
