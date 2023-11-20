package com.example.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todolist.db";
    private static final int DATABASE_VERSION = 3;  // Incrementar la versión

    private static final String TABLE_NAME = "products";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_DISCOUNT = "discount";  // Nueva columna
    private static final String COLUMN_QUANTITY = "quantity";  // Nueva columna
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COLUMN_CATEGORY_ID = "category_id";
    private static final String COLUMN_CATEGORY_NAME = "category_name";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createProductsTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PRICE + " REAL, " +
                COLUMN_DISCOUNT + " REAL DEFAULT 0, " +
                COLUMN_QUANTITY + " INTEGER DEFAULT 1, " +
                COLUMN_CATEGORY_ID + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID + "))";
        db.execSQL(createProductsTable);

        // Añadir la creación de la tabla de categorías
        String createCategoriesTable = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY_NAME + " TEXT UNIQUE)";
        db.execSQL(createCategoriesTable);
        //initializeCategories();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_DISCOUNT + " REAL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_QUANTITY + " INTEGER DEFAULT 1");
        }
        // Considerar agregar lógica para manejar actualizaciones futuras de la base de datos
    }

    public long insertProduct(String name, double price, double discount, int quantity, int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_DISCOUNT, discount);
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_CATEGORY_ID, categoryId);
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public int updateProduct(long id, String name, double price, double discount, int quantity, int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_DISCOUNT, discount);
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_CATEGORY_ID, categoryId);
        int rowsAffected = db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }

    public void deleteProduct(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public ArrayList<Producto> getAllProducts() {
        ArrayList<Producto> productList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
                int priceIndex = cursor.getColumnIndex(COLUMN_PRICE);
                int discountIndex = cursor.getColumnIndex(COLUMN_DISCOUNT);
                int quantityIndex = cursor.getColumnIndex(COLUMN_QUANTITY);
                int categoryIdIndex = cursor.getColumnIndex(COLUMN_CATEGORY_ID);

                long id = cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                double price = cursor.getDouble(priceIndex);
                double discount = cursor.getDouble(discountIndex);
                int quantity = cursor.getInt(quantityIndex);

                int categoryId = -1;  // Valor por defecto si no existe la columna CATEGORY_ID
                if (categoryIdIndex != -1) {
                    categoryId = cursor.getInt(categoryIdIndex);
                }

                Producto product = new Producto(id, name, price, discount, quantity, categoryId);
                productList.add(product);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return productList;
    }


    public void initializeCategories() {
        if (getAllCategories().isEmpty()) {
            String[] initialCategories = {
                    "Verduras", "Frutas", "Carnes", "Lácteos",
                    "Panes y Cereales", "Bebidas", "Limpieza",
                    "Cuidado Personal", "Mascotas", "Otros"
            };

            for (String category : initialCategories) {
                insertCategory(category);
            }
        }
    }


    // Métodos para manejar categorías:

    public long insertCategory(String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, categoryName);
        long id = db.insert(TABLE_CATEGORIES, null, values);
        db.close();
        return id;
    }

    public ArrayList<String> getAllCategories() {
        ArrayList<String> categoryList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CATEGORIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        int columnNameIndex = cursor.getColumnIndex(COLUMN_CATEGORY_NAME);

        if (columnNameIndex != -1 && cursor.moveToFirst()) {
            do {
                categoryList.add(cursor.getString(columnNameIndex));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return categoryList;
    }


    public int getCategoryIdByName(String categoryName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COLUMN_CATEGORY_ID}, COLUMN_CATEGORY_NAME + "=?", new String[]{categoryName}, null, null, null);

        int columnIdIndex = cursor.getColumnIndex(COLUMN_CATEGORY_ID);

        if (columnIdIndex != -1 && cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(columnIdIndex);
            cursor.close();
            db.close();
            return id;
        }
        db.close();
        return -1;
    }


    public String getCategoryNameById(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COLUMN_CATEGORY_NAME}, COLUMN_CATEGORY_ID + "=?", new String[]{String.valueOf(categoryId)}, null, null, null);

        int columnNameIndex = cursor.getColumnIndex(COLUMN_CATEGORY_NAME);

        if (columnNameIndex != -1 && cursor != null && cursor.moveToFirst()) {
            String categoryName = cursor.getString(columnNameIndex);
            cursor.close();
            db.close();
            return categoryName;
        }
        db.close();
        return "";
    }




}
