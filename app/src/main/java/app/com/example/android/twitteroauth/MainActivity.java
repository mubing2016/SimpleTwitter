package app.com.example.android.twitteroauth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String TWITTER_CONSUMER_KEY = "FZ5upj4lpNv2EbjkPMF5MirWx";
    private static final String TWITTER_CONSUMER_SECRET = "aq8Q1B7u8iHGDxFCInnfkRpDr2Wg1XwGi7fiQtRoGdpdIfL5eX";
    private static final String PREFERENCE_NAME = "twitter_oauth";
    private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    private static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    private static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";
    private static final String URL_TWITTER_AUTH = "auth_url";
    private static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    private static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

    Button btnLoginTwitter;
    Button btnUpdateStatus;
    Button btnLogoutTwitter;
    Button btnGetTimeline;
    EditText txtUpdate;
    TextView lblUpdate;
    TextView lblUserName;
    ProgressDialog pDialog;

    private static Twitter twitter;
    private static AccessToken accessToken = null;
    private static RequestToken requestToken;
    //private String verifier;
    private static SharedPreferences mSharedPreferences;
    private User user;
    private ConnectionDetector cd;
    AlertDialogManager alert = new AlertDialogManager();
    List<Status> statuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        checkInternet();

        btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
        btnUpdateStatus = (Button) findViewById(R.id.btnUpdateStatus);
        btnLogoutTwitter = (Button) findViewById(R.id.btnLogoutTwitter);
        btnGetTimeline = (Button) findViewById(R.id.btnGetTimeline);
        txtUpdate = (EditText) findViewById(R.id.txtUpdateStatus);
        lblUpdate = (TextView) findViewById(R.id.lblUpdate);
        lblUserName = (TextView) findViewById(R.id.lblUserName);

        mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);


        btnLoginTwitter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Call login twitter function
                loginToTwitter();

            }
        });

        if (!isTwitterLoggedInAlready()) {
            getUri(getIntent());
        }

        btnUpdateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call update status function
                // Get the status from EditText
                String status = txtUpdate.getText().toString();

                // Check for blank text
                if (status.trim().length() > 0) {
                    // update status
                    new updateTwitterStatus().execute(status);
                } else {
                    // EditText is empty
                    Toast.makeText(getApplicationContext(),
                            "Please enter status message", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        /**
         * Button click event for logout from twitter
         * */
        btnLogoutTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Call logout twitter function
                logoutFromTwitter();
            }
        });

        btnGetTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v(LOG_TAG, "STATUS LIST: " + statuses.toString());
                Intent intent = new Intent(MainActivity.this, TimelineActivity.class);
                startActivity(intent);
            }
        });

    }

