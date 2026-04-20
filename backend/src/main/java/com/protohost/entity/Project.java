package com.protohost.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("projects")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long groupId;
    private String name;
    private String version;
    private String description;
    private Boolean isPublic;
    private String accessPasswordHash;
    private String shareSlug;
    private String entryFile;
    private String storagePath;
    private Long viewCount;
    private LocalDateTime lastUpdatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
