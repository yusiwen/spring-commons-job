package cn.yusiwen.spring.commons.job.test;

import cn.yusiwen.spring.commons.job.core.dto.TaskInfoRequest;
import cn.yusiwen.spring.commons.job.core.dto.TaskInfoVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskInfoRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new TaskInfoRequest();
        validRequest.setName("test-task");
        validRequest.setCronExpression("0/30 * * * * ?");
        validRequest.setBeanName("testTaskBean");
        validRequest.setMethodName("execute");
        validRequest.setConcurrent(false);
    }

    @Test
    void createTask_shouldReturn201() throws Exception {
        String json = objectMapper.writeValueAsString(validRequest);
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("test-task"))
                .andExpect(jsonPath("$.status").value("ENABLED"));
    }

    @Test
    void createTask_withInvalidRequest_shouldReturn400() throws Exception {
        TaskInfoRequest invalid = new TaskInfoRequest();
        String json = objectMapper.writeValueAsString(invalid);
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTask_shouldReturnTask() throws Exception {
        String json = objectMapper.writeValueAsString(validRequest);
        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();
        TaskInfoVO vo = objectMapper.readValue(response, TaskInfoVO.class);

        mockMvc.perform(get("/api/tasks/{id}", vo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vo.getId()))
                .andExpect(jsonPath("$.name").value("test-task"));
    }

    @Test
    void getTask_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/tasks/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listTasks_shouldReturnPagedResult() throws Exception {
        String json = objectMapper.writeValueAsString(validRequest);
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        mockMvc.perform(get("/api/tasks")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    void triggerTask_shouldExecute() throws Exception {
        String json = objectMapper.writeValueAsString(validRequest);
        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();
        TaskInfoVO vo = objectMapper.readValue(response, TaskInfoVO.class);

        mockMvc.perform(post("/api/tasks/{id}/trigger", vo.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void enableDisableTask_shouldWork() throws Exception {
        String json = objectMapper.writeValueAsString(validRequest);
        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();
        TaskInfoVO vo = objectMapper.readValue(response, TaskInfoVO.class);

        mockMvc.perform(put("/api/tasks/{id}/disable", vo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISABLED"));

        mockMvc.perform(put("/api/tasks/{id}/enable", vo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENABLED"));
    }

    @Test
    void deleteTask_shouldRemoveTask() throws Exception {
        String json = objectMapper.writeValueAsString(validRequest);
        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();
        TaskInfoVO vo = objectMapper.readValue(response, TaskInfoVO.class);

        mockMvc.perform(delete("/api/tasks/{id}", vo.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", vo.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTask_shouldUpdateAndReschedule() throws Exception {
        String json = objectMapper.writeValueAsString(validRequest);
        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();
        TaskInfoVO vo = objectMapper.readValue(response, TaskInfoVO.class);

        validRequest.setName("updated-task");
        validRequest.setCronExpression("0/15 * * * * ?");
        String updateJson = objectMapper.writeValueAsString(validRequest);

        mockMvc.perform(put("/api/tasks/{id}", vo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated-task"));
    }

    @Test
    void refreshTasks_shouldReloadFromDatabase() throws Exception {
        String json = objectMapper.writeValueAsString(validRequest);
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        mockMvc.perform(post("/api/tasks/refresh"))
                .andExpect(status().isOk());
    }
}
