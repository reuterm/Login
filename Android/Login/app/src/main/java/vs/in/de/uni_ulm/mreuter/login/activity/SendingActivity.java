package vs.in.de.uni_ulm.mreuter.login.activity;

import android.app.Activity;
import android.database.Cursor;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;

import vs.in.de.uni_ulm.mreuter.login.R;
import vs.in.de.uni_ulm.mreuter.login.data.ActionSpecification;
import vs.in.de.uni_ulm.mreuter.login.data.MyKeyPair;
import vs.in.de.uni_ulm.mreuter.login.db.KeysDBAdapter;

public class SendingActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback,
        NfcAdapter.OnNdefPushCompleteCallback {

    private NfcAdapter mNfcAdapter;
    private KeysDBAdapter dbCon;
    private MyKeyPair keyPair;
    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);

        dbCon = new KeysDBAdapter(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        String jointHash = extras.getString(MainActivity.PASS);
        action = extras.getString("action");
        keyPair = getKeyPair(jointHash);

        // Check for available NFC Adapter.
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter != null) {
            // Register callback.
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            // Register callback to listen for message-sent success
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sending, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(item.getItemId() == R.id.action_cancel){
            this.finish();
            Toast canceled = Toast.makeText(getApplicationContext(), "Action canceled",
                    Toast.LENGTH_SHORT);
            canceled.setGravity(Gravity.CENTER, 0, 0);
            canceled.show();
        }
        return super.onOptionsItemSelected(item);
    }

    // Create NDEF message when another device (i.e. reader) is in range.
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        NdefMessage msg = null;
        // Decide what the message should contain.
        switch (action) {
            case ActionSpecification.NDEF_NEW_ACCOUNT:
                //TODO do something
                break;
            case ActionSpecification.NDEF_UPDATE_ACCOUNT:
                //TODO do something
                break;
            case ActionSpecification.NDEF_PROCESS_CHALLENGE:
                //TODO do something
                byte[] sig = keyPair.sign("test");
                Log.d("SendingActivity", "signature genuine: "+keyPair.verify(sig, "test"));

                NdefRecord pubKey = NdefRecord.createMime("application/vnd.vs.in.de.uni_ulm.mreuter.login",
                        keyPair.pubToPEM().getBytes(Charset.forName("UTF-8")));
                NdefRecord message = NdefRecord.createMime("application/vnd.vs.in.de.uni_ulm.mreuter.login",
                        ActionSpecification.NDEF_PROCESS_CHALLENGE.getBytes(Charset.forName("UTF-8")));
//                NdefRecord message = NdefRecord.createMime("application/vnd.vs.in.de.uni_ulm.mreuter.login",
//                        keyPair.privToPEM().getBytes(Charset.forName("UTF-8")));
                NdefRecord signature = NdefRecord.createMime("application/vnd.vs.in.de.uni_ulm.mreuter.login",
                       (keyPair.sign("test")));
                msg = new NdefMessage(new NdefRecord[] {message, signature, pubKey});
//                msg = new NdefMessage(new NdefRecord[] {message, pubKey});
        }
//        MessageDigest md = null;
//        byte[] digest;
//        try {
//            md = MessageDigest.getInstance("SHA-256");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        md.update("username@www.mybank.com".getBytes());
//        digest = md.digest();
//        String hash = Base64.encodeToString(digest, Base64.DEFAULT);
//        NdefRecord mimeRecord = NdefRecord.createMime("application/vnd.com.example.android.beam",
//                hash.getBytes(Charset.forName("UTF-8")));
//        msg = new NdefMessage(new NdefRecord[] { mimeRecord, NdefRecord.createApplicationRecord("vs.in.de.uni_ulm.mreuter.login")});

        return msg;
    }

    public NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }

    // Be notified once the message has been successfully sent and return back to MainActivity.
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        finish();
    }
}
