package uk.gov.hmcts.reform.orgrolemapping.controller;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

public class MyEmbeddedPostgres implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(MyEmbeddedPostgres.class);
    static final Duration DEFAULT_PG_STARTUP_WAIT = Duration.ofSeconds(10L);
    static final String POSTGRES = "postgres";
    static final String ENV_DOCKER_IMAGE = "PG_FULL_IMAGE";
    static final String ENV_DOCKER_PREFIX = "TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX";
    static final DockerImageName DOCKER_DEFAULT_IMAGE_NAME = DockerImageName.parse(POSTGRES);
    static final String DOCKER_DEFAULT_TAG = "13-alpine";
    private final PostgreSQLContainer<?> postgreDBContainer;
    private final UUID instanceId = UUID.randomUUID();
    static final String JDBC_URL_PREFIX = "jdbc:";

    MyEmbeddedPostgres(Map<String, String> postgresConfig, Map<String, String> localeConfig,
                       Map<String, MyBindMount> bindMounts, Optional<Network> network,
                       Optional<String> networkAlias, DockerImageName image,
                       Duration pgStartupWait, String databaseName) throws IOException {
        image = image.asCompatibleSubstituteFor("postgres");
        this.postgreDBContainer = new PostgreSQLContainer<>(image)
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
        processBindMounts(postgreDBContainer, bindMounts);
        network.ifPresent(postgreDBContainer::withNetwork);
        networkAlias.ifPresent(postgreDBContainer::withNetworkAliases);
        postgreDBContainer.start();
    }

    private void processBindMounts(PostgreSQLContainer<?> postgreDBContainer,
                                   Map<String, MyBindMount> bindMounts) {
        bindMounts.values().stream().filter((f) ->
                (new File(f.getLocalFile())).exists()).forEach(
                        (f) -> postgreDBContainer.addFileSystemBind(f.getLocalFile(),
                                f.getRemoteFile(), f.getBindMode()));
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

    public DataSource getTemplateDatabase() {
        return this.getDatabase(this.postgreDBContainer.getUsername(), "template1");
    }

    public DataSource getTemplateDatabase(Map<String, String> properties) {
        return this.getDatabase(this.postgreDBContainer.getUsername(), "template1", properties);
    }

    public DataSource getPostgresDatabase() {
        return this.getDatabase(this.postgreDBContainer.getUsername(),
                this.postgreDBContainer.getDatabaseName());
    }

    public DataSource getPostgresDatabase(Map<String, String> properties) {
        return this.getDatabase(this.postgreDBContainer.getUsername(),
                this.postgreDBContainer.getDatabaseName(), properties);
    }

    public DataSource getDatabase(String userName, String dbName) {
        return this.getDatabase(userName, dbName, Collections.emptyMap());
    }

    public DataSource getDatabase(String userName, String dbName, Map<String, String> properties) {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(this.postgreDBContainer.getJdbcUrl());
        ds.setDatabaseName(dbName);
        ds.setUser(userName);
        ds.setPassword(this.postgreDBContainer.getPassword());
        properties.forEach((propertyKey, propertyValue) -> {
            try {
                ds.setProperty(propertyKey, propertyValue);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return ds;
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

    public String getHost() {
        return this.postgreDBContainer.getContainerIpAddress();
    }

    public int getPort() {
        return this.postgreDBContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT);
    }

    public void close() throws IOException {
        this.postgreDBContainer.close();
    }

    public static MyEmbeddedPostgres start() throws IOException {
        return builder().start();
    }

    public static MyEmbeddedPostgres.Builder builder() {
        return new MyEmbeddedPostgres.Builder();
    }

    public String getUserName() {
        return this.postgreDBContainer.getUsername();
    }

    public String getPassword() {
        return this.postgreDBContainer.getPassword();
    }

    public String toString() {
        return "EmbeddedPG-" + this.instanceId;
    }

    public static class Builder {
        private final Map<String, String> config = new HashMap<>();
        private final Map<String, String> localeConfig = new HashMap<>();
        private final Map<String, MyBindMount> bindMounts = new HashMap<>();
        private Optional<Network> network = Optional.empty();
        private Duration pgStartupWait;
        private DockerImageName image;
        private String databaseName;
        private Optional<String> networkAlias;

        DockerImageName getDefaultImage() {
            if (this.getEnvOrProperty("PG_FULL_IMAGE") != null) {
                return DockerImageName.parse(this.getEnvOrProperty("PG_FULL_IMAGE"));
            } else {
                return this.getEnvOrProperty("TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX") != null
                        ? DockerImageName.parse(this.insertSlashIfNeeded(
                                this.getEnvOrProperty(
                                        "TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX"), "postgres"))
                        .withTag("13-alpine") :
                        MyEmbeddedPostgres.DOCKER_DEFAULT_IMAGE_NAME.withTag("13-alpine");
            }
        }

        String getEnvOrProperty(String key) {
            return (String)Optional.ofNullable(System.getenv(key)).orElse(System.getProperty(key));
        }

        String insertSlashIfNeeded(String prefix, String repo) {
            return !prefix.endsWith("/") && !repo.startsWith("/") ? prefix + "/" + repo : prefix + repo;
        }

        Builder() {
            this.pgStartupWait = MyEmbeddedPostgres.DEFAULT_PG_STARTUP_WAIT;
            this.image = this.getDefaultImage();
            this.databaseName = "postgres";
            this.networkAlias = Optional.empty();
            this.config.put("timezone", "UTC");
            this.config.put("synchronous_commit", "off");
            this.config.put("max_connections", "300");
            this.config.put("fsync", "off");
        }

        public MyEmbeddedPostgres.Builder setPGStartupWait(Duration pgStartupWait) {
            Objects.requireNonNull(pgStartupWait);
            if (pgStartupWait.isNegative()) {
                throw new IllegalArgumentException("Negative durations are not permitted.");
            } else {
                this.pgStartupWait = pgStartupWait;
                return this;
            }
        }

        public MyEmbeddedPostgres.Builder setServerConfig(String key, String value) {
            this.config.put(key, value);
            return this;
        }

        public MyEmbeddedPostgres.Builder setBindMount(String localFile, String remoteFile) {
            return this.setBindMount(MyBindMount.of(localFile, remoteFile, BindMode.READ_ONLY));
        }

        public MyEmbeddedPostgres.Builder setBindMount(MyBindMount bindMount) {
            this.bindMounts.put(bindMount.getLocalFile(), bindMount);
            return this;
        }

        public MyEmbeddedPostgres.Builder setNetwork(Network network, String networkAlias) {
            this.network = Optional.ofNullable(network);
            this.networkAlias = Optional.ofNullable(networkAlias);
            return this;
        }

        public MyEmbeddedPostgres.Builder setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public MyEmbeddedPostgres.Builder setLocaleConfig(String key, String value) {
            this.localeConfig.put(key, value);
            return this;
        }

        public MyEmbeddedPostgres.Builder setImage(DockerImageName image) {
            this.image = image;
            return this;
        }

        public MyEmbeddedPostgres.Builder setTag(String tag) {
            this.image = this.image.withTag(tag);
            return this;
        }

        DockerImageName getImage() {
            return this.image;
        }

        public MyEmbeddedPostgres start() throws IOException {
            return new MyEmbeddedPostgres(this.config, this.localeConfig, this.bindMounts,
                    this.network, this.networkAlias, this.image, this.pgStartupWait, this.databaseName);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                MyEmbeddedPostgres.Builder builder = (MyEmbeddedPostgres.Builder)o;
                return Objects.equals(this.config, builder.config) && Objects.equals(this.localeConfig,
                        builder.localeConfig) && Objects.equals(this.bindMounts, builder.bindMounts)
                        && Objects.equals(this.network, builder.network)
                        && Objects.equals(this.pgStartupWait, builder.pgStartupWait)
                        && Objects.equals(this.image, builder.image)
                        && Objects.equals(this.databaseName, builder.databaseName)
                        && Objects.equals(this.networkAlias, builder.networkAlias);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.config, this.localeConfig, this.bindMounts,
                this.network, this.pgStartupWait, this.image, this.databaseName, this.networkAlias});
        }
    }
}

