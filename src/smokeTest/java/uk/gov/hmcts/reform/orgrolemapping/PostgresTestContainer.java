package uk.gov.hmcts.reform.orgrolemapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PostgresTestContainer implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(PostgresTestContainer.class);
    static final Duration DEFAULT_PG_STARTUP_WAIT = Duration.ofSeconds(10L);
    static final String POSTGRES = "postgres";
    static final DockerImageName DOCKER_DEFAULT_IMAGE_NAME = DockerImageName.parse(POSTGRES);
    private final PostgreSQLContainer<?> postgreDBContainer;
    private final UUID instanceId = UUID.randomUUID();
    static final String JDBC_URL_PREFIX = "jdbc:";

    PostgresTestContainer(Map<String, String> postgresConfig, Map<String, String> localeConfig,
                          DockerImageName image,
                          Duration pgStartupWait, String databaseName) {
        image = image.asCompatibleSubstituteFor(POSTGRES);
        this.postgreDBContainer = new PostgreSQLContainer<>(image)
                .withReuse(true)
                .withDatabaseName(databaseName)
                .withUsername(POSTGRES)
                .withPassword(POSTGRES)
                .withStartupTimeout(pgStartupWait)
                .withLogConsumer(new Slf4jLogConsumer(LOG))
                // https://github.com/docker-library/docs/blob/master/postgres/README.md#postgres_initdb_args
                .withEnv("POSTGRES_INITDB_ARGS", String.join(" ", createInitOptions(localeConfig)))
                .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust");
        final List<String> cmd = new ArrayList<>(Collections.singletonList(POSTGRES));
        cmd.addAll(createConfigOptions(postgresConfig));
        postgreDBContainer.setCommand(cmd.toArray(new String[0]));
        postgreDBContainer.start();
    }

    private List<String> createConfigOptions(final Map<String, String> postgresConfig) {
        List<String> configOptions = new ArrayList<>();

        for (Map.Entry<String, String> config : postgresConfig.entrySet()) {
            configOptions.add("-c");
            String var10001 = (String)config.getKey();
            configOptions.add(var10001 + "=" + (String)config.getValue());
        }

        return configOptions;
    }

    private List<String> createInitOptions(final Map<String, String> localeConfig) {
        List<String> localeOptions = new ArrayList<>();

        for (Map.Entry<String, String> config : localeConfig.entrySet()) {
            localeOptions.add("--" + (String)config.getKey());
            localeOptions.add((String)config.getValue());
        }

        return localeOptions;
    }

    public String getJdbcUrl(String dbName) {
        try {
            return replaceDatabase(this.postgreDBContainer.getJdbcUrl(), dbName);
        } catch (URISyntaxException var3) {
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

    public String toString() {
        return "EmbeddedPG-" + this.instanceId;
    }

    public static class Builder {
        private final Map<String, String> config = new HashMap<>();
        private final Map<String, String> localeConfig = new HashMap<>();
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
                                        "TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX"), "postgres"))
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
            this.databaseName = "postgres";
            this.config.put("timezone", "UTC");
            this.config.put("synchronous_commit", "off");
            this.config.put("max_connections", "300");
            this.config.put("fsync", "off");
        }

        public PostgresTestContainer start() {
            return new PostgresTestContainer(this.config, this.localeConfig,
                    this.image, this.pgStartupWait, this.databaseName);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                Builder builder = (Builder)o;
                return Objects.equals(this.config, builder.config) && Objects.equals(this.localeConfig,
                        builder.localeConfig)
                        && Objects.equals(this.pgStartupWait, builder.pgStartupWait)
                        && Objects.equals(this.image, builder.image)
                        && Objects.equals(this.databaseName, builder.databaseName);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.config, this.localeConfig,
                this.pgStartupWait, this.image, this.databaseName});
        }
    }
}

