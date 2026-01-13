
import pandas as pd
import mysql.connector
from mysql.connector import Error
import math

def create_connection():
    try:
        connection = mysql.connector.connect(
            host='localhost',
            database='gestion_ventes',
            user='root',
            password='zago1234'
        )
        return connection
    except Error as e:
        print(f"Error connecting to MySQL: {e}")
        return None

def get_or_create_category(cursor, category_name):
    query = "SELECT id FROM categorie WHERE nom = %s"
    cursor.execute(query, (category_name,))
    result = cursor.fetchone()
    if result:
        return result[0]
    else:
        insert_query = "INSERT INTO categorie (nom, description) VALUES (%s, %s)"
        cursor.execute(insert_query, (category_name, "Imported Category"))
        return cursor.lastrowid

def get_default_user_id(cursor):
    cursor.execute("SELECT id FROM utilisateur LIMIT 1")
    result = cursor.fetchone()
    if result:
        return result[0]
    else:
        # Create default user if none exists
        insert_query = "INSERT INTO utilisateur (email, nom, mot_de_passe, role) VALUES (%s, %s, %s, %s)"
        cursor.execute(insert_query, ("admin@example.com", "Admin", "password", "ADMIN"))
        return cursor.lastrowid

def clean_value(val, default):
    if pd.isna(val) or val == 'nan':
        return default
    return val

def clean_currency(val):
    if isinstance(val, (int, float)):
        return float(val)
    if not isinstance(val, str):
        return 0.0
    val = val.replace('$', '').replace(',', '').strip()
    try:
        return float(val)
    except:
        return 0.0

def clean_int(val):
    if isinstance(val, (int, float)):
        return int(val)
    if not isinstance(val, str):
        return 0
    val = val.replace(',', '').replace('.', '').strip()
    try:
        return int(val)
    except:
        return 0

def import_csv(file_path):
    conn = create_connection()
    if not conn:
        return

    cursor = conn.cursor()
    
    try:
        print(f"Reading CSV from {file_path}...")
        df = pd.read_csv(file_path)
        
        user_id = get_default_user_id(cursor)
        print(f"Using User ID: {user_id}")
        
        count = 0
        for index, row in df.iterrows():
            asin = str(row['ASIN'])
            
            # Check for duplicates
            cursor.execute("SELECT id FROM produits WHERE code_asin = %s", (asin,))
            if cursor.fetchone():
                continue

            category_name = str(row['Category'])
            category_id = get_or_create_category(cursor, category_name)
            
            # Clean and map fields
            title = clean_value(row['title'], "No Title")
            description = clean_value(row['description'], "")
            image_url = clean_value(row['image_url'], "")
            no_sellers = clean_value(row['No of Sellers'], "0")
            
            rank = clean_int(row['Rank'])
            rating = clean_currency(row['Rating']) # Using clean_currency for float parsing
            reviews_count = clean_int(row['Reviews Count'])
            price = clean_currency(row['Price'])
            
            # Insert Product
            insert_query = """
            INSERT INTO produits (
                code_asin, titre, description, image_url, 
                nombre_vendur, rang_amazon, note_moyenne, nombre_avis, prix,
                categorie_id, utilisateur_id, quantite_disponible
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """
            
            # Note: Field names in DB might differ slightly based on Product.java annotations
            # Reading Product.java:
            # @Column(name = "code_asin") -> code_asin
            # @Column(name = "titre") -> titre
            # @Column(name = "description") -> description
            # @Column(name = "image_url") -> image_url
            # @Column(name = "Nombre_vendur") -> Nombre_vendur (Case sensitive in MySQL on Linux? Windows is case insensitive)
            # @Column(name = "rang_amazon") -> rang_amazon
            # @Column(name = "note_moyenne") -> note_moyenne
            # @Column(name = "nombre_avis") -> nombre_avis
            # @Column(name = "prix") -> prix
            # @JoinColumn(name = "categorie_id") -> categorie_id
            # @JoinColumn(name = "utilisateur_id") -> utilisateur_id
            
            # Correction for 'Nombre_vendur' column name based on entity
            insert_query = insert_query.replace("nombre_vendur", "Nombre_vendur")

            values = (
                asin, title, description, image_url, 
                no_sellers, rank, rating, reviews_count, price,
                category_id, user_id, 100
            )
            
            cursor.execute(insert_query, values)
            count += 1
            
            if count % 50 == 0:
                conn.commit()
                print(f"Imported {count} products...")
                
        conn.commit()
        print(f"Successfully imported {count} products!")
        
    except Error as e:
        print(f"Error during import: {e}")
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

if __name__ == "__main__":
    # Path to the CSV file on User's desktop
    csv_file_path = r"c:\Users\XPS\OneDrive\Desktop\ProjectJEE\FinalCleaned.csv"
    import_csv(csv_file_path)
