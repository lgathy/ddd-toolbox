package com.doctusoft.ddd.jpa.converter;

import com.doctusoft.guava.converter.MultipleLinesConverter;

import java.util.*;

public class JpaMultipleLinesConverter extends GenericJpaConverter<List<String>, String> {
    
    public JpaMultipleLinesConverter() { super(MultipleLinesConverter.INSTANCE); }
    
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null|| attribute.isEmpty()) return null;
        return super.convertToDatabaseColumn(attribute);
    }
    
}
