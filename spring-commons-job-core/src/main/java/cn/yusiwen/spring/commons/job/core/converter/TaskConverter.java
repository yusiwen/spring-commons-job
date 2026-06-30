package cn.yusiwen.spring.commons.job.core.converter;

import cn.yusiwen.spring.commons.job.core.dto.TaskInfoRequest;
import cn.yusiwen.spring.commons.job.core.dto.TaskInfoVO;
import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;

/**
 * Static converter between entity and DTO objects.
 */
public final class TaskConverter {

    private TaskConverter() {
    }

    /**
     * Converts a {@link TaskInfo} entity to a {@link TaskInfoVO} response DTO.
     *
     * @param entity the task entity, may be {@code null}
     * @return the corresponding response DTO, or {@code null} if input is {@code null}
     */
    public static TaskInfoVO toVO(TaskInfo entity) {
        if (entity == null) {
            return null;
        }
        TaskInfoVO vo = new TaskInfoVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setCronExpression(entity.getCronExpression());
        vo.setBeanName(entity.getBeanName());
        vo.setMethodName(entity.getMethodName());
        vo.setParams(entity.getParams());
        vo.setStatus(entity.getStatus());
        vo.setExecuteMode(entity.getExecuteMode());
        vo.setCallbackUrl(entity.getCallbackUrl());
        vo.setConcurrent(entity.getConcurrent());
        vo.setLastTriggerAt(entity.getLastTriggerAt());
        vo.setLastEndAt(entity.getLastEndAt());
        vo.setLastResult(entity.getLastResult());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    /**
     * Converts a {@link TaskInfoRequest} request DTO to a {@link TaskInfo} entity.
     * <p>Note: The returned entity does not have an {@code id} set; the caller
     * is responsible for generating and assigning the identifier.</p>
     *
     * @param request the request DTO, may be {@code null}
     * @return the corresponding entity, or {@code null} if input is {@code null}
     */
    public static TaskInfo toEntity(TaskInfoRequest request) {
        if (request == null) {
            return null;
        }
        TaskInfo entity = new TaskInfo();
        entity.setName(request.getName());
        entity.setCronExpression(request.getCronExpression());
        entity.setBeanName(request.getBeanName());
        entity.setMethodName(request.getMethodName());
        entity.setParams(request.getParams());
        entity.setConcurrent(request.getConcurrent() != null && request.getConcurrent());
        return entity;
    }
}
