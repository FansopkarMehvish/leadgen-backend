package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.enums.Role;
import com.example.leadgen_backend.model.Lead;
import com.example.leadgen_backend.model.LeadAssignment;
import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import com.example.leadgen_backend.repository.LeadRepository;
import com.example.leadgen_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final LeadAssignmentRepository assignmentRepository;

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userRepository.findAll(pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PutMapping("/users/{id}/verify")
    public ResponseEntity<User> verifyUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(true);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/leads")
    public ResponseEntity<Page<Lead>> getAllLeads(Pageable pageable) {
        return ResponseEntity.ok(leadRepository.findAll(pageable));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<LeadAssignment>> getAllAssignments() {
        return ResponseEntity.ok(assignmentRepository.findAll());
    }

    @GetMapping("/analytics/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        stats.put("totalUsers", userRepository.count());
        stats.put("totalLeads", leadRepository.count());
        stats.put("totalAssignments", assignmentRepository.count());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/analytics/lead-stats")
    public ResponseEntity<Map<String, Object>> getLeadStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Lead> allLeads = leadRepository.findAll();

        long newLeads = allLeads.stream().filter(l -> l.getStatus().name().equals("NEW")).count();
        long assignedLeads = allLeads.stream().filter(l -> l.getStatus().name().equals("ASSIGNED")).count();
        long claimedLeads = allLeads.stream().filter(l -> l.getStatus().name().equals("CLAIMED")).count();

        stats.put("total", allLeads.size());
        stats.put("new", newLeads);
        stats.put("assigned", assignedLeads);
        stats.put("claimed", claimedLeads);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/analytics/business-performance")
    public ResponseEntity<Map<String, Object>> getBusinessPerformance() {
        Map<String, Object> performance = new HashMap<>();
        List<LeadAssignment> assignments = assignmentRepository.findAll();

        long totalClaimed = assignments.stream().filter(LeadAssignment::getIsClaimed).count();
        long totalExpired = assignments.stream()
                .filter(a -> a.getStatus().name().equals("EXPIRED"))
                .count();

        double claimRate = assignments.isEmpty() ? 0 : (double) totalClaimed / assignments.size() * 100;

        performance.put("totalAssignments", assignments.size());
        performance.put("totalClaimed", totalClaimed);
        performance.put("totalExpired", totalExpired);
        performance.put("claimRate", String.format("%.2f%%", claimRate));

        return ResponseEntity.ok(performance);
    }
}
