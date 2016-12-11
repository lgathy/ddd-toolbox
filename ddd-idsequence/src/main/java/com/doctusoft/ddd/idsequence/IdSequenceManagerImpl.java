package com.doctusoft.ddd.idsequence;

import com.doctusoft.ddd.model.EntityKey;
import com.doctusoft.ddd.persistence.GenericPersistence;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.math.BigInteger;

import static java.util.Objects.*;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IdSequenceManagerImpl implements IdSequenceManager {
    
    private final GenericPersistence persistence;
    
    public <T extends IdSequence> IdAllocationRange allocate(EntityKey<T> key, long count) {
        requireNonNull(key);
        if (count <= 0) {
            throw new IllegalArgumentException("count=" + count + " for key: " + key);
        }
        IdSequence idSequence = persistence.require(key);
        BigInteger last = BigInteger.valueOf(idSequence.getLastValue());
        long lowerBound = last.add(BigInteger.ONE).longValueExact();
        long upperBound = last.add(BigInteger.valueOf(count)).longValueExact();
        long maxValue = idSequence.getMaxValue();
        if (upperBound > maxValue) {
            throw new IllegalArgumentException("maxValue exceeded: " + upperBound + " > " + maxValue + " for key: " + key);
        }
        IdAllocationRange idRange = IdAllocationRange.fromTo(lowerBound, upperBound);
        idSequence.setLastValue(upperBound);
        persistence.update(idSequence);
        return idRange;
    }
    
}
