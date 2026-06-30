package cn.yusiwen.spring.commons.job.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> data;
    private int page;
    private int size;
    private long total;
}
