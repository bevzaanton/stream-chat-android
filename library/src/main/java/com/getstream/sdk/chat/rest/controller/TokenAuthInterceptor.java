package com.getstream.sdk.chat.rest.controller;

import android.util.Log;

import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.rest.response.ErrorResponse;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenAuthInterceptor implements Interceptor {

    private final String TAG = getClass().getSimpleName();

    private CachedTokenProvider tokenProvider;
    private String token;

    TokenAuthInterceptor(CachedTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        final CountDownLatch latch = new CountDownLatch(token == null ? 1 : 0);

        if (token == null) {
            tokenProvider.getToken(token -> {
                this.token = token;
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Request request = chain.request();
        request = addTokenHeader(request);
        Response response = chain.proceed(request);

        // check the error and only hit this path if the token was expired (error response code)
        if (!response.isSuccessful()) {
            ErrorResponse err = ErrorResponse.parseError(response);
            if (err.getCode() == ErrorResponse.TOKEN_EXPIRED_CODE) {
                Log.d(TAG, "Retrying new request");
                token = null; // invalidate local cache
                tokenProvider.tokenExpired();
                response.close();
                response = chain.proceed(request);
            }
        }

        return response;
    }

    private Request addTokenHeader(Request req) {
        return req.newBuilder().header("Authorization", token).build();
    }

}
