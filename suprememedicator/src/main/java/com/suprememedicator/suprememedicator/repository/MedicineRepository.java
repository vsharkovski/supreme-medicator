package com.suprememedicator.suprememedicator.repository;

import com.suprememedicator.suprememedicator.domain.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    @Query("""
        select m
        from Medicine m
        where exists (
            select p
            from Product p
            where p.medicine = m
                and p.brandName like concat('%', :brandName, '%')
        )
    """)
    Set<Medicine> getMedicinesByAnyProductBrandNameContaining(String brandName);

    Set<Medicine> getMedicinesByDescriptionContainingIgnoreCase(String keyword);
}
