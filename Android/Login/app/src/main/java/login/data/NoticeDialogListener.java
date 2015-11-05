package vs.in.de.uni_ulm.mreuter.login.data;

import android.app.DialogFragment;

/**
 * Created by mreuter on 21/02/15.
 */
// Interface a caller must implement in order to receive event callbacks from dialogs.
public interface NoticeDialogListener {

    // Yes event.
    public void onDialogPositiveClick(DialogFragment dialog, String message, int action);

    // No event.
    public void onDialogNegativeClick(DialogFragment dialog, String message, int action);
}
