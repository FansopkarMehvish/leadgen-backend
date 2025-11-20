package com.example.leadgen_backend.repository;

import com.example.leadgen_backend.model.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
}
