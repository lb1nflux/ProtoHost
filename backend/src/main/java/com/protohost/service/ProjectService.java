package com.protohost.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.protohost.dto.ProjectDTO;
import com.protohost.entity.Project;
import com.protohost.mapper.ProjectMapper;
import com.protohost.util.ZipUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final ZipUtil zipUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.upload.base-path}")
    private String basePath;

    public List<ProjectDTO> listByUser(Long userId) {
        return projectMapper.selectList(
                new LambdaQueryWrapper<Project>().eq(Project::getUserId, userId)
                        .orderByDesc(Project::getUpdatedAt))
                .stream().map(this::toDTO).toList();
    }

    public ProjectDTO upload(Long userId, MultipartFile file, String name, String version,
                             String description, boolean isPublic, String accessPassword) throws Exception {
        String storagePath = userId + "/" + System.currentTimeMillis();
        File destDir = new File(basePath, storagePath);

        ZipUtil.ExtractResult result = zipUtil.extract(file.getInputStream(), destDir);

        Project project = new Project();
        project.setUserId(userId);
        project.setName(name);
        project.setVersion(version == null || version.isBlank() ? "1.0.0" : version);
        project.setDescription(description);
        project.setIsPublic(isPublic);
        project.setAccessPasswordHash(
                (!isPublic && accessPassword != null && !accessPassword.isBlank())
                        ? passwordEncoder.encode(accessPassword) : null);
        project.setShareSlug(generateSlug());
        project.setEntryFile(result.entryFile());
        project.setStoragePath(storagePath);
        projectMapper.insert(project);
        return toDTO(project);
    }

    public void delete(Long userId, Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || !project.getUserId().equals(userId)) {
            throw new RuntimeException("项目不存在");
        }
        deleteDirectory(new File(basePath, project.getStoragePath()));
        projectMapper.deleteById(projectId);
    }

    private void deleteDirectory(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) {
            if (f.isDirectory()) deleteDirectory(f);
            else f.delete();
        }
        dir.delete();
    }

    private String generateSlug() {
        byte[] bytes = new byte[8];
        new Random().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public ProjectDTO toDTO(Project p) {
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(p, dto);
        return dto;
    }
}
