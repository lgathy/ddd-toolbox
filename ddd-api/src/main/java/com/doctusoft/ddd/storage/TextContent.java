package com.doctusoft.ddd.storage;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

public interface TextContent extends FileContent {
    
    @NotNull String getTextContent();
    
    @NotNull Charset getEncoding();
    
    @NotNull default byte[] getContent() {
        return getTextContent().getBytes(getEncoding());
    }
}
