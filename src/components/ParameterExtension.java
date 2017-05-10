package components;

import kraken.container.ContainerResolver;
import kraken.unit.Container;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParameterExtension extends ContainerResolver {
    @Override
    public void resolve() {
        Container container = getContainer();

        LinkedHashMap<String, Object> parameters = container.getRawByExtensionRoot("parameters");
        for(Map.Entry<String, Object> parameter : parameters.entrySet()) {
            container.set(parameter.getKey(), parameter.getValue());
        }
    }
}
