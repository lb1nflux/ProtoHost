package com.protohost.controller;

import com.protohost.dto.ProjectGroupDTO;
import com.protohost.entity.ProjectGroup;
import com.protohost.service.ProjectGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class ProjectGroupController {

    private final ProjectGroupService groupService;

    @GetMapping
    public ResponseEntity<List<ProjectGroupDTO>> list(Authentication auth) {
        return ResponseEntity.ok(groupService.listByUser(userId(auth)));
    }

    @PostMapping
    public ResponseEntity<ProjectGroup> create(Authentication auth, @RequestBody ProjectGroup body) {
        return ResponseEntity.ok(groupService.create(userId(auth), body.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(Authentication auth, @PathVariable Long id, @RequestBody ProjectGroup body) {
        groupService.update(userId(auth), id, body.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        groupService.delete(userId(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/sort")
    public ResponseEntity<Void> updateSort(Authentication auth, @RequestBody List<Long> groupIds) {
        groupService.updateSortOrder(userId(auth), groupIds);
        return ResponseEntity.ok().build();
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
