from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import chromadb
from chromadb.config import Settings
from sentence_transformers import SentenceTransformer
import httpx
import os
from datetime import datetime

# Initialize FastAPI
app = FastAPI(
    title="Semantic Search API",
    description="API for semantic product search using ChromaDB",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:3000"],  # React frontend
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize ChromaDB client
CHROMA_PERSIST_DIR = "./chroma_data"
os.makedirs(CHROMA_PERSIST_DIR, exist_ok=True)
chroma_client = chromadb.PersistentClient(path=CHROMA_PERSIST_DIR)

# Initialize sentence transformer model for embeddings
# Using a multilingual model that supports French
print("Loading sentence transformer model...")
embedding_model = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
print("Model loaded successfully!")

# Collection name for products
COLLECTION_NAME = "products"

# Spring Boot backend URL
SPRING_BOOT_URL = os.getenv("SPRING_BOOT_URL", "http://localhost:8080")

# Pydantic models
class ProductSearchRequest(BaseModel):
    query: str
    n_results: int = 10
    min_price: Optional[float] = None
    max_price: Optional[float] = None
    min_rating: Optional[float] = None
    category_id: Optional[int] = None

class ProductSyncRequest(BaseModel):
    products: List[dict]

class ProductDocument(BaseModel):
    id: str
    title: str
    description: Optional[str] = ""
    price: Optional[float] = None
    rating: Optional[float] = None
    category: Optional[str] = None
    asin: Optional[str] = None

# Helper functions
def get_collection():
    """Get or create the products collection"""
    return chroma_client.get_or_create_collection(
        name=COLLECTION_NAME,
        metadata={"description": "Product semantic search collection"}
    )

def create_product_text(product: dict) -> str:
    """Create searchable text from product data"""
    parts = []
    
    if product.get("title"):
        parts.append(product["title"])
    
    if product.get("description"):
        parts.append(product["description"])
    
    if product.get("category"):
        parts.append(f"Category: {product['category']}")
    
    if product.get("asin"):
        parts.append(f"ASIN: {product['asin']}")
    
    return " | ".join(parts)

def generate_embedding(text: str) -> List[float]:
    """Generate embedding for text using sentence transformer"""
    embedding = embedding_model.encode(text, convert_to_numpy=True)
    return embedding.tolist()

# API Endpoints

@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "message": "Semantic Search API for Products",
        "version": "1.0.0",
        "endpoints": {
            "sync_products": "/sync-products",
            "semantic_search": "/search",
            "health": "/health"
        }
    }

