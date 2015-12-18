package com.diegoalejogm.enhueco.model.other;

/**
 * Created by Diego on 10/11/15.
 */
public interface CompletionListener<T>
{
    void onSuccess(T result);
    void onFailure();
}
