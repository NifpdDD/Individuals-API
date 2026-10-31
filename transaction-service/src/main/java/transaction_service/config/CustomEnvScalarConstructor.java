package transaction_service.config;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.env.EnvScalarConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Этот класс переопределяет часть конфигурации snakeyaml, чтобы использовать кастомный паттерн regex
 * <p>
 * Это решает следующую проблему: конфигурация ShardingSphere предполагает env-переменные только в формате
 * $${name::value}, а snakeyaml из коробки не умеет читать переменные в таком формате.
 */
public class CustomEnvScalarConstructor extends EnvScalarConstructor {

    public static final Pattern ENV_FORMAT = Pattern
            .compile("^\\$\\$\\{\\s*((?<name>\\w+)((?<separator>::)(?<value>\\S+)?)?)\\s*}$");

    public CustomEnvScalarConstructor() {
        super();
        this.yamlConstructors.put(ENV_TAG, new CustomEnvScalarConstructor.ConstructEnv());
    }

    private class ConstructEnv extends AbstractConstruct {

        public Object construct(Node node) {
            String val = constructScalar((ScalarNode) node);
            Matcher matcher = ENV_FORMAT.matcher(val);
            matcher.matches();
            String name = matcher.group("name");
            String value = matcher.group("value");
            String separator = matcher.group("separator");
            return apply(name, separator, value != null ? value : "", getEnv(name));
        }
    }

}