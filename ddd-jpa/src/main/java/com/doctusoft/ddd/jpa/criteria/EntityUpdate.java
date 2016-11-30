package com.doctusoft.ddd.jpa.criteria;

import com.doctusoft.ddd.model.Entity;
import com.doctusoft.ddd.model.EntityClass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.function.*;

import static java.util.Objects.*;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityUpdate<T extends Entity> {
    
    public static <T extends Entity> EntityUpdate<T> create(EntityManager em, Class<? extends T> entityClass) {
        requireNonNull(em, "entityManager");
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        EntityCriteria<T> entityCriteria = EntityCriteria.create(EntityClass.of(entityClass), criteriaBuilder);
        CriteriaUpdate criteriaUpdate = criteriaBuilder.createCriteriaUpdate(entityClass);
        return new EntityUpdate<>(em, entityCriteria, criteriaUpdate);
    }
    
    @Getter(AccessLevel.NONE)
    @NonNull private final EntityManager em;
    
    @NonNull private final EntityCriteria<T> criteria;
    
    @NonNull private final CriteriaUpdate criteriaUpdate;
    
    @Getter(AccessLevel.NONE)
    private final List<Consumer<CriteriaUpdate<T>>> updateActions = new ArrayList<>();
    
    public EntityUpdate<T> where(Consumer<EntityCriteria> conditions) {
        conditions.accept(criteria);
        return this;
    }
    
    public EntityUpdate<T> addUpdateAction(Consumer<CriteriaUpdate<T>> updateAction) {
        updateActions.add(updateAction);
        return this;
    }
    
    public EntityUpdate<T> set(String attributeName, Object value) {
        return addUpdateAction(u -> u.set(attributeName, value));
    }
    
    public Query prepare() {
        Root<T> root = criteriaUpdate.from(getEntityClass());
        updateActions.forEach(c -> c.accept(criteriaUpdate));
        criteria.applyConditions(root, criteriaUpdate::where);
        return em.createQuery(criteriaUpdate);
    }
    
    public Class<? extends T> getEntityClass() { return criteria.entityClass().getKind(); }
    
}
