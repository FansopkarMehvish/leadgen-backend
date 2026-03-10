package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.enums.Role;
import com.example.leadgen_backend.model.Lead;
import com.example.leadgen_backend.model.LeadAssignment;
import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import com.example.leadgen_backend.repository.LeadRepository;
import com.example.leadgen_backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only endpoints for user management and analytics")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final LeadAssignmentRepository assignmentRepository;

    @Operation(summary = "Get all users", description = "Retrieve all users with pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userRepository.findAll(pageable));
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user role", description = "Change a user's role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/{id}/role")
    public ResponseEntity<User> updateUserRole(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @Parameter(description = "New role", required = true) @RequestParam Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @Operation(summary = "Verify user", description = "Mark a user as verified")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User verified successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/{id}/verify")
    public ResponseEntity<User> verifyUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(true);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @Operation(summary = "Delete user", description = "Delete a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @Operation(summary = "Get all leads", description = "Retrieve all leads with pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved leads",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    @GetMapping("/leads")
    public ResponseEntity<Page<Lead>> getAllLeads(Pageable pageable) {
        return ResponseEntity.ok(leadRepository.findAll(pageable));
    }

    @Operation(summary = "Get all assignments", description = "Retrieve all lead assignments")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved assignments",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    @GetMapping("/assignments")
    public ResponseEntity<List<LeadAssignment>> getAllAssignments() {
        return ResponseEntity.ok(assignmentRepository.findAll());
    }

    @Operation(summary = "Dashboard statistics", description = "Get overview dashboard statistics")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalLeads", leadRepository.count());
        stats.put("totalAssignments", assignmentRepository.count());
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Lead statistics", description = "Get lead status breakdown")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved lead stats",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
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

    @Operation(summary = "Business performance", description = "Get assignment and claim rate metrics")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved performance metrics",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
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
