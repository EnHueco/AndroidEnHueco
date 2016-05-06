package com.enhueco.model.logicManagers;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.enhueco.model.EHApplication;
import com.enhueco.model.model.AppUser;
import com.enhueco.model.model.EnHueco;

import java.io.*;

/**
 * Created by Diego on 2/28/16.
 */
public class PersistenceManager
{
    private static PersistenceManager instance;

    public static PersistenceManager getSharedManager()
    {
        if (instance == null)
        {
            instance = new PersistenceManager();
        }

        return instance;
    }

    private PersistenceManager()
    {
    }

    /**
     * Persists all app's system data in path
     */
    public void persistData() throws IOException
    {

        FileOutputStream fos = EHApplication.getAppContext().openFileOutput(AppUser.FILE_NAME, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(EnHueco.getInstance().getAppUser());
        os.close();
        fos.close();
    }

    /**
     * Loads data from persistence
     *
     * @return true if data successfuly loaded, false otherwise
     */
    public boolean loadDataFromPersistence()
    {
        try
        {
            FileInputStream fis = EHApplication.getAppContext().openFileInput(AppUser.FILE_NAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            EnHueco.getInstance().setAppUser((AppUser) is.readObject());
            is.close();
            fis.close();
            return true;
        }
        catch (ClassNotFoundException | IOException e)
        {
            deletePersistenceData();
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes persistence contents
     */
    public void deletePersistenceData()
    {
        // Delete all persisted data
        EHApplication.getAppContext().deleteFile(AppUser.FILE_NAME);

        // Delete shared preferences
        PreferenceManager.getDefaultSharedPreferences(EHApplication.getAppContext()).edit().clear().commit();

    }
}
