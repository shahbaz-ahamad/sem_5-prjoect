package com.example.sem5

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.sem5.databinding.ActivityLoginBinding
import com.example.sem5.datamodel.DataClass
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

@Suppress("DEPRECATION")
class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    lateinit var progressDialog: ProgressDialog
    private lateinit var auth:FirebaseAuth
    private lateinit var email:String
    private lateinit var password:String
    private lateinit var googleSignInClient:GoogleSignInClient
    private val RC_SIGN_IN = 123
    lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        Thread.sleep(3000)
        installSplashScreen()
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //gotoSignUp
        binding.floatingActionButton.setOnClickListener{
            gotoSignUp()
        }

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Signing In")
        progressDialog.setMessage("Please wait....")
        progressDialog.setCancelable(false)

        binding.loginButton.setOnClickListener {
            login()
        }


        //google signin
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //getting instance
        auth=Firebase.auth



        binding.googleButton.setOnClickListener {
            progressDialog.show()
            signIn()
        }

        databaseReference=FirebaseDatabase.getInstance().getReference("Users")
    }



    private fun login() {

        email=binding.email.text.toString()
        password=binding.password.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            progressDialog.show()
            sigIn(email,password)
        }
        else{

            if (email.isEmpty()){
                binding.email.error="Enter Email"

            }
            else{
                binding.password.error="Enter Password"
            }
        }

    }

    private fun sigIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    progressDialog.dismiss()
                    gotoMainActivity()
                }
            }
            .addOnFailureListener{
                Toast.makeText(this@Login,it.message.toString(),Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

    }

    private fun gotoSignUp() {
        val intent=Intent(this@Login,SignUp::class.java)
        startActivity(intent)
    }



    //for google
    // [START onactivityresult]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                progressDialog.dismiss()

            }
        }
    }

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    gotoMainActivity()
                    val dataModel=DataClass(user?.displayName.toString(),user?.email.toString())

                    if (user != null) {
                        databaseReference.child(user.uid).setValue(dataModel)
                    }
                    progressDialog.dismiss()
                } else {
                    // If sign in fails, display a message to the user.

                    Toast.makeText(this@Login,"Failed to Login",Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }
    // [

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser!=null){
            gotoMainActivity()
        }
    }

    private fun gotoMainActivity() {
        val intent=Intent(this@Login,MainActivity::class.java)
        startActivity(intent)
        finish()
    }




}