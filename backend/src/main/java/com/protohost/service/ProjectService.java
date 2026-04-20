package com.protohost.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.protohost.dto.ProjectDTO;
import com.protohost.entity.Project;
import com.protohost.entity.ProjectVersion;
import com.protohost.mapper.ProjectMapper;
import com.protohost.mapper.ProjectVersionMapper;
import com.protohost.util.ZipUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectVersionMapper versionMapper;
    private final ZipUtil zipUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.upload.base-path}")
    private String basePath;

    public List<ProjectDTO> listByUser(Long userId) {
        return listByUser(userId, null);
    }

    public List<ProjectDTO> listByUser(Long userId, Long groupId) {
        LambdaQueryWrapper<Project> query = new LambdaQueryWrapper<Project>()
                .eq(Project::getUserId, userId)
                .orderByDesc(Project::getUpdatedAt);
        if (groupId != null) {
            if (groupId == 0) {
                query.isNull(Project::getGroupId);
            } else {
                query.eq(Project::getGroupId, groupId);
            }
        }
        return projectMapper.selectList(query).stream().map(this::toDTO).toList();
    }

    public ProjectDTO upload(Long userId, MultipartFile file, String name, String version,
                             String description, boolean isPublic, String accessPassword,
                             Long groupId) throws Exception {
        return upload(userId, null, file, name, version, description, isPublic, accessPassword, groupId, false, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProjectDTO upload(Long userId, Long projectId, MultipartFile file, String name, String version,
                             String description, boolean isPublic, String accessPassword,
                             Long groupId, boolean isReplace, String changelog) throws Exception {
        
        String storagePath = userId + "/" + System.currentTimeMillis();
        File destDir = new File(basePath, storagePath);

        ZipUtil.ExtractResult result = zipUtil.extract(file.getInputStream(), destDir);
        long fileSize = getDirSize(destDir);

        Project project;
        if (projectId != null) {
            project = projectMapper.selectById(projectId);
            if (project == null || !project.getUserId().equals(userId)) {
                throw new RuntimeException("项目不存在");
            }
            if (isReplace) {
                // Delete old files
                deleteDirectory(new File(basePath, project.getStoragePath()));
            }
            project.setName(name);
            project.setVersion(version == null || version.isBlank() ? project.getVersion() : version);
            project.setDescription(description);
            project.setIsPublic(isPublic);
            if (!isPublic && accessPassword != null && !accessPassword.isBlank()) {
                project.setAccessPasswordHash(passwordEncoder.encode(accessPassword));
            }
            project.setStoragePath(storagePath);
            project.setEntryFile(result.entryFile());
            project.setGroupId(groupId);
            project.setLastUpdatedAt(LocalDateTime.now());
            projectMapper.updateById(project);
        } else {
            project = new Project();
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
            project.setGroupId(groupId);
            project.setViewCount(0L);
            project.setLastUpdatedAt(LocalDateTime.now());
            projectMapper.insert(project);
        }

        // Save version record
        ProjectVersion pv = new ProjectVersion();
        pv.setProjectId(project.getId());
        pv.setVersionNumber(project.getVersion());
        pv.setChangelog(changelog);
        pv.setStoragePath(storagePath);
        pv.setFileSize(fileSize);
        versionMapper.insert(pv);

        return toDTO(project);
    }

    private long getDirSize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) size += getDirSize(f);
                else size += f.length();
            }
        }
        return size;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || !project.getUserId().equals(userId)) {
            throw new RuntimeException("项目不存在");
        }
        
        // Fetch all versions to delete their files
        List<ProjectVersion> versions = versionMapper.selectList(
                new LambdaQueryWrapper<ProjectVersion>().eq(ProjectVersion::getProjectId, projectId));
        
        for (ProjectVersion v : versions) {
            deleteDirectory(new File(basePath, v.getStoragePath()));
        }
        
        // Also delete current storage path just in case it's different from all version paths
        deleteDirectory(new File(basePath, project.getStoragePath()));
        
        projectMapper.deleteById(projectId);
    }

    public void updateGroup(Long userId, Long projectId, Long groupId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || !project.getUserId().equals(userId)) {
            throw new RuntimeException("项目不存在");
        }
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Project> update =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Project>()
                        .eq(Project::getId, projectId)
                        .set(Project::getGroupId, groupId == null || groupId == 0 ? null : groupId);
        projectMapper.update(null, update);
    }

    public void updateSettings(Long userId, Long projectId, boolean isPublic, String accessPassword) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || !project.getUserId().equals(userId)) {
            throw new RuntimeException("项目不存在");
        }
        project.setIsPublic(isPublic);
        if (!isPublic && accessPassword != null && !accessPassword.isBlank()) {
            project.setAccessPasswordHash(passwordEncoder.encode(accessPassword));
        } else if (isPublic) {
            project.setAccessPasswordHash(null);
        }
        projectMapper.updateById(project);
    }

    public List<ProjectVersion> listVersions(Long userId, Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || !project.getUserId().equals(userId)) {
            throw new RuntimeException("项目不存在");
        }
        
        // Group by versionNumber and get max ID for each to hide superseded/ghost records
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ProjectVersion> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.select("MAX(id)").eq("project_id", projectId).groupBy("version_number");
        List<Object> ids = versionMapper.selectObjs(qw);
        
        if (ids.isEmpty()) return List.of();

        return versionMapper.selectList(new LambdaQueryWrapper<ProjectVersion>()
                .in(ProjectVersion::getId, ids)
                .orderByDesc(ProjectVersion::getCreatedAt));
    }

    public void downloadVersion(Long userId, Long versionId, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        ProjectVersion pv = versionMapper.selectById(versionId);
        if (pv == null) throw new RuntimeException("版本不存在");
        
        Project project = projectMapper.selectById(pv.getProjectId());
        if (project == null || !project.getUserId().equals(userId)) {
            throw new RuntimeException("权限不足");
        }

        File dir = new File(basePath, pv.getStoragePath());
        if (!dir.exists()) throw new RuntimeException("物理文件已被删除");

        // Set response headers for download
        String filename = java.net.URLEncoder.encode(project.getName() + "_" + pv.getVersionNumber() + ".zip", "UTF-8").replace("+", "%20");
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + filename);

        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(response.getOutputStream())) {
            zipFolder(dir, dir, zos);
        }
    }

    private void zipFolder(File root, File source, java.util.zip.ZipOutputStream zos) throws java.io.IOException {
        File[] files = source.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                zipFolder(root, file, zos);
            } else {
                String path = root.toURI().relativize(file.toURI()).getPath();
                java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(path);
                zos.putNextEntry(entry);
                java.nio.file.Files.copy(file.toPath(), zos);
                zos.closeEntry();
            }
        }
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
        
        // Fetch latest version size
        ProjectVersion latest = versionMapper.selectOne(
                new LambdaQueryWrapper<ProjectVersion>()
                        .eq(ProjectVersion::getProjectId, p.getId())
                        .orderByDesc(ProjectVersion::getCreatedAt)
                        .last("LIMIT 1"));
        if (latest != null) {
            dto.setFileSize(latest.getFileSize());
        }
        
        return dto;
    }
}
