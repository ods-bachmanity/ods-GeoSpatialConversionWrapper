package mil.nga.ods.geotrans.coordinates;

import geotrans3.coordinates.CoordinateTuple;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.FormatOptions;

import java.io.IOException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public abstract class CoordinatesArray {

    protected CoordinateTuple[] coordinatesArray;

    public int getLength() {
        return coordinatesArray.length;
    }

    public CoordinateTuple[] getCoordinates() {
        return coordinatesArray;
    }

    protected void setCoordinates(CoordinateTuple[] cArray) {
        coordinatesArray = cArray;
    }

    // Json output
    public abstract JSONObject toJson(FormatOptions format) throws JSONException, CoordinateConversionException;

    // File output
    public abstract String toString(FormatOptions format) throws IOException, CoordinateConversionException;
}
