package com.protohost.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectDTO {
    private Long id;
    private String name;
    private String version;
    private String description;
    private Boolean isPublic;
    private String shareSlug;
    private String entryFile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
