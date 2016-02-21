package com.diegoalejogm.enhueco.model.other;

/**
 * Created by Diego on 10/11/15.
 */
public class EHURLS
{
    /**
     * Base URL for all EH API requests
     */
    public static final String BASE = "https://enhueco.uniandes.edu.co/api";

    /**
     * URL segment for authentication purposes
     */
    public static final String AUTH_SEGMENT = "/auth/";

    /**
     * URL segment for friends details purposes
     */
    public static final String FRIENDS_SEGMENT = "/friends/";

    /**
     * URL segment for friends quick synchronization purposes
     */
    public static final String FRIENDS_SYNC_SEGMENT = "/friends/sync/";

    /**
     * URL segment for user search purposes
     */
    public static final String USERS_SEARCH = "/users/";

    /**
     * URL segment for outgoing friend requests purposes
     */
    public static final String OUTGOING_FRIEND_REQUESTS_SEGMENT = "/requests/sent/";

    /**
     * URL segment for incoming friend requests purposes
     */
    public static final String INCOMING_FRIEND_REQUESTS_SEGMENT = "/requests/received/";

    /**
     * URL segment for user's details purposes
     */
    public static final String ME_SEGMENT = "/me/";

    /**
     * URL segment for user's events purposes
     */
    public static final String EVENTS_SEGMENT = "/gaps/";
}
