package com.iceshardgames.gamercommunity.Activity.OtpScreen;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityOtpVerificationScreenBinding;
import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import java.util.Locale;

import retrofit2.Call;

public class OtpVerificationScreenActivity extends AppCompatActivity {

    ActivityOtpVerificationScreenBinding binding;
    private String email;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOtpVerificationScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Utills.GradientText(binding.headerStart.screenTitleNav);
        binding.headerStart.screenTitleNav.setText("Confirm Your Account");
        Utills.GradientText(binding.tvVerify);
        Clicks();
    }

    private void Clicks() {
        email = getIntent().getStringExtra("email");
        // Set email text
        binding.emailText.setText("OTP sent to " + email);

        // Start OTP timer (2 minutes)
        startOtpTimer();
        binding.pinView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.pinView, InputMethodManager.SHOW_IMPLICIT);
        // Set click listeners
        binding.btnVerifyOtp.setOnClickListener(v -> {
            String otp = binding.pinView.getText().toString();
//            String otp = "123456";
            if (otp.length() == 6) {
                verifyOtp(otp);
            } else {
                Toast.makeText(this, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });

        binding.resendOtpTextlayout.setOnClickListener(v -> {
            if (binding.resendOtpText.getText().toString().contains("Resend")) {
                resendOtp();
            }
        });

        // Set OnClickListener for the back button (top nav)
        binding.headerStart.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity or close the app
            }
        });
    }
    private void startOtpTimer() {
        binding.resendOtpText.setVisibility(View.GONE);
        binding.timerText.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                binding.timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                binding.timerText.setVisibility(View.GONE);
                binding.resendOtpText.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void verifyOtp(String otp) {
//        // Show loading
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Verifying OTP...");
//        progressDialog.show();

        Utills.showLoadingDialog(OtpVerificationScreenActivity.this);

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);

        apiService.verifyOtp(request).enqueue(new retrofit2.Callback<OtpVerifyResponse>() {
            @Override
            public void onResponse(Call<OtpVerifyResponse> call, retrofit2.Response<OtpVerifyResponse> response) {
                Utills.hideLoadingDialog();

                if (response.isSuccessful() && response.body() != null) {
                    OtpVerifyResponse otpResponse = response.body();

                    if (otpResponse.isSuccess()) {
                        Toast.makeText(OtpVerificationScreenActivity.this, otpResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(OtpVerificationScreenActivity.this, ResetPasswordScreenActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    } else {
                        Toast.makeText(OtpVerificationScreenActivity.this, otpResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(OtpVerificationScreenActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OtpVerifyResponse> call, Throwable t) {
                Utills.hideLoadingDialog();
                Toast.makeText(OtpVerificationScreenActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendOtp() {
        // Show loading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Resending OTP...");
        progressDialog.show();

        // Simulate network call
        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            Toast.makeText(this, "OTP resent to your email", Toast.LENGTH_SHORT).show();
            startOtpTimer();
        }, 1500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}