package com.example.consumir_products;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.consumir_products.adapter.ProductsAdapter;
import com.example.consumir_products.api.ApiService;
import com.example.consumir_products.api.RetrofitClient;
import com.example.consumir_products.model.Product;
import com.example.consumir_products.model.ProductsResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProductsAdapter adapter;
    ProgressBar progressBar;
    SearchView searchView;
    Spinner spinnerFilter;

    List<Product> allProducts = new ArrayList<>();
    List<Product> displayedProducts = new ArrayList<>();

    int limit = 5;
    int skip = 0;
    boolean isLoading = false;
    boolean isLastPage = false;

    ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchView);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductsAdapter(this);
        recyclerView.setAdapter(adapter);

        api = RetrofitClient.getClient().create(ApiService.class);

        loadProducts();

        setupScrollListener();
        setupSearchView();
        setupFilter();
    }

    private void loadProducts() {
        if (isLoading || isLastPage) return;

        isLoading = true;
        progressBar.setVisibility(ProgressBar.VISIBLE);

        api.getProducts(limit, skip).enqueue(new Callback<ProductsResponse>() {
            @Override
            public void onResponse(Call<ProductsResponse> call, Response<ProductsResponse> response) {
                progressBar.setVisibility(ProgressBar.GONE);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    List<Product> newProducts = response.body().products;
                    if (newProducts.isEmpty()) {
                        isLastPage = true;
                    } else {
                        skip += limit;
                        allProducts.addAll(newProducts);
                        displayedProducts.addAll(newProducts);
                        adapter.setProducts(displayedProducts);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProductsResponse> call, Throwable t) {
                progressBar.setVisibility(ProgressBar.GONE);
                isLoading = false;
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);

                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm != null && lm.findLastCompletelyVisibleItemPosition() == displayedProducts.size() - 1) {
                    loadProducts();
                }
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });
    }

    private void filterProducts(String query) {
        displayedProducts.clear();
        if (query.isEmpty()) {
            displayedProducts.addAll(allProducts);
        } else {
            for (Product p : allProducts) {
                if (p.title.toLowerCase().contains(query.toLowerCase())) {
                    displayedProducts.add(p);
                }
            }
        }
        adapter.setProducts(displayedProducts);
    }

    private void setupFilter() {
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                switch (position) {
                    case 1:
                        Collections.sort(displayedProducts, Comparator.comparing(p -> p.title.toLowerCase()));
                        break;
                    case 2:
                        Collections.sort(displayedProducts, (p1, p2) -> p2.title.compareToIgnoreCase(p1.title));
                        break;
                    case 3:
                        Collections.sort(displayedProducts, (p1, p2) -> Double.compare(p2.price, p1.price));
                        break;
                    case 4:
                        Collections.sort(displayedProducts, Comparator.comparingDouble(p -> p.price));
                        break;
                }
                adapter.setProducts(displayedProducts);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
}