package com.diegoalejogm.enhueco.Model.Other.ConnectionManager;

public class ConnectionManagerCompoundError
{
    public final Exception error;
    public final ConnectionManagerRequest request;

    public ConnectionManagerCompoundError(Exception error, ConnectionManagerRequest request)
    {
        this.error = error;
        this.request = request;
    }
}
