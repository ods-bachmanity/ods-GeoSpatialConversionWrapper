package mil.nga.ods.geotrans.coordinates;

import geotrans3.coordinates.ConvertResults;
import geotrans3.coordinates.MapProjectionCoordinates;
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

public class MapProjectionCoordinatesArray extends CoordinatesArray {

    private static final Logger log = LoggerFactory.getLogger(MapProjectionCoordinatesArray.class.getName());

    // Single Results input
    public MapProjectionCoordinatesArray(MapProjectionCoordinates coords) {
        log.debug("Entering MapProjectionCoordinatesArray(MapProjectionCoordinates)");

        setCoordinates(new MapProjectionCoordinates[] { coords });

        log.debug("Leaving MapProjectionCoordinatesArray(MapProjectionCoordinates)");
    }

    // Bulk Results input
    public MapProjectionCoordinatesArray(ConvertResults[] results) {
        log.debug("Entering MapProjectionCoordinatesArray(ConvertResults[])");

        MapProjectionCoordinates[] coordArray = new MapProjectionCoordinates[results.length];

        for (int i = 0; i < results.length; i++) {
            coordArray[i] = (MapProjectionCoordinates) results[i].getCoordinateTuple();
        }

        setCoordinates(coordArray);

        log.debug("Leaving MapProjectionCoordinatesArray(ConvertResults[])");
    }

    // File input
    public MapProjectionCoordinatesArray(ArrayList<String> coords, int projectionType) throws CoordinateConversionException {
        log.debug("Entering MapProjectionCoordinatesArray(ArrayList<String>, int)");

        MapProjectionCoordinates[] coordinates = new MapProjectionCoordinates[coords.size()];
        Iterator<String> iter = coords.iterator();

        String coordinateTuple[];
        StringToVal stringToVal = new StringToVal();

        for (int i = 0; iter.hasNext(); i++) {
            coordinateTuple = iter.next().split(",");

            coordinates[i] = new MapProjectionCoordinates(
                    projectionType,
                    stringToVal.stringToDouble(coordinateTuple[0].trim()),
                    stringToVal.stringToDouble(coordinateTuple[1].trim()));
        }

        setCoordinates(coordinates);

        log.debug("Leaving MapProjectionCoordinatesArray(ArrayList<String>, int)");
    }

    // Json input
    public MapProjectionCoordinatesArray(String prefix, Map<String, Object> input, int projectionType) throws CoordinateConversionException, JSONException {
        log.debug("Entering MapProjectionCoordinatesArray(String, Map<String, Object>)");

        InputVerifier iv = new InputVerifier();
        MapProjectionCoordinates[] coordsToSet;

        if( input.get(prefix + GeoTransConstants.COORDINATES) != null) {
            JSONArray sourceCoords = new JSONArray(input.get(prefix + GeoTransConstants.COORDINATES).toString());

            coordsToSet = new MapProjectionCoordinates[sourceCoords.length()];

            for( int i = 0; i < sourceCoords.length(); i++ ) {
                coordsToSet[i] = buildFromMap( iv.convertJSONToMap(sourceCoords.getJSONObject(i)), prefix, projectionType );
            }
        }
        else {
            coordsToSet = new MapProjectionCoordinates[1];
            coordsToSet[0] = buildFromMap( input, prefix, projectionType );
        }

        setCoordinates(coordsToSet);

        log.debug("Leaving MapProjectionCoordinatesArray(String, Map<String, Object>)");
    }

    // Json output
    public JSONObject toJson(FormatOptions format) throws JSONException, CoordinateConversionException {
        log.debug("Entering toJson(FormatOptions)");

        MapProjectionCoordinates[] mapCoordinates = (MapProjectionCoordinates[]) getCoordinates();
        JSONObject jsonToReturn = new JSONObject();
        JSONArray arrayToBuild = new JSONArray();

        if( mapCoordinates.length > 1 ) {
            for( int i = 0; i < mapCoordinates.length; i++ ) {
                arrayToBuild = arrayToBuild.put( buildJSONOutput( mapCoordinates[i], format ) );
            }
            jsonToReturn.put(GeoTransConstants.COORDINATES,  arrayToBuild);
        }
        else {
            jsonToReturn = buildJSONOutput( mapCoordinates[0], format );
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

        MapProjectionCoordinates[] coords = (MapProjectionCoordinates[]) getCoordinates();

        StringToRawVal stringToRawVal = new StringToRawVal(format.getRange(), format.getLeadingZeros(), format.getSeparator());

        for (int i = 0; i < coords.length; i++) {
            writer.write(stringToRawVal.doubleToString( iv.verifyDoubleIsValid( coords[i].getEasting() ) ));
            writer.write(GeoTransConstants.COMMA_SPACE);
            writer.write(stringToRawVal.doubleToString( iv.verifyDoubleIsValid( coords[i].getNorthing() ) ));
            writer.newLine();
        }

        writer.close();

        log.debug("Leaving toString(FormatOptions)");
        return sw.toString();
    }

    private MapProjectionCoordinates buildFromMap(Map<String,Object> input, String prefix, int projectionType) throws CoordinateConversionException {
        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();
        
        if( prefix.equals(GeoTransConstants.TARGET_PREFIX) ) {        
	        return new MapProjectionCoordinates(projectionType);
        }
        else {
            return new MapProjectionCoordinates(
                    projectionType,
                    stringToVal.stringToDouble(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.EASTING)),
                    stringToVal.stringToDouble(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.NORTHING)));        	
        }
    }

    private JSONObject buildJSONOutput(MapProjectionCoordinates mapCoordinates, FormatOptions format) throws CoordinateConversionException, JSONException {
        InputVerifier iv = new InputVerifier();
        StringToRawVal stringToRawVal = new StringToRawVal(format.getRange(), format.getLeadingZeros(), format.getSeparator());

        JSONObject jsonToReturn = new JSONObject();
        jsonToReturn.put(GeoTransConstants.EASTING, stringToRawVal.doubleToString( iv.verifyDoubleIsValid(mapCoordinates.getEasting()) ));
        jsonToReturn.put(GeoTransConstants.NORTHING, stringToRawVal.doubleToString( iv.verifyDoubleIsValid(mapCoordinates.getNorthing()) ));

        return jsonToReturn;
    }
}