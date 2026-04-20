package com.protohost.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectDTO {
    private Long id;
    private Long groupId;
    private String name;
    private String version;
    private String description;
    private Boolean isPublic;
    private String shareSlug;
    private String entryFile;
    private Long viewCount;
    private Long fileSize;
    private LocalDateTime lastUpdatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
