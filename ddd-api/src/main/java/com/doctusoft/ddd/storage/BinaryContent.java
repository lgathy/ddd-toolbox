package com.doctusoft.ddd.storage;

import lombok.NonNull;
import lombok.Value;

@Value
public class BinaryContent implements FileContent {
    
    @NonNull String fileName;
    
    @NonNull byte[] content;
    
}
