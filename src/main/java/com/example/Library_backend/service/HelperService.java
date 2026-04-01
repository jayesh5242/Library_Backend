package com.example.Library_backend.service;

import com.example.Library_backend.dto.response.authresponse.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class HelperService {

    public <T> PagedResponse<T> toPagedResponse(Page<T> page, String message) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .message(message)
                .build();
    }
}