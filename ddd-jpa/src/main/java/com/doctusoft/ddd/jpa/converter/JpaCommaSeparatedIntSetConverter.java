package com.doctusoft.ddd.jpa.converter;

import com.doctusoft.guava.converter.CommaSeparatedSortedSetConverter;
import com.google.common.primitives.Ints;

import java.util.*;

public class JpaCommaSeparatedIntSetConverter extends GenericJpaConverter<Set<Integer>, String> {
    
    public JpaCommaSeparatedIntSetConverter() {
        super(new CommaSeparatedSortedSetConverter<>(Ints.stringConverter()));
    }
    
}
