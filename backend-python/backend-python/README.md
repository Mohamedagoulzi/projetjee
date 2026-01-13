# Semantic Search API with FastAPI and ChromaDB

This FastAPI service provides semantic search capabilities for products using ChromaDB and sentence transformers.

## Features

- 🔍 **Semantic Search**: Find products using natural language queries that understand meaning, not just keywords
- 🌐 **Multilingual Support**: Uses a multilingual model that supports French and English
- 🔄 **Product Sync**: Automatically sync products from Spring Boot backend
- ⚡ **Fast & Efficient**: Powered by ChromaDB for fast vector similarity search
- 🎯 **Filtering**: Combine semantic search with price, rating, and category filters

## Setup Instructions

### 1. Install Dependencies

Make sure you have Python 3.8+ installed, then install the required packages:

```bash
cd backend-python
pip install -r requirements.txt
```

**Note**: The first time you run the application, it will download the sentence transformer model (~420MB). This is a one-time download.

### 2. Start the FastAPI Server

```bash
# From the backend-python directory
python main.py
```

Or using uvicorn directly:

```bash
uvicorn main:app --reload --port 8000
```

The API will be available at `http://localhost:8000`

### 3. Sync Products from Spring Boot Backend

Before using semantic search, you need to sync products from your Spring Boot backend:

**Option 1: Using PowerShell script (Windows)**
```powershell
.\sync_products.ps1
```

**Option 2: Using Python script**
```bash
python sync_products.py
```

**Option 3: Using PowerShell/curl commands**
```powershell
# Automatic sync (fetches from Spring Boot)
Invoke-WebRequest -Uri "http://localhost:8000/sync-from-springboot" -Method POST

# Or using curl.exe (if available)
curl.exe -X POST http://localhost:8000/sync-from-springboot
```

**Option 4: Manual sync (POST products directly)**
```powershell
$body = @{
    products = @(
        @{
            id = 1
            title = "Product Title"
            description = "Product description"
            price = 29.99
            rating = 4.5
            asin = "B001234"
            categorie = @{id = 1}
        }
    )
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "http://localhost:8000/sync-products" -Method Post -Body $body -ContentType "application/json"
```

### 4. Test the Semantic Search

**Using PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8000/search?query=smartphone%20pas%20cher&n_results=10"
```

**Using curl.exe (if available):**
```bash
curl.exe "http://localhost:8000/search?query=smartphone%20pas%20cher&n_results=10"
```

## API Endpoints

### Health Check
```
GET /health
```
Returns the health status and number of indexed products.

### Semantic Search
```
GET /search?query=<search_query>&n_results=10&min_price=0&max_price=100&min_rating=4&category_id=1
POST /search
```

**Query Parameters:**
- `query` (required): The search query text
- `n_results` (optional, default: 10): Number of results to return
- `min_price` (optional): Minimum price filter
- `max_price` (optional): Maximum price filter
- `min_rating` (optional): Minimum rating filter
- `category_id` (optional): Category ID filter

**Response:**
```json
{
  "query": "smartphone pas cher",
  "product_ids": [1, 5, 12, 23],
  "count": 4,
  "results": [...]
}
```

### Sync Products
```
POST /sync-products
POST /sync-from-springboot
```

### Clear Collection
```
DELETE /clear-collection
```
⚠️ **Warning**: This will delete all indexed products!

## Configuration

### Environment Variables

- `SPRING_BOOT_URL`: URL of the Spring Boot backend (default: `http://localhost:8080`)

Set it before running:
```bash
export SPRING_BOOT_URL=http://localhost:8080
python main.py
```

## How It Works

1. **Embedding Generation**: When products are synced, the API generates embeddings using a multilingual sentence transformer model (`paraphrase-multilingual-MiniLM-L12-v2`)

2. **Text Creation**: For each product, a searchable text is created from:
   - Product title
   - Product description
   - Category name
   - ASIN code

3. **Vector Storage**: Embeddings are stored in ChromaDB with product metadata

4. **Semantic Search**: When a user searches:
   - The query is converted to an embedding
   - ChromaDB finds the most similar product embeddings
   - Results are filtered by price, rating, and category if specified
   - Product IDs are returned to fetch full details from Spring Boot

## Frontend Integration

The React frontend has been updated to support semantic search:

1. Enable "Recherche sémantique (IA)" checkbox in the ProductSearch component
2. Enter your search query
3. The frontend will call the FastAPI backend for semantic search
4. Results are displayed with full product details

## Troubleshooting

### Import Error: `cannot import name 'cached_download'`
This is a version compatibility issue. Fix it by upgrading packages:
```powershell
pip install --upgrade sentence-transformers huggingface-hub transformers torch
```

### Model Download Issues
If the model download fails, you can manually download it:
```python
from sentence_transformers import SentenceTransformer
model = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
```

### ChromaDB Persistence
Product embeddings are stored in `./chroma_data` directory. This directory is created automatically.

### Connection Issues
- Make sure Spring Boot backend is running on port 8080 (or update `SPRING_BOOT_URL`)
- Make sure FastAPI is running on port 8000
- Check CORS settings if frontend can't connect

### PowerShell curl Command Issues
PowerShell's `curl` is an alias for `Invoke-WebRequest` with different syntax. Use:
- `Invoke-WebRequest` or `Invoke-RestMethod` for PowerShell
- `curl.exe` if you have curl installed
- The provided PowerShell scripts (`.ps1` files)

## Performance Notes

- First search may be slower as the model loads
- Embedding generation for many products may take time (consider batching)
- ChromaDB persists data, so subsequent runs are faster
- For production, consider using a GPU for faster embedding generation

## Example Queries

Try these semantic search queries:
- "smartphone pas cher" → finds affordable smartphones
- "livre de cuisine" → finds cooking books
- "cadeau pour enfant" → finds gifts for children
- "écran 4K" → finds 4K displays

The semantic search understands synonyms and related concepts, not just exact keyword matches!
