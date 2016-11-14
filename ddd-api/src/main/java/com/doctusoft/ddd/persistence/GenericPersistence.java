package com.doctusoft.ddd.persistence;

import com.doctusoft.ddd.model.Entity;
import com.doctusoft.ddd.model.EntityClass;
import com.doctusoft.ddd.model.EntityKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.*;

import static com.doctusoft.java.Failsafe.checkArgument;

public interface GenericPersistence {
    
    /* Primitive operations: */
    
    @Nullable <T extends Entity> T load(@NotNull EntityKey<T> key);
    
    @NotNull default <T extends Entity> Optional<T> find(@NotNull EntityKey<T> key) {
        return Optional.ofNullable(load(key));
    }
    
    @NotNull default <T extends Entity> T require(@NotNull EntityKey<T> key) {
        return find(key).orElseThrow(() -> new EntityNotFoundException(key));
    }
    
    <T extends Entity> void insert(@NotNull T entity);
    
    <T extends Entity> void update(@NotNull T entity);
    
    <T extends Entity> void remove(@NotNull T entity);
    
    default <T extends Entity> void save(@NotNull T entitiy) {
        Entity existing = selectForUpdate(entitiy.getKey());
        if (existing == null) {
            insert(entitiy);
        } else {
            update(entitiy);
        }
    }
    
    @Nullable default <T extends Entity> T selectForUpdate(@NotNull EntityKey<T> key) {
        return load(key);
    }
    
    @NotNull default <T extends Entity> T requireForUpdate(@NotNull EntityKey<T> key) {
        T entity = selectForUpdate(key);
        if (entity == null) {
            throw new EntityNotFoundException(key);
        }
        return entity;
    }
    
    void evictCache();
    
    /* BATCH operations: */
    
    default <T extends Entity> List<T> loadByIdAsc(@NotNull Class<T> kind, @NotNull Collection idList) {
        
        EntityClass<T> entityClass = EntityClass.of(kind);
        ArrayList<T> resultList = new ArrayList<>(idList.size());
        
        Stream<Object> idsOrdered = idList.stream()
            .map(entityClass::checkId)
            .sorted();
        
        idsOrdered
            .map(entityClass::toKey)
            .forEachOrdered(key -> {
                find(key).ifPresent(resultList::add);
            });
        
        return resultList;
    }
    
    default <T extends Entity> void insertMany(@NotNull Class<T> kind, @NotNull Collection<? extends T> newEntities) {
        EntityClass<T> entityClass = EntityClass.of(kind);
        newEntities
            .stream()
            .forEach(entity -> {
                entityClass.checkInstance(entity);
                insert(entity);
            });
    }
    
    default <T extends Entity> void updateMany(@NotNull Class<T> kind,
        @NotNull Collection<? extends T> changedEntities) {
        EntityClass<T> entityClass = EntityClass.of(kind);
        changedEntities
            .stream()
            .forEach(entity -> {
                entityClass.checkInstance(entity);
                update(entity);
            });
    }
    
    /**
     * @param kind
     * @return All instances of an entityKind, if it counts less than {@link #SMALL_DATASET_ROW_LIMIT}.
     * @throws IllegalArgumentException If count >= {@link #SMALL_DATASET_ROW_LIMIT}.
     */
    <T extends Entity> List<T> loadSmallDataset(@NotNull Class<T> kind) throws IllegalArgumentException;
    
    int SMALL_DATASET_ROW_LIMIT = 1000;
    
    /* Exception handling: */
    
    static <T extends Entity> List<T> checkSmallDataset(Class<T> kind, List<T> entities) {
        checkArgument(entities.size() < SMALL_DATASET_ROW_LIMIT, () -> kind.getSimpleName() + " has too many rows");
        return entities;
    }
    
}
