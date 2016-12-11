package com.doctusoft.ddd.jpa.criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

@FunctionalInterface
public interface BinaryFilterOperation {
    
    Predicate apply(CriteriaBuilder criteriaBuilder, Expression left, Expression right);
    
}
