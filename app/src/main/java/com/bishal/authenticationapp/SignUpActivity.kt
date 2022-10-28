package com.bishal.authenticationapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bishal.authenticationapp.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleBtn.setOnClickListener{
            signInGoogle()
        }


        binding.createAccountBtn.setOnClickListener{
            val email = binding.emailInput.text.toString()
            val pass = binding.passwordInput.text.toString()
            val name = binding.nameInput.text.toString()

            if (email.isNotEmpty() && name.isNotEmpty() && pass.isNotEmpty()){
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener{
                    if (it.isSuccessful){
                        startActivity(Intent(applicationContext, SignInActivity::class.java))
                        finish()
                    }
                    else{
                        Toast.makeText(applicationContext, it.exception.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
            else{
                Toast.makeText(applicationContext, "Please fill out all the empty fields !", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginText.setOnClickListener{
            startActivity(Intent(applicationContext, SignInActivity::class.java))
            finish()
        }

        binding.phoneBtn.setOnClickListener{
            startActivity(Intent(applicationContext, OTPLoginActivity::class.java))
            finish()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->
            if (result.resultCode == Activity.RESULT_OK){
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                updateUI(account)
            }
        }
        else{
            Toast.makeText(applicationContext, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener{
            if (it.isSuccessful){
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
            else{
                Toast.makeText(applicationContext, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}