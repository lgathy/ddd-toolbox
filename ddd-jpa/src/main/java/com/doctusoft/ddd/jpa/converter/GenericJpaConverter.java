package com.doctusoft.ddd.jpa.converter;

import com.google.common.base.Converter;

import javax.persistence.AttributeConverter;

import static java.util.Objects.*;

public class GenericJpaConverter<A, B> implements AttributeConverter<A, B> {
    
    private final Converter<A, B> converter;
    
    public GenericJpaConverter(Converter<A, B> converter) { this.converter = requireNonNull(converter); }
    
    public B convertToDatabaseColumn(A attribute) { return converter.convert(attribute); }
    
    public A convertToEntityAttribute(B dbData) { return converter.reverse().convert(dbData); }
    
}
