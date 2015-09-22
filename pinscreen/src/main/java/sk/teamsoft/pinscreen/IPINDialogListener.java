package sk.teamsoft.pinscreen;

/**
 * Interface to be used as a callback object
 * @author Dusan Bartos
 */
public interface IPINDialogListener {
    /**
     * Fired when entered PIN is correct
     */
    void onPINEntered();

    /**
     * Used for setting up a new PIN
     *
     * @param pin entered PIN
     */
    void onPINSetup(String pin);

    /**
     * Fired when entered PIN is not correct
     */
    void onWrongEntry();
}
