package io.cogswell.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import io.cogswell.sdk.GambitSDKService;
import io.cogswell.sdk.message.GambitRequestMessage;
import io.cogswell.sdk.message.GambitResponseMessage;
import io.cogswell.sdk.request.GambitRequestEvent;
import io.cogswell.sdk.response.GambitResponseEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.cogswell.example.table.GambitAttribute;

public class EventActivity extends AppCompatActivity  {
    private String accessKey;
    private String namespaceName;
    private String eventName;
    private String attributesJSONAsString;
    private String platform;
    private String enviornment;
    private String platform_app_id;
    private int campaign_id = 1;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String debug_directive = "";
    private String message = "";
    private EditText editTextAccessKey;
    private EditText editTextClientSalt;
    private EditText editTextClientSecret;
    private EditText editTextCampaignID;
    private EditText editTextNamespace;
    private EditText editTextEventName;
    private EditText editTextAttributes;
    private TextView textViewMessageDescription;
    private String clientSalt = null;
    private String clientSecret = null;
    private Switch buttonDebugDirective;

    private String randomUUID = null;
    private String randomUUIDBody = null;
    private ArrayList<GambitAttribute> namespaceAttributs = null;
    private String namespaceBody = null;
    private String eventBody = null;
    private String receivedMessage = null;
    private boolean pushServiceStarted = false;


    private String UDID;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    protected final ExecutorService executor = Executors.newCachedThreadPool();


    private class message extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                // This will throw an exception if the json is invalid.
                JSONObject attributes = new JSONObject(attributesJSONAsString);

