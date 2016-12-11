package com.doctusoft.ddd.jpa.converter;

import com.doctusoft.guava.converter.CommaSeparatedSortedSetConverter;
import com.google.common.base.Converter;

import java.util.*;

public class JpaCommaSeparatedStringSetConverter extends GenericJpaConverter<Set<String>, String> {
    
    public JpaCommaSeparatedStringSetConverter() {
        super(new CommaSeparatedSortedSetConverter<>(Converter.identity()));
    }
    
}
