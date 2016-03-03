package com.enhueco.model.other;

/**
 * Created by Diego on 10/11/15.
 */
public interface CompletionListener<R>
{
    void onSuccess(R result);
    void onFailure(Exception error);
}
