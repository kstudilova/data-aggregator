package source;

import exception.RegistryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceRegistry {
    private final Map< String, DataSource > sources_;

    public SourceRegistry(List< DataSource > sources) {
        sources_ = new HashMap< >();

        for (DataSource source : sources) {
            sources_.put(source.getName(), source);
        }
    }

    public List< DataSource > select(List< String > names) {
        List< DataSource > selected = new ArrayList< >();

        for (String name : names) {
            DataSource source = sources_.get(name);

            if (source == null) {
                throw new RegistryException("Неизвестный источник: " + name);
            }

            selected.add(source);
        }

        return selected;
    }
}