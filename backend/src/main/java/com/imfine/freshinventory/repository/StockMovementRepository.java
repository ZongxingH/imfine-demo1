package com.imfine.freshinventory.repository;

import com.imfine.freshinventory.domain.MovementType;
import com.imfine.freshinventory.domain.SourceType;
import com.imfine.freshinventory.domain.StockMovement;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    @Query("select coalesce(sum(sm.quantityDelta), 0) from StockMovement sm where sm.product.id = :productId")
    BigDecimal currentStock(Long productId);

    @Query("""
            select sm from StockMovement sm
            where sm.product.id = :productId
            order by sm.movementDate desc, sm.id desc
            """)
    Page<StockMovement> findByProductId(Long productId, Pageable pageable);

    @Query("""
            select max(sm.movementDate) from StockMovement sm
            where sm.product.id = :productId and sm.movementType = :movementType
            """)
    LocalDate lastMovementDate(Long productId, MovementType movementType);

    @Modifying
    @Query("delete from StockMovement sm where sm.sourceType = :sourceType and sm.sourceId = :sourceId")
    void deleteBySource(SourceType sourceType, Long sourceId);
}
