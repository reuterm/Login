package vs.in.de.uni_ulm.mreuter.login.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import vs.in.de.uni_ulm.mreuter.login.R;
import vs.in.de.uni_ulm.mreuter.login.data.MyKeyPair;
import vs.in.de.uni_ulm.mreuter.login.data.NoticeDialogListener;
import vs.in.de.uni_ulm.mreuter.login.db.KeysDBAdapter;
import vs.in.de.uni_ulm.mreuter.login.data.ActionSpecification;

public class NewKeyActivity extends Activity implements NoticeDialogListener {

    private EditText username_edit;
    private EditText website_edit;
    private String jointHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_key);

        // Disable Android Beam UI.
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (nfcAdapter != null){
            nfcAdapter.setNdefPushMessage(null, this);
        }

        Bundle extras = getIntent().getExtras();
        jointHash = extras.getString(MainActivity.PASS);

        username_edit = (EditText) findViewById(R.id.username_edit);
        website_edit = (EditText) findViewById(R.id.website_edit);

        username_edit.setText("");
        website_edit.setText("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_key, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_save:
                DialogFragment dialog = new SaveChangesDialog();
                dialog.show(getFragmentManager(), "SaveChangesDialog");
                return super.onOptionsItemSelected(item);
            case R.id.action_cancel:
                this.finish();
                Toast canceled = Toast.makeText(getApplicationContext(), "Action canceled",
                        Toast.LENGTH_SHORT);
                canceled.setGravity(Gravity.CENTER, 0, 0);
                canceled.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String message, int action) {

        switch (action) {
            case ActionSpecification.ACTION_SAVE_CHANGES:
                String user = String.valueOf(username_edit.getText());
                String website = String.valueOf(website_edit.getText());

               // Make sure data is complete
                if(user==null||website==null||user.equals("")||user.equals("")){
                    Toast error = Toast.makeText(this, "Please provide both username and website",
                            Toast.LENGTH_SHORT);
                    error.setGravity(Gravity.CENTER, 0, 0);
                    error.show();
                    return;
                }

                MyKeyPair keyPair = new MyKeyPair(jointHash);
                keyPair.setUser(user);
                keyPair.setSite(website);

                KeysDBAdapter dbCon = new KeysDBAdapter(getApplicationContext());
                dbCon.open();
                dbCon.saveKeyPairDetailed(keyPair.getJointHash(), keyPair.getUser(), keyPair.getSite(),
                        keyPair.getPubKeyData(), keyPair.getPrivKeyData());
                dbCon.close();

                setResult(RESULT_OK);
                finish();

                Toast saved = Toast.makeText(getApplicationContext(), "Entry saved",
                        Toast.LENGTH_SHORT);
                saved.setGravity(Gravity.CENTER, 0, 0);
                saved.show();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog, String message, int action) {

    }
}
