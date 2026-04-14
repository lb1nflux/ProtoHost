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
    public ResponseEntity<List<ProjectDTO>> list(Authentication auth) {
        return ResponseEntity.ok(projectService.listByUser(userId(auth)));
    }

    @PostMapping("/upload")
    public ResponseEntity<ProjectDTO> upload(
            Authentication auth,
            @RequestParam MultipartFile file,
            @RequestParam String name,
            @RequestParam(defaultValue = "1.0.0") String version,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "true") boolean isPublic,
            @RequestParam(required = false) String accessPassword) throws Exception {
        return ResponseEntity.ok(projectService.upload(userId(auth), file, name, version, description, isPublic, accessPassword));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        projectService.delete(userId(auth), id);
        return ResponseEntity.noContent().build();
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
