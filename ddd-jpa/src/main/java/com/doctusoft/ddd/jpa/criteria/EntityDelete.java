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
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import java.util.function.*;

import static java.util.Objects.*;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityDelete<T extends Entity> {
    
    public static <T extends Entity> EntityDelete<T> create(EntityManager em, Class<? extends T> entityClass) {
        requireNonNull(em, "entityManager");
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        EntityCriteria<T> criteria = EntityCriteria.create(EntityClass.of(entityClass), criteriaBuilder);
        CriteriaDelete criteriaDelete = criteriaBuilder.createCriteriaDelete(entityClass);
        return new EntityDelete<T>(em, criteria, criteriaDelete);
    }
    
    @Getter(AccessLevel.NONE)
    @NonNull private final EntityManager em;
    
    @NonNull private final EntityCriteria<T> criteria;
    
    @NonNull private final CriteriaDelete criteriaDelete;
    
    public EntityDelete<T> where(Consumer<EntityCriteria<? super T>> conditions) {
        conditions.accept(criteria);
        return this;
    }
    
    public Query prepare() {
        Root root = criteriaDelete.from(getEntityClass());
        criteria.applyConditions(root, criteriaDelete::where);
        return em.createQuery(criteriaDelete);
    }
    
    public Class<? extends T> getEntityClass() { return criteria.entityClass().getKind(); }
    
}
