package com.doctusoft.ddd.storage;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.nio.charset.Charset;

@Value
@Builder
public class StringContent implements TextContent {
    
    public static StringContent parse(String fileName, Charset encoding, byte[] binaryContent) {
        String textContent = new String(binaryContent, encoding);
        return new StringContent(fileName, encoding, textContent);
    }
    
    @NonNull String fileName;
    
    @NonNull Charset encoding;
    
    @NonNull String textContent;
    
}
