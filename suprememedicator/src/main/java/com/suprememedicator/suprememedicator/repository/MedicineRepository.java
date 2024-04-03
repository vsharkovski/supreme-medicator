package com.suprememedicator.suprememedicator.repository;

import com.suprememedicator.suprememedicator.domain.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
}
