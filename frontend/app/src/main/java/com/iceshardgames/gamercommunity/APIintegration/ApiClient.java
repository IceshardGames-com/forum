package com.iceshardgames.gamercommunity.APIintegration;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ApiClient {
    private static volatile Retrofit retrofit;
    private static volatile TokenProvider tokenProvider;
    public static final String BASE_URL = "https://forum-sjpj.onrender.com/";

    public static void setTokenProvider(TokenProvider provider) {
        tokenProvider = provider;
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

                    Interceptor auth = chain -> {
                        Request original = chain.request();
                        String token = tokenProvider != null ? tokenProvider.getToken() : null;

                        Request.Builder rb = original.newBuilder()
                                .addHeader("Content-Type", "application/json");
                        if (token != null && !token.isEmpty()) {
                            rb.addHeader("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(rb.build());
                    };

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .addInterceptor(auth)
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }
}