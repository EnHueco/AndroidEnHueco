package com.enhueco.model.logicManagers.privacyManager;

import com.enhueco.model.model.User;

import java.util.Collection;

/**
 * Created by Diego Montoya Sefair on 3/2/16.
 *
 * Policy applied to the group of friends it accepts for parameter
 */
public abstract class PrivacyPolicy
{
    /**
     * A policy that specifies that the setting is applied to everyone except
     * the group of users given.
     */
    public class EveryoneExcept extends PrivacyPolicy
    {
        private Collection<User> users;

        public EveryoneExcept (Collection<User> users)
        {
            this.users = users;
        }

        public Collection<User> getUsers()
        {
            return users;
        }
    }

    /**
     * A policy that specifies that the setting is applied to only
     * the group of users given.
     */
    public class Only extends PrivacyPolicy
    {
        private Collection<User> users;

        public Only(Collection<User> users)
        {
            this.users = users;
        }

        public Collection<User> getUsers()
        {
            return users;
        }
    }
}
