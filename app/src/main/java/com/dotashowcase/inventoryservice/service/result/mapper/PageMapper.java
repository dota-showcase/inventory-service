package com.dotashowcase.inventoryservice.service.result.mapper;

import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class PageMapper<T, R> {

    //     "pageable": {
    //        "sort": {
    //            "empty": true,
    //            "sorted": false,
    //            "unsorted": true
    //        },
    //        "offset": 1200,
    //        "pageSize": 48,
    //        "pageNumber": 25,
    //        "unpaged": false,
    //        "paged": true
    //    },
    //    "last": true,
    //    "totalPages": 26,
    //    "totalElements": 1228,
    //    "size": 48,
    //    "number": 25,
    //    "sort": {
    //        "empty": true,
    //        "sorted": false,
    //        "unsorted": true
    //    },
    //    "first": false,
    //    "numberOfElements": 28,
    //    "empty": false

    public PageResult<R> getPageResult(Page<T> page, Function<T, R> mapper) {
        PageResult<R> pageResult = new PageResult<>();

        pageResult.setData(page.getContent().stream().map(mapper).toList());

        PageResult.Pagination pagination = new PageResult.Pagination();
        pagination.setCurrentPage(page.getNumber() + 1);
        pagination.setTotalPages(page.getTotalPages());
        pagination.setItemsOnPage(page.getNumberOfElements());
        pagination.setTotalItems(page.getTotalElements());
        pageResult.setPagination(pagination);

        return pageResult;
    }
}
