package cn.yusiwen.spring.commons.job.core.executor;

import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * {@link TaskExecutor} that invokes the target method on a local Spring bean
 * using reflection.
 * <p>Parameters are deserialized from JSON using Jackson. The target bean is
 * resolved from the Spring {@link ApplicationContext} by its bean name.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalTaskExecutor implements TaskExecutor {

    private final ApplicationContext applicationContext;

    /**
     * Looks up the Spring bean by name, resolves the target method, and invokes
     * it with deserialized parameters.
     *
     * @param taskInfo the task definition containing bean/method/params
     * @throws Exception if the bean or method is not found, or invocation fails
     */
    @Override
    public void execute(TaskInfo taskInfo) throws Exception {
        Object bean = applicationContext.getBean(taskInfo.getBeanName());

        Class<?> clazz = bean.getClass();
        Method method = findMethod(clazz, taskInfo.getMethodName());

        if (method == null) {
            throw new NoSuchMethodException("Method " + taskInfo.getMethodName()
                    + " not found on bean " + taskInfo.getBeanName());
        }

        Object[] args = resolveArgs(method, taskInfo.getParams());
        log.info("Executing task [{}] -> {}.{}()", taskInfo.getName(),
                taskInfo.getBeanName(), taskInfo.getMethodName());
        method.invoke(bean, args);
    }

    /**
     * Returns {@code "LOCAL"} as the executor type.
     *
     * @return {@code "LOCAL"}
     */
    @Override
    public String getType() {
        return "LOCAL";
    }

    /**
     * Finds a method by name on the given class, checking public methods only.
     *
     * @param clazz the class to inspect
     * @param methodName the method name to find
     * @return the matching method, or {@code null} if not found
     */
    private Method findMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Deserializes the JSON parameter string into an argument array matching
     * the method's parameter types.
     *
     * @param method the target method
     * @param paramsJson the JSON parameter string
     * @return an array of deserialized arguments
     * @throws Exception if JSON parsing fails
     */
    private Object[] resolveArgs(Method method, String paramsJson) throws Exception {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 0) {
            return new Object[0];
        }
        if (paramsJson == null || paramsJson.isEmpty()) {
            return new Object[0];
        }
        ObjectMapper mapper = new ObjectMapper();
        if (paramTypes.length == 1) {
            return new Object[]{mapper.readValue(paramsJson, paramTypes[0])};
        }
        List<Map<String, Object>> paramList = mapper.readValue(paramsJson, List.class);
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (i < paramList.size()) {
                args[i] = mapper.convertValue(paramList.get(i), paramTypes[i]);
            }
        }
        return args;
    }
}
