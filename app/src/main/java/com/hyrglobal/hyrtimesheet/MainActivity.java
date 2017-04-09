package com.hyrglobal.hyrtimesheet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.Tag;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class MainActivity extends AppCompatActivity{


    private FirebaseAuth mFirebaseAuth;
    private SharedPreferences mSharedPreferences;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;
    private GoogleApiClient mGoogleApiClient;

    private IntentFilter intentFilter;
    //private BroadcastReceiver broadcastReceiver;

    private CardView mOldTimeEntryButton, mNewTimeEntryButton, mProfileButton, mSettingsButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mNewTimeEntryButton = (CardView) findViewById(R.id.newTimeEntryButton);

        mNewTimeEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,NewTimeEntry.class));
            }
        });

        mOldTimeEntryButton = (CardView) findViewById(R.id.oldTimeEntryButton);

        mOldTimeEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ViewTimeEntries.class));
            }
        });

        //Broadcast Receiver for Logout signalling to all activityes
        intentFilter = new IntentFilter();
        intentFilter.addAction("CLOSE_ALL");
    //    broadcastReceiver = new BroadcastReceiver() {
      //      @Override
       //     public void onReceive(Context context, Intent intent) {

//            }
 //       };
   //     registerReceiver(broadcastReceiver, intentFilter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;

    }

    @Override
    protected void onStop() {
        super.onStop();
//        if(broadcastReceiver)
  //      {
    //        unregisterReceiver(broadcastReceiver);
      //  }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_logout:
                signOut();
                break;
        }
        return true;
    }



    private void signOut()
    {
        if(mFirebaseAuth.getCurrentUser() != null) {
            mSharedPreferences.edit().remove("hyrUserName");
            mSharedPreferences.edit().remove("hyrUserID");
            mSharedPreferences.edit().commit();

            mFirebaseAuth.signOut();
            HomeScreenActivity.googleSignOut();

            finish();
            startActivity( new Intent(MainActivity.this,HomeScreenActivity.class));
        }
    }
}
