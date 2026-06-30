package cn.yusiwen.spring.commons.job.core.mapper;

import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

@Mapper
public interface TaskInfoMapper {

    @Results(id = "taskInfoMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "cronExpression", column = "cron_expression"),
            @Result(property = "beanName", column = "bean_name"),
            @Result(property = "methodName", column = "method_name"),
            @Result(property = "params", column = "params"),
            @Result(property = "status", column = "status"),
            @Result(property = "executeMode", column = "execute_mode"),
            @Result(property = "callbackUrl", column = "callback_url"),
            @Result(property = "concurrent", column = "concurrent"),
            @Result(property = "lastTriggerAt", column = "last_trigger_at"),
            @Result(property = "lastEndAt", column = "last_end_at"),
            @Result(property = "lastResult", column = "last_result"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("SELECT * FROM task_info WHERE id = #{id}")
    TaskInfo findById(@Param("id") String id);

    @ResultMap("taskInfoMap")
    @Select("SELECT * FROM task_info WHERE status = #{status}")
    List<TaskInfo> findByStatus(@Param("status") String status);

    @ResultMap("taskInfoMap")
    @Select("SELECT * FROM task_info ORDER BY created_at DESC")
    List<TaskInfo> findAll(RowBounds rowBounds);

    @Select("SELECT COUNT(*) FROM task_info")
    long countAll();

    @ResultMap("taskInfoMap")
    @Select("SELECT * FROM task_info WHERE status = #{status} ORDER BY created_at DESC")
    List<TaskInfo> findByStatusWithRowBounds(@Param("status") String status, RowBounds rowBounds);

    @Select("SELECT COUNT(*) FROM task_info WHERE status = #{status}")
    long countByStatus(@Param("status") String status);

    @Insert("INSERT INTO task_info (id, name, cron_expression, bean_name, method_name, params, status, " +
            "execute_mode, callback_url, concurrent, last_trigger_at, last_end_at, last_result, created_at, updated_at) " +
            "VALUES (#{id}, #{name}, #{cronExpression}, #{beanName}, #{methodName}, #{params}, #{status}, " +
            "#{executeMode}, #{callbackUrl}, #{concurrent}, #{lastTriggerAt}, #{lastEndAt}, #{lastResult}, #{createdAt}, #{updatedAt})")
    int insert(TaskInfo taskInfo);

    @Update("UPDATE task_info SET name = #{name}, cron_expression = #{cronExpression}, bean_name = #{beanName}, " +
            "method_name = #{methodName}, params = #{params}, status = #{status}, execute_mode = #{executeMode}, " +
            "callback_url = #{callbackUrl}, concurrent = #{concurrent}, last_trigger_at = #{lastTriggerAt}, " +
            "last_end_at = #{lastEndAt}, last_result = #{lastResult}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int update(TaskInfo taskInfo);

    @Delete("DELETE FROM task_info WHERE id = #{id}")
    int deleteById(@Param("id") String id);
}
