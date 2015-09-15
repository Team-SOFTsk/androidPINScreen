package sk.teamsoft.pinscreen;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;

/**
 * PINScreen component
 * Overrides dialog fragment
 * uses {@link android.support.v7.widget.RecyclerView} widget
 *
 * @author Dusan Bartos
 */
//@SuppressWarnings("unused")
public class PINScreen extends DialogFragment implements RecyclerItemClickListener.OnItemClickListener {

    protected static final String FRAGMENT_TAG = "sk_teamsoft_pinscreen_lock_fragment";

    private static final String BUNDLE_REAL_VALUE = "realVal";
    private static final String BUNDLE_CURRENT_VALUE = "val";
    private static final String BUNDLE_CANCELABLE = "cancelable";
    private static final String BUNDLE_SETUP = "setup";
    private static final String BUNDLE_MAXLENGTH = "maxLength";

    // empty initial listener
    private static IPINDialogListener mListener;
    private static int sActiveColor = -1;

    public static void setActiveColor(int color) {
        sActiveColor = color;
    }

    /**
     * Maximum PIN length
     * 4 by default
     * Can be changed via {@link #setMaxLength(int)}
     */
    private int mMaxLength = 4;

    private boolean mCancelable = false;
    private boolean mSetup = false;
    private CharSequence mRealValue;
    private StringBuilder mValue = new StringBuilder("");
    private TextView mValueTextView;

    private RecyclerView mNumbersGridView;
    private LockGridAdapter mAdapter;
    private RecyclerItemClickListener mItemClickListener;

    /**
     * Creates new instance of PIN screen and shows it
     *
     * @param fm fragment manager
     *
     * @return created instance
     */
    protected static PINScreen show(FragmentManager fm) {
        PINScreen dialog = new PINScreen();
        dialog.show(fm, FRAGMENT_TAG);
        return dialog;
    }

    /**
     * Sets whether lock screen is cancelable/not-cancelable
     *
     * @param cancelable true to be able to cancel lock screen
     *                   by default, lock screen is not cancellable
     */
    public void setCancelableDialog(boolean cancelable) {
        mCancelable = cancelable;
        this.setCancelable(cancelable);
    }

    /**
     * Sets maximum PIN length
     *
     * @param length maximum pin length
     */
    public void setMaxLength(int length) {
        mMaxLength = length;
    }

    /**
     * Sets real PIN value to compare with
     *
     * @param realPIN real PIN value
     */
    public void setRealValue(CharSequence realPIN) {
        mRealValue = realPIN;
    }

    /**
     * Sets fragment to be in setup mode
     * setup mode is used when new PIN is created
     *
     * @param isSetup true to be in setup mode
     */
    public void setSetup(boolean isSetup) {
        mSetup = isSetup;
    }

    /**
     * Updates lock screen settings all at once
     *
     * @param realPIN      PIN to compare user entry to
     * @param isCancelable is lock dialog cancellable
     * @param isSetup      is dialog for setting up the first PIN
     */
    public void updateSettings(String realPIN, Boolean isCancelable, boolean isSetup) {
        setRealValue(realPIN);
        if (isCancelable != null) {
            setCancelableDialog(isCancelable);
        }
        setSetup(isSetup);
        if (!isSetup) {
            mMaxLength = realPIN.length();
        }
    }

    /**
     * Make this dialog fullscreen
     *
     * @param savedInstanceState saved state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mMaxLength = savedInstanceState.getInt(BUNDLE_MAXLENGTH);
            mRealValue = savedInstanceState.getString(BUNDLE_REAL_VALUE);
            mCancelable = savedInstanceState.getBoolean(BUNDLE_CANCELABLE);
            mSetup = savedInstanceState.getBoolean(BUNDLE_SETUP);
            mValue = new StringBuilder();
            mValue.append(savedInstanceState.getString(BUNDLE_CURRENT_VALUE));
        }

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        this.setCancelable(mCancelable);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.lock_screen, container);

        mValueTextView = (TextView) layoutView.findViewById(R.id.pin_value);
        mNumbersGridView = (RecyclerView) layoutView.findViewById(R.id.numbers_grid);

        if (mValueTextView == null || mNumbersGridView == null) {
            throw new AssertionError("Lock screen has invalid layout");
        }

        // set value and hint
        if (mSetup) {
            mValueTextView.setHint(R.string.pinscreen_setup_pin_hint);
        } else {
            mValueTextView.setHint(R.string.pinscreen_pin_hint_default);
        }
        refreshValueText();

        // initialize numbers grid
        mItemClickListener = new RecyclerItemClickListener(getActivity(), this);
        mAdapter = new LockGridAdapter(getActivity(), sActiveColor);
        mNumbersGridView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mNumbersGridView.setAdapter(mAdapter);
        mNumbersGridView.setHasFixedSize(true);
        mNumbersGridView.addOnItemTouchListener(mItemClickListener);

        return layoutView;
    }

    /**
     * Save values to Bundle when changing orientation
     *
     * @param outState state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_REAL_VALUE, mRealValue != null ? mRealValue.toString() : "");
        outState.putString(BUNDLE_CURRENT_VALUE, mValue.toString());
        outState.putBoolean(BUNDLE_CANCELABLE, mCancelable);
        outState.putBoolean(BUNDLE_SETUP, mSetup);
        outState.putInt(BUNDLE_MAXLENGTH, mMaxLength);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (getDialog() != null) {
            // do whatever
            switch (mAdapter.getItemType(position)) {
                // char clicked, append it to the value
                case LockGridValues.NUMBER_TYPE:
                    if (mValue.length() < mMaxLength) {
                        mValue.append(mAdapter.getItem(position));
                        refreshValueText();
                    }
                    break;

                // delete the last character from PIN
                case LockGridValues.BACK_TYPE:
                    if (mValue.length() > 0) {
                        mValue.deleteCharAt(mValue.length() - 1);
                    }
                    refreshValueText();
                    break;

                // submit entered PIN and clear the value
                case LockGridValues.SUBMIT_TYPE:
                    submitPIN();
                    mValue = new StringBuilder("");
                    break;
            }
        }
    }

    /**
     * Mask currently entered PIN with asterisk signs
     */
    private void refreshValueText() {
        char[] maskedCode = new char[mValue.length()];
        Arrays.fill(maskedCode, '*');
        mValueTextView.setText(String.valueOf(maskedCode));
    }

