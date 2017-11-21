package com.example.mlab.androidpresencesystem;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.firebase.ui.auth.AuthUI;

public class MainActivity extends AppCompatActivity {
    Button btnLogin;
    private int LOGIN_PERMISSION = 1000;
    RelativeLayout relativeLayout;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == LOGIN_PERMISSION) {
            startNewActivity(resultCode, data);
        }
    }

    private void startNewActivity(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            startActivity(new Intent(MainActivity.this, ListOnline.class));
            finish();
        } else {
            Snackbar snackbar = Snackbar.make(relativeLayout, "Cant Login", Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                btnLogin.performClick();
                }
            });

            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLogin = findViewById(R.id.btnSignIn);
        relativeLayout = findViewById(R.id.relativelayout);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        AuthUI.getInstance().createSignInIntentBuilder()
                                .setAllowNewEmailAccounts(true).build(), LOGIN_PERMISSION
                );
            }
        });


    }
}
