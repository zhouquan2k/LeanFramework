package io.leanddd.module.file.api;

import lombok.Data;

@Data
public class File {
    String fileId;
    String fileName;
    String path;
    Long size;
    String mimeType;
    String accessUrl;
}
