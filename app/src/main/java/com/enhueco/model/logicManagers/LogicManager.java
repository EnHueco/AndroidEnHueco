package com.enhueco.model.logicManagers;

import android.os.Handler;
import android.os.Looper;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.CompletionListener;

/**
 * Created by Diego on 5/1/16.
 */
public abstract class LogicManager
{
    public void callCompletionListenerSuccessHandlerOnMainThread(final BasicCompletionListener completionListener)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                completionListener.onSuccess();
            }
        });
    }

    public <R> void callCompletionListenerSuccessHandlerOnMainThread(final CompletionListener<R> completionListener, final R result)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                completionListener.onSuccess(result);
            }
        });
    }

    public void callCompletionListenerFailureHandlerOnMainThread(final CompletionListener completionListener, final Exception exception)
    {
        exception.printStackTrace();
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                completionListener.onFailure(exception);
            }
        });
    }

    public void callCompletionListenerFailureHandlerOnMainThread(final BasicCompletionListener completionListener, final Exception exception)
    {
        exception.printStackTrace();
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                completionListener.onFailure(exception);
            }
        });
    }
}
