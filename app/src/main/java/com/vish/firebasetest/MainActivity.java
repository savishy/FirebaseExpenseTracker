package com.vish.firebasetest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * References:
 *
 * Firebase Google Auth: https://www.firebase.com/docs/android/guide/login/google.html
 * Get String resource: http://developer.android.com/guide/topics/resources/string-resource.html
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String ot;
    public static final int RC_GOOGLE_LOGIN = 1;
    private String firebaseUid;
    private Firebase firebase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebase = new Firebase(getString(R.string.firebase_url));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    }

    /**
     * Start Google Auth activity and get token as result.
     * http://developer.android.com/training/basics/intents/result.html
     */
    public void startGoogleAuthActivity() {
        Intent intent = new Intent(this,GoogleAuthActivity.class);
        startActivityForResult(intent, RC_GOOGLE_LOGIN);
    }

    /**
     * We launch Google Authentication activity with #startActivityForResult. When that
     * call returns the result, this method gets called.
     * Here we use the returned auth token to authenticate with firebase using method
     * #authenticateWithFirebase.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "oauth token: " + data.getExtras().getString("oauth_token"));
        } else if (resultCode == RC_GOOGLE_LOGIN) {
            Log.d(TAG, "result code:" + resultCode +
                    (data.hasExtra("oauth_token") ? " token received" : " token not received"));
            ot = data.getExtras().getString("oauth_token");
            authenticateWithFirebase();
        }
    }

    /**
     * Use google auth token to authenticate with Firebase.
     * Then update the user's info in Firebase.
     *
     * Ref:
     * Storing authenticated user data in db:
     * https://www.firebase.com/docs/android/guide/user-auth.html
     */
    private void authenticateWithFirebase() {
        firebase.authWithOAuthToken("google", ot, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG,"auth success! firebase UID:" + authData.getUid());
                firebaseUid = authData.getUid();

                //update users info in firebase
                Map<String, String> map = new HashMap<String, String>();
                map.put("provider", authData.getProvider());
                if(authData.getProviderData().containsKey("displayName")) {
                    map.put("displayName", authData.getProviderData().get("displayName").toString());
                }
                firebase.child("users").child(authData.getUid()).setValue(map);

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.e(TAG,"auth failure");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     *
     * When options menu is selected, launch the appropriate activity.
     *
     * Ref:
     * http://stackoverflow.com/questions/4169714/how-to-call-activity-from-a-menu-item-in-android
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()) {
            case R.id.action_login:
                startGoogleAuthActivity();
                break;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
        return true;
    }
}
