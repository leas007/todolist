package com.example.todolist;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
//import java.util.Comparator;
import java.util.Locale;
import android.text.Editable;
import android.text.TextWatcher;


public class MainActivity extends AppCompatActivity {

    private TextView totalPriceView;
    private ProductoAdapter adapter;
    private ArrayList<Producto> products;
    private DatabaseHelper dbHelper;
    private Spinner  sortSpinner;
    private ListView taskList;
    private Button addButton;
    private EditText searchInput;
    private int categoryId;
    private ArrayList<String> categories;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        products = dbHelper.getAllProducts();
        adapter = new ProductoAdapter(this, products);
        dbHelper.initializeCategories(); // Línea añadida

        taskList = findViewById(R.id.taskList);
        taskList.setAdapter(adapter);
        totalPriceView = findViewById(R.id.totalPrice);
        sortSpinner = findViewById(R.id.sortSpinner);

        //Poblar el Spinner con Categorías
        // Inicializa el spinner
        Spinner categorySpinner = findViewById(R.id.categorySpinner);

        Spinner categoryFilterSpinner = findViewById(R.id.categorySpinner);

        categories = new ArrayList<>();

        // Agregar "Todas las categorías" al inicio de la lista
        categories.add(0, "Todas las categorías");

        // Obtiene todas las categorías de la base de datos
        ArrayList<String> categories = dbHelper.getAllCategories();

        // Crea un adaptador para el spinner con las categorías
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asigna el adaptador al spinner
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = (String) parent.getItemAtPosition(position);
                categoryId = dbHelper.getCategoryIdByName(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Puedes dejar este método vacío o manejarlo según lo necesites
            }
        });

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sort_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                orderProductList(position);
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do here
            }
        });



        searchInput = findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        taskList.setOnItemLongClickListener((parent, view, position, id) -> {
            Producto selectedProduct = (Producto) parent.getItemAtPosition(position);
            showEditDialog(selectedProduct, position);
            return true;
        });

        updateTotalPrice();

        EditText taskInput = findViewById(R.id.taskInput);
        EditText priceInput = findViewById(R.id.priceInput);
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            String name = taskInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();

            if (!name.isEmpty() && !priceStr.isEmpty()) {
                double price = Double.parseDouble(priceStr);
                double discount = 0.0; // valor por defecto
                int quantity = 1; // valor por defecto

                long id = dbHelper.insertProduct(name, price, discount, quantity, categoryId);
                if (id != -1) {
                    Producto newProduct = new Producto(id, name, price, discount, quantity, categoryId);
                    adapter.addProduct(newProduct);  // Cambio aquí
                    updateTotalPrice();

                    taskInput.setText("");
                    priceInput.setText("");
                    Toast.makeText(MainActivity.this, "Producto agregado!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error al agregar producto.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Por favor, ingrese nombre y precio.", Toast.LENGTH_SHORT).show();
            }
            updateProductList();  // Llama a este método para forzar la actualización
        });
    }

    private void filterProductsByCategory(String category) {
        ProductoAdapter adapter = (ProductoAdapter) taskList.getAdapter();
        if ("Todas las categorías".equals(category)) {
            adapter.getFilter().filter(null);
        } else {
            adapter.getFilter().filter(category);
        }
    }


    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    private void updateProductList() {
        products.clear();
        products.addAll(dbHelper.getAllProducts());
        orderProductList(sortSpinner.getSelectedItemPosition());
        adapter.notifyDataSetChanged();
        updateTotalPrice();
    }

    private void showEditDialog(Producto product, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_product, null);

        EditText editName = view.findViewById(R.id.editProductName);
        EditText editPrice = view.findViewById(R.id.editProductPrice);
        EditText editDiscount = view.findViewById(R.id.editProductDiscount);
        EditText editQuantity = view.findViewById(R.id.editProductQuantity);

        Button saveButton = view.findViewById(R.id.saveButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button deleteInDialogButton = view.findViewById(R.id.deleteInDialogButton);

        editName.setText(product.getNombre());
        editPrice.setText(String.valueOf(product.getPrecio()));
        editDiscount.setText(String.valueOf(product.getDiscount()));
        editQuantity.setText(String.valueOf(product.getQuantity()));

        final AlertDialog dialog = builder.setView(view).create();

        Spinner categorySpinnerInDialog = view.findViewById(R.id.categorySpinner);

        // Usamos el método dbHelper.getAllCategories() directamente
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dbHelper.getAllCategories());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerInDialog.setAdapter(categoryAdapter);
        // Setea la categoría actual del producto en el Spinner
        int spinnerPosition = categoryAdapter.getPosition(dbHelper.getCategoryNameById(product.getCategoryId()));
        categorySpinnerInDialog.setSelection(spinnerPosition);

        saveButton.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            String newPriceStr = editPrice.getText().toString().trim();
            String newDiscountStr = editDiscount.getText().toString().trim();
            String newQuantityStr = editQuantity.getText().toString().trim();

            if (!newName.isEmpty() && !newPriceStr.isEmpty() && !newDiscountStr.isEmpty() && !newQuantityStr.isEmpty()) {
                double newPrice = Double.parseDouble(newPriceStr);
                double newDiscount = Double.parseDouble(newDiscountStr);
                int newQuantity = Integer.parseInt(newQuantityStr);

                String selectedCategory = categorySpinnerInDialog.getSelectedItem().toString();
                int categoryId = dbHelper.getCategoryIdByName(selectedCategory);

                int rowsAffected = dbHelper.updateProduct(product.getId(), newName, newPrice, newDiscount, newQuantity, categoryId);

                if (rowsAffected > 0) {
                    product.setNombre(newName);
                    product.setPrecio(newPrice);
                    product.setDiscount(newDiscount);
                    product.setQuantity(newQuantity);
                    adapter.updateProduct(product);

                    updateTotalPrice();
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Error al actualizar el producto.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            }
            updateProductList();
        });

        deleteInDialogButton.setOnClickListener(v -> {
            dbHelper.deleteProduct(product.getId());
            products.remove(position);
            adapter.deleteProduct(product.getId());
            updateTotalPrice();
            dialog.dismiss();
            updateProductList();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    private void updateTotalPrice() {
        double total = products.stream().mapToDouble(product -> calcularPrecioConDescuento(product.getPrecio(), product.getQuantity(), product.getDiscount())).sum();
        totalPriceView.setText(String.format(Locale.getDefault(), getString(R.string.total_price_format), total));
    }

    private double calcularPrecioConDescuento(double precioUnitario, int unidades, double porcentajeDescuento) {
        // Determinar cuántas unidades tienen descuento
        int unidadesConDescuento = unidades / 2;

        // Calcular el precio con descuento
        double precioConDescuento = precioUnitario * (1 - porcentajeDescuento / 100);

        // Devuelve el cálculo directamente sin usar una variable adicional
        return (unidades - unidadesConDescuento) * precioUnitario + unidadesConDescuento * precioConDescuento;
    }


    private void orderProductList(int sortOption) {
        switch (sortOption) {
            case 0: // Ordenar por nombre ascendente
                adapter.orderByNameAsc();
                break;
            case 1: // Ordenar por nombre descendente
                adapter.orderByNameDesc();
                break;
            case 2: // Ordenar por precio ascendente
                adapter.orderByPriceAsc();
                break;
            case 3: // Ordenar por precio descendente
                adapter.orderByPriceDesc();
                break;
        }
    }


}
