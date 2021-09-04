package com.khaldiAbadrhmane.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.time.Instant




class SignInActivity : AppCompatActivity(), TextWatcher {


    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        editText_email_sign_in1.addTextChangedListener(this)
        editText_password_sign_in1.addTextChangedListener(this)




btn_sign_inregister.setOnClickListener {

    val intent= Intent(this,SignUpActivity::class.java)
    startActivity(intent)

}

        btn_sign_in1.setOnClickListener {


            val email=editText_email_sign_in1.text.toString().trim()
            val password=editText_password_sign_in1.text.toString().trim()



            signin(email,password)
        }


    }

    private fun signin(email: String, password: String) {
        erurecom_sign_in.visibility = View.GONE
        var bol = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editText_email_sign_in1.error = "please enter a valid email"
            editText_email_sign_in1.requestFocus()
            bol = false
        }


        if (password.length < 6) {
            editText_password_sign_in1.error = "6 char required "
            editText_password_sign_in1.requestFocus()
            bol = false
        }

        if (bol == false) {
            return
        }


        progressBarsign_in.visibility= View.VISIBLE
        mater_sign_in.visibility= View.GONE
       mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
           task ->
           if(task.isSuccessful)
           {


               val intent = Intent(this, MainActivity::class.java)


               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

               progressBarsign_in.visibility= View.GONE
               mater_sign_in.visibility= View.VISIBLE
               startActivity(intent)


           }
           else{

               progressBarsign_in.visibility= View.GONE
               mater_sign_in.visibility= View.VISIBLE
               erurecom_sign_in.visibility = View.VISIBLE
               erurecom_sign_in.text=task.exception?.message
              // Toast.makeText(this, "eruure:"+task.exception?.message,Toast.LENGTH_SHORT).show()

           }

       }

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        btn_sign_in1.isEnabled=  editText_email_sign_in1.text.trim().isNotEmpty() &&  editText_password_sign_in1.text.trim().isNotEmpty()
    }

    override fun afterTextChanged(s: Editable?) {


    }


    override fun onStart() {
        super.onStart()

        if (mAuth.currentUser?.uid !=null){
            val intent = Intent(this, MainActivity::class.java)
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)


        }
    }

}