package com.doctusoft.ddd.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadResponse implements Serializable {
    
    private String fileName;
    
    private byte[] content;
    
    private String mimeType;
    
}