                GambitRequestMessage.Builder builder = new GambitRequestMessage.Builder(
                        accessKey, clientSalt, clientSecret
                ).setUDID(receivedMessage)
                        .setAttributes(attributes)
                        .setNamespace(namespaceName);
                Log.d("accessKey", accessKey);
                Log.d("clientSalt", clientSalt);
                Log.d("clientSecret", clientSecret);
                Log.d("attributesJSONAsString", attributesJSONAsString.toString());
                Future<io.cogswell.sdk.GambitResponse> future = null;
                try {
                    future = executor.submit(builder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.alert(activity, "Error building message request", null, e, null);
                }

                Log.d("future", String.valueOf(future));
                GambitResponseMessage response;
                try {
                    response = (GambitResponseMessage) future.get();
                    Log.d("response message", String.valueOf(response.getRawBody()));
                    final String responseMessage = response.getRawBody();
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (!responseMessage.equals("") && responseMessage != null && !responseMessage.isEmpty()) {
                                String prettyResponseMessage = responseMessage.replaceAll("\\\\\"", "\"");
                                try {
                                    // Attempt to pretty-print the JSON.
                                    JSONObject responseJSON = new JSONObject(responseMessage);
                                    String message = responseJSON.getString("message");
                                    JSONObject responseMessageJSON = new JSONObject(message);
                                    prettyResponseMessage = Utils.unescapeJavaString(responseMessageJSON.toString(2));
                                } catch (JSONException je) {
                                    Log.w("response message", "Invalid JSON: "+responseMessage, je);
                                    // If we can't parse the json, default to just showing the response text.
                                }
                                Utils.alert(activity, "Message Response", prettyResponseMessage, null);
                            }
                        }
                    });

                    Log.d("response message", String.valueOf(response.getRawBody()));

                } catch (Exception ex) {
                    Log.d("extest", ex.getLocalizedMessage());
                    ex.printStackTrace();
                    Utils.alert(activity, "Error getting message details", null, ex, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                final String messageFinal;
                if (e instanceof JSONException) {
                    messageFinal = "The JSON syntax for Attributes as JSON is invalid.";
                } else {
                    messageFinal = "Please confirm your keys, ids, and namespace are correct.";
                }

                Utils.alert(activity, "Invalid data used to query for message details", messageFinal,null);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            //Log.d("executed", "executed");
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class event extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                // This will throw an exception if the json is invalid.
                JSONObject attributes = new JSONObject(attributesJSONAsString);

                GambitRequestEvent.Builder builder = new GambitRequestEvent.Builder(accessKey, clientSalt, clientSecret);
                builder.setEventName(eventName);
                builder.setNamespace(namespaceName);
                builder.setAttributes(attributes);
                builder.setCampaignId(campaign_id);
                builder.setForwardAsMessage(true);

                String timestamp = null;

                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
                df.setTimeZone(tz);
                timestamp = df.format(new Date());


                builder.setTimestamp(timestamp);

                builder.setForwardAsMessage(true);

                builder.setDebugDirective(debug_directive);


                Future<io.cogswell.sdk.GambitResponse> future = null;
                try {
                    future = GambitSDKService.getInstance().sendGambitEvent(builder);
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.alert(activity, "Error sending event", null, e, null);
                }

                GambitResponseEvent response;
                try {
                    response = (GambitResponseEvent) future.get();

                    eventBody = response.getRawBody();

                    //Log.d("eventBody", eventBody);
                    message = response.getMessage();
                    //Log.d("response", response.getMessage());

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Utils.alert(activity, "Error getting event response", null, ex, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                final String messageFinal;
                if (e instanceof JSONException) {
                    messageFinal = "The JSON syntax for Attributes as JSON is invalid.";
                } else {
                    messageFinal = "Please confirm your keys, ids, and namespace are correct.";
                }
                Utils.alert(activity, "Invalid event data", messageFinal, null);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            textViewMessageDescription.setText(message);
            //Log.d("executed", "executed");
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private Activity activity;

    public boolean validateFields() {

        accessKey = editTextAccessKey.getText().toString();
        clientSalt = editTextClientSalt.getText().toString();
        clientSecret = editTextClientSecret.getText().toString();
        namespaceName = editTextNamespace.getText().toString();
        if (isInteger(editTextCampaignID.getText().toString())) {
            campaign_id = Integer.parseInt(editTextCampaignID.getText().toString());
        }
        namespaceName = editTextNamespace.getText().toString();
        eventName = editTextEventName.getText().toString();

        String message = null;
        if (accessKey == null || accessKey.equals("") || accessKey.isEmpty()) {
            message = "Access Key is Missing!";
        }
        if (clientSalt == null || clientSalt.equals("") || clientSalt.isEmpty()) {
            message = "Client Salt is Missing!";
        }
        if (clientSecret == null || clientSecret.equals("") || clientSecret.isEmpty()) {
            message = "Client Secret is Missing!";
        }
        if (namespaceName == null || accessKey.equals("") || namespaceName.isEmpty()) {
            message = "Namespace is Missing!";
        }
        if (eventName == null || eventName.equals("") || eventName.isEmpty()) {
            message = "Event Name is Missing!";
        }

        try {
            // Change the field to use the converted JSON so the user clearly sees what the server is seeing.
            attributesJSONAsString = editTextAttributes.getText().toString();
            attributesJSONAsString = new JSONObject(attributesJSONAsString).toString();
            editTextAttributes.setText(attributesJSONAsString);
        } catch (Exception e) {
            e.printStackTrace();
            message = "Attributes as JSON is not valid JSON!";
        }

        if (message != null) {
            Utils.alert(activity, "Missing event data:", message, null);
            return false;
        }

        return true;
    }
    public void saveFields() {
        accessKey = editTextAccessKey.getText().toString();
        clientSalt = editTextClientSalt.getText().toString();
        clientSecret = editTextClientSecret.getText().toString();
        attributesJSONAsString = editTextAttributes.getText().toString();
        eventName = editTextEventName.getText().toString();
        namespaceName = editTextNamespace.getText().toString();
        if (isInteger(editTextCampaignID.getText().toString())) {
            campaign_id = Integer.parseInt(editTextCampaignID.getText().toString());
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPreferences.edit().putString("accessKey", accessKey).apply();
        sharedPreferences.edit().putString("clientSalt", clientSalt).apply();
        sharedPreferences.edit().putString("clientSecret", clientSecret).apply();
        sharedPreferences.edit().putString("attributes", attributesJSONAsString).apply();
        sharedPreferences.edit().putString("eventName", eventName).apply();
        sharedPreferences.edit().putString("namespaceName", namespaceName).apply();
        sharedPreferences.edit().putInt("campaign_id", campaign_id).apply();
    }
    public void fillData() {
        accessKey = editTextAccessKey.getText().toString();
        clientSalt = editTextClientSalt.getText().toString();
        clientSecret = editTextClientSecret.getText().toString();
        attributesJSONAsString = editTextAttributes.getText().toString();
        namespaceName = editTextNamespace.getText().toString();
        if (isInteger(editTextCampaignID.getText().toString())) {
            campaign_id = Integer.parseInt(editTextCampaignID.getText().toString());
        }
        namespaceName = editTextNamespace.getText().toString();
        eventName = editTextEventName.getText().toString();

        if(validateFields() == false) {
            return;
        }
        saveFields();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        activity = this;
        editTextAccessKey = (EditText) findViewById(R.id.editTextAccessKey);
        editTextClientSalt = (EditText) findViewById(R.id.editTextClientSalt);
        editTextClientSecret = (EditText) findViewById(R.id.editTextClientSecret);
        editTextCampaignID = (EditText) findViewById(R.id.editTextCampaignID);
        editTextNamespace = (EditText) findViewById(R.id.editTextNamespace);
        editTextEventName = (EditText) findViewById(R.id.editTextEventName);
        editTextAttributes = (EditText) findViewById(R.id.editTextAttributes);
        textViewMessageDescription = (TextView) findViewById(R.id.textViewMessageDescription);
        buttonDebugDirective = (Switch) findViewById(R.id.buttonDebugDirective);
        RelativeLayout toolbar_start = (RelativeLayout) findViewById(R.id.toolbar_start);
        //new CleintSecretCall().execute("");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        if (sharedPreferences.getString("accessKey", null) != null) {
            editTextAccessKey.setText(sharedPreferences.getString("accessKey", null));
        }
        if (sharedPreferences.getString("clientSalt", null) != null) {
            editTextClientSalt.setText(sharedPreferences.getString("clientSalt", null));
        }
        if (sharedPreferences.getString("clientSecret", null) != null) {
            editTextClientSecret.setText(sharedPreferences.getString("clientSecret", null));
        }
        if (sharedPreferences.getInt("campaign_id", 0) != 0) {
            editTextCampaignID.setText(String.valueOf(sharedPreferences.getInt("campaign_id", 0)));
        }
        if (sharedPreferences.getString("namespaceName", null) != null) {
            editTextNamespace.setText(sharedPreferences.getString("namespaceName", null));
        }
        if (sharedPreferences.getString("eventName", null) != null) {
            editTextEventName.setText(sharedPreferences.getString("eventName", null));
        }
        if (sharedPreferences.getString("attributes", null) != null) {
            editTextAttributes.setText(sharedPreferences.getString("attributes", null));
        }
        if (sharedPreferences.getString("debug_directive", null) != null && !sharedPreferences.getString("debug_directive", null).isEmpty()) {
            buttonDebugDirective.setChecked(true);
            debug_directive = "echo-as-message";
        }
        Intent intent = getIntent();
        String message_received = intent.getStringExtra("message_received");
        String message_received_id = intent.getStringExtra("message_received_id");

        if (message_received != null) {

            accessKey = editTextAccessKey.getText().toString();
            clientSalt = editTextClientSalt.getText().toString();
            clientSecret = editTextClientSecret.getText().toString();
            attributesJSONAsString = editTextAttributes.getText().toString();
            namespaceName = editTextNamespace.getText().toString();

            new AlertDialog.Builder(activity)
                    .setTitle("Push Payload")
                    .setMessage(message_received)
                    .setPositiveButton("View Message", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new message().execute("");
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            receivedMessage = message_received_id;
            Log.d("receivedMessage", receivedMessage);

        }

       /* editTextAccessKey.setText(accessKey);
        editTextClientSalt.setText(clientSalt);
        editTextClientSecret.setText(clientSecret);
        editTextCampaignID.setText(String.valueOf(campaign_id));
        editTextNamespace.setText(namespaceName);
        editTextEventName.setText(eventName);*/

        RelativeLayout toolbar_execute = (RelativeLayout) findViewById(R.id.toolbar_execute);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.debug_strings, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buttonDebugDirective.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    debug_directive = "echo_as_message";
                } else {
                    debug_directive = "";
                }
            }
        });



        toolbar_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillData();

                Intent mainIntent = new Intent(EventActivity.this, StartActivity.class);
                EventActivity.this.startActivity(mainIntent);
                EventActivity.this.finish();
            }
        });
        toolbar_execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if(validateFields() == false) {
                return;
            }

            saveFields();

            new event().execute("");
            }
        });


    }
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    @Override
    protected void onPause() {
        fillData();
        super.onPause();
    }
}
