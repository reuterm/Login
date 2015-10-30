package vs.in.de.uni_ulm.mreuter.login.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;

import vs.in.de.uni_ulm.mreuter.login.R;
import vs.in.de.uni_ulm.mreuter.login.data.MyKeyPair;
import vs.in.de.uni_ulm.mreuter.login.data.NoticeDialogListener;
import vs.in.de.uni_ulm.mreuter.login.db.KeysDBAdapter;
import vs.in.de.uni_ulm.mreuter.login.data.ActionSpecification;


public class MainActivity extends Activity implements NoticeDialogListener{

    private KeysDBAdapter dbCon;
    private SimpleCursorAdapter dataAdapter;
    private ListView listView;
    private NfcAdapter mNfcAdapter;
    private final static int ACTIVITY_EDIT=0;
    private final static int ACTIVITY_CREATE=1;
    private final static int ACTIVITY_UPDATE=2;
    private final static int ACTIVITY_SEND=3;
    protected final static String PASS = "vs.in.de.uni_ulm.mreuter.login.data.MyKeyPair";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add Spongy Castle as new Security Provider ensure all algorithms are supported
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);

        dbCon = new KeysDBAdapter(this);

        // Check for available NFC Adapter.
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter == null) {
            Toast nfcError = Toast.makeText(this, "NFC is not available", Toast.LENGTH_SHORT);
            nfcError.setGravity(Gravity.CENTER, 0, 0);
            nfcError.show();
            finish();
            return;
        }

        // Disable Android Beam UI
        mNfcAdapter.setNdefPushMessage(null, this);

        // For testing purposes.
        dbCon.open();
        dbCon.deleteAllKeyPairs();
        insertDummyData();
        dbCon.close();

        // Display all keys saved in the database
        fillContent();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_change_pw:
                DialogFragment pwDialog = new SetPasswordDialog();
                pwDialog.show(getFragmentManager(), "SetPasswordDialog");
                return super.onOptionsItemSelected(item);
            case R.id.action_incoming_new:
                //TODO Get DeviceID
                String hash ="someData";
                // Call newInstance method to pass some data
                DialogFragment keyDialog = NewKeyDialog.newInstance(hash);
                keyDialog.show(getFragmentManager(), "NewKeyDialog");
                return super.onOptionsItemSelected(item);
            case R.id.action_incoming_request:
                //TODO Get DeviceID and username + website
                String user = "username";
                String site = "www.example.com";
                byte[] digest1;
                MessageDigest md1 = null;
                try {
                    md1 = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                md1.update("username@www.google.com".getBytes());
                digest1 = md1.digest();
                hash = Base64.encodeToString(digest1, Base64.DEFAULT).trim();

                // Call newInstance method to pass some data
                DialogFragment challengeDialog = IncomingChallengeDialog.newInstance(user, site, hash);
                challengeDialog.show(getFragmentManager(), "IncomingChallengeDialog");
                return super.onOptionsItemSelected(item);
            case R.id.action_incoming_update:
                //TODO Get some hash like old:new
                byte[] digest;
                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                md.update("username@www.google.com".getBytes());
                digest = md.digest();
                hash = Base64.encodeToString(digest, Base64.DEFAULT).trim();
                DialogFragment updateDialog = UpdateKeyDialog.newInstance(hash);
                updateDialog.show(getFragmentManager(), "UpdateKeyDialog");
                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fillContent() {

        dbCon.open();
        // Load all data available.
        Cursor cursor = dbCon.fetchAllKeys();

        // Desired columns to be bound.
        String [] from = new String[] {
                KeysDBAdapter.KEY_USER,
                KeysDBAdapter.KEY_SITE
        };

        // View elements to be bound to the data obtained.
        int [] to = new int[] {
                R.id.username_info,
                R.id.website_info
        };

        // Create new cursor with all the information specified before to be displayed in the
        // key_info layout.
        dataAdapter = new SimpleCursorAdapter(this, R.layout.key_info, cursor, from, to, 0);

        listView = (ListView) findViewById(R.id.list);
        // Assign view to model.
        listView.setAdapter(dataAdapter);

        // Handle touch events on list elements in order to display more specific data.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                String jointHash = cursor.getString(0);

                MyKeyPair keyPair = MainActivity.this.getKeyPair(jointHash);
                cursor.close();
                if (keyPair != null) {
                    Intent intent = new Intent(MainActivity.this, KeyEditActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(PASS, keyPair);
                    intent.putExtras(bundle);
                    MainActivity.this.startActivityForResult(intent, ACTIVITY_EDIT);
                }

            }
        });
