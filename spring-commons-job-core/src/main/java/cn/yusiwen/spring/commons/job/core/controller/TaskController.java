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

/**
 * REST controller exposing task management endpoints.
 * <p>Base path: {@code /api/tasks}</p>
 *
 * @see TaskService
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Creates a new scheduled task.
     *
     * @param request the task creation request
     * @return the created task with HTTP 201
     */
    @PostMapping
    public ResponseEntity<TaskInfoVO> createTask(@Valid @RequestBody TaskInfoRequest request) {
        TaskInfoVO vo = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vo);
    }

    /**
     * Updates an existing task and reschedules it.
     *
     * @param id      the task ID
     * @param request the updated task data
     * @return the updated task
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskInfoVO> updateTask(@PathVariable String id,
                                                  @Valid @RequestBody TaskInfoRequest request) {
        TaskInfoVO vo = taskService.updateTask(id, request);
        return ResponseEntity.ok(vo);
    }

    /**
     * Deletes a task and cancels its scheduling.
     *
     * @param id the task ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a task by its ID.
     *
     * @param id the task ID
     * @return the task details
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskInfoVO> getTask(@PathVariable String id) {
        TaskInfoVO vo = taskService.getTask(id);
        return ResponseEntity.ok(vo);
    }

    /**
     * Lists tasks with optional status filtering and pagination.
     *
     * @param page   the page number (1-indexed)
     * @param size   the page size
     * @param status optional status filter
     * @return a paginated list of tasks
     */
    @GetMapping
    public ResponseEntity<PageResult<TaskInfoVO>> listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PageResult<TaskInfoVO> result = taskService.listTasks(page, size, status);
        return ResponseEntity.ok(result);
    }

    /**
     * Manually triggers a task execution.
     *
     * @param id the task ID
     * @return HTTP 200 on success, HTTP 500 on failure
     */
    @PostMapping("/{id}/trigger")
    public ResponseEntity<Void> triggerTask(@PathVariable String id) {
        try {
            taskService.triggerTask(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Enables a task and activates its scheduling.
     *
     * @param id the task ID
     * @return the updated task
     */
    @PutMapping("/{id}/enable")
    public ResponseEntity<TaskInfoVO> enableTask(@PathVariable String id) {
        TaskInfoVO vo = taskService.enableTask(id);
        return ResponseEntity.ok(vo);
    }

    /**
     * Disables a task and cancels its scheduling.
     *
     * @param id the task ID
     * @return the updated task
     */
    @PutMapping("/{id}/disable")
    public ResponseEntity<TaskInfoVO> disableTask(@PathVariable String id) {
        TaskInfoVO vo = taskService.disableTask(id);
        return ResponseEntity.ok(vo);
    }

    /**
     * Cancels all scheduled tasks and reloads them from the database.
     *
     * @return HTTP 200
     */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshTasks() {
        taskService.refreshTasks();
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves execution logs for a task with pagination.
     *
     * @param id   the task ID
     * @param page the page number (1-indexed)
     * @param size the page size
     * @return a paginated list of execution logs
     */
    @GetMapping("/{id}/logs")
    public ResponseEntity<PageResult<TaskLog>> listTaskLogs(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<TaskLog> result = taskService.listTaskLogs(id, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Handles {@link IllegalArgumentException} thrown by service methods.
     *
     * @param e the exception
     * @return HTTP 404 with an error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", e.getMessage()));
    }
}