@app.get("/health")
async def health():
    """Health check endpoint"""
    try:
        collection = get_collection()
        count = collection.count()
        return {
            "status": "healthy",
            "collection": COLLECTION_NAME,
            "products_indexed": count,
            "model": "paraphrase-multilingual-MiniLM-L12-v2"
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.post("/sync-products")
async def sync_products(request: ProductSyncRequest):
    """
    Sync products from Spring Boot backend to ChromaDB
    This endpoint receives products and indexes them for semantic search
    """
    try:
        collection = get_collection()
        
        products_to_add = []
        embeddings_to_add = []
        metadatas_to_add = []
        ids_to_add = []
        
        for product in request.products:
            product_id = str(product.get("id", ""))
            if not product_id:
                continue
            
            # Create searchable text
            text = create_product_text(product)
            
            # Generate embedding
            embedding = generate_embedding(text)
            
            # Prepare metadata
            metadata = {
                "product_id": product_id,
                "title": product.get("title", ""),
                "description": product.get("description", ""),
                "price": float(product.get("price", 0) or 0),  # ✅ Store as float
                "rating": float(product.get("rating", 0) or 0), # ✅ Store as float
                "ratingCount": int(product.get("ratingCount", 0) or 0), # ✅ Store as int
                "category_id": int(product.get("categorie", {}).get("id", 0) if isinstance(product.get("categorie"), dict) else 0), # ✅ Store as int
                "category_name": product.get("categorie", {}).get("nom", "") if isinstance(product.get("categorie"), dict) else "",
                "imageUrl": product.get("imageUrl", ""),
                "asin": product.get("asin", ""),
                "synced_at": datetime.now().isoformat()
            }
            
            products_to_add.append(text)
            embeddings_to_add.append(embedding)
            metadatas_to_add.append(metadata)
            ids_to_add.append(f"product_{product_id}")
        
        if products_to_add:
            # Upsert to ChromaDB (will update if exists, add if new)
            collection.upsert(
                documents=products_to_add,
                embeddings=embeddings_to_add,
                metadatas=metadatas_to_add,
                ids=ids_to_add
            )
        
        return {
            "message": f"Successfully synced {len(products_to_add)} products",
            "count": len(products_to_add)
        }
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error syncing products: {str(e)}")

@app.post("/sync-from-springboot")
async def sync_from_springboot():
    """
    Fetch all products from Spring Boot backend and sync to ChromaDB
    """
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(f"{SPRING_BOOT_URL}/api/produits")
            response.raise_for_status()
            products = response.json()
        
        # Create sync request
        sync_request = ProductSyncRequest(products=products)
        
        # Use the sync endpoint
        result = await sync_products(sync_request)
        
        return {
            "message": "Products synced from Spring Boot backend",
            **result
        }
    
    except httpx.RequestError as e:
        raise HTTPException(
            status_code=503,
            detail=f"Could not connect to Spring Boot backend: {str(e)}"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")

@app.post("/search")
async def semantic_search(request: ProductSearchRequest):
    """
    Perform semantic search on products
    Returns product IDs sorted by relevance
    """
    try:
        if not request.query or not request.query.strip():
            raise HTTPException(status_code=400, detail="Query cannot be empty")
        
        collection = get_collection()
        
        # Generate embedding for the search query
        query_embedding = generate_embedding(request.query)
        
        # Build where clause for filtering
        # ChromaDB uses $and for multiple conditions
        where_conditions = []
        
        if request.min_price is not None:
            where_conditions.append({"price": {"$gte": request.min_price}}) # ✅ No str()
        if request.max_price is not None:
            where_conditions.append({"price": {"$lte": request.max_price}}) # ✅ No str()
        if request.min_rating is not None:
            where_conditions.append({"rating": {"$gte": request.min_rating}}) # ✅ No str()
        if request.category_id is not None:
            where_conditions.append({"category_id": {"$eq": request.category_id}}) # ✅ No str(), compare number
        
        # Build final where clause
        where_clause = None
        if len(where_conditions) == 1:
            where_clause = where_conditions[0]
        elif len(where_conditions) > 1:
            where_clause = {"$and": where_conditions}
        
        # Query ChromaDB
        query_kwargs = {
            "query_embeddings": [query_embedding],
            "n_results": request.n_results
        }
        if where_clause:
            query_kwargs["where"] = where_clause
        
        results = collection.query(**query_kwargs)
        
        # Extract product IDs from results
        product_ids = []
        if results["ids"] and len(results["ids"]) > 0:
            for id_list in results["ids"]:
                for product_id_str in id_list:
                    # Extract numeric ID from "product_123" format
                    if product_id_str.startswith("product_"):
                        product_id = product_id_str.replace("product_", "")
                        product_ids.append(int(product_id))
        
        return {
            "query": request.query,
            "product_ids": product_ids,
            "count": len(product_ids),
            "results": results.get("metadatas", [])
        }
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error performing search: {str(e)}")

@app.get("/search")
async def semantic_search_get(
    query: str,
    n_results: int = 10,
    min_price: Optional[float] = None,
    max_price: Optional[float] = None,
    min_rating: Optional[float] = None,
    category_id: Optional[int] = None
):
    """
    GET endpoint for semantic search (for easier frontend integration)
    """
    request = ProductSearchRequest(
        query=query,
        n_results=n_results,
        min_price=min_price,
        max_price=max_price,
        min_rating=min_rating,
        category_id=category_id
    )
    return await semantic_search(request)

@app.delete("/clear-collection")
async def clear_collection():
    """
    Clear all products from the collection (use with caution)
    """
    try:
        chroma_client.delete_collection(name=COLLECTION_NAME)
        return {"message": "Collection cleared successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error clearing collection: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
