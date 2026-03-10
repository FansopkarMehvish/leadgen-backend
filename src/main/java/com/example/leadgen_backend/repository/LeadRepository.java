package com.example.leadgen_backend.repository;

import com.example.leadgen_backend.enums.LeadStatus;
import com.example.leadgen_backend.model.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findByStatus(LeadStatus status);

    List<Lead> findByCategoryId(Long categoryId);

    List<Lead> findByStatusAndCategoryId(LeadStatus status, Long categoryId);

    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);

    Page<Lead> findAll(Pageable pageable);
}
