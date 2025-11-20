package com.example.leadgen_backend.repository;

import com.example.leadgen_backend.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {
}
