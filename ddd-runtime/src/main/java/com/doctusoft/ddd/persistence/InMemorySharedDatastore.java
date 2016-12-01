package com.doctusoft.ddd.persistence;

import com.doctusoft.ddd.model.DynaBeanEntities;
import com.doctusoft.ddd.model.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static java.util.Objects.*;

public class InMemorySharedDatastore extends InMemoryDatastore {
    
    public static InMemorySharedDatastore singleThreaded() {
        return new InMemorySharedDatastore(TreeMap::new);
    }
    
    public static InMemorySharedDatastore multiThreaded() {
        return new InMemorySharedDatastore(ConcurrentSkipListMap::new);
    }
    
    public InMemorySharedDatastore(Supplier<? extends NavigableMap> entityMapFactory) {
        super(entityMapFactory);
    }
    
    protected <T extends Entity> @NotNull T encodeEntity(@NotNull T entity) {
        return copy(entity);
    }
    
    protected <T extends Entity> @NotNull T decodeEntity(@NotNull T entity) {
        return copy(entity);
    }
    
    protected <T extends Entity> @NotNull T copy(@NotNull T entity) {
        requireNonNull(entity, "entity");
        Class<T> kind = (Class<T>) entity.getKind();
        requireNonNull(kind, "entity.kind");
        T copy = DynaBeanEntities.INSTANCE.copyProperties(kind, entity);
        return requireNonNull(copy, "Copy of: " + entity);
    }
    
}
