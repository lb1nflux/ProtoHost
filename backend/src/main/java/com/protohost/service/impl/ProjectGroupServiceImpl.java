package com.protohost.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.protohost.dto.ProjectGroupDTO;
import com.protohost.entity.Project;
import com.protohost.entity.ProjectGroup;
import com.protohost.mapper.ProjectGroupMapper;
import com.protohost.mapper.ProjectMapper;
import com.protohost.service.ProjectGroupService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectGroupServiceImpl extends ServiceImpl<ProjectGroupMapper, ProjectGroup> implements ProjectGroupService {

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public List<ProjectGroupDTO> listByUser(Long userId) {
        List<ProjectGroup> groups = list(new LambdaQueryWrapper<ProjectGroup>()
                .eq(ProjectGroup::getUserId, userId)
                .orderByAsc(ProjectGroup::getSortOrder));

        return groups.stream().map(g -> {
            ProjectGroupDTO dto = new ProjectGroupDTO();
            BeanUtils.copyProperties(g, dto);
            Long count = projectMapper.selectCount(new LambdaQueryWrapper<Project>()
                    .eq(Project::getGroupId, g.getId()));
            dto.setProjectCount(count.intValue());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ProjectGroup create(Long userId, String name) {
        ProjectGroup group = new ProjectGroup();
        group.setUserId(userId);
        group.setName(name);
        
        // Get max sort order
        ProjectGroup maxSortGroup = getOne(new LambdaQueryWrapper<ProjectGroup>()
                .eq(ProjectGroup::getUserId, userId)
                .orderByDesc(ProjectGroup::getSortOrder)
                .last("LIMIT 1"));
        group.setSortOrder(maxSortGroup == null ? 0 : maxSortGroup.getSortOrder() + 1);
        
        save(group);
        return group;
    }

    @Override
    public void update(Long userId, Long groupId, String name) {
        ProjectGroup group = getById(groupId);
        if (group != null && group.getUserId().equals(userId)) {
            group.setName(name);
            updateById(group);
        }
    }

    @Override
    @Transactional
    public void delete(Long userId, Long groupId) {
        ProjectGroup group = getById(groupId);
        if (group != null && group.getUserId().equals(userId)) {
            // Unset groupId for all projects in this group using a wrapper to handle NULL
            projectMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Project>()
                    .eq(Project::getGroupId, groupId)
                    .set(Project::getGroupId, null));
            
            removeById(groupId);
        }
    }

    @Override
    @Transactional
    public void updateSortOrder(Long userId, List<Long> groupIds) {
        for (int i = 0; i < groupIds.size(); i++) {
            Long id = groupIds.get(i);
            ProjectGroup group = getById(id);
            if (group != null && group.getUserId().equals(userId)) {
                group.setSortOrder(i);
                updateById(group);
            }
        }
    }
}
