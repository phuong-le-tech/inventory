# Inventory Application Implementation Plan

## Goal Description
Create a full-stack Inventory Management application allowing users to monitor, add, edit, and delete items. The app will feature a dashboard for overview and detailed list views.
Items have: **Name, Category, Status, Image**.

## Tech Stack
- **Backend**: Java 21, Spring Boot 3.x
- **Database**: PostgreSQL
- **Frontend**: React, TypeScript, Vite, TailwindCSS (for styling)

## Proposed Changes

### Database Schema
We will use a single table `items` (or similar) to store inventory.

**Table: `items`**
| Column | Type | Notes |
|Str |---|---|
| `id` | UUID | Primary Key |
| `name` | VARCHAR(255) | Not Null |
| `category` | VARCHAR(255) | |
| `status` | VARCHAR(50) | e.g., 'In Stock', 'Low Stock', 'Out of Stock' |
| `image_data` | BYTEA / OID | Storing image directly in DB for simplicity |
| `content_type` | VARCHAR(50) | e.g., 'image/jpeg' |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |

*Note: For a production app, images might go to S3/Cloudinary, but for a personal app, DB storage keeps it self-contained.*

### Backend (Spring Boot)
Structure:
- `config/`: CORS configuration, OpenAPI/Swagger (optional)
- `model/`: `Item` entity
- `repository/`: `ItemRepository` (JpaRepository)
- `service/`: `ItemService`
- `controller/`: `ItemController`
- `dto/`: `ItemRequest`, `ItemResponse` (to handle multipart file uploads separately)

**API Endpoints:**
- `GET /api/items`: List all items
- `GET /api/items/{id}`: Get single item
- `POST /api/items`: Create item (Multipart request: data + image file)
- `PUT /api/items/{id}`: Update item
- `DELETE /api/items/{id}`: Delete item
- `GET /api/dashboard/stats`: (Optional) Get counts by status/category

### Frontend (React + TypeScript)
Structure:
- `components/`: Reusable UI components (Input, Card, Button)
- `pages/`:
    - `Dashboard`: Charts/Summary cards (Total items, per category)
    - `InventoryList`: Grid/Table of items with search/filter
    - `ItemForm`: Modal or Page for Add/Edit
- `services/`: API client (Axios)
- `types/`: TS interfaces for Item

**Key Libraries:**
- `react-router-dom`: Navigation
- `axios`: HTTP requests
- `tailwindcss`: Styling
- `react-hook-form`: Form handling
- `lucide-react`: Icons

## Verification Plan

### Automated Tests
- Backend: JUnit/Mockito tests for Service layer. `MockMvc` for Controller endpoints.
- Frontend: Basic component rendering tests.

### Manual Verification
1. **Start Backend**: Verify connection to Postgres.
2. **Start Frontend**: Open in browser.
3. **Flow**:
    - Click "Add Item", fill details, upload image -> Save.
    - Verify Item appears in List and Dashboard.
    - Click "Edit", change status -> Save -> Verify update.
    - Click "Delete" -> Verify removal.
