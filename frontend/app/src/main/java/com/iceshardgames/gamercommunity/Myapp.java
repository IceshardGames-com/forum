package com.iceshardgames.gamercommunity;

import android.app.Application;

import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.Utills.SessionManager;

public class Myapp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Configure ApiClient with a TokenProvider that reads from SessionManager
        ApiClient.setTokenProvider(() -> SessionManager.get().getAccessToken());
    }
}
