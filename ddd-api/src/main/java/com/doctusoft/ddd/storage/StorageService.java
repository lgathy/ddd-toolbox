package com.doctusoft.ddd.storage;

import com.doctusoft.ddd.model.EntityKey;

import java.util.*;

public interface StorageService {
    
    <T extends FileContent> EntityKey<StorageObject> createArchive(EntityKey<?> referencingEntity, String category, Collection<T> archiveEntries, String archiveBaseName);
    
    EntityKey<StorageObject> createFile(EntityKey<?> referencingEntity, String category, FileContent fileContent, String mimeType);
    
    <T extends FileContent> EntityKey<StorageObject> createFileOrArchive(EntityKey<?> referencingEntity, String category, Collection<T> archiveEntries, String mimeType, String archiveBaseName);
    
    FileDownloadResponse download(EntityKey<StorageObject> key);
    
    int COMPRESSION_THRESHOLD = 1 << 11;
    
}
