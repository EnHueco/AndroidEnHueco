package com.enhueco.view;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.enhueco.R;
import com.enhueco.model.logicManagers.AccountManager;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.other.BasicCompletionListener;

public class LoginActivity extends AppCompatActivity
{

    ProgressDialog loginProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginProgressDialog = new ProgressDialog(this);
        loginProgressDialog.setMessage("Ingresando...");

        if (EnHueco.getInstance().getAppUser() != null) {

            Intent intent = new Intent(LoginActivity.this, MainTabbedActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        }
    }

    public void logIn(View view)
    {
        loginProgressDialog.show();
        EditText loginET = (EditText) findViewById(R.id.loginText);
        String loginString = loginET.getText().toString();
        EditText passwordET = (EditText) findViewById(R.id.passwordText);
        String passwordString= passwordET.getText().toString();

        // Testing values
        if(loginString.equals("test") && passwordString.equals("test"))
        {
            Intent intent = new Intent(this, MainTabbedActivity.class);
            startActivity(intent);
            EnHueco.getInstance().createTestAppUser();
            finish();
        }
        // Actual login
        else
        {
            AccountManager.getSharedManager().login(loginString, passwordString, new BasicCompletionListener()
            {
                @Override
                public void onSuccess()
                {
                    if ((loginProgressDialog != null) && loginProgressDialog.isShowing())
                    {
                        loginProgressDialog.dismiss();
                    }

                    Intent intent = new Intent(LoginActivity.this, MainTabbedActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }

                @Override
                public void onFailure(Exception error)
                {
                    if ((loginProgressDialog != null) && loginProgressDialog.isShowing())
                    {
                        loginProgressDialog.dismiss();
                    }

                    Toast.makeText(getApplicationContext(), "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (loginProgressDialog.isShowing()) {
            loginProgressDialog.cancel();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (loginProgressDialog.isShowing()) {
            loginProgressDialog.cancel();
        }
    }
}
