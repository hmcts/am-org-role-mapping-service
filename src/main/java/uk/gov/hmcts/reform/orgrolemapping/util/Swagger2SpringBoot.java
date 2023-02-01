package uk.gov.hmcts.reform.orgrolemapping.util;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityScheme(name = "ServiceAuthorization", type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER, bearerFormat = "JWT", description = "ServiceAuthorization")
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.orgrolemapping"})
public class Swagger2SpringBoot implements CommandLineRunner {

    public static void main(String[] args) {
        new SpringApplication(Swagger2SpringBoot.class).run("");
    }

    @Override
    public void run(String... arg0) {
        if (arg0.length > 0 && arg0[0].equals("exitcode")) {
            throw new ExitException();
        }
    }

    class ExitException extends RuntimeException implements ExitCodeGenerator {
        private static final long serialVersionUID = 1L;

        @Override
        public int getExitCode() {
            return 10;
        }

    }
}
