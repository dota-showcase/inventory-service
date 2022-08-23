package com.dotashowcase.inventoryservice.service.result.dto.pagination;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<Type> {

    List<Type> data;

    Pagination pagination;

    @Data
    public static class Pagination {

        Integer currentPage;

        Integer totalPages;

        Integer itemsOnPage;

        Long totalItems;
    }
}
