package com.enhueco.view.dialog;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Diego on 5/2/16.
 */
public class EHProgressDialog extends ProgressDialog
{

    public EHProgressDialog(Context context)
    {
        super(context);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setMessage("Actualizando...");
    }
}
