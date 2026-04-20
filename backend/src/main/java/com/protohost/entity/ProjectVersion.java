package com.protohost.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project_versions")
public class ProjectVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String versionNumber;
    private String changelog;
    private String storagePath;
    private Long fileSize;
    private LocalDateTime createdAt;
}