//    public List<Status> getTimeline() {
//
//        try {
//            //Twitter twitter = new TwitterFactory().getInstance();
//            //User user = twitter.verifyCredentials();
//            statuses = twitter.getHomeTimeline();
//        } catch (TwitterException te) {
//            te.printStackTrace();
//            System.out.println("Failed to get timeline: " + te.getMessage());
//            System.exit(-1);
//        }
//        return statuses;
//        /** This if conditions is tested once is
//         * redirected from twitter page. Parse the uri to get oAuth
//         * Verifier
//         * */
//
////        if (!isTwitterLoggedInAlready()) {
////            Log.v(LOG_TAG, "DID NOT LOG IN");
////            Uri uri = getIntent().getData();
////            if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
////                // oAuth verifier
////                final String verifier = uri
////                        .getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
////                new OAuthAccessTokenTask().execute(verifier);
////            }
////        }
//    }

    private void loginToTwitter() {
        Log.v(LOG_TAG, "start log in");
        // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            twitter = TwitterFactory.getSingleton();
            //twitter.setOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();

            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
                        Log.v(LOG_TAG, requestToken.toString());
                        MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                                .parse(requestToken.getAuthenticationURL())));
                        Log.v(LOG_TAG, "lead to web");

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "log in twitter error.");
                    }
                }
            });
            thread.start();
        } else {
            // user already logged into twitter
            Toast.makeText(getApplicationContext(),
                    "Already Logged into twitter", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "GOT TOKEN");
//            btnLoginTwitter.setVisibility(View.GONE);
//            lblUpdate.setVisibility(View.VISIBLE);
//            txtUpdate.setVisibility(View.VISIBLE);
//            btnUpdateStatus.setVisibility(View.VISIBLE);
//            btnLogoutTwitter.setVisibility(View.VISIBLE);

            // Getting user details from twitter
            // For now i am getting his name only
//            String username = user.getName();
//
//            // Displaying in xml ui
//            lblUserName.setText(Html.fromHtml("<b>Welcome " + username + "</b>"));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getUri(intent);
    }

    private void getUri(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
            // oAuth verifier
            final String verifier = uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
            Log.v(LOG_TAG, "VERIFIER: " + verifier);
            try {
                new OAuthAccessTokenTask().execute(verifier);
//                    SharedPreferences.Editor e = mSharedPreferences.edit();
//
//                    // After getting access token, access token secret
//                    // store them in application preferences
//                    e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
//                    e.putString(PREF_KEY_OAUTH_SECRET,
//                            accessToken.getTokenSecret());
//                    // Store login status - true
//                    e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
//                    e.commit(); // save changes
//
//
//
//                    // Hide login button
//                    Log.d(LOG_TAG, "GOT TOKEN");
//                    btnLoginTwitter.setVisibility(View.GONE);
//                    lblUpdate.setVisibility(View.VISIBLE);
//                    Log.v(LOG_TAG, "SET VISIBLE");
//                    txtUpdate.setVisibility(View.VISIBLE);
//                    btnUpdateStatus.setVisibility(View.VISIBLE);
//                    btnLogoutTwitter.setVisibility(View.VISIBLE);
//
//                    // Getting user details from twitter
//                    // For now i am getting his name only
//                    String username = user.getName();
//
//                    // Displaying in xml ui
//                    lblUserName.setText(Html.fromHtml("<b>Welcome " + username + "</b>"));
            } catch (Exception e) {
                //Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
            }
        }
    }

    private class OAuthAccessTokenTask extends AsyncTask<String, Void, Exception> {

        @Override
        protected Exception doInBackground(String... params) {
            Exception toReturn = null;
            try {
                accessToken = twitter.getOAuthAccessToken(requestToken, params[0]);
                Log.v(LOG_TAG, "ACCESS TOKEN: " + accessToken.toString());
                user = twitter.showUser(accessToken.getUserId());

            } catch (TwitterException e) {
                Log.e(LOG_TAG, "Twitter Error: " + e.getErrorMessage());
                toReturn = e;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error: " + e.getMessage());
                toReturn = e;
            }
            return toReturn;
        }

        //@Override
        protected void onPostExecute(Exception exception) {

            onRequestTokenReceived(exception);


        }
    }

    private void onRequestTokenReceived(Exception result) {
        if (result != null) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
        } else {
            try {
                SharedPreferences.Editor e = mSharedPreferences.edit();

                // After getting access token, access token secret
                // store them in application preferences
                e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                e.putString(PREF_KEY_OAUTH_SECRET,
                        accessToken.getTokenSecret());
                // Store login status - true
                e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
                e.commit(); // save changes


                // Hide login button
                Log.d(LOG_TAG, "GOT TOKEN");
                btnLoginTwitter.setVisibility(View.GONE);

                // Show Update Twitter


                // Getting user details from twitter
                // For now i am getting his name only
                String username = user.getName();

                // Displaying in xml ui
                lblUserName.setText(Html.fromHtml("<b>Welcome " + username + "</b>"));
                lblUpdate.setVisibility(View.VISIBLE);
                txtUpdate.setVisibility(View.VISIBLE);
                btnUpdateStatus.setVisibility(View.VISIBLE);
                btnLogoutTwitter.setVisibility(View.VISIBLE);
                btnGetTimeline.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
            }
        }
    }


//    private void storeAccessToken(long useId, AccessToken accessToken) {
//
//
//    }

//    private void logoutFromTwitter() {
//
//    }


    class updateTwitterStatus extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Updating to twitter...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Places JSON
         */
        protected String doInBackground(String... args) {
            Log.d("Tweet Text", "> " + args[0]);
            String status = args[0];
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

                // Access Token
                String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                // Access Token Secret
                String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
                User user = twitter.verifyCredentials();
                statuses = twitter.getHomeTimeline();

                Log.v(LOG_TAG, "STATUSES: " + statuses.get(0).toString());
                // Update status
                twitter4j.Status response = twitter.updateStatus(status);

                //getting timeline



                Log.d("Status", "> " + response.getText());
            } catch (TwitterException e) {
                // Error in updating status
                Log.d("Twitter Update Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog and show
         * the data in UI Always use runOnUiThread(new Runnable()) to update UI
         * from background thread, otherwise you will get error
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Status tweeted successfully", Toast.LENGTH_SHORT)
                            .show();
                    // Clearing EditText field
                    txtUpdate.setText("");
                }
            });
        }

    }

    /**
     * Function to logout from twitter
     * It will just clear the application shared preferences
     */
    private void logoutFromTwitter() {
        // Clear the shared preferences
        SharedPreferences.Editor e = mSharedPreferences.edit();
        e.remove(PREF_KEY_OAUTH_TOKEN);
        e.remove(PREF_KEY_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.commit();

        // After this take the appropriate action
        // I am showing the hiding/showing buttons again
        // You might not needed this code
        btnLogoutTwitter.setVisibility(View.GONE);
        btnUpdateStatus.setVisibility(View.GONE);
        txtUpdate.setVisibility(View.GONE);
        lblUpdate.setVisibility(View.GONE);
        lblUserName.setText("");
        lblUserName.setVisibility(View.GONE);
        btnGetTimeline.setVisibility(View.GONE);

        btnLoginTwitter.setVisibility(View.VISIBLE);
    }

    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     */
    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }

    public void checkInternet() {
        cd = new ConnectionDetector(getApplicationContext());
        if (!cd.isConnectingToInternet()) {
            alert.showAlertDialog(MainActivity.this, "InternetConnection Error", "Please connect to working internet", false);
            return;
        }
        if (TWITTER_CONSUMER_KEY.trim().length() == 0
                || TWITTER_CONSUMER_SECRET.trim().length() == 0) {
            alert.showAlertDialog(MainActivity.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
            return;
        }
    }


}
