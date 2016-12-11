package com.doctusoft.ddd.jpa.persistence;

import com.doctusoft.ddd.jpa.criteria.EntityQuery;
import com.doctusoft.ddd.model.*;
import com.doctusoft.ddd.persistence.GenericPersistence;
import com.doctusoft.java.Failsafe;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Objects.*;

public abstract class JpaPersistence implements GenericPersistence {
    
    @Inject private Instantiator instantiator;
    
    protected EntityManager em;
    
    protected Instantiator instantiator() { return instantiator; }
    
    public <T extends Entity> void insert(@NotNull T entity) {
        Class<? extends Entity> kind = entity.getKind();
        createPreInsertAction(kind).accept(entity);
        em.persist(entity);
    }
    
    @Nullable public <T extends Entity> T load(@NotNull EntityKey<T> key) {
        return em.find(key.getKind(), key.getId());
    }
    
    public <T extends Entity> void update(@NotNull T entity) {
        Class<? extends Entity> kind = entity.getKind();
        createUpdateAction(kind).accept(entity);
    }
    
    public <T extends Entity> void remove(@NotNull T entity) {
        em.remove(entity);
    }
    
    @Nullable public <T extends Entity> T selectForUpdate(@NotNull EntityKey<T> key) {
        return em.find(key.getKind(), key.getId(), LockModeType.PESSIMISTIC_WRITE);
    }
    
    public void evictCache() {
        if (em.isOpen()) {
            em.clear();
        }
    }
    
    public <T extends Entity> List<T> loadByIdAsc(@NotNull Class<T> kind, @NotNull Collection idList) {
        return createEntityQuery(kind)
            .where(e -> e.idIn(idList))
            .idAsc()
            .query()
            .getResultList();
    }
    
    public <T extends Entity> List<T> loadSmallDataset(@NotNull Class<T> kind) throws IllegalArgumentException {
        List<T> result = createEntityQuery(kind)
            .query()
            .setMaxResults(SMALL_DATASET_ROW_LIMIT)
            .getResultList();
        return GenericPersistence.checkSmallDataset(kind, result);
    }
    
    public <T extends Entity> void updateMany(@NotNull Class<T> kind, @NotNull Collection<? extends T> changedEntities) {
        requireNonNull(kind, "kind");
        changedEntities.forEach(createUpdateAction(kind));
    }
    
    protected <T extends Entity> EntityQuery<T> createEntityQuery(@NotNull Class<T> kind) {
        requireNonNull(kind, "kind");
        Class<? extends T> implementationClass = instantiator.getImplementationClass(kind);
        return EntityQuery.create(em, kind, implementationClass);
    }
    
    protected static <T extends Entity, U> PagedList<U> loadPage(@NotNull EntityQuery<T> query, @NotNull PageToken pageToken, @NotNull Function<? super T, U> mapperFun) {
        requireNonNull(query, "query");
        requireNonNull(pageToken, "pageToken");
        requireNonNull(mapperFun, "mapperFun");
        int totalRowCount = query.count();
        List<U> pageRows = new ArrayList<>();
        if (pageToken.getFrom() < totalRowCount) {
            pageRows = query
                .query()
                .setFirstResult(pageToken.getFrom())
                .setMaxResults(pageToken.getLimit())
                .getResultList()
                .stream()
                .map(mapperFun)
                .collect(Collectors.toList());
        }
        return new PagedList<>(pageRows, totalRowCount);
    }
    
    protected void mergeIfUnmanaged(Entity entity) {
        if (!em.contains(entity)) {
            em.merge(entity);
        }
    }
    
    protected Consumer<Entity> createUpdateAction(@NotNull Class<? extends Entity> kind) {
        Consumer<Entity> updateAction = this::mergeIfUnmanaged;
        return LastModifiedBy.class.isAssignableFrom(kind)
            ? new SetLastModifiedBy(getUserId()).andThen(updateAction)
            : updateAction;
    }
    
    protected Consumer<Entity> createPreInsertAction(@NotNull Class<? extends Entity> kind) {
        EntityClass<? extends Entity> entityClass = EntityClass.of(kind);
        Consumer<Entity> checkInstanceOf = entityClass::checkInstance;
        boolean hasCreatedBy = CreatedBy.class.isAssignableFrom(kind);
        boolean hasLastModifiedBy = LastModifiedBy.class.isAssignableFrom(kind);
        if (hasCreatedBy || hasLastModifiedBy) {
            return checkInstanceOf.andThen(createAuditAction(hasCreatedBy, hasLastModifiedBy));
        }
        return checkInstanceOf;
    }
    
    protected Consumer<Entity> createAuditAction(boolean hasCreatedBy, boolean hasLastModifiedBy) {
        String userId = getUserId();
        if (hasCreatedBy && hasLastModifiedBy) return new SetCreatedAndLastModifiedBy(userId);
        if (hasLastModifiedBy) return new SetLastModifiedBy(userId);
        if (hasCreatedBy) return new SetCreatedBy(userId);
        throw Failsafe.cannotHappen();
    }
    
    protected abstract String getUserId();
    
    @Value
    private static final class SetCreatedBy implements Consumer<Entity> {
        
        @NotNull String userId;
        
        public void accept(Entity entity) {
            CreatedBy.class.cast(entity).setCreatedBy(userId);
        }
    }
    
    @Value
    private static final class SetLastModifiedBy implements Consumer<Entity> {
        
        @NotNull String userId;
        
        public void accept(Entity entity) {
            LastModifiedBy.class.cast(entity).setLastModifiedBy(userId);
        }
    }
    
    @Value
    private static final class SetCreatedAndLastModifiedBy implements Consumer<Entity> {
        
        @NotNull String userId;
        
        public void accept(Entity entity) {
            CreatedBy.class.cast(entity).setCreatedBy(userId);
            LastModifiedBy.class.cast(entity).setLastModifiedBy(userId);
        }
    }
    
}
