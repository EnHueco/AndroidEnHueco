package com.diegoalejogm.enhueco.Model.Other;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Diego on 10/13/15.
 */
public class Utilities
{
    public static Date dateFromServerFormattedString (String date) throws ParseException
    {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(date);
    }
}
