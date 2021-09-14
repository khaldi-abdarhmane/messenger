package com.khaldiAbadrhmane.messenger

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

import com.khaldiAbadrhmane.messenger.model.User
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity(),TextWatcher {


// ...
// Initialize Firebase Auth


    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentUserDocRef: DocumentReference
    get() = firestoreInstance.document("users/user:${mAuth.currentUser?.uid.toString() }")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        FirebaseApp.initializeApp(this)





        editText_name_sign_up.addTextChangedListener(this)
        editText_email_sign_up.addTextChangedListener(this)
        editText_password_sign_up.addTextChangedListener(this)

        btn_sign_up.setOnClickListener {

            val name =editText_name_sign_up.text.toString().trim()
            val email =editText_email_sign_up.text.toString().trim()
            val password =editText_password_sign_up.text.toString().trim()



            creatNewAccount(name,email,password)






            }

        }




    private fun creatNewAccount(name:String,email: String, password: String) {
        erurecom_sign_up.visibility = View.GONE
        var bol = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editText_email_sign_up.error = "please enter a valid email"
            editText_email_sign_up.requestFocus()
            bol = false
        }


        if (password.length < 6) {
            editText_password_sign_up.error = "6 char required "
            editText_password_sign_up.requestFocus()
            bol = false
        }

        if (bol == false) {
            return
        }

        progressBarsign_up.visibility= View.VISIBLE
        mater_sign_up.visibility= View.GONE

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            task ->
            val newUser= User(name)
            currentUserDocRef.set(newUser)
            if (task.isSuccessful) {

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                progressBarsign_up.visibility= View.GONE
                mater_sign_up.visibility= View.VISIBLE
                startActivity(intent)



            } else {
                mater_sign_up.visibility= View.VISIBLE
                progressBarsign_up.visibility= View.GONE

                erurecom_sign_up.visibility = View.VISIBLE
                erurecom_sign_up.text=task.exception?.message

              //  Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()

            }


        }
    }


    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        btn_sign_up.isEnabled= editText_name_sign_up.text.trim().isNotEmpty()
                && editText_email_sign_up.text.trim().isNotEmpty()
                && editText_password_sign_up.text.trim().isNotEmpty()

    }

    override fun afterTextChanged(s: Editable?) {



    }
}