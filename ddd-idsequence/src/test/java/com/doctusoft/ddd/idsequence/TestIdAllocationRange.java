package com.doctusoft.ddd.idsequence;

import com.doctusoft.math.ClosedRange;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;
import java.util.concurrent.*;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public final class TestIdAllocationRange {
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void createNegative() {
        long randomLong = 100 + new Random().nextInt(100);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("range");
        IdAllocationRange.fromTo(randomLong, randomLong - 1);
    }
    
    @Test
    public void createNegativeCount() {
        long randomLong = new Random().nextInt(100);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("positive");
        IdAllocationRange.fromCount(randomLong, -1);
    }
    
    @Test
    public void create0Count() {
        long randomLong = 100 + new Random().nextInt(100);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("positive");
        IdAllocationRange.fromCount(randomLong, 0);
    }
    
    @Test
    public void fromRangeNull() {
        exception.expect(NullPointerException.class);
        IdAllocationRange.fromRange(null);
    }
    
    @Test
    public void create1Sized() {
        Random random = new Random();
        for (long i = 0; i < 100; ++i) {
            long randomLong = 500 - random.nextInt(1000);
            IdAllocationRange idRange = random.nextBoolean()
                ? IdAllocationRange.fromTo(randomLong, randomLong)
                : IdAllocationRange.fromRange(ClosedRange.create(randomLong, randomLong));
            assertThat(idRange, notNullValue(IdAllocationRange.class));
            assertThat("isEmpty()", idRange.isEmpty(), is(false));
            assertThat("hasRemaining()", idRange.hasRemaining(), is(true));
            assertThat("allocateNext()", idRange.allocateNext(), equalTo(randomLong));
            assertThat("isEmpty()", idRange.isEmpty(), is(false));
            assertThat("hasRemaining()", idRange.hasRemaining(), is(false));
        }
    }
    
    @Test
    public void createMaxSized() {
        IdAllocationRange idRange = IdAllocationRange.fromTo(MIN_VALUE, MAX_VALUE - 1);
        assertThat(idRange, notNullValue(IdAllocationRange.class));
        assertThat("hasRemaining()", idRange.hasRemaining(), is(true));
        assertThat("allocateNext()", idRange.allocateNext(), equalTo(MIN_VALUE));
    }
    
    @Test
    public void longMinValueAndAllocation() {
        long count = new Random().nextInt(100) + 1;
        IdAllocationRange idRange = IdAllocationRange.fromCount(MIN_VALUE, count);
        assertThat(idRange, notNullValue(IdAllocationRange.class));
        for (long i = 0; i < count; ++i) {
            assertThat("hasRemaining()", idRange.hasRemaining(), is(true));
            assertThat("allocateNext()", idRange.allocateNext(), equalTo(MIN_VALUE + i));
        }
        assertThat("hasRemaining()", idRange.hasRemaining(), is(false));
        exception.expect(IllegalStateException.class);
        exception.expectMessage("exceeded");
        idRange.allocateNext();
    }
    
    @Test
    public void longMaxValue() {
        IdAllocationRange idRange = IdAllocationRange.fromTo(MAX_VALUE, MAX_VALUE);
        assertThat(idRange, notNullValue(IdAllocationRange.class));
    }
    
    @Test
    public void allocateMaxValue() {
        IdAllocationRange idRange = IdAllocationRange.fromTo(MAX_VALUE, MAX_VALUE);
        long last = idRange.allocateNext();
        assertEquals(last, MAX_VALUE);
    }
    
    @Test
    public void allocateMaxRange() {
        long diff = ThreadLocalRandom.current().nextLong(1000L);
        IdAllocationRange idRange = IdAllocationRange.fromTo(MAX_VALUE - diff, MAX_VALUE);
        IdAllocationRange subRange = idRange.allocateRange(diff + 1L);
        assertEquals(subRange.getUpperBound().longValue(), MAX_VALUE);
        assertEquals(subRange.getLowerBound().longValue(), MAX_VALUE - diff);
    }
    
}
