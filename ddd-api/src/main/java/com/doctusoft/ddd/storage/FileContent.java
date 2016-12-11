package com.doctusoft.ddd.storage;

import org.jetbrains.annotations.NotNull;

public interface FileContent {
    
    @NotNull String getFileName();
    
    @NotNull byte[] getContent();
    
}
