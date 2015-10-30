package vs.in.de.uni_ulm.mreuter.login.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import vs.in.de.uni_ulm.mreuter.login.R;
import vs.in.de.uni_ulm.mreuter.login.data.ActionSpecification;
import vs.in.de.uni_ulm.mreuter.login.data.MyKeyPair;
import vs.in.de.uni_ulm.mreuter.login.data.NoticeDialogListener;
import vs.in.de.uni_ulm.mreuter.login.db.KeysDBAdapter;

public class UpdateKeyActivity extends Activity implements NoticeDialogListener{

    private MyKeyPair keyPairOld;
    private EditText username_edit;
    private EditText website_edit;
    private KeysDBAdapter dbCon;
    private String jointHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_key);
        //TODO Split sent message into something like old:new

        // Disable Android Beam UI.
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (nfcAdapter != null){
            nfcAdapter.setNdefPushMessage(null, this);
        }

        Bundle extras = getIntent().getExtras();
        jointHash = extras.getString(MainActivity.PASS);

        dbCon = new KeysDBAdapter(getApplicationContext());
        dbCon.open();
        Cursor cursor = dbCon.fetchKeyPair(jointHash);
        if(cursor.moveToFirst()) {
            String user = cursor.getString(1);
            String site = cursor.getString(2);
            byte[] pubKeyData = cursor.getBlob(3);
            byte[] privKeyData = cursor.getBlob(4);
            String created = cursor.getString(5);

            keyPairOld = new MyKeyPair(jointHash, user, site, pubKeyData, privKeyData, created);
            TextView createdV = (TextView) findViewById(R.id.created_view);
            username_edit = (EditText) findViewById(R.id.username_edit);
            website_edit = (EditText) findViewById(R.id.website_edit);

            username_edit.setText(keyPairOld.getUser());
            website_edit.setText(keyPairOld.getSite());
            createdV.setText("Created on "+keyPairOld.getCreated());
        }
        cursor.close();
        dbCon.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_key, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
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

                keyPair.setUser(String.valueOf(username_edit.getText()));
                keyPair.setSite(String.valueOf(website_edit.getText()));

                if(dbCon==null)
                    dbCon = new KeysDBAdapter(getApplicationContext());
                dbCon.open();
                dbCon.updateKeyPair(keyPair.getJointHash(), keyPairOld.getJointHash(),
                        keyPair.getUser(), keyPair.getSite(), keyPair.getPubKeyData(),
                        keyPair.getPrivKeyData());
                dbCon.close();

                setResult(RESULT_OK);
                finish();

                Toast changed = Toast.makeText(getApplicationContext(), "Changes saved",
                        Toast.LENGTH_SHORT);
                changed.setGravity(Gravity.CENTER, 0, 0);
                changed.show();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog, String message, int action) {

    }
}
