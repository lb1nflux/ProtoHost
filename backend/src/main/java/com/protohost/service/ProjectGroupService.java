package com.protohost.service;

import com.protohost.dto.ProjectGroupDTO;
import com.protohost.entity.ProjectGroup;
import java.util.List;

public interface ProjectGroupService {
    List<ProjectGroupDTO> listByUser(Long userId);
    ProjectGroup create(Long userId, String name);
    void update(Long userId, Long groupId, String name);
    void delete(Long userId, Long groupId);
    void updateSortOrder(Long userId, List<Long> groupIds);
}
