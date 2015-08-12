package com.casso.mobilehelloworld;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.l7tech.msso.MobileSso;
import com.l7tech.msso.MobileSsoFactory;
import com.l7tech.msso.gui.HttpResponseFragment;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.HttpResponse;


import java.io.IOException;

public class MobileHelloWorld extends AppCompatActivity {

    private MobileSso sso;
    private SimpleTextFragment introFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_hello_world);

        if (sso == null) {
            sso = MobileSsoFactory.getInstance(this);
        }

        // Initialize text fragment that displays intro text.
        introFragment = (SimpleTextFragment)
                getSupportFragmentManager().findFragmentById(R.id.intro_fragment);

        introFragment.setText(R.string.intro_text);
        introFragment.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);


        final Button testButton = (Button) findViewById(R.id.test_button);
        final Button clearButton = (Button) findViewById(R.id.clear_button);

        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendTestRequest();

            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                introFragment.setText(R.string.onclick_clear);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean result = false;

        switch (item.getItemId()) {

            case R.id.logoutDevice:
                doServerLogout();
                result = true;
                break;

            case R.id.deregDevice:
                doDeviceRegistration();
                result = true;
                break;

            case R.id.destroyTokens:
                sso.destroyAllPersistentTokens();
                showMessage("Tokens Destroyed", Toast.LENGTH_SHORT);
                result = true;
                break;
        }

        if (result == true) {

            introFragment.setText(R.string.intro_text);
        }

        return result;
    }

    private void sendTestRequest() {
//        String uri = new String("https://vdev.ca.com:8443/voonair_demo/helloworld");

        String uri = new String("https://vdev.ca.com:8443/voonair_demo/helloworld");
        HttpGet httpGet = new HttpGet(uri);

        HttpResponseFragment responseFragment = new HttpResponseFragment() {
            protected void onResponse(long requestId, int resultCode,
                                      String errorMessage, HttpResponse httpResponse) {

                if (errorMessage == null) {

                    try {

                        String responseString =
                                new BasicResponseHandler().handleResponse(httpResponse);

                        introFragment.getTextView().setText(responseString);

                    } catch (IOException ioe) {
                        introFragment.getTextView().setText(ioe.getMessage());
                    }
                } else {
                    introFragment.getTextView().setText(errorMessage);
                }
            }
        };

        sso.processRequest(httpGet, responseFragment.getResultReceiver());
    }

    // Network operations performed asynchronously
    private void doServerLogout() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    sso.logout(true);
                    showMessage("Logged Out Successfully", Toast.LENGTH_SHORT);
                } catch (Exception e) {
                    String msg = "Server Logout Failed: " + e.getMessage();
                    showMessage(msg, Toast.LENGTH_LONG);
                }
                return null;
            }
        }.execute((Void) null);
    }

    // Network operations performed asynchronously
    private void doDeviceRegistration() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    sso.removeDeviceRegistration();
                    showMessage("Device Unregistered Successfully", Toast.LENGTH_SHORT);
                } catch (Exception e) {
                    String msg = "Unregistering Device Failed: " + e.getMessage();
                    showMessage(msg, Toast.LENGTH_LONG);
                }
                return null;
            }
        }.execute((Void) null);
    }

    public void showMessage(final String message, final int toastLength) {
        if (message.toLowerCase().contains("jwt")) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("JWT Error");
            alertDialog.setMessage(message);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Add your code for the button here.
                }
            });
            alertDialog.show();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MobileHelloWorld.this, message, toastLength).show();
                }
            });
        }
    }
}
