package storage;

import exception.RegistryException;

import java.util.Map;

public class StorageRegistry {
    private final Map< String, RecordStorage > storages_;

    public StorageRegistry(Map< String, RecordStorage > storages) {
        storages_ = Map.copyOf(storages);
    }

    public RecordStorage select(String format) {
        RecordStorage storage = storages_.get(format);

        if (storage == null) {
            throw new RegistryException("Неизвестный формат хранения: " + format);
        }

        return storage;
    }
}