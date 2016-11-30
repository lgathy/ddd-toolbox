package com.doctusoft.ddd.jpa.converter;

import com.doctusoft.guava.converter.CommaSeparatedSortedSetConverter;
import com.google.common.primitives.Longs;

import java.util.*;

public class JpaCommaSeparatedIdSetConverter extends GenericJpaConverter<Set<Long>, String> {
    
    public JpaCommaSeparatedIdSetConverter() {
        super(new CommaSeparatedSortedSetConverter<>(Longs.stringConverter()));
    }
    
}
