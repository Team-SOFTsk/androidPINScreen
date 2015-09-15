package sk.teamsoft.pinscreen;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Controller for PINScreen widget
 * All interaction should be managed through this class
 *
 * @author Dusan Bartos
 */
@SuppressWarnings("unused")
public class PINScreenManager {

    private static PINScreenManager sInstance;

    public static PINScreenManager getInstance() {
        if (sInstance == null) {
            synchronized (PINScreenManager.class) {
                if (sInstance == null) {
                    sInstance = new PINScreenManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * Application Locker + handler
     */
    private final Runnable lockTask = new Runnable() {
        @Override
        public void run() {
            mLocked = true;
        }
    };
    private final Handler lockHandler = new Handler();

    /**
     * Is application locked
     */
    private boolean mLocked = false;
    /**
     * Delay for app locking
     * Defined in seconds
     * 0 by default
     */
    private int mLockDelay = 0;
    /**
     * PIN for unlocking the app
     */
    private String mPIN = "";

    /**
     * @return true if app is locked
     */
    public boolean isAppLocked() {
        return mLocked;
    }

    /**
     * Locks app
     */
    public void lock() {
        mLocked = true;
    }

    /**
     * Unlocks app
     */
    public void unLock() {
        mLocked = false;
    }

    /**
     * Locks app with defined delay
     * Delay is 0 seconds by default
     */
    public void lockWithDelay() {
        lockHandler.removeCallbacks(lockTask);
        lockHandler.postDelayed(lockTask, mLockDelay * 1000);
    }

    /**
     * Sets pin to compare entry with
     *
     * @param newPIN pin
     */
    public void setPIN(String newPIN) {
        mPIN = newPIN;
    }

    /**
     * Gets pin
     *
     * @return pin
     */
    public String getPIN() {
        return mPIN;
    }

    /**
     * Sets delay for locking
     *
     * @param delay delay in seconds
     */
    public void setPINDelay(int delay) {
        mLockDelay = delay;
    }

    /**
     * Prompt PIN dialog to unlock app
     *
     * @param fm         fragment manager
     * @param cancelable true if dialog can be cancelled
     */
    private void askForPINInternal(FragmentManager fm,
                                   Boolean cancelable,
                                   boolean setup) {
        Fragment fragment = fm.findFragmentByTag(PINScreen.FRAGMENT_TAG);
        PINScreen lockScreen;

        if (fragment instanceof PINScreen) {
            lockScreen = ((PINScreen) fragment);
        } else {
            lockScreen = PINScreen.show(fm);
        }

        lockScreen.updateSettings(mPIN, cancelable, setup);
    }

    /**
     * Opens PIN screen to ask for PIN
     * PIN has to be set before, or it will be the default one ("")
     *
     * @param fragmentManager fragment manager to use when showing Lock screen
     * @param cancelable      true to make PIN dialog cancellable
     */
    public void askForPIN(FragmentManager fragmentManager, Boolean cancelable) {
        askForPINInternal(fragmentManager, cancelable, false);
    }

    /**
     * Sets new PIN
     *
     * @see #askForPIN(android.support.v4.app.FragmentManager, Boolean)
     */
    public void setupPIN(FragmentManager fragmentManager, Integer maxLength) {
        askForPINInternal(fragmentManager, true, true);
    }
}
