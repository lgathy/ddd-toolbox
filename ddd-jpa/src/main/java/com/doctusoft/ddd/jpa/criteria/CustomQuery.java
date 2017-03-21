package com.doctusoft.ddd.jpa.criteria;

import com.doctusoft.ddd.model.Entity;
import com.google.common.collect.Streams;
import lombok.Getter;

import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public abstract class CustomQuery<T extends Entity, U> {
    
    @Getter
    protected final EntityQuery<T> entityQuery;
    
    protected final CriteriaQuery<Tuple> tupleQuery;
    
    protected final Root<? extends T> root;
    
    protected final ArrayList<Predicate> additionalConditions;
    
    protected CustomQuery(EntityQuery<T> entityQuery) {
        this.entityQuery = requireNonNull(entityQuery, "entityQuery");
        this.tupleQuery = getCriteriaBuilder().createTupleQuery();
        this.root = tupleQuery.from(entityQuery.entityClass());
        this.additionalConditions = new ArrayList<>();
    }
    
    public abstract TypedQuery<Tuple> createCustomQuery();
    
    public final int count() {
        TypedQuery<Tuple> query = select(getCriteriaBuilder().count(root));
        Tuple result = query.getSingleResult();
        return result.get(0, Number.class).intValue();
    }
    
    public final TypedQuery<Tuple> select(Selection<?>... selections) {
        tupleQuery.multiselect(selections);
        Stream<Predicate> entityConditions = entityQuery.criteria().conditions().stream().map(f -> f.apply(root));
        Predicate[] conditions = Streams
            .concat(entityConditions, additionalConditions.stream())
            .toArray(Predicate[]::new);
        if (conditions.length > 0) {
            tupleQuery.where(conditions);
        }
        entityQuery.applyOrderBy(tupleQuery, root);
        return entityQuery.em().createQuery(tupleQuery);
    }
    
    public final List<U> getResultList() {
        return createCustomQuery().getResultList()
            .stream()
            .map(this::mapResult)
            .collect(Collectors.toList());
    }
    
    public abstract U mapResult(Tuple row);
    
    public final CriteriaBuilder getCriteriaBuilder() {
        return entityQuery.criteria().builder();
    }
    
    protected final <F extends Object & Entity> Root<F> join(Class<F> joinedEntityClass) {
        requireNonNull(joinedEntityClass, "joinedEntityClass");
        return tupleQuery.from(joinedEntityClass);
    }
    
    public boolean isDecorateResultsOnly() { return true; }
    
}
