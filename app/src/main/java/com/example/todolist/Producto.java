package com.example.todolist;

import androidx.annotation.NonNull;

public class Producto {
    private long id;
    private String nombre;
    private double precio;
    private double descuento;  // Nuevo campo
    private int cantidad;      // Nuevo campo

    private int categoryId;  // Nuevo campo para el ID de la categoría
    private String categoryName;  // Variable para almacenar el nombre de la categoría

    public Producto(long id, String nombre, double precio, double descuento, int cantidad, int categoryId) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.descuento = descuento;
        this.cantidad = cantidad;
        this.categoryId = categoryId;  // Inicializar el ID de la categoría
    }

    @SuppressWarnings("unused")
    public Producto(String nombre, double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    // Setter para establecer el nombre de la categoría
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getDiscount() {
        return descuento;
    }

    public void setDiscount(double descuento) {
        this.descuento = descuento;
    }

    public int getQuantity() {
        return cantidad;
    }

    public void setQuantity(int cantidad) {
        this.cantidad = cantidad;
    }

    @NonNull
    @Override
    public String toString() {
        return nombre + " - " + precio;
    }
}
