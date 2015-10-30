package vs.in.de.uni_ulm.mreuter.login.data;

/**
 * Created by mreuter on 25/02/15.
 *
 * This class is used to differentiate between the events raised by different dialogs or
 * incoming NDEF messages.
 */
public class ActionSpecification {
    public final static int ACTION_SET_PASSWORD = 0;
    public final static int ACTION_NEW_KEY = 1;
    public final static int ACTION_UPDATE_KEY = 2;
    public final static int ACTION_SAVE_CHANGES = 3;
    public final static int ACTION_DELETE_KEY = 4;
    public final static int ACTION_INCOMING_CHALLENGE = 5;
    public final static String NDEF_NEW_ACCOUNT = "create";
    public final static String NDEF_UPDATE_ACCOUNT = "update";
    public final static String NDEF_PROCESS_CHALLENGE = "process";
}
