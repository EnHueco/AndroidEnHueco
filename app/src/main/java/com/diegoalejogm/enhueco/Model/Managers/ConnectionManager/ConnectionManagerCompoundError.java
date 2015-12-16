package com.diegoalejogm.enhueco.model.managers.connectionManager;

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
