package com.hyrglobal.hyrtimesheet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Created by nisha on 3/21/2017.
 */

public class HomeScreenActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private static GoogleApiClient mGoogleApiClient;
    private SignInButton mGoogleSignInButton;
    private static final int RC_SIGN_IN = 9001;
    private Button mEmailLoginButton,mEmailRegisterButton,mResetPasswordButton;
    private EditText mEmailText, mPasswordText;
    private SharedPreferences mSharedPreferences;


    private static final String TAG = "HOME_ACTIVITY";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);
                // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleApiClient.connect();

        mGoogleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mEmailLoginButton = (Button) findViewById(R.id.btn_login);
        mEmailRegisterButton = (Button) findViewById(R.id.btn_signup);
        mEmailText = (EditText) findViewById(R.id.email);
        mPasswordText = (EditText) findViewById(R.id.password);
        mResetPasswordButton = (Button) findViewById(R.id.btn_reset_password);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null)
                {
                    mSharedPreferences.edit().putString("hyrUserName",user.getEmail().toString());
                    mSharedPreferences.edit().putString("hyrUserID",user.getUid().toString());
                    mSharedPreferences.edit().commit();
                    startActivity(new Intent(HomeScreenActivity.this,MainActivity.class));
                }
                else
                {
                    Log.d(TAG,"User signed out");
                }
            }
        };

        findViewById(R.id.sign_in_button).setOnClickListener(HomeScreenActivity.this);
        findViewById(R.id.btn_login).setOnClickListener(HomeScreenActivity.this);
        findViewById(R.id.btn_signup).setOnClickListener(HomeScreenActivity.this);
        findViewById(R.id.btn_reset_password).setOnClickListener(HomeScreenActivity.this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.sign_in_button:
                signInWithGoogle();
                break;
            case R.id.btn_login:
                signInWithEmail();
                break;
            case R.id.btn_signup:
                createAccount();
                break;
            case R.id.btn_reset_password:
                resetPassword();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(HomeScreenActivity.this,"Google Sign In Failed",Toast.LENGTH_SHORT).show();
    }

    protected void signInWithEmail()
    {
        if (mPasswordText.getText().toString().isEmpty() || mEmailText.getText().toString().isEmpty()) {
            if(mPasswordText.getText().toString().isEmpty())
            {
                mPasswordText.setError("Password cannot be empty");
            }
            if(mEmailText.getText().toString().isEmpty())
            {
                mEmailText.setError("Email cannot be empty");
            }
        }
        else
        {
            signInWithEmailAndPassword(mEmailText.getText().toString(),mPasswordText.getText().toString());
        }
    }

    protected void resetPassword()
    {
        startActivity(new Intent(HomeScreenActivity.this,ResetPasswordActivity.class));
    }

    protected void signInWithGoogle()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("HYRTimeSheet", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(this,"Sign in successful",Toast.LENGTH_SHORT).show();
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(this,"Sign in Failed",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    protected void createAccount() //Create new user with email and password
    {
        if (mPasswordText.getText().toString().isEmpty() || mEmailText.getText().toString().isEmpty()) {
            if(mPasswordText.getText().toString().isEmpty())
            {
                mPasswordText.setError("Password cannot be empty");
            }
            if(mEmailText.getText().toString().isEmpty())
            {
                mEmailText.setError("Email cannot be empty");
            }
        } else {
            mFirebaseAuth.createUserWithEmailAndPassword(mEmailText.getText().toString(), mPasswordText.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "User Has been created");
                    if (!task.isSuccessful()) {
                        Toast.makeText(HomeScreenActivity.this, "User creation has been failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    protected void signInWithEmailAndPassword(String mEmail, String mPassword)
    {
        mFirebaseAuth.signInWithEmailAndPassword(mEmail,mPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(! task.isSuccessful())
                {
                    Toast.makeText(HomeScreenActivity.this,"Sign in Failed!",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(HomeScreenActivity.this,"Sign in Successful!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void getCurrentUser()
    {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if(user != null)
        {
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(HomeScreenActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    @Override
    protected void onStop() {
        if(mFirebaseAuthStateListener!=null)
        {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthStateListener);
            //signOut();
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);
        super.onStart();
    }

    public static void googleSignOut()
    {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.v(TAG,"Google user signed out");
                    }
                });
    }
}
