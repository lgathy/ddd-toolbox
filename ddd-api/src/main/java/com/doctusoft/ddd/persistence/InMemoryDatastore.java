package com.doctusoft.ddd.persistence;

import com.doctusoft.ddd.model.Entity;
import com.doctusoft.ddd.model.EntityKey;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Objects.*;

@RequiredArgsConstructor
public class InMemoryDatastore implements GenericPersistence {
    
    private final NavigableMap<KeyWrapper, Entity> entityMap;
    
    @Nullable public <T extends Entity> T load(@NotNull EntityKey<T> key) {
        Entity entity = entityMap.get(new KeyWrapper(key));
        if (entity == null) {
            return null;
        }
        return encodeEntity((T) entity, key);
    }
    
    public <T extends Entity> void insert(@NotNull T entity) {
        EntityKey key = entity.getKey();
        Entity existing = entityMap.putIfAbsent(new KeyWrapper(key), decodeEntity(entity, key));
        if (existing != null) {
            throw new IllegalStateException("Entity with key " + key + " already exists: " + existing);
        }
    }
    
    public <T extends Entity> void update(@NotNull T entity) {
        EntityKey key = entity.getKey();
        Entity nullIfNotExisting = entityMap.computeIfPresent(new KeyWrapper(key), (k, oldValue) -> encodeEntity(entity, key));
        if (nullIfNotExisting == null) {
            throw new IllegalStateException("Entity with key " + key + " did not exist");
        }
    }
    
    public <T extends Entity> void remove(@NotNull T entity) {
        EntityKey key = entity.getKey();
        entityMap.remove(new KeyWrapper(key));
    }
    
    public <T extends Entity> List<T> loadSmallDataset(@NotNull Class<T> kind) throws IllegalArgumentException {
        List<T> allInstances = new ArrayList<T>(allInstanceOf(kind));
        return GenericPersistence.checkSmallDataset(kind, allInstances);
    }
    
    public <T extends Entity> Collection<T> allInstanceOf(Class<T> kind) {
        Collection entities = entityMap.subMap(KeyWrapper.startOf(kind), KeyWrapper.endOf(kind)).values();
        return Collections.unmodifiableCollection(entities);
    }
    
    @NotNull protected <T extends Entity> T decodeEntity(@NotNull T entity, @NotNull EntityKey key) {
        return entity;
    }
    
    @NotNull protected <T extends Entity> T encodeEntity(@NotNull T entity, @NotNull EntityKey key) {
        return entity;
    }
    
    public void evictCache() {
        // nothing to do
    }
    
    @EqualsAndHashCode(of = "asString", doNotUseGetters = true)
    private static final class KeyWrapper implements Comparable<KeyWrapper> {
        
        public static KeyWrapper startOf(Class<?> kind) {
            return new KeyWrapper(kind.getSimpleName() + "(");
        }
        
        public static KeyWrapper endOf(Class<?> kind) {
            return new KeyWrapper(kind.getSimpleName() + ")");
        }
        
        private final
        @Nullable EntityKey entityKey;
        
        private final
        @NotNull String asString;
        
        private KeyWrapper(String asString) {
            this.entityKey = null;
            this.asString = requireNonNull(asString);
        }
        
        public KeyWrapper(EntityKey entityKey) {
            this.entityKey = requireNonNull(entityKey);
            this.asString = entityKey.getKind().getSimpleName() + "(" + entityKey.getIdAsString() + ")";
        }
        
        public String toString() {
            return asString;
        }
        
        public int compareTo(KeyWrapper other) {
            return asString.compareTo(other.asString);
        }
    }
    
}
