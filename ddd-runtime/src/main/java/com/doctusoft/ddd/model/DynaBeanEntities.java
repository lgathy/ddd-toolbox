package com.doctusoft.ddd.model;

import com.doctusoft.dynabean.SharedDynaBeanFactory;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.*;

public class DynaBeanEntities implements Instantiator {
    
    public DynaBeanEntities() {}
    
    public <T> @NotNull T instantiate(@NotNull Class<T> kind) {
        return FACTORY.create(kind);
    }
    
    public static <T extends Entity> @NotNull T copy(@NotNull T entity) {
        requireNonNull(entity, "entity");
        Class<T> kind = (Class<T>) entity.getKind();
        requireNonNull(kind, "entity.kind");
        T copy = FACTORY.copyProperties(kind, entity);
        return requireNonNull(copy, "Copy of: " + entity);
    }
    
    private static final SharedDynaBeanFactory FACTORY = new SharedDynaBeanFactory();
    
}
