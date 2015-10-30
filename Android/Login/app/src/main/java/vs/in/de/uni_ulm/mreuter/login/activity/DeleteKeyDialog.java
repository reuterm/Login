package vs.in.de.uni_ulm.mreuter.login.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import vs.in.de.uni_ulm.mreuter.login.R;
import vs.in.de.uni_ulm.mreuter.login.data.ActionSpecification;
import vs.in.de.uni_ulm.mreuter.login.data.NoticeDialogListener;

/**
 * Created by mreuter on 25/02/15.
 */
public class DeleteKeyDialog extends DialogFragment {
    // Instance of the interface to deliver action events.
    private NoticeDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener.
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface.
        try {
            // Instantiate the NoticeDialogListener so events can be sent to the host.
            listener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception.
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the to be displayed message
        builder.setMessage(R.string.message_dialog_delete_key)
                // Set title.
                .setTitle(R.string.title_dialog_delete_key)
                        // Add action buttons.
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Send positive button even back to host activity.
                        // No message needed.
                        // Action is set to this dialog.
                        listener.onDialogPositiveClick(DeleteKeyDialog.this, null, ActionSpecification.ACTION_DELETE_KEY);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send negative button event back to host activity.
                        // No message needed.
                        // Action is set to this dialog.
                        listener.onDialogNegativeClick(DeleteKeyDialog.this, null, ActionSpecification.ACTION_DELETE_KEY);
                    }
                });
        return builder.create();
    }
}
