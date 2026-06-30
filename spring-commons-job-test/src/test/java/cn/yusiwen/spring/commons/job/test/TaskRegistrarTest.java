package cn.yusiwen.spring.commons.job.test;

import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;
import cn.yusiwen.spring.commons.job.core.mapper.TaskInfoMapper;
import cn.yusiwen.spring.commons.job.core.registrar.TaskRegistrar;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskRegistrarTest {

    @Autowired
    private TaskRegistrar taskRegistrar;

    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Autowired
    private TestTaskBean testTaskBean;

    @Test
    void schedule_shouldRegisterTaskForExistingBean() {
        String taskId = taskRegistrar.schedule("registrar-bean-task", "0 */10 * * * ?",
                        testTaskBean, "execute")
                .params("{}")
                .concurrent(true)
                .register();

        TaskInfo saved = taskInfoMapper.findById(taskId);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("registrar-bean-task");
        assertThat(saved.getBeanName()).isEqualTo("testTaskBean");
        assertThat(saved.getMethodName()).isEqualTo("execute");
        assertThat(saved.getStatus()).isEqualTo("ENABLED");
        assertThat(saved.getCronExpression()).isEqualTo("0 */10 * * * ?");
    }

    @Test
    void schedule_shouldRegisterTaskForBeanName() {
        String taskId = taskRegistrar.schedule("registrar-beanName-task", "0 */10 * * * ?",
                        "testTaskBean", "execute")
                .register();

        TaskInfo saved = taskInfoMapper.findById(taskId);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("registrar-beanName-task");
        assertThat(saved.getBeanName()).isEqualTo("testTaskBean");
    }

    @Test
    void schedule_shouldRegisterTaskForRunnable() {
        String taskId = taskRegistrar.schedule("registrar-runnable-task", "0 */10 * * * ?",
                () -> System.out.println("runnable executed"));

        TaskInfo saved = taskInfoMapper.findById(taskId);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("registrar-runnable-task");
        assertThat(saved.getBeanName()).startsWith("__job_");
        assertThat(saved.getMethodName()).isEqualTo("execute");
    }

    @Test
    void cancelByName_shouldDisableTask() {
        String taskId = taskRegistrar.schedule("registrar-cancel-task", "0 */10 * * * ?",
                        "testTaskBean", "execute")
                .register();

        taskRegistrar.cancelByName("registrar-cancel-task");

        TaskInfo saved = taskInfoMapper.findById(taskId);
        assertThat(saved.getStatus()).isEqualTo("DISABLED");
    }

    @Test
    void scheduledTaskAnnotation_shouldAutoRegisterOnStartup() {
        TaskInfo saved = taskInfoMapper.findByName("annotated-task");
        assertThat(saved).isNotNull();
        assertThat(saved.getCronExpression()).isEqualTo("0 */5 * * * ?");
        assertThat(saved.getBeanName()).isEqualTo("scheduledTaskTestBean");
        assertThat(saved.getMethodName()).isEqualTo("execute");
        assertThat(saved.getStatus()).isEqualTo("ENABLED");
    }
}
