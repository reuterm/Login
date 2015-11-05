package vs.in.de.uni_ulm.mreuter.login.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import vs.in.de.uni_ulm.mreuter.login.R;
import vs.in.de.uni_ulm.mreuter.login.data.ActionSpecification;
import vs.in.de.uni_ulm.mreuter.login.data.NoticeDialogListener;

/**
 * Created by mreuter on 19/02/15.
 */
public class SetPasswordDialog extends DialogFragment {

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

//        // Create custom view to display multi line title.
//        TextView title = new TextView(getActivity().getApplicationContext());
//        title.setText(R.string.title_dialog_set_password);
//        // Adjust text properties to default.
//        title.setTextColor(Color.parseColor("#ff33b5e5"));
//        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because its going in the dialog layout.
        final View view = inflater.inflate(R.layout.dialog_set_password, null);
        builder.setView(view)
                // Set title.
                .setTitle(R.string.title_dialog_set_password)
//                .setCustomTitle(title)
                // Add action buttons.
                .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Send positive button even back to host activity.
                        // Find UI components to inspect entered text.
                        EditText pw = (EditText) view.findViewById(R.id.password);
                        EditText pw_rep = (EditText) view.findViewById(R.id.password_repeat);

                        // Check equality of the entered passwords. If equal password is sent back,
                        // else null.
                        // Action is set to this dialog.
                        if (String.valueOf(pw.getText()).equals(String.valueOf(pw_rep.getText()))) {
                            listener.onDialogPositiveClick(SetPasswordDialog.this,
                                    String.valueOf(pw.getText()), 0);
                        } else {
                            listener.onDialogPositiveClick(SetPasswordDialog.this, null, ActionSpecification.ACTION_SET_PASSWORD);
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send negative button event back to host activity.
                        // No message needed.
                        // Action is set to this dialog.
                        listener.onDialogNegativeClick(SetPasswordDialog.this, null, ActionSpecification.ACTION_SET_PASSWORD);
                    }
                });
        return builder.create();
    }
}
