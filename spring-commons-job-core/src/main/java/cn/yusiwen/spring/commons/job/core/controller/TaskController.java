package cn.yusiwen.spring.commons.job.core.controller;

import cn.yusiwen.spring.commons.job.core.dto.PageResult;
import cn.yusiwen.spring.commons.job.core.dto.TaskInfoRequest;
import cn.yusiwen.spring.commons.job.core.dto.TaskInfoVO;
import cn.yusiwen.spring.commons.job.core.entity.TaskLog;
import cn.yusiwen.spring.commons.job.core.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskInfoVO> createTask(@Valid @RequestBody TaskInfoRequest request) {
        TaskInfoVO vo = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskInfoVO> updateTask(@PathVariable String id,
                                                  @Valid @RequestBody TaskInfoRequest request) {
        TaskInfoVO vo = taskService.updateTask(id, request);
        return ResponseEntity.ok(vo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskInfoVO> getTask(@PathVariable String id) {
        TaskInfoVO vo = taskService.getTask(id);
        return ResponseEntity.ok(vo);
    }

    @GetMapping
    public ResponseEntity<PageResult<TaskInfoVO>> listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PageResult<TaskInfoVO> result = taskService.listTasks(page, size, status);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/trigger")
    public ResponseEntity<Void> triggerTask(@PathVariable String id) {
        try {
            taskService.triggerTask(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<TaskInfoVO> enableTask(@PathVariable String id) {
        TaskInfoVO vo = taskService.enableTask(id);
        return ResponseEntity.ok(vo);
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<TaskInfoVO> disableTask(@PathVariable String id) {
        TaskInfoVO vo = taskService.disableTask(id);
        return ResponseEntity.ok(vo);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshTasks() {
        taskService.refreshTasks();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<PageResult<TaskLog>> listTaskLogs(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<TaskLog> result = taskService.listTaskLogs(id, page, size);
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", e.getMessage()));
    }
}
