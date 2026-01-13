"""
Helper script to sync products from Spring Boot backend to ChromaDB
Run this script after starting both Spring Boot and FastAPI servers
"""

import requests
import sys

SPRING_BOOT_URL = "http://localhost:8080"
FASTAPI_URL = "http://localhost:8000"

def sync_products():
    """Fetch products from Spring Boot and sync to ChromaDB"""
    try:
        print("Fetching products from Spring Boot backend...")
        response = requests.get(f"{SPRING_BOOT_URL}/api/produits")
        response.raise_for_status()
        products = response.json()
        
        print(f"Found {len(products)} products")
        
        if len(products) == 0:
            print("No products found. Make sure Spring Boot backend has products.")
            return
        
        print("Syncing products to ChromaDB...")
        sync_response = requests.post(
            f"{FASTAPI_URL}/sync-products",
            json={"products": products}
        )
        sync_response.raise_for_status()
        
        result = sync_response.json()
        print(f"✅ Successfully synced {result['count']} products!")
        print(f"Products are now searchable using semantic search.")
        
    except requests.exceptions.ConnectionError as e:
        print(f"❌ Error: Could not connect to backend.")
        print(f"   Make sure Spring Boot is running on {SPRING_BOOT_URL}")
        print(f"   Make sure FastAPI is running on {FASTAPI_URL}")
        sys.exit(1)
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    sync_products()
