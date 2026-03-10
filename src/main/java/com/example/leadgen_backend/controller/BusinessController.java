package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.model.LeadAssignment;
import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.repository.LeadAssignmentRepository;
import com.example.leadgen_backend.repository.UserRepository;
import com.example.leadgen_backend.service.LeadAssignmentService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/business")
@Tag(name = "Business Operations", description = "Endpoints for businesses to view and claim leads")
@SecurityRequirement(name = "bearerAuth")
public class BusinessController {

    private final LeadAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final LeadAssignmentService assignmentService;

    @Operation(summary = "Get available leads", description = "Retrieve all unclaimed leads assigned to the authenticated business, sorted by score")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved leads",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/leads")
    @PreAuthorize("hasAnyRole('BUSINESS','ADMIN')")
    public ResponseEntity<?> getLeadsForBusiness(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long businessId = user.getId();
        var assignments = assignmentRepository.findByBusinessIdAndIsClaimedFalseOrderByScoreDesc(businessId);
        return ResponseEntity.ok(assignments);
    }

    @Operation(summary = "Get available leads (paginated)", description = "Retrieve unclaimed leads with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved leads",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/leads/paged")
    @PreAuthorize("hasAnyRole('BUSINESS','ADMIN')")
    public ResponseEntity<?> getLeadsForBusinessPaged(Authentication authentication, Pageable pageable) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long businessId = user.getId();
        List assignments = assignmentRepository.findByBusinessIdAndIsClaimedFalseOrderByScoreDesc(businessId);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), assignments.size());
        Page page = new PageImpl<>(assignments.subList(start, end), pageable, assignments.size());

        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Claim a lead", description = "Claim an assigned lead. First to claim wins, others get expired.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead claimed successfully"),
            @ApiResponse(responseCode = "400", description = "Lead already claimed or expired"),
            @ApiResponse(responseCode = "403", description = "Not your assignment"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PostMapping("/claim/{assignmentId}")
    @PreAuthorize("hasAnyRole('BUSINESS','ADMIN')")
    public ResponseEntity<?> claimLead(
            @Parameter(description = "Assignment ID to claim", required = true) @PathVariable Long assignmentId,
            Authentication auth) {

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        assignmentService.claimLead(assignmentId, user.getId());

        return ResponseEntity.ok("Lead claimed successfully");
    }
}
