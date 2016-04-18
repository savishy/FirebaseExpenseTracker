package com.vish.firebasetest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private List<Category> categories;
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
            syncExpenseCategories();
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
                Log.d(TAG, "auth success! firebase UID:" + authData.getUid());
                firebaseUid = authData.getUid();

                //update users info in firebase
                Map<String, String> map = new HashMap<String, String>();
                map.put("provider", authData.getProvider());
                if (authData.getProviderData().containsKey("displayName")) {
                    map.put("displayName", authData.getProviderData().get("displayName").toString());
                    map.put("lastSeen", getCurrentTimeStamp());
                }
                firebase.child("users").child(authData.getUid()).setValue(map);

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.e(TAG, "auth failure");
            }
        });
    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    /**
     * sync all expense categories from db. If no expense categories found,
     * add them.
     */
    private void syncExpenseCategories() {
        Firebase categoryRef = new Firebase(
                getString(R.string.firebase_url) + "/" +
                getString(R.string.firebase_categories));
        categoryRef.addValueEventListener((new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                categories.clear();
                Log.d(TAG, "There are " + snapshot.getChildrenCount() + " categories");
                if (snapshot.getChildrenCount() == 0) addExpenseCategories();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    Category c = categorySnapshot.getValue(Category.class);
                    Log.d(TAG, c.getId() + ":" + c.getName() + ", " + c.getExpenseCategory());
                    categories.add(c);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "The read failed: " + firebaseError.getMessage());
            }
        }));
    }

    /**
     * Initialize the database by adding expense categories.
     * Categories are read from keys.xml.
     *
     * Ref:
     * http://stackoverflow.com/questions/7256514/search-value-for-key-in-string-array-android
     * https://commons.apache.org/proper/commons-lang/javadocs/api-2.6/org/apache/commons/lang/RandomStringUtils.html
     */
    private void addExpenseCategories() {
        String[] expenseCategories = getResources().getStringArray(R.array.expense_categories);
        for (String name : expenseCategories) {
            Log.d(TAG,"add category " + name);
            Category c = new Category(name,true);
            firebase.child(getString(R.string.firebase_categories) + "/" + c.getId()).setValue(c);
        }

        String[] incomeCategories = getResources().getStringArray(R.array.income_categories);
        for (String name : incomeCategories) {
            Log.d(TAG,"add category " + name);
            Category c = new Category(name,true);
            firebase.child(getString(R.string.firebase_categories) + "/" + c.getId()).setValue(c);
        }

    }

    private void addAnExpense() {
        if (categories == null || categories.size() == 0) {
            Log.e(TAG,"categories has not been initialized yet!");
            return;
        }
        Expense e = new Expense(getCurrentTimeStamp(),categories.get(0),firebaseUid, 100f);
        firebase.child(getString(R.string.firebase_expenses)).setValue(e);
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
