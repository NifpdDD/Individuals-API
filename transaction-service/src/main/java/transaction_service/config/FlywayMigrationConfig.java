package transaction_service.config;


import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.env.EnvScalarConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
@Configuration
public class FlywayMigrationConfig {

    @Value("${spring.datasource.sharding-config-path}")
    private String shardingConfigPath;

    @Bean
    public FlywayMigrationInitializer flywayMigrationInitializer() throws IOException {
        String dataSourcesConfig = getDataSourcesConfig();
        List<Map<String, String>> dataSources = getDataSources(dataSourcesConfig);
        return new FlywayMigrationInitializer(Flyway.configure().load(), _ -> {
            for (Map<String, String> dataSource : dataSources) {
                Flyway.configure()
                        .dataSource(dataSource.get("jdbcUrl"), dataSource.get("username"), dataSource.get("password"))
                        .locations("classpath:db/migration/v1")
                        .load().migrate();
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
        Yaml yaml = new Yaml(new CustomEnvScalarConstructor());
        yaml.addImplicitResolver(EnvScalarConstructor.ENV_TAG, CustomEnvScalarConstructor.ENV_FORMAT, null);
        Map<String, Object> shardingProperties = yaml.load(dataSourcesConfig);
        Map<String, Map<String, String>> dataSources = (Map<String, Map<String, String>>) shardingProperties.get("dataSources");
        return new ArrayList<>(dataSources.values());
    }
}