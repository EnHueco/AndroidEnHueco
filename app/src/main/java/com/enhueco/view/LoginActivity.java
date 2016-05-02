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
import com.enhueco.view.dialog.EHProgressDialog;

public class LoginActivity extends AppCompatActivity
{


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (EnHueco.getInstance().getAppUser() != null) {

            Intent intent = new Intent(LoginActivity.this, MainTabbedActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        }
    }

    public void logIn(View view)
    {
        final EHProgressDialog progressDialog = new EHProgressDialog(this);
        progressDialog.setMessage("Ingresando...");
        progressDialog.show();

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
                    progressDialog.dismiss();

                    Intent intent = new Intent(LoginActivity.this, MainTabbedActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }

                @Override
                public void onFailure(Exception error)
                {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }
}
