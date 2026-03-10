package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.enums.LeadStatus;
import com.example.leadgen_backend.model.Lead;
import com.example.leadgen_backend.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@Tag(name = "Leads", description = "Lead management endpoints for creating, retrieving, updating and deleting leads")
@SecurityRequirement(name = "bearerAuth")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @Operation(summary = "Create new lead", description = "Create a new service request lead. Customer or Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lead created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lead.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<?> createLead(@RequestBody Lead lead) {
        Lead saved = leadService.createLead(lead);
        return ResponseEntity.status(201).body(saved);
    }

    @Operation(summary = "Get all leads", description = "Retrieve all leads with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved leads",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS','CUSTOMER')")
    public ResponseEntity<?> getAllLeads(Pageable pageable) {
        Page<Lead> leads = leadService.getAllLeadsPaged(pageable);
        return ResponseEntity.ok(leads);
    }

    @Operation(summary = "Get lead by ID", description = "Retrieve a specific lead by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lead.class))),
            @ApiResponse(responseCode = "404", description = "Lead not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS','CUSTOMER')")
    public ResponseEntity<?> getLeadById(@Parameter(description = "Lead ID", required = true) @PathVariable Long id) {
        Lead lead = leadService.getLeadById(id);
        return ResponseEntity.ok(lead);
    }

    @Operation(summary = "Get leads by status", description = "Filter leads by their current status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved leads",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS')")
    public ResponseEntity<?> getLeadsByStatus(
            @Parameter(description = "Lead status filter", required = true) @PathVariable LeadStatus status,
            Pageable pageable) {
        Page<Lead> leads = leadService.getLeadsByStatus(status, pageable);
        return ResponseEntity.ok(leads);
    }

    @Operation(summary = "Get leads by category", description = "Filter leads by service category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved leads",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    })
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS')")
    public ResponseEntity<?> getLeadsByCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long categoryId) {
        List<Lead> leads = leadService.getLeadsByCategory(categoryId);
        return ResponseEntity.ok(leads);
    }

    @Operation(summary = "Filter leads by status and category", description = "Filter leads using both status and category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved leads",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    })
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS')")
    public ResponseEntity<?> getLeadsByStatusAndCategory(
            @Parameter(description = "Lead status", required = true) @RequestParam LeadStatus status,
            @Parameter(description = "Category ID", required = true) @RequestParam Long categoryId) {
        List<Lead> leads = leadService.getLeadsByStatusAndCategory(status, categoryId);
        return ResponseEntity.ok(leads);
    }

    @Operation(summary = "Update lead", description = "Update an existing lead. Owner or Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lead.class))),
            @ApiResponse(responseCode = "404", description = "Lead not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this lead")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<?> updateLead(
            @Parameter(description = "Lead ID", required = true) @PathVariable Long id,
            @RequestBody Lead lead) {
        Lead updated = leadService.updateLead(id, lead);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete lead", description = "Delete a lead. Owner or Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Lead not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this lead")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<?> deleteLead(
            @Parameter(description = "Lead ID", required = true) @PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok("Lead deleted successfully");
    }
}
