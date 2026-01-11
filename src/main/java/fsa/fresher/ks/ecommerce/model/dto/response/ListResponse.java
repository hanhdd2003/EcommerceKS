package fsa.fresher.ks.ecommerce.model.dto.response;

import java.util.List;

public record ListResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {}