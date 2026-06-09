package com.imfine.freshinventory.repository;

import com.imfine.freshinventory.domain.Product;
import com.imfine.freshinventory.domain.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("""
            select p from Product p join fetch p.category
            where p.id = :id
            """)
    java.util.Optional<Product> findDetailById(Long id);

    @Query("""
            select p from Product p
            where (:status is null or p.status = :status)
              and (:categoryId is null or p.category.id = :categoryId)
              and (:keyword is null
                or lower(p.code) like lower(concat('%', :keyword, '%'))
                or lower(p.name) like lower(concat('%', :keyword, '%')))
            order by p.id desc
            """)
    Page<Product> search(String keyword, Long categoryId, Status status, Pageable pageable);
}
