package com.doctusoft.ddd.jpa.converter;

import com.doctusoft.guava.converter.MultipleLinesConverter;

import java.util.*;

public class JpaMultipleLinesConverter extends GenericJpaConverter<List<String>, String> {
    
    public JpaMultipleLinesConverter() { super(MultipleLinesConverter.INSTANCE); }
    
}
