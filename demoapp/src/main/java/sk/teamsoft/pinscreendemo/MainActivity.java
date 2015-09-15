package sk.teamsoft.pinscreendemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import sk.teamsoft.pinscreen.PINScreen;
import sk.teamsoft.pinscreen.PINScreenManager;

public class MainActivity extends AppCompatActivity implements
        PINScreen.IPINDialogListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PINScreenManager.getInstance().setPIN("123456789");
    }

    public void showClosableDialog(View v) {
        PINScreenManager.getInstance().askForPIN(getSupportFragmentManager(), true);
    }

    public void showUnclosableDialog(View v) {
        PINScreenManager.getInstance().askForPIN(getSupportFragmentManager(), false);
    }

    @Override
    public void onPINEntered() {
        Toast.makeText(this, "PIN entered", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPINSetup(String pin) {
        Toast.makeText(this, "PIN set", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWrongEntry() {
        Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show();
    }
}
