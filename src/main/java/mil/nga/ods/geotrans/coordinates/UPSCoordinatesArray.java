package mil.nga.ods.geotrans.coordinates;

import geotrans3.coordinates.ConvertResults;
import geotrans3.coordinates.CoordinateTuple;
import geotrans3.coordinates.UPSCoordinates;
import geotrans3.enumerations.CoordinateType;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.FormatOptions;
import geotrans3.misc.StringToVal;
import mil.nga.ods.geotrans.utils.GeoTransConstants;
import mil.nga.ods.geotrans.utils.InputVerifier;
import mil.nga.ods.geotrans.utils.StringToRawVal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UPSCoordinatesArray extends CoordinatesArray {

    private static final Logger log = LoggerFactory.getLogger(UPSCoordinatesArray.class.getName());

    // Single Results input
    public UPSCoordinatesArray(UPSCoordinates coords) {
        log.debug("Entering UPSCoordinatesArray(UPSCoordinates)");

        setCoordinates(new UPSCoordinates[] { coords });

        log.debug("Leaving UPSCoordinatesArray(UPSCoordinates)");
    }

    // Bulk Results input
    public UPSCoordinatesArray(ConvertResults[] results) {
        log.debug("Entering UPSCoordinatesArray(ConvertResults[])");

        UPSCoordinates[] coordArray = new UPSCoordinates[results.length];

        for (int i = 0; i < results.length; i++) {
            coordArray[i] = (UPSCoordinates) results[i].getCoordinateTuple();
        }

        setCoordinates(coordArray);

        log.debug("Leaving UPSCoordinatesArray(ConvertResults[])");
    }

    // File input
    public UPSCoordinatesArray(ArrayList<String> coords, int projectionType) throws CoordinateConversionException {
        log.debug("Entering UPSCoordinatesArray(ArrayList<String>, int)");

        StringToVal stringToVal = new StringToVal();

        CoordinateTuple[] coordinates = new CoordinateTuple[0];
        Iterator<String> iter = coords.iterator();

        String coordinateTuple[];

        coordinates = new UPSCoordinates[coords.size()];

        for (int i = 0; iter.hasNext(); i++) {
            coordinateTuple = iter.next().split(",");

            coordinates[i] = new UPSCoordinates(
                    projectionType,
                    coordinateTuple[0].trim().charAt(0),
                    stringToVal.stringToDouble(coordinateTuple[1].trim()),
                    stringToVal.stringToDouble(coordinateTuple[2].trim()));
        }

        setCoordinates(coordinates);

        log.debug("Leaving UPSCoordinatesArray(ArrayList<String>, int)");
    }

    // Json input
    public UPSCoordinatesArray(String prefix, Map<String, Object> input ) throws CoordinateConversionException, JSONException {
        log.debug("Entering UPSCoordinatesArray(String, Map<String, Object>)");

        InputVerifier iv = new InputVerifier();
        UPSCoordinates[] coordsToSet;

        if( input.get(prefix + GeoTransConstants.COORDINATES) != null) {
            JSONArray sourceCoords = new JSONArray(input.get(prefix + GeoTransConstants.COORDINATES).toString());

            coordsToSet = new UPSCoordinates[sourceCoords.length()];

            for( int i = 0; i < sourceCoords.length(); i++ ) {
                coordsToSet[i] = buildFromMap( iv.convertJSONToMap(sourceCoords.getJSONObject(i)), prefix );
            }
        }
        else {
            coordsToSet = new UPSCoordinates[1];
            coordsToSet[0] = buildFromMap( input, prefix );
        }

        setCoordinates(coordsToSet);

        log.debug("Leaving UPSCoordinatesArray(String, Map<String, Object>)");
    }

    // Json output
    public JSONObject toJson(FormatOptions format) throws JSONException, CoordinateConversionException {
        log.debug("Entering toJson(FormatOptions)");

        UPSCoordinates[] upsCoordinates = (UPSCoordinates[]) getCoordinates();
        JSONObject jsonToReturn = new JSONObject();
        JSONArray arrayToBuild = new JSONArray();

        if( upsCoordinates.length > 1 ) {
            for( int i = 0; i < upsCoordinates.length; i++ ) {
                arrayToBuild = arrayToBuild.put( buildJSONOutput( upsCoordinates[i], format) );
            }
            jsonToReturn.put(GeoTransConstants.COORDINATES,  arrayToBuild);
        }
        else {
            jsonToReturn = buildJSONOutput( upsCoordinates[0], format);
        }

        log.debug("Leaving toJson(FormatOptions) with {}", jsonToReturn.toString());
        return jsonToReturn;
    }

    // File output
    public String toString(FormatOptions format) throws IOException, CoordinateConversionException {
        log.debug("Entering toString(FormatOptions)");

        StringToRawVal stringToRawVal = new StringToRawVal(format.getRange(), format.getLeadingZeros(), format.getSeparator());

        InputVerifier iv = new InputVerifier();
        StringWriter sw = new StringWriter();
        BufferedWriter writer = new BufferedWriter(sw);

        UPSCoordinates[] coords = (UPSCoordinates[]) getCoordinates();

        for (int i = 0; i < coords.length; i++) {
            writer.write(String.valueOf( coords[i].getHemisphere() ));
            writer.write(GeoTransConstants.COMMA_SPACE);
            writer.write(stringToRawVal.doubleToString( iv.verifyDoubleIsValid(coords[i].getEasting()) ));
            writer.write(GeoTransConstants.COMMA_SPACE);
            writer.write(stringToRawVal.doubleToString( iv.verifyDoubleIsValid(coords[i].getNorthing()) ));
            writer.newLine();
        }

        writer.close();

        log.debug("Leaving toString(FormatOptions)");
        return sw.toString();
    }

    private UPSCoordinates buildFromMap(Map<String,Object> input, String prefix) throws CoordinateConversionException {
        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();
        if( prefix.equals(GeoTransConstants.TARGET_PREFIX) ) {        
	        return new UPSCoordinates(CoordinateType.UPS);
        } 
        else {
	        return new UPSCoordinates(
	                CoordinateType.UPS,
	                iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.HEMISPHERE).charAt(0),
	                stringToVal.stringToDouble(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.EASTING)),
	                stringToVal.stringToDouble(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.NORTHING)));
        }
    }

    private JSONObject buildJSONOutput(UPSCoordinates upsCoordinates, FormatOptions format) throws CoordinateConversionException, JSONException {
        InputVerifier iv = new InputVerifier();
        StringToRawVal stringToRawVal = new StringToRawVal(format.getRange(), format.getLeadingZeros(), format.getSeparator());

        JSONObject jsonToReturn = new JSONObject();
        jsonToReturn.put(GeoTransConstants.HEMISPHERE, String.valueOf( upsCoordinates.getHemisphere() ));
        jsonToReturn.put(GeoTransConstants.EASTING, stringToRawVal.doubleToString( iv.verifyDoubleIsValid(upsCoordinates.getEasting()) ));
        jsonToReturn.put(GeoTransConstants.NORTHING, stringToRawVal.doubleToString( iv.verifyDoubleIsValid(upsCoordinates.getNorthing()) ));

        return jsonToReturn;
    }
}