package com.doctusoft.ddd.jpa.criteria;

import com.doctusoft.ddd.model.Entity;
import com.doctusoft.ddd.model.EntityClass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Objects.*;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityQuery<T extends Entity> {
    
    public static <T extends Entity> EntityQuery<T> create(EntityManager em, Class<T> kind, Class<? extends T> entityClass) {
        requireNonNull(em, "entityManager");
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        EntityCriteria<T> criteria = EntityCriteria.create(EntityClass.of(entityClass), criteriaBuilder);
        return new EntityQuery<>(em, entityClass, criteria);
    }
    
    @Getter(AccessLevel.NONE)
    private final EntityManager em;
    
    private final Class<? extends T> entityClass;
    
    private final EntityCriteria<T> criteria;
    
    @Getter(AccessLevel.NONE)
    private final List<Function<Root, Order>> orders = new ArrayList<>();
    
    public EntityQuery<T> where(Consumer<EntityCriteria<? super T>> conditions) {
        conditions.accept(criteria);
        return this;
    }
    
    public EntityQuery<T> asc(String attributeName) {
        orders.add(criteria.get(attributeName).andThen(attr -> criteria.builder().asc(attr)));
        return this;
    }
    
    public EntityQuery<T> desc(String attributeName) {
        orders.add(criteria.get(attributeName).andThen(attr -> criteria.builder().desc(attr)));
        return this;
    }
    
    public EntityQuery<T> idAsc() { return asc(Entity.ID); }
    
    public EntityQuery<T> idDesc() { return desc(Entity.ID); }
    
    public TypedQuery<T> query() { return em.createQuery(createCriteriaQuery()); }
    
    private CriteriaQuery<T> createCriteriaQuery() {
        CriteriaQuery<T> query = criteria.builder().createQuery((Class<T>) entityClass);
        Root<? extends T> root = query.from(entityClass);
        query.select(root);
        applyConditionsAndOrderBy(query, root);
        return query;
    }
    
    public void applyConditionsAndOrderBy(CriteriaQuery<?> query, Root<? extends T> entity) {
        criteria.applyConditions(entity, query::where);
        applyOrderBy(query, entity);
    }
    
    private void applyOrderBy(CriteriaQuery query, Root<? extends T> entity) {
        if (orders.size() > 0) {
            query.orderBy(orders
                .stream()
                .map(f -> f.apply(entity))
                .collect(Collectors.toList()));
        }
    }
    
    public TypedQuery<T> selectForUpdate() {
        return query().setLockMode(LockModeType.PESSIMISTIC_WRITE);
    }
    
    public int count() {
        CriteriaQuery<Long> query = criteria.builder().createQuery(Long.class);
        Root<? extends T> root = query.from(entityClass);
        query.select(criteria.builder().count(root));
        criteria.applyConditions(root, query::where);
        Long result = em.createQuery(query).getSingleResult();
        return Math.toIntExact(result);
    }
    
    public boolean exists() { return count() > 0; }
    
    public Optional<T> queryOptionalExpected() {
        List<T> resultList = query()
            .setMaxResults(2)
            .getResultList();
        if (resultList.isEmpty()) return Optional.empty();
        if (resultList.size() == 1) return Optional.of(resultList.get(0));
        throw new IllegalStateException("Query returned more than 1 row");
    }
    
    public T querySingletonExpected() {
        List<T> resultList = query()
            .setMaxResults(2)
            .getResultList();
        if (resultList.size() == 1) return resultList.get(0);
        String message = "Query returned " + (resultList.isEmpty() ? "no result" : "more than 1 row");
        throw new IllegalStateException(message);
    }
    
}
