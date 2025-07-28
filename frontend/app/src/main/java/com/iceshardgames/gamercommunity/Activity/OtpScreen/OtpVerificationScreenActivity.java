package com.iceshardgames.gamercommunity.Activity.OtpScreen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityOtpVerificationScreenBinding;

import java.util.Locale;

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
        binding.headerStart.screenTitleNav.setText("OTP \\nVerification");
        Utills.GradientText(binding.tvVerify);
        Clicks();
    }

    private void Clicks() {
        email = getIntent().getStringExtra("email");
        // Set email text
        binding.emailText.setText("OTP sent to " + email);

        // Start OTP timer (2 minutes)
        startOtpTimer();

        // Set click listeners
        binding.btnVerifyOtp.setOnClickListener(v -> {
//            String otp = binding.pinView.getText().toString();
            String otp = "123456";
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
        // Show loading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying OTP...");
        progressDialog.show();

        // Simulate network call
        new Handler().postDelayed(() -> {
            progressDialog.dismiss();

            // In a real app, you would verify the OTP with your backend
            // For demo, we'll assume it's correct
            if (otp.equals("123456")) { // Replace with actual verification
                // Navigate to password reset screen
                Intent intent = new Intent(this, ResetPasswordScreenActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid OTP. Please try again", Toast.LENGTH_SHORT).show();
            }
        }, 1500);
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