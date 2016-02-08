package de.flowment.wanderlust;

import android.location.Location;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by
 * Timo Raschke (s812538) <a href="mailto:Timo.Raschke@gmail.com">Timo Raschke, Timo.Raschke@gmail.com</a> and
 * Khaled Reguieg (s813812) <a href="mailto:Khaled.Reguieg@gmail.com">Khaled Reguieg, Khaled.Reguieg@gmail.com</a>
 * on 10.12.2015.
 * <p>
 * This class prints .kml files with the coordinates of a tour in it, a photo and it's coordinates.
 */
public class KmlWriter {
    private PrintWriter printWriter;
    private List<Location> locationList;
    private String mCurrentPhotopath;

    public KmlWriter(OutputStream outputStream) {
        this.printWriter = new PrintWriter(outputStream);
        locationList = new ArrayList<>();
    }

    public void pushLocation(Location location) {
        this.locationList.add(location);
    }

    public void writeKml() {
        writeHeader();
        writePlacemark();
        writeFooter();
        this.printWriter.flush();
    }

    private void writeHeader() {
        this.printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        this.printWriter.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">");
    }

    private void writePlacemark() {
        this.printWriter.println("<Placemark>");
        this.printWriter.println("<name>gx:altitudeMode Example</name>");
        this.printWriter.println("<Snippet>Photo</Snippet>");
        this.printWriter.println("<description><![CDATA[  \n" +
                " <img src='" + mCurrentPhotopath + "' width='400' /><br/&gt;  \n" +
                " Photo taken from near the palace in Monaco<br/>  \n" +
                " ]]>  \n" +
                " </description>");
        this.printWriter.println("<LookAt>");
        this.printWriter.println("<longitude>" + this.locationList.get(0).getLongitude() + "</longitude>");
        this.printWriter.println("<latitude>" + this.locationList.get(0).getLatitude() + "</latitude>");
        this.printWriter.println("<heading>-60</heading>");
        this.printWriter.println("<tilt>70</tilt>");
        this.printWriter.println("<range>6300</range>");
        this.printWriter.println("<gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>");
        this.printWriter.println("</LookAt>");
        this.printWriter.println("<LineString>");
        this.printWriter.println("<extrude>1</extrude>");
        this.printWriter.println("<gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>");
        this.printWriter.println("<coordinates>");
        for (Location location : this.locationList) {
            this.printWriter.println(location.getLongitude() + "," + location.getLatitude() + "," + location.getAltitude());
        }
        this.printWriter.println("</coordinates>");
        this.printWriter.println("</LineString>");
        this.printWriter.println("</Placemark>");
    }

    private void writeFooter() {
        this.printWriter.println("</kml>");
    }

    public void pushImagePath(String path) {
        mCurrentPhotopath = path;
    }
}
