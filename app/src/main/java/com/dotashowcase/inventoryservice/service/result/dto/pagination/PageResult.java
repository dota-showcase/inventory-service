package com.dotashowcase.inventoryservice.service.result.dto.pagination;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(title="PageResult")
@Data
public class PageResult<Type> {

    private List<Type> data;

    private Pagination pagination;

    @Data
    public static class Pagination {

        private Integer currentPage;

        private Integer totalPages;

        private Integer itemsOnPage;

        private Long totalItems;
    }
}
