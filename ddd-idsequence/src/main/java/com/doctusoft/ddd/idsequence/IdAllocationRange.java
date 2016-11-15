package com.doctusoft.ddd.idsequence;

import com.doctusoft.ddd.model.EntityKey;
import com.doctusoft.java.Failsafe;
import com.doctusoft.math.ClosedRange;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigInteger;

import static com.doctusoft.java.Failsafe.checkArgument;
import static com.doctusoft.java.Failsafe.checkState;

@EqualsAndHashCode(callSuper = true, of = "nextValue", doNotUseGetters = true)
@ToString(callSuper = true)
public final class IdAllocationRange extends ClosedRange<Long> {
    
    public static final IdAllocationRange fromTo(long lowerBound, long upperBound) {
        checkArgument(lowerBound <= upperBound, () -> "Invalid range: " + lowerBound + " > " + upperBound);
        checkArgument(upperBound <= Long.MAX_VALUE, () -> "Limit exceeded");
        return new IdAllocationRange(lowerBound, upperBound);
    }
    
    public static final IdAllocationRange fromCount(long lowerBound, long count) {
        return new IdAllocationRange(lowerBound, calculateUpperBound(lowerBound, count));
    }
    
    public static final IdAllocationRange fromRange(ClosedRange<Long> range) {
        return fromTo(range.getLowerBound(), range.getUpperBound());
    }
    
    private static long calculateUpperBound(long lowerBound, long count) {
        checkArgument(count > 0, () -> "Count must be positive: " + count);
        return BigInteger.valueOf(lowerBound).add(BigInteger.valueOf(count)).subtract(BigInteger.ONE).longValueExact();
    }
    
    private Long nextValue;
    
    private IdAllocationRange(long lowerBound, long upperBound) {
        super(lowerBound, upperBound);
        this.nextValue = lowerBound;
    }
    
    public boolean hasRemaining() {
        if (nextValue == null) return false;
        return nextValue <= getUpperBound();
    }
    
    public ClosedRange<Long> getRemaining() {
        checkRemaining();
        return ClosedRange.create(nextValue, getUpperBound());
    }
    
    public void checkRemaining() {
        checkState(hasRemaining(), () -> "Upper bound exceeded: " + getUpperBound());
    }
    
    public long allocateNext() {
        checkRemaining();
        long value = nextValue.longValue();
        nextValue = (getUpperBound() > value) ? (value + 1L) : null;
        return value;
    }
    
    public IdAllocationRange allocateRange(long count) {
        checkRemaining();
        long first = nextValue;
        long last = calculateUpperBound(first, count);
        int c = Long.compare(getUpperBound(), last);
        if (c > 0) {
            nextValue = last + 1L;
        } else if (c == 0) {
            nextValue = null;
        } else {
            throw new IllegalStateException("Upper bound exceeded: " + getUpperBound());
        }
        return fromTo(first, last);
    }
    
    public void checkUnusedRange(EntityKey<? extends IdSequence> sequenceKey) {
        Failsafe.checkState(!hasRemaining(), () -> "Unused ID range: " + getRemaining() + " for key: " + sequenceKey);
    }
    
}
