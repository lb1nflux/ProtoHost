package com.protohost.controller;

import com.protohost.dto.ProjectDTO;
import com.protohost.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> list(Authentication auth, @RequestParam(required = false) Long groupId) {
        return ResponseEntity.ok(projectService.listByUser(userId(auth), groupId));
    }

    @PostMapping("/upload")
    public ResponseEntity<ProjectDTO> upload(
            Authentication auth,
            @RequestParam(required = false) Long projectId,
            @RequestParam MultipartFile file,
            @RequestParam String name,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "true") boolean isPublic,
            @RequestParam(required = false) String accessPassword,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "false") boolean isReplace,
            @RequestParam(required = false) String changelog) throws Exception {
        return ResponseEntity.ok(projectService.upload(userId(auth), projectId, file, name, version, description, isPublic, accessPassword, groupId, isReplace, changelog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        projectService.delete(userId(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/group")
    public ResponseEntity<Void> moveGroup(
            Authentication auth, 
            @PathVariable Long id, 
            @RequestParam(required = false) Long groupId) {
        projectService.updateGroup(userId(auth), id, groupId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/settings")
    public ResponseEntity<Void> updateSettings(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam boolean isPublic,
            @RequestParam(required = false) String accessPassword) {
        projectService.updateSettings(userId(auth), id, isPublic, accessPassword);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<com.protohost.entity.ProjectVersion>> listVersions(
            Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(projectService.listVersions(userId(auth), id));
    }

    @GetMapping("/versions/{versionId}/download")
    public void downloadVersion(
            Authentication auth, @PathVariable Long versionId, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        projectService.downloadVersion(userId(auth), versionId, response);
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
