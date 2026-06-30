package cn.yusiwen.spring.commons.job.core.mapper;

import cn.yusiwen.spring.commons.job.core.entity.TaskLog;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

@Mapper
public interface TaskLogMapper {

    @Results(id = "taskLogMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "result", column = "result"),
            @Result(property = "errorMessage", column = "error_message"),
            @Result(property = "createdAt", column = "created_at")
    })
    @Select("SELECT * FROM task_log WHERE id = #{id}")
    TaskLog findById(@Param("id") String id);

    @ResultMap("taskLogMap")
    @Select("SELECT * FROM task_log WHERE task_id = #{taskId} ORDER BY created_at DESC")
    List<TaskLog> findByTaskId(@Param("taskId") String taskId);

    @ResultMap("taskLogMap")
    @Select("SELECT * FROM task_log WHERE task_id = #{taskId} ORDER BY created_at DESC")
    List<TaskLog> findByTaskIdWithRowBounds(@Param("taskId") String taskId, RowBounds rowBounds);

    @Select("SELECT COUNT(*) FROM task_log WHERE task_id = #{taskId}")
    long countByTaskId(@Param("taskId") String taskId);

    @Insert("INSERT INTO task_log (id, task_id, start_time, end_time, result, error_message, created_at) " +
            "VALUES (#{id}, #{taskId}, #{startTime}, #{endTime}, #{result}, #{errorMessage}, #{createdAt})")
    int insert(TaskLog taskLog);
}
