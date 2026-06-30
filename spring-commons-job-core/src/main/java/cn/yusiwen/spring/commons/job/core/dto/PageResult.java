package cn.yusiwen.spring.commons.job.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * @param <T> the type of data in the current page
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    /** The list of items in the current page. */
    private List<T> data;

    /** Current page number (1-indexed). */
    private int page;

    /** Number of items per page. */
    private int size;

    /** Total number of items across all pages. */
    private long total;
}
