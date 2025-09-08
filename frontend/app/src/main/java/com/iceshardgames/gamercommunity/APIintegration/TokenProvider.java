package com.iceshardgames.gamercommunity.APIintegration;

public interface TokenProvider {
    String getToken(); // return raw JWT or null if not available
}
