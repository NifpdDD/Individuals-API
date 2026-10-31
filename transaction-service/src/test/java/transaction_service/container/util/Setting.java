package transaction_service.container.util;


import lombok.experimental.UtilityClass;
import org.testcontainers.containers.Network;

@UtilityClass
public class Setting {
    public Network GLOBAL_NETWORK = Network.newNetwork();
}
