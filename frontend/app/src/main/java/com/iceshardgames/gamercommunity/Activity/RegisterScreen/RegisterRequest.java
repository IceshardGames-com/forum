package com.iceshardgames.gamercommunity.Activity.RegisterScreen;
public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private String confirmPassword;

    public RegisterRequest(String username,String email , String password, String confirmPassword) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
}

