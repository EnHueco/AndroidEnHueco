package com.enhueco.model.other;

/**
 * Created by Diego on 2/28/16.
 */
public interface BasicCompletionListener
{
    void onSuccess();
    void onFailure(Exception error);
}
