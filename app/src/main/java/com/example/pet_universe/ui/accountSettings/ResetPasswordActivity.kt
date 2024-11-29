package com.example.pet_universe.ui.accountSettings
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pet_universe.MainActivity
import com.example.pet_universe.R
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var  setPassword:EditText
    private lateinit var  btnResetPassword:Button
    private lateinit var auth:FirebaseAuth
    private lateinit var btnCancel:Button

    private fun validateInputs(
        email: String,

        ): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            Toast.makeText(this, "Email address is not filled in.", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_forget_password)

        setPassword=findViewById(R.id.emailForgetPassword)
        btnResetPassword = findViewById(R.id.sendLink)
        btnCancel = findViewById(R.id.cancelButton)

        auth = FirebaseAuth.getInstance()

        btnResetPassword.setOnClickListener {
            val sPassword = setPassword.text.toString()
            if (validateInputs(sPassword)) {
                auth.sendPasswordResetEmail(sPassword)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Please check your email.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                    }
            }
        }
        btnCancel.setOnClickListener {
            finish()
        }
    }
}