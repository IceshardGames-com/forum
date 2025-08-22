package com.iceshardgames.gamercommunity.Activity.OtpScreen;

public class VerifyOtpRequest {
    private String email;
    private String otp;

    public VerifyOtpRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }
}
