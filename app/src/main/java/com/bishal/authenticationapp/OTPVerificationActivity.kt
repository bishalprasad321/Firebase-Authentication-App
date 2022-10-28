package com.bishal.authenticationapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bishal.authenticationapp.databinding.ActivityOtpverificationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class OTPVerificationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityOtpverificationBinding
    private lateinit var number : String
    private lateinit var otp : String
    private lateinit var resendToken : PhoneAuthProvider.ForceResendingToken

    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout for binding the XML with Code
        binding = ActivityOtpverificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

//        val intent = Intent(this@OTPVerificationActivity, OTPLoginActivity::class.java)
        number = intent.getStringExtra("phoneNumber").toString()
        otp = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!

        addTextChangeListener()
        resendOtpVisibility()

        binding.verifyBtn.setOnClickListener {
            val typedOtp = (binding.otp1.text.toString() + binding.otp2.text.toString() +
                    binding.otp3.text.toString() + binding.otp4.text.toString() + binding.otp5.text.toString() + binding.otp6.text.toString())
            if (typedOtp.isNotEmpty()){
                if (typedOtp.length == 6){
                    val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        otp, typedOtp
                    )
                    binding.otpProgressBar.visibility = View.VISIBLE
                    signInWithPhoneAuthCredential(credential)
                } else{
                    Toast.makeText(this@OTPVerificationActivity, "Please enter the correct OTP", Toast.LENGTH_SHORT).show()
                }
            } else{
                Toast.makeText(this@OTPVerificationActivity, "Please Enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }

        binding.resendOtp.setOnClickListener {
            resendVerificationCode()
            resendOtpVisibility()
        }

    }

    private fun resendOtpVisibility(){
        binding.otp1.setText("")
        binding.otp2.setText("")
        binding.otp3.setText("")
        binding.otp4.setText("")
        binding.otp5.setText("")
        binding.otp6.setText("")

        binding.resendOtp.visibility = View.INVISIBLE
        binding.resendOtp.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed({
            binding.resendOtp.visibility = View.VISIBLE
            binding.resendOtp.isEnabled = true
        }, 60000)
    }

    private fun resendVerificationCode(){
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(number)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this@OTPVerificationActivity)                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
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
                Toast.makeText(this@OTPVerificationActivity, "Invalid Credentials, Log In Failed!", Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: $e")
                Toast.makeText(this@OTPVerificationActivity, "Too many OTP requests, Try after some time!", Toast.LENGTH_SHORT).show()
            }

            // Show a message and update the UI
            startActivity(Intent(this@OTPVerificationActivity, SignInActivity::class.java))
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
            otp = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    binding.otpProgressBar.visibility = View.GONE
                    Toast.makeText(this@OTPVerificationActivity, "Authentication was successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@OTPVerificationActivity, MainActivity::class.java))
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this@OTPVerificationActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                    Toast.makeText(this@OTPVerificationActivity, "Could not Log In due to : ${task.exception.toString()}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addTextChangeListener(){
        binding.otp1.addTextChangedListener(EditTextWatcher(binding.otp1))
        binding.otp2.addTextChangedListener(EditTextWatcher(binding.otp2))
        binding.otp3.addTextChangedListener(EditTextWatcher(binding.otp3))
        binding.otp4.addTextChangedListener(EditTextWatcher(binding.otp4))
        binding.otp5.addTextChangedListener(EditTextWatcher(binding.otp5))
        binding.otp6.addTextChangedListener(EditTextWatcher(binding.otp6))
    }

    inner class EditTextWatcher(private val view: View) : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.otp1.text?.let { binding.verifyBtn.isEnabled = it.isNotEmpty() }
            binding.otp2.text?.let { binding.verifyBtn.isEnabled = it.isNotEmpty() }
            binding.otp3.text?.let { binding.verifyBtn.isEnabled = it.isNotEmpty() }
            binding.otp4.text?.let { binding.verifyBtn.isEnabled = it.isNotEmpty() }
            binding.otp5.text?.let { binding.verifyBtn.isEnabled = it.isNotEmpty() }
            binding.otp6.text?.let { binding.verifyBtn.isEnabled = it.isNotEmpty() }
        }

        override fun afterTextChanged(s: Editable?) {
            val text = s.toString()
            when(view.id){
                R.id.otp1 -> if (text.length == 1) binding.otp2.requestFocus() else if (text.isEmpty()) binding.otp1.requestFocus()
                R.id.otp2 -> if (text.length == 1) binding.otp3.requestFocus() else if (text.isEmpty()) binding.otp1.requestFocus()
                R.id.otp3 -> if (text.length == 1) binding.otp4.requestFocus() else if (text.isEmpty()) binding.otp2.requestFocus()
                R.id.otp4 -> if (text.length == 1) binding.otp5.requestFocus() else if (text.isEmpty()) binding.otp3.requestFocus()
                R.id.otp5 -> if (text.length == 1) binding.otp6.requestFocus() else if (text.isEmpty()) binding.otp4.requestFocus()
                R.id.otp6 -> if (text.isEmpty()) binding.otp5.requestFocus()
            }
        }

    }
}
