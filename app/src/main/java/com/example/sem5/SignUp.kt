package com.example.sem5

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.sem5.databinding.ActivitySignUpBinding
import com.example.sem5.datamodel.DataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding:ActivitySignUpBinding
    lateinit var name:String
    lateinit var email:String
    lateinit var password:String
    lateinit var rePassword:String
    lateinit var progressDialog: ProgressDialog
    lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        binding.signUpButton.setOnClickListener {

            creatUser()
        }
        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Signing Up")
        progressDialog.setMessage("Please wait....")
        progressDialog.setCancelable(false)

        databaseReference=FirebaseDatabase.getInstance().getReference("Users")
    }

    private fun creatUser() {

        name=binding.name.text.toString()
        password=binding.password.text.toString()
        rePassword=binding.reTypePassword.text.toString()
        email=binding.email.text.toString()


        if(name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && rePassword.isNotEmpty()){

            if(password==rePassword){

                progressDialog.show()
                signUp(email,password)
            }else{
                Toast.makeText(this@SignUp,"Password Doesn't match",Toast.LENGTH_SHORT).show()

            }
        }
        else{
            if(name.isEmpty()){
                binding.name.error="Enter name"
            }else  if(email.isEmpty()){
                binding.email.error="Enter Email"
            }else  if(password.isEmpty()){
                binding.password.error="Enter password"
            }
            else{
                binding.reTypePassword.error="Retype password"
            }
        }
    }

    private fun signUp(email: String, password: String) {

        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{

                if(it.isSuccessful){
                    Toast.makeText(this@SignUp,"SignUp Sucessfull",Toast.LENGTH_SHORT).show()
                    gotoSignInActivity()
                    progressDialog.dismiss()
                    val user=FirebaseAuth.getInstance().currentUser
                    val userId= user?.uid
                    val email=user?.email
                    val dataModel=DataClass(binding.name.text.toString(),email!!)
                    databaseReference.child(userId!!).setValue(dataModel)

                }else{
                    progressDialog.dismiss()
                    Toast.makeText(this@SignUp, it.exception.toString(),Toast.LENGTH_SHORT).show()

                }
            }
    }

    private fun gotoSignInActivity() {
        val intent=Intent(this@SignUp,Login::class.java)
        startActivity(intent)
        finish()
    }

}