//        cursor.close();
//        dbCon.close();
    }

    // Obtain specific key pair
    private MyKeyPair getKeyPair(String jointHash) {
        dbCon.open();
        Cursor cursor = dbCon.fetchKeyPair(jointHash);
        if(cursor.moveToFirst()) {
            String user = cursor.getString(1);
            String site = cursor.getString(2);
            byte[] pubKeyData = cursor.getBlob(3);
            byte[] privKeyData = cursor.getBlob(4);
            String created = cursor.getString(5);

            cursor.close();
            dbCon.close();

            return new MyKeyPair(jointHash, user, site, pubKeyData, privKeyData, created);
        } else {
            Toast error = Toast.makeText(getApplicationContext(),
                    "No matching entry found",
                    Toast.LENGTH_SHORT);
            error.setGravity(Gravity.CENTER, 0, 0);
            error.show();

            cursor.close();
            dbCon.close();

            return null;
        }
    }

//    @Override
//    public void onNewIntent(Intent i) {
//        // Check to see that the Activity started due to an Android Beam
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
//            processIntent(getIntent());
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ACTIVITY_CREATE:
                fillContent();
                break;
            case ACTIVITY_EDIT:
                fillContent();
                break;
            case ACTIVITY_UPDATE:
                fillContent();
                break;
            case ACTIVITY_SEND:

        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String message, int action) {

        switch (action) {
            // Event is from the set password dialog.
            case ActionSpecification.ACTION_SET_PASSWORD:
                // Message functions as password
                if(message!=null) {

                    //TODO change encryption
                    Toast success = Toast.makeText(getApplicationContext(),
                            "Password successfully changed",
                            Toast.LENGTH_SHORT);
                    success.setGravity(Gravity.CENTER, 0, 0);
                    success.show();
                } else {
                    Toast error = Toast.makeText(getApplicationContext(),
                            "Entered passwords are different. Password not changed",
                            Toast.LENGTH_SHORT);
                    error.setGravity(Gravity.CENTER, 0, 0);
                    error.show();
                }
                break;
            // Event is from the simulate new key dialog.
            case ActionSpecification.ACTION_NEW_KEY:
                Intent newKeyIntent = new Intent(MainActivity.this, NewKeyActivity.class);
                // Provide joint hash for new key activity.
                newKeyIntent.putExtra(PASS, message);
                MainActivity.this.startActivityForResult(newKeyIntent, ACTIVITY_CREATE);
                break;
            // Event is from the simulate incoming challenge dialog.
            case ActionSpecification.ACTION_INCOMING_CHALLENGE:
                //TODO sign create valid response
                Intent sendingIntent = new Intent(MainActivity.this, SendingActivity.class);
                // Provide joint hash for sending activity.
                sendingIntent.putExtra("action", ActionSpecification.NDEF_PROCESS_CHALLENGE);
                sendingIntent.putExtra(PASS, message);
                MainActivity.this.startActivityForResult(sendingIntent, ACTIVITY_SEND);
                break;
            case ActionSpecification.ACTION_UPDATE_KEY:
                Intent updateKeyIntent = new Intent(MainActivity.this, UpdateKeyActivity.class);
                // Provide joint hash for update key activity.
                updateKeyIntent.putExtra(PASS, message);
                MainActivity.this.startActivityForResult(updateKeyIntent, ACTIVITY_UPDATE);
                break;

        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog, String message, int action) {

    }


    /**
     * Parses the NDEF Message from the intent and decides based on the content of the
     * first NDEF Record how to handle it.
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
//        String hash;
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        String action = new String(msg.getRecords()[0].getId());
        String hash = new String(msg.getRecords()[0].getPayload());
        switch (action) {
            case ActionSpecification.NDEF_NEW_ACCOUNT:
                Log.d("MainActivity", "Case: "+ActionSpecification.NDEF_NEW_ACCOUNT);
                Log.d("MainActivity", "Action: "+action);
//                    hash = new String(msg.getRecords()[1].getPayload());
                    if (hash != null && !(hash.equals(""))) {
                        DialogFragment keyDialog = NewKeyDialog.newInstance(hash);
                        keyDialog.show(getFragmentManager(), "NewKeyDialog");
                    }
                break;
            case ActionSpecification.NDEF_UPDATE_ACCOUNT:
                Log.d("MainActivity", "Case: "+ActionSpecification.NDEF_UPDATE_ACCOUNT);
                Log.d("MainActivity", "Action: "+action);

//                    hash = new String(msg.getRecords()[1].getPayload());
                    if (hash != null && !(hash.equals(""))) {
                        DialogFragment keyDialog = UpdateKeyDialog.newInstance(hash);
                        keyDialog.show(getFragmentManager(), "UpdateKeyDialog");
                    }

                break;
            case ActionSpecification.NDEF_PROCESS_CHALLENGE:
                Log.d("MainActivity", "Case:   " + ActionSpecification.NDEF_PROCESS_CHALLENGE);
                Log.d("MainActivity", "Action: " + action);
                MyKeyPair keyPair = getKeyPair(hash);
//                MyKeyPair keyPair = getKeyPair(new String(msg.getRecords()[1].getPayload()));
                if (keyPair != null) {
                    DialogFragment challengeDialog =
                            IncomingChallengeDialog.newInstance(keyPair.getUser(),
                                    keyPair.getSite(), keyPair.getJointHash());
                    challengeDialog.show(getFragmentManager(), "IncomingChallengeDialog");
                }
                break;
            default:
                Log.d("MainActivity", "Case: default");
                Toast malformed = Toast.makeText(getApplicationContext(),
                        "Malformed NDEF message",
                        Toast.LENGTH_SHORT);
                malformed.setGravity(Gravity.CENTER, 0, 0);
                malformed.show();
        }

    }


    private void insertDummyData() {
        //##########Dummy data###############
        ArrayList<MyKeyPair> dummy = new ArrayList();
        MessageDigest md = null;
        String hash;
        MyKeyPair temp;
        byte[] digest;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update("username@www.google.com".getBytes());
        digest = md.digest();
        hash = Base64.encodeToString(digest, Base64.DEFAULT).trim();
        Log.d("MainActivity Insert", "raw: "+hash.length()+"| trimmed: "+hash.trim().length());
        temp = new MyKeyPair(hash.trim());
        temp.setUser("username");
        temp.setSite("www.google.com");
        dummy.add(temp);

        md.update("username@www.facebook.com".getBytes());
        digest = md.digest();
        hash = Base64.encodeToString(digest, Base64.DEFAULT).trim();
        Log.d("MainActivity Insert", "raw: "+hash.length()+"| trimmed: "+hash.trim().length());
        temp = new MyKeyPair(hash.trim());
        temp.setUser("username");
        temp.setSite("www.facebook.com");
        dummy.add(temp);

        md.update("username@www.mybank.com".getBytes());
        digest = md.digest();
        hash = Base64.encodeToString(digest, Base64.DEFAULT).trim();
        Log.d("MainActivity Insert", "raw: "+hash.length()+"| trimmed: "+hash.trim().length());
        temp = new MyKeyPair(hash.trim());
        temp.setUser("username");
        temp.setSite("www.mybank.com");
        dummy.add(temp);

        md.update("username@www.amazon.com".getBytes());
        digest = md.digest();
        hash = Base64.encodeToString(digest, Base64.DEFAULT).trim();
        Log.d("MainActivity Insert", "raw: "+hash.length()+"| trimmed: "+hash.trim().length());
        temp = new MyKeyPair(hash.trim());
        temp.setUser("username");
        temp.setSite("www.amazon.com");
        dummy.add(temp);

        md.update("username@www.youtube.com".getBytes());
        digest = md.digest();
        hash = Base64.encodeToString(digest, Base64.DEFAULT).trim();
        Log.d("MainActivity Insert", "raw: "+hash.length()+"| trimmed: "+hash.trim().length());
        temp = new MyKeyPair(hash.trim());
        temp.setUser("username");
        temp.setSite("www.youtube.com");
        dummy.add(temp);

        for (MyKeyPair key : dummy) {
            dbCon.saveKeyPairDetailed(key.getJointHash(), key.getUser(), key.getSite(),
                    key.getPubKeyData(), key.getPrivKeyData());
        }
    }

}
