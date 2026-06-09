package com.imfine.freshinventory.repository;

import com.imfine.freshinventory.domain.ProductCategory;
import com.imfine.freshinventory.domain.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);

    @Query("""
            select c from ProductCategory c
            where (:status is null or c.status = :status)
              and (:keyword is null or lower(c.name) like lower(concat('%', :keyword, '%')))
            order by c.id desc
            """)
    Page<ProductCategory> search(String keyword, Status status, Pageable pageable);
}
