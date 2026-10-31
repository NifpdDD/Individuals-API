package transaction_service.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.env.EnvScalarConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
@Configuration
@Profile("!test")
public class FlywayMigrationConfig {

    @Value("${spring.datasource.sharding-config-path}")
    private String shardingConfigPath;

    // Регулярное выражение для поиска $${ПЕРЕМЕННАЯ::дефолт} внутри строки
    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\$\\{(\\w+)::([^}]*)\\}");

    @Bean
    public FlywayMigrationInitializer flywayMigrationInitializer() throws IOException {
        String dataSourcesConfig = getDataSourcesConfig();
        List<Map<String, String>> dataSources = getDataSources(dataSourcesConfig);
        return new FlywayMigrationInitializer(Flyway.configure().load(), _ -> {
            for (Map<String, String> dataSource : dataSources) {
                String rawJdbcUrl = dataSource.get("jdbcUrl");
                String resolvedJdbcUrl = resolveEnvPlaceholders(rawJdbcUrl);

                Flyway.configure()
                        .dataSource(resolvedJdbcUrl, dataSource.get("username"), dataSource.get("password"))
                        .locations("classpath:db/migration/v1")
                        .baselineOnMigrate(true)
                        .load()
                        .migrate();
            }
        });
    }

    protected String getDataSourcesConfig() throws IOException {
        String shardingConfig;
        try (InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(shardingConfigPath)) {
            shardingConfig = new String(Objects.requireNonNull(inputStream).readAllBytes(), StandardCharsets.UTF_8);
        }
        shardingConfig = shardingConfig.substring(0, shardingConfig.indexOf("rules"));
        return shardingConfig;
    }

    protected static List<Map<String, String>> getDataSources(String dataSourcesConfig) {
        // Чистый парсер без кастомных конструкторов, так как мы обрабатываем строку ниже самостоятельно
        Yaml yaml = new Yaml();
        Map<String, Object> shardingProperties = yaml.load(dataSourcesConfig);
        Map<String, Map<String, String>> dataSources = (Map<String, Map<String, String>>) shardingProperties.get("dataSources");
        return new ArrayList<>(dataSources.values());
    }

    /**
     * Ищет вхождения $${VAR::default} и заменяет их на переменные окружения или дефолтные значения.
     */
    private static String resolveEnvPlaceholders(String text) {
        if (text == null) return null;

        Matcher matcher = ENV_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String envName = matcher.group(1);
            String defaultValue = matcher.group(2);

            // Сначала смотрим в переменные окружения ОС (Docker), если пусто — берем дефолт
            String envValue = System.getenv(envName);
            if (envValue == null || envValue.isEmpty()) {
                envValue = defaultValue;
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(envValue));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}