package com.example.leadgen_backend;

import com.example.leadgen_backend.dto.AuthRegisterRequest;
import com.example.leadgen_backend.dto.BusinessProfileRequest;
import com.example.leadgen_backend.enums.LeadStatus;
import com.example.leadgen_backend.enums.LeadUrgency;
import com.example.leadgen_backend.enums.Role;
import com.example.leadgen_backend.model.*;
import com.example.leadgen_backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LeadGenerationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private LeadAssignmentRepository assignmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String customerToken;
    private String businessToken;
    private String adminToken;
    private Long businessId;
    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        notificationRepository.deleteAll();
        assignmentRepository.deleteAll();
        leadRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create category
        Category category = Category.builder()
                .name("Plumbing")
                .description("Plumbing services")
                .active(true)
                .build();
        category = categoryRepository.save(category);
        categoryId = category.getId();

        // Register customer
        AuthRegisterRequest customerRequest = new AuthRegisterRequest(
                "John Customer",
                "customer@test.com",
                "1234567890",
                "password123",
                Role.CUSTOMER,
                40.7128,
                -74.0060,
                null
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isOk());

        // Login customer
        var customerLoginResult = mockMvc.perform(post("/api/auth/login")
                        .param("username", "customer@test.com")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andReturn();
        customerToken = customerLoginResult.getResponse().getContentAsString();

        // Register business
        BusinessProfileRequest businessProfile = new BusinessProfileRequest(
                "Quick Plumbers",
                "Fast plumbing services",
                50,
                500,
                25,
                categoryId
        );

        AuthRegisterRequest businessRequest = new AuthRegisterRequest(
                "Bob Business",
                "business@test.com",
                "0987654321",
                "password123",
                Role.BUSINESS,
                40.7580,
                -73.9855,
                businessProfile
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(businessRequest)))
                .andExpect(status().isOk());

        // Login business
        var businessLoginResult = mockMvc.perform(post("/api/auth/login")
                        .param("username", "business@test.com")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andReturn();
        businessToken = businessLoginResult.getResponse().getContentAsString();
        businessId = userRepository.findByEmail("business@test.com").get().getId();

        // Create admin user
        User admin = User.builder()
                .name("Admin User")
                .email("admin@test.com")
                .phone("5555555555")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .verified(true)
                .build();
        userRepository.save(admin);

        // Login admin
        var adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .param("username", "admin@test.com")
                        .param("password", "admin123"))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = adminLoginResult.getResponse().getContentAsString();
    }

    @Test
    void testCompleteLeadFlow() throws Exception {
        // 1. Customer creates a lead
        Lead lead = Lead.builder()
                .customerName("John Customer")
                .customerPhone("1234567890")
                .customerEmail("customer@test.com")
                .categoryId(categoryId)
                .description("Need plumbing help")
                .budgetFrom(100)
                .budgetTo(300)
                .locationText("New York")
                .latitude(40.7128)
                .longitude(-74.0060)
                .urgency(LeadUrgency.HIGH)
                .build();

        var createLeadResult = mockMvc.perform(post("/api/leads")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lead)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andReturn();

        Lead createdLead = objectMapper.readValue(
                createLeadResult.getResponse().getContentAsString(),
                Lead.class
        );
        Long leadId = createdLead.getId();

        // 2. Verify lead assignment was created
        var assignments = assignmentRepository.findByLeadId(leadId);
        assertThat(assignments).hasSize(1);
        assertThat(assignments.get(0).getBusinessId()).isEqualTo(businessId);
        assertThat(assignments.get(0).getStatus().name()).isEqualTo("NOTIFIED");

        // 3. Verify notification was sent to business
        var notifications = notificationRepository.findByUserIdOrderBySentAtDesc(businessId);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getType()).isEqualTo("LEAD_ASSIGNED");

        // 4. Business views available leads
        mockMvc.perform(get("/api/business/leads")
                        .header("Authorization", "Bearer " + businessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leadId").value(leadId));

        // 5. Business claims the lead
        Long assignmentId = assignments.get(0).getId();
        mockMvc.perform(post("/api/business/claim/{assignmentId}", assignmentId)
                        .header("Authorization", "Bearer " + businessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Lead claimed successfully"));

        // 6. Verify lead status is now CLAIMED
        var updatedLead = leadRepository.findById(leadId).orElseThrow();
        assertThat(updatedLead.getStatus()).isEqualTo(LeadStatus.CLAIMED);

        // 7. Verify assignment status is CLAIMED
        var updatedAssignment = assignmentRepository.findById(assignmentId).orElseThrow();
        assertThat(updatedAssignment.getIsClaimed()).isTrue();
        assertThat(updatedAssignment.getStatus().name()).isEqualTo("CLAIMED");

        // 8. Verify notification sent to business for claiming
        notifications = notificationRepository.findByUserIdOrderBySentAtDesc(businessId);
        assertThat(notifications).hasSizeGreaterThanOrEqualTo(2);
        boolean hasClaimedNotification = notifications.stream()
                .anyMatch(n -> n.getType().equals("LEAD_CLAIMED"));
        assertThat(hasClaimedNotification).isTrue();

        // 9. Admin can view all leads
        mockMvc.perform(get("/api/admin/leads")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(leadId));

        // 10. Admin can view dashboard analytics
        mockMvc.perform(get("/api/admin/analytics/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(3))
                .andExpect(jsonPath("$.totalLeads").value(1));
    }

    @Test
    void testLeadFilteringAndPagination() throws Exception {
        // Create multiple leads
        for (int i = 0; i < 5; i++) {
            Lead lead = Lead.builder()
                    .customerName("Customer " + i)
                    .customerPhone("123456789" + i)
                    .customerEmail("customer" + i + "@test.com")
                    .categoryId(categoryId)
                    .description("Test lead " + i)
                    .budgetFrom(100)
                    .budgetTo(200)
                    .locationText("Location " + i)
                    .latitude(40.7128)
                    .longitude(-74.0060)
                    .urgency(LeadUrgency.MEDIUM)
                    .build();

            leadRepository.save(lead);
        }

        // Test pagination
        mockMvc.perform(get("/api/leads?page=0&size=3")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(5));

        // Test filtering by status
        mockMvc.perform(get("/api/leads/status/NEW")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testCategoryManagement() throws Exception {
        Category newCategory = Category.builder()
                .name("Electrical")
                .description("Electrical services")
                .active(true)
                .build();

        // Create category as admin
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electrical"));

        // Get all categories
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2)); // Plumbing + Electrical
    }
}
