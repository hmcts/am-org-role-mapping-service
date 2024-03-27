package uk.gov.hmcts.reform.orgrolemapping.controller.utils;

import com.google.common.collect.Lists;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;


public class MockUtils {

    public static final String S2S_CCD_GW = "ccd_gw";
    public static final String S2S_ORM = "am_org_role_mapping_service";
    public static final String S2S_RARB = "am_role_assignment_refresh_batch";
    public static final String S2S_XUI = "xui_webapp";

    public static final String ROLE_CASEWORKER = "caseworker";

    private MockUtils() {

    }

    public static String generateDummyS2SToken(String serviceName) {
        return Jwts.builder()
            .setSubject(serviceName)
            .setIssuedAt(new Date())
            .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
            .compact();
    }

    @NotNull
    public static HttpHeaders getHttpHeaders(String serviceName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer user1");
        var s2SToken = MockUtils.generateDummyS2SToken(serviceName);
        headers.add(SERVICE_AUTHORIZATION, "Bearer " + s2SToken);
        headers.add(Constants.CORRELATION_ID_HEADER_NAME, "38a90097-434e-47ee-8ea1-9ea2a267f51d");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public static void setSecurityAuthorities(Authentication authenticationMock, String... authorities) {
        setSecurityAuthorities("aJwtToken", authenticationMock, authorities);
    }

    public static void setSecurityAuthorities(String jwtToken,
                                                    Authentication authenticationMock,
                                                    String... authorities) {

        Jwt jwt = Jwt.withTokenValue(jwtToken)
            .claim("aClaim", "aClaim")
            .claim("aud", Lists.newArrayList(S2S_CCD_GW, S2S_ORM))
            .header("aHeader", "aHeader")
            .build();
        when(authenticationMock.getPrincipal()).thenReturn(jwt);

        Collection<? extends GrantedAuthority> authorityCollection = Stream.of(authorities)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(ArrayList::new));

        when(authenticationMock.getAuthorities()).thenAnswer(invocationOnMock -> authorityCollection);

    }

}