    /**
     * Submits PIN
     * Takes care of Error handling
     * If PIN is correctly entered, it runs registered callback
     */
    private void submitPIN() {
        if (mListener != null) {
            if (mSetup) {
                mListener.onPINSetup(mValue.toString());
                mNumbersGridView.removeOnItemTouchListener(mItemClickListener);
                dismiss();
            } else if (mValue.toString().equals(mRealValue)) {
                mListener.onPINEntered();
                mNumbersGridView.removeOnItemTouchListener(mItemClickListener);
                dismiss();
            } else {
                mListener.onWrongEntry();
                mValueTextView.setText("");
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (IPINDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getName()
                    + " must implement IPINDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Interface to be used as a callback object
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

    /**
     * Lock Grid adapter
     * Adapter for displaying grid of numbers (plus functional buttons) to enter PIN
     * Based on {@link android.support.v7.widget.RecyclerView}
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class LockGridAdapter extends RecyclerView.Adapter<LockGridViewHolder> {

        private Context mContext;
        private String[][] mValues;
        private int mActiveColor;
        private float dp;

        public LockGridAdapter(Context context, int activeColor) {
            mContext = context;
            mValues = LockGridValues.gridValues;
            mActiveColor = activeColor;

            dp = mContext.getResources().getDisplayMetrics().density;
        }

        @Override
        public LockGridViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new LockGridViewHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.lock_grid_item, viewGroup, false),
                    getBackgroundColorScheme(ContextCompat.getColor(mContext, android.R.color.transparent),
                            mActiveColor));
        }

        @Override
        public void onBindViewHolder(LockGridViewHolder lockGridViewHolder, int i) {
            String code = mValues[i][1];

            lockGridViewHolder.setValue(mValues[i][0]);

            // submit and back have smaller font-size
            if (code.equals(LockGridValues.SUBMIT_TYPE) || code.equals(LockGridValues.BACK_TYPE)) {
                lockGridViewHolder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
            } else {
                lockGridViewHolder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            }
        }

        @Override
        public int getItemCount() {
            return mValues.length;
        }

        public String getItemType(int position) {
            return mValues[position][1];
        }

        public String getItem(int position) {
            return mValues[position][0];
        }

        /**
         * Sets stateList background drawable for viewHolder (numbers adapter) views
         *
         * @param defaultColor default color
         * @param pressedColor pressed color
         *
         * @return drawable
         */
        private Drawable getBackgroundColorScheme(int defaultColor, int pressedColor) {
            int defaultBorder = Color.argb(30, 0, 0, 0);
            int pressedBorder = Color.argb(80, 0, 0, 0);

            // default state
            GradientDrawable defaultGd = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{defaultColor, defaultColor});
            defaultGd.setStroke(1, defaultBorder);
            defaultGd.setCornerRadius(3f * dp);

            // pressed state
            GradientDrawable pressedGd = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{pressedColor, pressedColor});
            pressedGd.setStroke(1, pressedBorder);
            pressedGd.setCornerRadius(3f * dp);

            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_pressed}, pressedGd);
            drawable.addState(new int[]{}, defaultGd);
            return drawable;
        }
    }

    /**
     * Lock Grid viewHolder
     * Based on {@link android.support.v7.widget.RecyclerView}
     *
     * @see android.support.v7.widget.RecyclerView.ViewHolder
     */
    private class LockGridViewHolder extends RecyclerView.ViewHolder {

        TextView mItemValue;

        public LockGridViewHolder(View v, Drawable background) {
            super(v);
            mItemValue = (TextView) v.findViewById(R.id.grid_value);

            if (mItemValue == null) {
                throw new AssertionError("Grid item has invalid layout");
            }

            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                //noinspection deprecation
                itemView.setBackgroundDrawable(background);
            } else {
                itemView.setBackground(background);
            }
        }

        public void setValue(String value) {
            mItemValue.setText(value);
        }

        public void setTextSize(int unit, float size) {
            mItemValue.setTextSize(unit, size);
        }
    }

    /**
     * Lock Grid values
     */
    private static class LockGridValues {
        public static final String NUMBER_TYPE = "Number";
        public static final String SUBMIT_TYPE = "Submit";
        public static final String BACK_TYPE = "Back";

        public static final String[][] gridValues = new String[][]{
                {"1", NUMBER_TYPE},
                {"2", NUMBER_TYPE},
                {"3", NUMBER_TYPE},
                {"4", NUMBER_TYPE},
                {"5", NUMBER_TYPE},
                {"6", NUMBER_TYPE},
                {"7", NUMBER_TYPE},
                {"8", NUMBER_TYPE},
                {"9", NUMBER_TYPE},
                {"<", BACK_TYPE},
                {"0", NUMBER_TYPE},
                {"OK", SUBMIT_TYPE}
        };
    }
}
