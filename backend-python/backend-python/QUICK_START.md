# Quick Start Guide - Semantic Search Setup

## ✅ Fixed Issues

1. **Version Compatibility**: Upgraded `sentence-transformers` from 2.2.2 to 5.2.0 to fix the `cached_download` import error
2. **PowerShell Support**: Added PowerShell scripts for Windows users

## 🚀 Quick Start Steps

### Step 1: Install/Upgrade Dependencies

```powershell
cd "C:\Users\asus\Desktop\javaProject\JEE APP\backend-python"
pip install -r requirements.txt
pip install --upgrade sentence-transformers huggingface-hub
```

### Step 2: Start FastAPI Server

```powershell
python main.py
```

You should see:
```
Loading sentence transformer model...
Model loaded successfully!
INFO:     Started server process
INFO:     Uvicorn running on http://0.0.0.0:8000
```

**Note**: The first time, it will download the model (~420MB). This is normal and only happens once.

### Step 3: Sync Products (in a new terminal)

Make sure your Spring Boot backend is running first, then:

**Option A: PowerShell Script**
```powershell
.\sync_products.ps1
```

**Option B: Python Script**
```powershell
python sync_products.py
```

**Option C: Direct API Call**
```powershell
Invoke-RestMethod -Uri "http://localhost:8000/sync-from-springboot" -Method POST
```

### Step 4: Test Semantic Search

```powershell
# Test search
Invoke-RestMethod -Uri "http://localhost:8000/search?query=smartphone&n_results=5"
```

### Step 5: Use in Frontend

1. Start your React frontend
2. Go to the buyer (achteur) view
3. Check the "Recherche sémantique (IA)" checkbox
4. Enter your search query
5. Results will use semantic matching!

## 🔧 Troubleshooting

### Server won't start
- Check if port 8000 is already in use
- Make sure all dependencies are installed
- Check Python version (needs 3.8+)

### Import errors
```powershell
pip install --upgrade sentence-transformers huggingface-hub transformers torch
```

### 404 errors when syncing
- Make sure FastAPI server is running (`python main.py`)
- Check the URL: `http://localhost:8000`
- Verify Spring Boot is running on port 8080

### PowerShell curl issues
Use `Invoke-RestMethod` or `Invoke-WebRequest` instead of `curl`:
```powershell
# Instead of: curl -X POST http://localhost:8000/sync-from-springboot
Invoke-RestMethod -Uri "http://localhost:8000/sync-from-springboot" -Method POST
```

## 📝 API Endpoints

- `GET /` - API info
- `GET /health` - Health check
- `POST /sync-products` - Sync products (manual)
- `POST /sync-from-springboot` - Auto-sync from Spring Boot
- `GET /search?query=...` - Semantic search
- `POST /search` - Semantic search (with body)

## 🎯 Example Queries

Try these in the frontend with semantic search enabled:
- "smartphone pas cher" → finds affordable phones
- "livre de cuisine" → finds cooking books  
- "cadeau pour enfant" → finds children's gifts
- "écran 4K" → finds 4K displays

The semantic search understands meaning, not just exact keywords!
