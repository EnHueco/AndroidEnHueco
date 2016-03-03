package com.enhueco.model.logicManagers;

import android.content.Context;
import com.enhueco.model.EHApplication;
import com.enhueco.model.model.AppUser;
import com.enhueco.model.model.EnHueco;

import java.io.*;

/**
 * Created by Diego on 2/28/16.
 */
public abstract class PersistenceManager
{
    /**
     * Persists all app's system data in path
     *
     * @return true if correctly persisted or false otherwise
     */
    public static boolean persistData()
    {
        try
        {
            FileOutputStream fos = EHApplication.getAppContext().openFileOutput(AppUser.FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(EnHueco.getInstance().getAppUser());
            os.close();
            fos.close();
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Loads data from persistence
     *
     * @return true if data successfuly loaded, false otherwise
     */
    public static boolean loadDataFromPersistence()
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
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes persistence contents
     */
    static void deletePersistenceData()
    {
        EHApplication.getAppContext().deleteFile(AppUser.FILE_NAME);
    }
}
