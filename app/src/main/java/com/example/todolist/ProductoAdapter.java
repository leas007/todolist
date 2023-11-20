package com.example.todolist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Comparator;


public class ProductoAdapter extends BaseAdapter implements Filterable {

    private final Context context;
    private final ArrayList<Producto> products;
    private final ArrayList<Producto> originalProducts;
    private final ArrayList<Producto> filteredProducts;


    public ProductoAdapter(Context context, ArrayList<Producto> products) {
        this.context = context;
        this.originalProducts = new ArrayList<>(products);
        this.filteredProducts = new ArrayList<>(products);
        this.products = filteredProducts;
    }

    public void addProduct(Producto product) {
        this.originalProducts.add(product);
        this.filteredProducts.add(product);
        notifyDataSetChanged();
    }

    public void orderByNameAsc() {
        products.sort(Comparator.comparing(Producto::getNombre));
        notifyDataSetChanged();
    }

    public void orderByNameDesc() {
        products.sort(Comparator.comparing(Producto::getNombre).reversed());
        notifyDataSetChanged();
    }

    public void orderByPriceAsc() {
        products.sort(Comparator.comparingDouble(Producto::getPrecio));
        notifyDataSetChanged();
    }

    public void orderByPriceDesc() {
        products.sort(Comparator.comparingDouble(Producto::getPrecio).reversed());
        notifyDataSetChanged();
    }



    public void updateProduct(Producto updatedProduct) {
        int originalIndex = findProductIndexById(originalProducts, updatedProduct.getId());
        if (originalIndex != -1) {
            this.originalProducts.set(originalIndex, updatedProduct);
        }

        int filteredIndex = findProductIndexById(filteredProducts, updatedProduct.getId());
        if (filteredIndex != -1) {
            this.filteredProducts.set(filteredIndex, updatedProduct);
        }
        notifyDataSetChanged();
    }

    public void deleteProduct(long productId) {
        this.originalProducts.removeIf(p -> p.getId() == productId);
        this.filteredProducts.removeIf(p -> p.getId() == productId);
        notifyDataSetChanged();
    }

    private int findProductIndexById(ArrayList<Producto> list, long id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }



    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Producto> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(originalProducts);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Producto product : originalProducts) {
                        if (product.getNombre().toLowerCase().contains(filterPattern)) {
                            filteredList.add(product);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredProducts.clear();

                // Comprobar que results.values es una ArrayList
                if (results.values instanceof ArrayList<?>) {
                    ArrayList<?> list = (ArrayList<?>) results.values;

                    // Iterar sobre los elementos de la lista
                    for (Object item : list) {
                        if (item instanceof Producto) { // Comprobar que el elemento es un Producto
                            filteredProducts.add((Producto) item);
                        }
                    }
                }

                notifyDataSetChanged();
            }

        };
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_row, parent, false);
        }

        Producto currentProduct = products.get(position);

        TextView tvProductName = convertView.findViewById(R.id.productName);
        TextView tvProductPrice = convertView.findViewById(R.id.productPrice);
        TextView tvQuantity = convertView.findViewById(R.id.tvQuantity);
        TextView tvDiscount = convertView.findViewById(R.id.tvDiscount);

        TextView tvProductCategory = convertView.findViewById(R.id.productCategory);


        tvProductName.setText(currentProduct.getNombre());
        tvProductPrice.setText(String.valueOf(currentProduct.getPrecio()));
        tvQuantity.setText(context.getString(R.string.cantidad_text, currentProduct.getQuantity()));
        tvDiscount.setText(context.getString(R.string.descuento_text, currentProduct.getDiscount()));

        tvProductCategory.setText(currentProduct.getCategoryName());


        return convertView;
    }
}
