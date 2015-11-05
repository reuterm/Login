package vs.in.de.uni_ulm.mreuter.login.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import vs.in.de.uni_ulm.mreuter.login.R;
import vs.in.de.uni_ulm.mreuter.login.data.MyKeyPair;
import vs.in.de.uni_ulm.mreuter.login.data.NoticeDialogListener;
import vs.in.de.uni_ulm.mreuter.login.db.KeysDBAdapter;
import vs.in.de.uni_ulm.mreuter.login.data.ActionSpecification;


public class KeyEditActivity extends Activity implements NoticeDialogListener {

    private MyKeyPair keyPair;
    private EditText username_edit;
    private EditText website_edit;
    private KeysDBAdapter dbCon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_edit);

        // Disable Android Beam UI.
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (nfcAdapter != null){
            nfcAdapter.setNdefPushMessage(null, this);
        }

        keyPair = getIntent().getParcelableExtra(MainActivity.PASS);

        TextView created = (TextView) findViewById(R.id.created_view);
        username_edit = (EditText) findViewById(R.id.username_edit);
        website_edit = (EditText) findViewById(R.id.website_edit);

        username_edit.setText(keyPair.getUser());
        website_edit.setText(keyPair.getSite());
        created.setText("Created on "+keyPair.getCreated());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_key_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_save:
                DialogFragment saveDialog = new SaveChangesDialog();
                saveDialog.show(getFragmentManager(), "SaveChangesDialog");
                return super.onOptionsItemSelected(item);
            case R.id.action_delete:
                DialogFragment deleteDialog = new DeleteKeyDialog();
                deleteDialog.show(getFragmentManager(), "DeleteKeyDialog");
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String message, int action) {
        switch (action) {
            case ActionSpecification.ACTION_SAVE_CHANGES:
                keyPair.setUser(String.valueOf(username_edit.getText()));
                keyPair.setSite(String.valueOf(website_edit.getText()));

                if(dbCon==null)
                    dbCon = new KeysDBAdapter(getApplicationContext());
                dbCon.open();
                dbCon.updateKeyPair(keyPair.getJointHash(), keyPair.getJointHash(),
                        keyPair.getUser(), keyPair.getSite(), keyPair.getPubKeyData(),
                        keyPair.getPrivKeyData());
                dbCon.close();

                setResult(RESULT_OK);
                finish();

                Toast changed = Toast.makeText(getApplicationContext(), "Changes saved",
                        Toast.LENGTH_SHORT);
                changed.setGravity(Gravity.CENTER, 0, 0);
                changed.show();
                break;
            case ActionSpecification.ACTION_DELETE_KEY:
                if(dbCon==null)
                    dbCon = new KeysDBAdapter(getApplicationContext());
                dbCon.open();
                dbCon.deleteKeyPair(keyPair.getJointHash());
                dbCon.close();

                setResult(RESULT_OK);
                finish();

                Toast deleted = Toast.makeText(getApplicationContext(), "Key deleted",
                        Toast.LENGTH_SHORT);
                deleted.setGravity(Gravity.CENTER, 0, 0);
                deleted.show();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog, String message, int action) {

    }
}
