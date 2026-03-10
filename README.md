# LeadGen Platform

A **Lead Generation Platform** built with Spring Boot that intelligently connects customers who need services with local businesses. The platform uses a multi-factor weighted scoring algorithm to match leads with the most suitable businesses based on category, distance, budget, and urgency.

## Features

### Core Functionality
- **Smart Lead Matching** - 4-factor weighted scoring algorithm (category 25%, distance 30%, budget 25%, urgency 20%)
- **Geolocation-Based Matching** - Uses Haversine formula for accurate distance calculation
- **30-Minute Claim Window** - Businesses must claim leads within 30 minutes or they expire
- **Real-time Notifications** - In-app notifications for lead assignments and claims
- **Role-Based Access Control** - JWT-based authentication with CUSTOMER, BUSINESS, and ADMIN roles

### Admin Features
- User management (CRUD operations)
- Analytics dashboard with lead statistics
- Business performance metrics
- Audit logging for all operations

### Security Features
- JWT-based stateless authentication
- BCrypt password hashing
- Optimistic locking for race condition prevention
- Bean validation on all input DTOs

## Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.5.7 |
| Language | Java 21 |
| Database | PostgreSQL |
| ORM | Spring Data JPA |
| Migrations | Liquibase |
| Security | Spring Security + JWT |
| Validation | Jakarta Bean Validation |
| Build Tool | Maven |

## API Endpoints

### Authentication
```
POST /api/auth/register    - Register new user (Customer/Business)
POST /api/auth/login       - Login and receive JWT token
```

### Leads
```
POST   /api/leads                    - Create new lead
GET    /api/leads                    - List all leads (paginated)
GET    /api/leads/{id}               - Get lead by ID
GET    /api/leads/status/{status}    - Filter by status
GET    /api/leads/category/{id}      - Filter by category
GET    /api/leads/filter             - Filter by status & category
PUT    /api/leads/{id}               - Update lead
DELETE /api/leads/{id}               - Delete lead
```

### Business Operations
```
GET    /api/business/leads           - View matched leads
GET    /api/business/leads/paged     - View leads (paginated)
POST   /api/business/claim/{id}      - Claim a lead
```

### Categories
```
GET    /api/categories               - List all categories
POST   /api/categories               - Create category (Admin only)
PUT    /api/categories/{id}          - Update category
DELETE /api/categories/{id}          - Delete category
```

### User Profile
```
GET    /api/users/profile            - Get current user profile
PUT    /api/users/profile            - Update profile
GET    /api/users/business-profile   - Get business profile
PUT    /api/users/business-profile   - Update business profile
```

### Notifications
```
GET    /api/notifications            - Get my notifications
GET    /api/notifications/unread    - Get unread notifications
PUT    /api/notifications/{id}/read  - Mark as read
```

### Admin
```
GET    /api/admin/users              - List all users
PUT    /api/admin/users/{id}/verify  - Verify user
GET    /api/admin/analytics/dashboard - Dashboard stats
```

## The Matching Algorithm

The platform uses a 100-point weighted scoring system:

| Factor | Weight | Description |
|--------|--------|-------------|
| Category Match | 25% | Exact category match = 25pts, partial = 12pts |
| Distance | 30% | Tiered scoring based on proximity within service radius |
| Budget Overlap | 25% | Percentage of customer's budget range that overlaps |
| Urgency | 20% | HIGH = 20pts, MEDIUM = 14pts, LOW = 8pts |

### Distance Scoring
- < 25% of radius: 30 points
- 25-50% of radius: 24 points
- 50-75% of radius: 18 points
- 75-100% of radius: 12 points
- Beyond radius: 6 points

### Budget Overlap
- 80%+ overlap: 25 points
- 50%+ overlap: 17 points
- 25%+ overlap: 10 points
- Near miss (<5000 gap): 7 points

## Database Schema

### Core Tables
- `users` - User accounts with roles
- `business_profiles` - Business-specific data linked 1:1 with users
- `categories` - Service categories
- `leads` - Customer service requests
- `lead_assignments` - Match records with scores and expiration
- `notifications` - In-app notifications
- `audit_logs` - Audit trail for all operations
- `verification_tokens` - Email/phone verification tokens

### Enums
- `role_enum` - CUSTOMER, BUSINESS, ADMIN
- `lead_status_enum` - NEW, ASSIGNED, CLAIMED, IN_PROGRESS, CLOSED, REJECTED
- `lead_urgency_enum` - LOW, MEDIUM, HIGH
- `assignment_status_enum` - NOTIFIED, CLAIMED, EXPIRED

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL 14+
- Maven 3.8+

### Configuration
Edit `application.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/leadgen
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT
jwt.secret=your_secure_random_string
jwt.expiration-ms=3600000
```

### Build & Run
```bash
# Build
mvn clean compile

# Run
mvn spring-boot:run

# Run tests
mvn test
```

### Database Setup
```bash
# Create database
createdb leadgen

# Run migrations (automatic on startup)
mvn liquibase:update
```

## Project Structure

```
src/main/java/com/example/leadgen_backend/
├── config/          # Security configuration
├── controller/      # REST controllers
├── dto/            # Data transfer objects
├── enums/          # Enum definitions
├── exception/      # Exception handlers
├── model/          # JPA entities
├── repository/     # Spring Data repositories
├── security/       # JWT utilities and filters
├── service/        # Business logic services
└── util/           # Utility classes

src/main/resources/
├── db/changelog/   # Liquibase migrations
└── application.properties
```

## Key Design Decisions

1. **Shared Primary Key** - BusinessProfile uses the same ID as User (1:1 relationship)
2. **Optimistic Locking** - `@Version` on LeadAssignment prevents concurrent claim conflicts
3. **Graceful Degradation** - Missing data gets partial credit instead of zero in scoring
4. **Scheduled Jobs** - Dual scheduler: every minute for expiration, daily for cleanup
5. **Stateless Auth** - JWT tokens with no server-side session storage

## Testing

Integration tests cover:
- Complete lead flow (registration → creation → matching → claiming)
- Pagination and filtering
- Category management
- Notification delivery

Run tests:
```bash
mvn test -Dtest=LeadGenerationFlowIntegrationTest
```

## Future Enhancements

- Machine learning for automatic weight optimization
- WebSocket/SSE for real-time notifications
- Payment integration (Stripe) for lead claiming fees
- Elasticsearch for full-text search
- Redis caching for business profiles

## License

MIT License - feel free to use for learning or commercial projects.

## Author

Built for interview demonstration purposes showcasing:
- Spring Boot best practices
- Clean architecture
- Complex algorithm implementation
- Comprehensive testing
