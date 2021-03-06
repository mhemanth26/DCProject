package com.dcproject.nodues;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends Activity {

    EditText email, password ;

    String EmailHolder, PasswordHolder;
    Button Login;//,ForgotPassword;
    TextView ForgotPassword;
    Boolean EditTextEmptyCheck;
    ProgressDialog progressDialog;


    FirebaseAuth firebaseAuth;

    //google sigin
    public static final String TAG = "LoginActivity";
    public static final int RequestSignInCode = 7;
    public GoogleApiClient googleApiClient;
    com.google.android.gms.common.SignInButton googlesignInBtn;
    TextView LoginUserName, LoginUserEmail;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email=(EditText)findViewById(R.id.editText_email);
        password=(EditText)findViewById(R.id.editText_password);

        Login=(Button)findViewById(R.id.button_login);
        ForgotPassword=(TextView) findViewById(R.id.ForgotPasswordBtn);

        googlesignInBtn=(SignInButton) findViewById(R.id.googleSignInBtn);

        progressDialog=new ProgressDialog(LoginActivity.this);

        firebaseAuth=FirebaseAuth.getInstance();

        // Creating and Configuring Google Sign In object.
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Creating and Configuring Google Api Client.
        googleApiClient = new GoogleApiClient.Builder(LoginActivity.this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();


        if(firebaseAuth.getCurrentUser()!=null){
            finish();

            Intent intent =new Intent(LoginActivity.this,StudentActivity.class);
            startActivity(intent);
        }


        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Validate())
                {
                    LoginFunction();
                }
                else {
                    //Toast.makeText(LoginActivity.this, "Please Fill All the Fields", Toast.LENGTH_LONG).show();
                }
            }
        });

        googlesignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmailHolder = email.getText().toString().trim();
                if(TextUtils.isEmpty(EmailHolder)||!android.util.Patterns.EMAIL_ADDRESS.matcher(EmailHolder).matches() ){
                    Toast.makeText(LoginActivity.this, "Enter a valid Email", Toast.LENGTH_LONG).show();
                }
                else {
                    finish();
                    Toast.makeText(LoginActivity.this, "Reset Link sent to your Mail", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    public boolean Validate(){
        EmailHolder = email.getText().toString().trim();
        PasswordHolder = password.getText().toString().trim();

        if(TextUtils.isEmpty(EmailHolder)||!android.util.Patterns.EMAIL_ADDRESS.matcher(EmailHolder).matches() ){
            Toast.makeText(LoginActivity.this, "Enter a valid Email", Toast.LENGTH_LONG).show();
            return false;
        }
        if(TextUtils.isEmpty(PasswordHolder)){
            Toast.makeText(LoginActivity.this, "Enter a valid Password", Toast.LENGTH_LONG).show();
            return false;
        }


        return true ;
    }

    public void LoginFunction(){

        progressDialog.setMessage("Please Wait");
        progressDialog.show();

        // Calling  signInWithEmailAndPassword function with firebase object and passing EmailHolder and PasswordHolder inside it.
        firebaseAuth.signInWithEmailAndPassword(EmailHolder, PasswordHolder)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If task done Successful.
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            // Closing the current Login Activity.
                            finish();
                            // Opening the UserProfileActivity.
                            Intent intent = new Intent(LoginActivity.this, StudentActivity.class);
                            startActivity(intent);
                        }
                        else {
                            // Hiding the progress dialog.
                            progressDialog.dismiss();
                            // Showing toast message when email or password not found in Firebase Online database.
                            Toast.makeText(LoginActivity.this, "Email or Password Not found, Please Try Again", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    public void googleSignIn(){
        Intent AuthIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(AuthIntent, RequestSignInCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestSignInCode){
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (googleSignInResult.isSuccess()){
                GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();
                FirebaseUserAuth(googleSignInAccount);
            }

        }
    }

    private void FirebaseUserAuth(GoogleSignInAccount googleSignInAccount) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        Toast.makeText(LoginActivity.this,""+ authCredential.getProvider(),Toast.LENGTH_LONG).show();

        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task AuthResultTask) {

                        if (AuthResultTask.isSuccessful()){

                            Intent intent = new Intent(LoginActivity.this, StudentActivity.class);
                            startActivity(intent);
                            /*
                            // Getting Current Login user details.
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                            // Showing Log out button.
                            SignOutButton.setVisibility(View.VISIBLE);

                            // Hiding Login in button.
                            signInButton.setVisibility(View.GONE);

                            // Showing the TextView.
                            LoginUserEmail.setVisibility(View.VISIBLE);
                            LoginUserName.setVisibility(View.VISIBLE);

                            // Setting up name into TextView.
                            LoginUserName.setText("NAME =  "+ firebaseUser.getDisplayName().toString());

                            // Setting up Email into TextView.
                            LoginUserEmail.setText("Email =  "+ firebaseUser.getEmail().toString());
                            */
                        }else {
                            Toast.makeText(LoginActivity.this,"Something Went Wrong",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
