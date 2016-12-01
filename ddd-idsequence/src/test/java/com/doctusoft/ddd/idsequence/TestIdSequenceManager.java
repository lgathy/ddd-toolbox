package com.doctusoft.ddd.idsequence;

import com.doctusoft.ddd.model.EntityKey;
import com.doctusoft.ddd.persistence.GenericPersistence;
import com.doctusoft.ddd.persistence.InMemoryDatastore;
import com.doctusoft.java.AnException;
import com.doctusoft.math.ClosedRange;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;

import static com.doctusoft.java.LambdAssert.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestIdSequenceManager {
    
    private static final String DEFAULT_ID = "test_entity_id";
    private static final EntityKey<IdSequenceTestdata> DEFAULT_KEY = IdSequenceTestdata.createKey(DEFAULT_ID);
    private static final Long sequenceInitialValue = 1L;
    private static final Long getSequenceMaxValue = 10L;
    
    private GenericPersistence persistence;
    
    private IdSequenceManager manager;
    
    @Before
    public void setup() {
        persistence = InMemoryDatastore.singleThreaded();
        manager = new IdSequenceManagerImpl(persistence);
        persistence.insert(createSequence());
    }
    
    private IdSequenceTestdata createSequence() {
        IdSequenceTestdata entity = new IdSequenceTestdata();
        entity.setId(DEFAULT_ID);
        entity.setLastValue(sequenceInitialValue);
        entity.setMaxValue(getSequenceMaxValue);
        return entity;
    }
    
    @Test
    public void nullEntityThrowsException() {
        assertThrows(() -> manager.allocate(null, 1), AnException.of(NullPointerException.class));
    }
    
    @Test
    public void allocateExceedsMaxValueThrowsException() {
        assertThrows(() -> manager.allocate(DEFAULT_KEY, 10_000L), AnException.of(IllegalArgumentException.class));
    }
    
    @Test
    public void nonPositiveIdAllocationThrowsException() {
        assertThrows(() -> manager.allocate(DEFAULT_KEY, -1), AnException.of(IllegalArgumentException.class));
    }
    
    @Test
    public void overusedAllocatedIdsThrowException() {
        ClosedRange<Long> closedRange = manager.allocate(DEFAULT_KEY, 1);
        IdAllocationRange allocationRange = IdAllocationRange.fromRange(closedRange);
        allocationRange.allocateNext(); //should not throw Exception
        
        assertThrows(() -> allocationRange.allocateNext(), AnException.of(IllegalStateException.class));
    }
    
    @Test
    public void ifNotAllAllocatedIdsUsedAllocationRangeHasRemaining() {
        ClosedRange<Long> closedRange = manager.allocate(DEFAULT_KEY, 2);
        IdAllocationRange allocationRange = IdAllocationRange.fromRange(closedRange);
        allocationRange.allocateNext();
        assertTrue(allocationRange.hasRemaining());
    }
    
    @Test
    public void usedAllocatedIdsShouldNotThrowException() {
        ClosedRange<Long> closedRange = manager.allocate(DEFAULT_KEY, 1);
        IdAllocationRange allocationRange = IdAllocationRange.fromRange(closedRange);
        allocationRange.allocateNext();
        assertFalse(allocationRange.hasRemaining());
    }
    
    @Test
    public void testMultipleAllocationsProcuedDifferentRanges() {
        Long count1 = 3L;
        IdAllocationRange range1 = manager.allocate(DEFAULT_KEY, count1);
        assertTrue(range1.getLowerBound().equals(sequenceInitialValue + 1));
        assertTrue(range1.getUpperBound().equals(sequenceInitialValue + count1));
        
        Long count2 = 5L;
        IdAllocationRange range2 = manager.allocate(DEFAULT_KEY, count2);
        assertTrue(range2.getLowerBound().equals(sequenceInitialValue + 1 + count1));
        assertTrue(range2.getUpperBound().equals(sequenceInitialValue + count1 + count2));
    }
    
    @Data
    public static class IdSequenceTestdata implements IdSequence {
        
        static EntityKey<IdSequenceTestdata> createKey(String id) {
            return EntityKey.create(IdSequenceTestdata.class, id);
        }
        
        public Class<IdSequenceTestdata> getKind() { return IdSequenceTestdata.class; }
        
        public EntityKey<IdSequenceTestdata> getKey() {return createKey(getId()); }
        
        private String id;
        
        private Long lastValue;
        
        private Long maxValue;
    }
    
}
