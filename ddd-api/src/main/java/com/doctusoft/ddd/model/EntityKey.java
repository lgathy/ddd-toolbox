package com.doctusoft.ddd.model;

import lombok.*;

import java.io.Serializable;

import static com.doctusoft.java.Failsafe.checkArgument;
import static java.util.Objects.*;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(doNotUseGetters = true, exclude = "idAsString")
public class EntityKey<T extends Entity> implements Serializable {
    
    @NonNull Class<T> kind;
    
    @NonNull Object id;
    
    @NonNull String idAsString;
    
    public String toString() {
        return kind.getSimpleName() + "(" + idAsString + ")";
    }
    
    public static <T extends EntityWithLongId> EntityKey<T> create(Class<T> kind, long id) {
        return new EntityKey<>(kind, Long.valueOf(id), Long.toString(id));
    }
    
    public static <T extends EntityWithStringId> EntityKey<T> create(Class<T> kind, String id) {
        checkArgument(!id.isEmpty(), "id was empty");
        return new EntityKey<>(kind, id, id);
    }
    
    public static <T extends EntityWithCustomId> EntityKey<T> custom(Class<T> kind, Object id) {
        // TODO we should use reflection here to transform the id to its String representation
        return new EntityKey<>(kind, id, id.toString());
    }
    
    public static <T extends EntityWithLongId> EntityKey<T> parseLongId(Class<T> kind, String idString) {
        Long id = Long.valueOf(idString);
        return new EntityKey<>(kind, id, id.toString());
    }
    
    public static <T extends EntityWithCustomId> EntityKey<T> parseCustomId(Class<T> kind, String idString) {
        // TODO we should use reflection here to parse the id from its String representation
        throw new UnsupportedOperationException();
    }
    
    public static <T extends Entity> EntityKey<? extends T> parseIdString(Class<T> kind, String idString) {
        requireNonNull(kind, "kind");
        requireNonNull(idString, "idString");
        if (EntityWithStringId.class.isAssignableFrom(kind)) {
            return create((Class) kind, idString);
        }
        if (EntityWithLongId.class.isAssignableFrom(kind)) {
            return parseLongId((Class) kind, idString);
        }
        return parseCustomId((Class) kind, idString);
    }
    
}


