package com.enhueco.model.logicManagers;

import android.os.Handler;
import android.os.Looper;
import com.enhueco.model.other.CompletionListener;

/**
 * Created by Diego on 5/1/16.
 */
public class EHManager
{

    public void generateError(final Exception e, final CompletionListener completionListener)
    {
        e.printStackTrace();
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                completionListener.onFailure(e);
            }
        });
    }
}
