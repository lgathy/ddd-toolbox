package com.doctusoft.ddd.storage;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileDownloadResponse implements Serializable {
    
    private String fileName;
    
    private byte[] content;
    
    private String mimeType;
    
}
