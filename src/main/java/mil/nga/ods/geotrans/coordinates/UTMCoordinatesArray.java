package mil.nga.ods.geotrans.coordinates;

import geotrans3.coordinates.ConvertResults;
import geotrans3.coordinates.UPSCoordinates;
import geotrans3.coordinates.UTMCoordinates;
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

public class UTMCoordinatesArray extends CoordinatesArray {

    private static final Logger log = LoggerFactory.getLogger(UTMCoordinatesArray.class.getName());

    // Single Results input
    public UTMCoordinatesArray(UTMCoordinates coords) {
        log.debug("Entering UTMCoordinatesArray(UTMCoordinates)");

        setCoordinates(new UTMCoordinates[] { coords });

        log.debug("Leaving UTMCoordinatesArray(UTMCoordinates)");
    }

    // Bulk Results input
    public UTMCoordinatesArray(ConvertResults[] results) {
        log.debug("Entering UTMCoordinatesArray(ConvertResults[])");

        UTMCoordinates[] coordArray = new UTMCoordinates[results.length];

        for (int i = 0; i < results.length; i++) {
            coordArray[i] = (UTMCoordinates) results[i].getCoordinateTuple();
        }

        setCoordinates(coordArray);

        log.debug("Leaving UTMCoordinatesArray(ConvertResults[])");
    }

    // File input
    public UTMCoordinatesArray(ArrayList<String> coords, int projectionType) throws CoordinateConversionException {
        log.debug("Entering UTMCoordinatesArray(ArrayList<String>, int)");

        UTMCoordinates[] coordinates = new UTMCoordinates[coords.size()];
        Iterator<String> iter = coords.iterator();

        String coordinateTuple[];
        StringToVal stringToVal = new StringToVal();

        for (int i = 0; iter.hasNext(); i++) {
            coordinateTuple = iter.next().split(",");

            coordinates[i] = new UTMCoordinates(
                    projectionType,
                    stringToVal.stringToInt(coordinateTuple[0]),
                    coordinateTuple[1].trim().charAt(0),
                    stringToVal.stringToDouble(coordinateTuple[2].trim()),
                    stringToVal.stringToDouble(coordinateTuple[3].trim()));
        }

        setCoordinates(coordinates);

        log.debug("Leaving UTMCoordinatesArray(ArrayList<String>, int)");
    }

    // Json input
    public UTMCoordinatesArray(String prefix, Map<String, Object> input) throws CoordinateConversionException, JSONException {
        log.debug("Entering UTMCoordinatesArray(String, Map<String, Object>)");

        InputVerifier iv = new InputVerifier();
        UTMCoordinates[] coordsToSet;

        if( input.get(prefix + GeoTransConstants.COORDINATES) != null) {
            JSONArray sourceCoords = new JSONArray(input.get(prefix + GeoTransConstants.COORDINATES).toString());

            coordsToSet = new UTMCoordinates[sourceCoords.length()];

            for( int i = 0; i < sourceCoords.length(); i++ ) {
                coordsToSet[i] = buildFromMap( iv.convertJSONToMap(sourceCoords.getJSONObject(i)), prefix);
            }
        }
        else {
            coordsToSet = new UTMCoordinates[1];
            coordsToSet[0] = buildFromMap( input, prefix );
        }

        setCoordinates(coordsToSet);

        log.debug("Leaving UTMCoordinatesArray(String, Map<String, Object>)");
    }

    // Json output
    public JSONObject toJson(FormatOptions format) throws JSONException, CoordinateConversionException {
        log.debug("Entering toJson(FormatOptions)");

        UTMCoordinates[] utmCoordinates = (UTMCoordinates[]) getCoordinates();
        JSONObject jsonToReturn = new JSONObject();
        JSONArray arrayToBuild = new JSONArray();

        if( utmCoordinates.length > 1 ) {
            for( int i = 0; i < utmCoordinates.length; i++ ) {
                arrayToBuild = arrayToBuild.put( buildJSONOutput( utmCoordinates[i], format) );
            }
            jsonToReturn.put(GeoTransConstants.COORDINATES,  arrayToBuild);
        }
        else {
            jsonToReturn = buildJSONOutput( utmCoordinates[0], format);
        }

        log.debug("Leaving toJson(FormatOptions) with {}", jsonToReturn.toString());
        return jsonToReturn;
    }

    // File output
    public String toString(FormatOptions format) throws IOException, CoordinateConversionException {
        log.debug("Entering toString(FormatOptions)");

        InputVerifier iv = new InputVerifier();
        StringWriter sw = new StringWriter();
        BufferedWriter writer = new BufferedWriter(sw);

        UTMCoordinates[] coords = (UTMCoordinates[]) getCoordinates();

        StringToRawVal stringToRawVal = new StringToRawVal(format.getRange(), format.getLeadingZeros(), format.getSeparator());

        for (int i = 0; i < coords.length; i++) {
            writer.write(String.valueOf( coords[i].getZone() ));
            writer.write(GeoTransConstants.COMMA_SPACE);
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

    private UTMCoordinates buildFromMap(Map<String,Object> input, String prefix) throws CoordinateConversionException {
        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();
        if( prefix.equals(GeoTransConstants.TARGET_PREFIX) ) {        
	        return new UTMCoordinates(CoordinateType.UTM);
        } 
        else {        
	        return new UTMCoordinates(
	                CoordinateType.UTM,
	                stringToVal.stringToInt( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.ZONE_NUMBER) ),
	                iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.HEMISPHERE).charAt(0),
	                stringToVal.stringToDouble(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.EASTING) ),
	                stringToVal.stringToDouble(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.NORTHING) ));
        }
    }

    private JSONObject buildJSONOutput(UTMCoordinates utmCoordinates, FormatOptions format) throws CoordinateConversionException, JSONException {
        InputVerifier iv = new InputVerifier();
        StringToRawVal stringToRawVal = new StringToRawVal(format.getRange(), format.getLeadingZeros(), format.getSeparator());

        JSONObject jsonToReturn = new JSONObject();
        jsonToReturn.put(GeoTransConstants.HEMISPHERE, String.valueOf(utmCoordinates.getHemisphere()));
        jsonToReturn.put(GeoTransConstants.ZONE_NUMBER, utmCoordinates.getZone());
        jsonToReturn.put(GeoTransConstants.EASTING, stringToRawVal.doubleToString(iv.verifyDoubleIsValid(utmCoordinates.getEasting())));
        jsonToReturn.put(GeoTransConstants.NORTHING, stringToRawVal.doubleToString(iv.verifyDoubleIsValid(utmCoordinates.getNorthing())));

        return jsonToReturn;
    }
}