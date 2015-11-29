package com.diegoalejogm.enhueco.Model.Managers;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;
import com.diegoalejogm.enhueco.Model.EHApplication;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Diego on 11/9/15.
 */
public class ProximityManager
{
    private static final ProximityManager sharedManager = new ProximityManager();

    private ProximityManager () {}

    private UndirectedGraph<String, DefaultEdge> wifiAccessPointsBSSIDSGraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

    public static ProximityManager getSharedManager()
    {
        return sharedManager;
    }

    public static List<ScanResult> scanVisibleWifiAccessPoints ()
    {
        WifiManager wifiManager = (WifiManager) EHApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled())
        {
            //TODO: Ask user kindly
            wifiManager.setWifiEnabled(true);
        }

        return wifiManager.getScanResults();
    }

    public static Optional<ScanResult> getVisibleWifiAccessPointWithBestSignal ()
    {
        List<ScanResult> visibleAccessPoints = scanVisibleWifiAccessPoints();

        if (visibleAccessPoints.isEmpty()) return Optional.absent();

        ScanResult bestSignalAccessPoint = visibleAccessPoints.get(0);

        for (ScanResult accessPoint: visibleAccessPoints)
        {
            if (accessPoint.level > bestSignalAccessPoint.level)
            {
                bestSignalAccessPoint = accessPoint;
            }
        }

        return Optional.of(bestSignalAccessPoint);
    }

    public void generateGraphFromFile ()
    {
        try
        {
            CSVReader reader = new CSVReader(new FileReader("yourfile.csv"));

            List<String[]> rows = reader.readAll();

            final DateFormat format = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);

            Collections.sort(rows, new Comparator<String[]>()
            {
                @Override
                public int compare(String[] lhs, String[] rhs)
                {
                    try
                    {
                        int dateComparison = format.parse(lhs[4]).compareTo(format.parse(rhs[4]));

                        if (dateComparison != 0)
                        {
                            return dateComparison;
                        }
                        else
                        {
                            return Integer.getInteger(lhs[2]).compareTo(Integer.getInteger(rhs[2]));
                        }
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }

                    return 0;
                }
            });

            String currentDate = "";
            String referenceAccessPointBSSID = "";

            for (String[] row: rows)
            {
                wifiAccessPointsBSSIDSGraph.addVertex(row[4]);

                if (!row[4].equals(currentDate))
                {
                    currentDate = row[4];
                    referenceAccessPointBSSID = row[4];
                }
                else
                {
                    wifiAccessPointsBSSIDSGraph.addEdge(referenceAccessPointBSSID, row[4]);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean accessPointsAreNear (String bssidA, String bssidB)
    {
        for (DefaultEdge edge: wifiAccessPointsBSSIDSGraph.edgesOf(bssidA))
        {
            String neighbor = wifiAccessPointsBSSIDSGraph.getEdgeTarget(edge);

            if (neighbor.equals(bssidB)) return true;

            for (DefaultEdge edge2: wifiAccessPointsBSSIDSGraph.edgesOf(neighbor))
            {
                String neighbor2 = wifiAccessPointsBSSIDSGraph.getEdgeTarget(edge2);

                if (neighbor2.equals(bssidB)) return true;
            }
        }

        return false;
    }

    public void reportVisibleBestSignalBSSID ()
    {
        Optional<ScanResult> visibleWifiAccessPointWithBestSignal = getVisibleWifiAccessPointWithBestSignal();

        //TODO
    }
}
