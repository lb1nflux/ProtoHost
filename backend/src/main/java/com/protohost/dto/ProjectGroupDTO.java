package com.protohost.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectGroupDTO {
    private Long id;
    private String name;
    private Integer sortOrder;
    private Integer projectCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
