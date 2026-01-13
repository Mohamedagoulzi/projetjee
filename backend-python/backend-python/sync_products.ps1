# PowerShell script to sync products from Spring Boot backend to ChromaDB
# Usage: .\sync_products.ps1

$SPRING_BOOT_URL = "http://localhost:8080"
$FASTAPI_URL = "http://localhost:8000"

Write-Host "Fetching products from Spring Boot backend..." -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$SPRING_BOOT_URL/api/produits" -Method Get
    $products = $response
    
    Write-Host "Found $($products.Count) products" -ForegroundColor Green
    
    if ($products.Count -eq 0) {
        Write-Host "No products found. Make sure Spring Boot backend has products." -ForegroundColor Yellow
        exit
    }
    
    Write-Host "Syncing products to ChromaDB..." -ForegroundColor Cyan
    
    $body = @{
        products = $products
    } | ConvertTo-Json -Depth 10
    
    $syncResponse = Invoke-RestMethod -Uri "$FASTAPI_URL/sync-products" -Method Post -Body $body -ContentType "application/json"
    
    Write-Host "✅ Successfully synced $($syncResponse.count) products!" -ForegroundColor Green
    Write-Host "Products are now searchable using semantic search." -ForegroundColor Green
    
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "   Make sure FastAPI server is running on $FASTAPI_URL" -ForegroundColor Yellow
    } elseif ($_.Exception.Response.StatusCode -eq 503) {
        Write-Host "   Make sure Spring Boot is running on $SPRING_BOOT_URL" -ForegroundColor Yellow
    }
    exit 1
}
