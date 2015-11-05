package vs.in.de.uni_ulm.mreuter.login.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import vs.in.de.uni_ulm.mreuter.login.R;
import vs.in.de.uni_ulm.mreuter.login.data.ActionSpecification;
import vs.in.de.uni_ulm.mreuter.login.data.NoticeDialogListener;

/**
 * Created by mreuter on 26/02/15.
 */
public class IncomingChallengeDialog extends DialogFragment {
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

    // Use this method to pass data to the dialog
    public static IncomingChallengeDialog newInstance(String user, String site, String jointHash) {
        IncomingChallengeDialog dialog = new IncomingChallengeDialog();

        // Save data in bundle
        Bundle args = new Bundle();
        args.putString("user", user);
        args.putString("site", site);
        args.putString("hash", jointHash);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {

        // Access data from MainActivity
        String user = getArguments().getString("user");
        String site = getArguments().getString("site");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because its going in the dialog layout.
        final View view = inflater.inflate(R.layout.dialog_incoming_challenge, null);

        // Get the TextViews and provide information
        TextView username_info = (TextView) view.findViewById(R.id.username_info);
        TextView website_info = (TextView) view.findViewById(R.id.website_info);
        username_info.setText(user);
        website_info.setText(site);

        // Set remaining properties
        builder.setView(view)
                // Set title.
                .setTitle(R.string.title_dialog_incoming_challenge)
                        // Add action buttons.
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Send positive button even back to host activity.
                        // Message is joint hash.
                        // Action is set to this dialog.
                        listener.onDialogPositiveClick(IncomingChallengeDialog.this,
                                getArguments().getString("hash"),
                                ActionSpecification.ACTION_INCOMING_CHALLENGE);
                        IncomingChallengeDialog.this.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send negative button event back to host activity.
                        // No message needed.
                        // Action is set to this dialog.
                        listener.onDialogNegativeClick(IncomingChallengeDialog.this, null,
                                ActionSpecification.ACTION_INCOMING_CHALLENGE);
                        IncomingChallengeDialog.this.dismiss();
                    }
                });
        return builder.create();
    }
}
