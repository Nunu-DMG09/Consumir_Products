package com.example.consumir_products.api;

import com.example.consumir_products.model.ProductsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("products")
    Call<ProductsResponse> getProducts(@Query("limit") int limit, @Query("skip") int skip);
}
