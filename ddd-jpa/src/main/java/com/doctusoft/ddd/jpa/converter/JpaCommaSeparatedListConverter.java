package com.doctusoft.ddd.jpa.converter;

import com.doctusoft.guava.converter.CommaSeparatedListConverter;

import java.util.*;

public class JpaCommaSeparatedListConverter extends GenericJpaConverter<List<String>, String> {
    
    public JpaCommaSeparatedListConverter() { super(CommaSeparatedListConverter.INSTANCE); }
    
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null|| attribute.isEmpty()) return null;
        return super.convertToDatabaseColumn(attribute);
    }
    
}
