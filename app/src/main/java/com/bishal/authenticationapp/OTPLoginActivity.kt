package com.bishal.authenticationapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bishal.authenticationapp.databinding.ActivityOtploginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class OTPLoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityOtploginBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var number: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtploginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.sendOtpBtn.setOnClickListener {
            number = binding.mobileNumberInput.text?.trim().toString()
            if (number.isNotEmpty()){
                if (number.length == 10){
                    number = "+91$number"
                    binding.mobileProgressBar.visibility = View.VISIBLE
                    val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)

                    firebaseAuth.setLanguageCode("en")
                    // To apply the default app language instead of explicitly setting it.
                    // auth.useAppLanguage()
                }
                else{
                    Toast.makeText(this@OTPLoginActivity, "Provide a correct mobile number!", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this@OTPLoginActivity, "Please provide your mobile number!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this@OTPLoginActivity, "Authentication was successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@OTPLoginActivity, MainActivity::class.java))
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this@OTPLoginActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                    Toast.makeText(this@OTPLoginActivity, "Could not Log In due to : ${task.exception.toString()}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "onVerificationFailed: $e")
                Toast.makeText(this@OTPLoginActivity, "Invalid Credentials, Log In Failed!", Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: $e")
                Toast.makeText(this@OTPLoginActivity, "Too many OTP requests, Try after some time!", Toast.LENGTH_SHORT).show()
            }

            // Show a message and update the UI
            startActivity(Intent(this@OTPLoginActivity, SignInActivity::class.java))
            finish()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            val intent = Intent(this@OTPLoginActivity, OTPVerificationActivity::class.java)
            intent.putExtra("OTP", verificationId)
            intent.putExtra("resendToken", token)
            intent.putExtra("phoneNumber", number)
            startActivity(intent)
            binding.mobileProgressBar.visibility = View.GONE
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null){
            startActivity(Intent(this@OTPLoginActivity, MainActivity::class.java))
            finish()
        }
    }
}