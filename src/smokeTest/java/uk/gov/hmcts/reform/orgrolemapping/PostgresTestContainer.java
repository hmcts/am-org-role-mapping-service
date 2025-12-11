package uk.gov.hmcts.reform.orgrolemapping;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public class PostgresTestContainer implements Closeable {
    static final Duration DEFAULT_PG_STARTUP_WAIT = Duration.ofSeconds(10L);
    static final String POSTGRES = "postgres";
    static final DockerImageName DOCKER_DEFAULT_IMAGE_NAME = DockerImageName.parse(POSTGRES);
    private final PostgreSQLContainer<?> postgreDBContainer;
    static final String JDBC_URL_PREFIX = "jdbc:";

    PostgresTestContainer(DockerImageName image,
                          Duration pgStartupWait, String databaseName) {
        image = image.asCompatibleSubstituteFor(POSTGRES);
        this.postgreDBContainer = new PostgreSQLContainer<>(image)
                .withReuse(true)
                .withDatabaseName(databaseName)
                .withUsername(POSTGRES)
                .withPassword(POSTGRES)
                .withStartupTimeout(pgStartupWait)
                .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust");
        postgreDBContainer.start();
    }

    public String getJdbcUrl(String dbName) {
        try {
            return replaceDatabase(this.postgreDBContainer.getJdbcUrl(), dbName);
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    /**
     * Replaces database name in the JDBC url.
     *
     * @param url    JDBC url
     * @param dbName Database name
     * @return Modified Url
     * @throws URISyntaxException If Url violates RFC&nbsp;2396
     */
    static String replaceDatabase(final String url, final String dbName) throws URISyntaxException {
        final URI uri = URI.create(url.substring(JDBC_URL_PREFIX.length()));
        return JDBC_URL_PREFIX + new URI(uri.getScheme(),
                uri.getUserInfo(),
                uri.getHost(),
                uri.getPort(),
                "/" + dbName,
                uri.getQuery(),
                uri.getFragment());
    }

    public void close() {
        this.postgreDBContainer.close();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Duration pgStartupWait;
        private DockerImageName image;
        private String databaseName;

        DockerImageName getDefaultImage() {
            if (this.getEnvOrProperty("PG_FULL_IMAGE") != null) {
                return DockerImageName.parse(this.getEnvOrProperty("PG_FULL_IMAGE"));
            } else {
                return this.getEnvOrProperty("TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX") != null
                        ? DockerImageName.parse(this.insertSlashIfNeeded(
                                this.getEnvOrProperty(
                                        "TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX"), POSTGRES))
                        .withTag("13-alpine") :
                        PostgresTestContainer.DOCKER_DEFAULT_IMAGE_NAME.withTag("13-alpine");
            }
        }

        String getEnvOrProperty(String key) {
            return (String)Optional.ofNullable(System.getenv(key)).orElse(System.getProperty(key));
        }

        String insertSlashIfNeeded(String prefix, String repo) {
            return !prefix.endsWith("/") && !repo.startsWith("/") ? prefix + "/" + repo : prefix + repo;
        }

        Builder() {
            this.pgStartupWait = PostgresTestContainer.DEFAULT_PG_STARTUP_WAIT;
            this.image = this.getDefaultImage();
            this.databaseName = PostgresTestContainer.POSTGRES;
        }

        public PostgresTestContainer start() {
            return new PostgresTestContainer(this.image, this.pgStartupWait, this.databaseName);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                Builder builder = (Builder)o;
                return Objects.equals(this.pgStartupWait, builder.pgStartupWait)
                        && Objects.equals(this.image, builder.image)
                        && Objects.equals(this.databaseName, builder.databaseName);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.pgStartupWait, this.image, this.databaseName});
        }
    }
}

