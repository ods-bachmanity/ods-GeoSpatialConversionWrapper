package mil.nga.ods.geotrans.coordinates;

import geotrans3.coordinates.CartesianCoordinates;
import geotrans3.coordinates.ConvertResults;
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

public class CartesianCoordinatesArray extends CoordinatesArray {

    private static final Logger log = LoggerFactory.getLogger(CartesianCoordinatesArray.class.getName());

    // Single Results input
    public CartesianCoordinatesArray(CartesianCoordinates coords) {
        log.debug("Entering CartesianCoordinatesArray(CartesianCoordinates)");

        setCoordinates(new CartesianCoordinates[] { coords });

        log.debug("Leaving CartesianCoordinatesArray(CartesianCoordinates)");
    }

    // Bulk Results input
    public CartesianCoordinatesArray(ConvertResults[] results) {
        log.debug("Entering CartesianCoordinatesArray(ConvertResults[])");

        CartesianCoordinates[] coordArray = new CartesianCoordinates[results.length];

        for (int i = 0; i < results.length; i++) {
            coordArray[i] = (CartesianCoordinates) results[i].getCoordinateTuple();
        }

        setCoordinates(coordArray);

        log.debug("Leaving CartesianCoordinatesArray(ConvertResults[])");
    }

    // File input
    public CartesianCoordinatesArray(ArrayList<String> coords, int projectionType) throws CoordinateConversionException {
        log.debug("Entering CartesianCoordinatesArray(ArrayList<String>, int)");

        CartesianCoordinates[] coordinates = new CartesianCoordinates[coords.size()];
        Iterator<String> iter = coords.iterator();

        String coordinateTuple[];
        StringToVal stringToVal = new StringToVal();

        for (int i = 0; iter.hasNext(); i++) {
            coordinateTuple = iter.next().split(",");

            coordinates[i] = new CartesianCoordinates(
                    projectionType,
                    stringToVal.stringToDouble(coordinateTuple[0].trim()),
                    stringToVal.stringToDouble(coordinateTuple[1].trim()),
                    stringToVal.stringToDouble(coordinateTuple[2].trim()));
        }

        setCoordinates(coordinates);

        log.debug("Leaving CartesianCoordinatesArray(ArrayList<String>, int)");
    }

    // Json input
    public CartesianCoordinatesArray(String prefix, Map<String, Object> input, int projectionType) throws CoordinateConversionException, JSONException {
        log.debug("Entering CartesianCoordinatesArray(String, Map<String,Object>)");

        InputVerifier iv = new InputVerifier();
        CartesianCoordinates[] coordsToSet;

        if( input.get(prefix + GeoTransConstants.COORDINATES) != null) {
            JSONArray sourceCoords = new JSONArray(input.get(prefix + GeoTransConstants.COORDINATES).toString());

            coordsToSet = new CartesianCoordinates[sourceCoords.length()];

            for( int i = 0; i < sourceCoords.length(); i++ ) {
                coordsToSet[i] = buildFromMap( iv.convertJSONToMap(sourceCoords.getJSONObject(i)), prefix, projectionType);
            }
        }
        else {
            coordsToSet = new CartesianCoordinates[1];
            coordsToSet[0] = buildFromMap( input, prefix, projectionType );
        }

        setCoordinates(coordsToSet);

        log.debug("Leaving CartesianCoordinatesArray(String, Map<String,Object>)");
    }

    // Json output
    public JSONObject toJson(FormatOptions format) throws JSONException, CoordinateConversionException {
        log.debug("Entering toJson(FormatOptions)");

        CartesianCoordinates[] cartCoordinates = (CartesianCoordinates[]) getCoordinates();
        JSONObject jsonToReturn = new JSONObject();
        JSONArray arrayToBuild = new JSONArray();

        if( cartCoordinates.length > 1 ) {
            for( int i = 0; i < cartCoordinates.length; i++ ) {
                arrayToBuild = arrayToBuild.put( buildJSONOutput( cartCoordinates[i], format ) );
            }
            jsonToReturn.put(GeoTransConstants.COORDINATES,  arrayToBuild);
        }
        else {
            jsonToReturn = buildJSONOutput( cartCoordinates[0], format );
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

        CartesianCoordinates[] coords = (CartesianCoordinates[]) getCoordinates();
        StringToRawVal stringToRawVal = new StringToRawVal(format.getRange(), format.getLeadingZeros(), format.getSeparator());

        for (int i = 0; i < coords.length; i++) {
            writer.write(stringToRawVal.doubleToString( iv.verifyDoubleIsValid(coords[i].getX()) ));
            writer.write(GeoTransConstants.COMMA_SPACE);
            writer.write(stringToRawVal.doubleToString( iv.verifyDoubleIsValid(coords[i].getY()) ));
            writer.write(GeoTransConstants.COMMA_SPACE);
            writer.write(stringToRawVal.doubleToString( iv.verifyDoubleIsValid(coords[i].getZ()) ));
            writer.newLine();
        }
        writer.close();

        log.debug("Leaving toString(FormatOptions)");
        return sw.toString();
    }

    private CartesianCoordinates buildFromMap(Map<String,Object> input, String prefix, int projectionType) throws CoordinateConversionException {
        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();
        
        if( prefix.equals(GeoTransConstants.TARGET_PREFIX) ) {
	        return new CartesianCoordinates(projectionType);        	
        }
        else {
	        return new CartesianCoordinates(
	                projectionType,
	                stringToVal.stringToDouble(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.X_VALUE)),
	                stringToVal.stringToDouble(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.Y_VALUE)),
	                stringToVal.stringToDouble(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.Z_VALUE)));
        }
    }

    private JSONObject buildJSONOutput(CartesianCoordinates cartCoordinates, FormatOptions format) throws CoordinateConversionException, JSONException {
        InputVerifier iv = new InputVerifier();
        StringToRawVal stringToRawVal = new StringToRawVal(format.getRange(), format.getLeadingZeros(), format.getSeparator());

        JSONObject jsonToReturn = new JSONObject();
        jsonToReturn.put(GeoTransConstants.X_VALUE, stringToRawVal.doubleToString( iv.verifyDoubleIsValid(cartCoordinates.getX()) ));
        jsonToReturn.put(GeoTransConstants.Y_VALUE, stringToRawVal.doubleToString( iv.verifyDoubleIsValid(cartCoordinates.getY()) ));
        jsonToReturn.put(GeoTransConstants.Z_VALUE, stringToRawVal.doubleToString( iv.verifyDoubleIsValid(cartCoordinates.getZ()) ));;

        return jsonToReturn;
    }
}