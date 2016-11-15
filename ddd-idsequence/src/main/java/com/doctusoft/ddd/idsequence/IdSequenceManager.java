package com.doctusoft.ddd.idsequence;

import com.doctusoft.ddd.model.EntityKey;
import com.doctusoft.ddd.model.EntityWithLongId;
import com.doctusoft.java.Failsafe;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Objects.*;

public interface IdSequenceManager {
    
    <T extends IdSequence> IdAllocationRange allocate(EntityKey<T> key, long count);
    
    default <T extends EntityWithLongId> Stream<T> allocateAndSetEntityIds(EntityKey<? extends IdSequence> sequenceKey, Collection<T> entities) {
        return allocateAndSetEntityIds(sequenceKey, entities, Function.identity());
    }
    
    default <T, E extends EntityWithLongId> Stream<E> allocateAndSetEntityIds(EntityKey<? extends IdSequence> sequenceKey, Collection<T> entityBuilders, Function<? super T, ? extends E> buildFun) {
        requireNonNull(buildFun);
        int count = entityBuilders.size();
        if (count > 0) {
            IdAllocationRange idRange = allocate(sequenceKey, count);
            ArrayList<E> entities = new ArrayList<>(count);
            for (T builder : entityBuilders) {
                E entity = buildFun.apply(builder);
                Failsafe.checkState(entity != null, () -> buildFun + " returned null for builder: " + builder);
                checkEntityIdUnassigned(entity);
                entity.setId(idRange.allocateNext());
                entities.add(entity);
            }
            idRange.checkUnusedRange(sequenceKey);
            return entities.stream().sequential();
        }
        return Stream.empty();
    }
    
    static void checkEntityIdUnassigned(EntityWithLongId entity) {
        Failsafe.checkState(entity.getId() == null, () -> "Entity.id already assigned: " + entity.getKey());
    }
    
}
