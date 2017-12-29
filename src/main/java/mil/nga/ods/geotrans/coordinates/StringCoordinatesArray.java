package mil.nga.ods.geotrans.coordinates;

import geotrans3.coordinates.BNGCoordinates;
import geotrans3.coordinates.ConvertResults;
import geotrans3.coordinates.CoordinateTuple;
import geotrans3.coordinates.GARSCoordinates;
import geotrans3.coordinates.GEOREFCoordinates;
import geotrans3.coordinates.MGRSorUSNGCoordinates;
import geotrans3.coordinates.StringCoordinates;
import geotrans3.enumerations.CoordinateType;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.FormatOptions;
import mil.nga.ods.geotrans.utils.GeoTransConstants;
import mil.nga.ods.geotrans.utils.InputVerifier;

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

public class StringCoordinatesArray extends CoordinatesArray {

    private static final Logger log = LoggerFactory.getLogger(StringCoordinatesArray.class.getName());

    // Single Results input
    public StringCoordinatesArray(StringCoordinates coords) {
        log.debug("Entering StringCoordinatesArray(StringCoordinates)");

        setCoordinates(new StringCoordinates[] { coords });

        log.debug("Leaving StringCoordinatesArray(StringCoordinates)");
    }

    // Bulk Results input
    public StringCoordinatesArray(ConvertResults[] results) {
        log.debug("Entering StringCoordinatesArray(ConvertResults[])");

        StringCoordinates[] coordArray = new StringCoordinates[results.length];

        for (int i = 0; i < results.length; i++) {
            coordArray[i] = (StringCoordinates) results[i].getCoordinateTuple();
        }

        setCoordinates(coordArray);

        log.debug("Leaving StringCoordinatesArray(ConvertResults[])");
    }

    // File input
    public StringCoordinatesArray(ArrayList<String> inputCoords, int projectionType, int precision) throws CoordinateConversionException {
        log.debug("Entering StringCoordinatesArray(ArrayList<String>, int, int)");

        CoordinateTuple[] coordinates;
        Iterator<String> iter = inputCoords.iterator();

        switch (projectionType) {

        case CoordinateType.USNG:
        case CoordinateType.MGRS:
            coordinates = new MGRSorUSNGCoordinates[inputCoords.size()];

            for (int i = 0; iter.hasNext(); i++) {
                coordinates[i] = new MGRSorUSNGCoordinates(projectionType, iter.next(), precision);
            }
            break;

        case CoordinateType.BNG:
            coordinates = new BNGCoordinates[inputCoords.size()];

            for (int i = 0; iter.hasNext(); i++) {
                coordinates[i] = new BNGCoordinates(projectionType, iter.next(), precision);
            }
            break;

        case CoordinateType.GARS:
            coordinates = new GARSCoordinates[inputCoords.size()];

            for (int i = 0; iter.hasNext(); i++) {
                coordinates[i] = new GARSCoordinates(projectionType, iter.next(), precision);
            }
            break;

        case CoordinateType.GEOREF:
            coordinates = new GEOREFCoordinates[inputCoords.size()];

            for (int i = 0; iter.hasNext(); i++) {
                coordinates[i] = new GEOREFCoordinates(projectionType, iter.next(), precision);
            }

            break;

        case CoordinateType.F16GRS:

            coordinates = new MGRSorUSNGCoordinates[inputCoords.size()];
            int length;
            String coordinateString;

            for (int i = 0; iter.hasNext(); i++) {
                coordinateString = iter.next();
                length = coordinateString.length();

                if ((coordinateString.charAt(length - 1) == ('0')) && (coordinateString.charAt(length - 2) == ('0'))) {
                    coordinateString = coordinateString.substring(0, length - 2);
                }

                coordinates[i] = new MGRSorUSNGCoordinates(CoordinateType.MGRS, coordinateString, precision);
            }
            break;
        default:
            throw new CoordinateConversionException("Invalid string coordinate type");
        }

        setCoordinates(coordinates);

        log.debug("Leaving StringCoordinatesArray(ArrayList<String>, int, int)");
    }

    // Json input
    public StringCoordinatesArray(String prefix, Map<String, Object> input, int projectionType, int precision) throws CoordinateConversionException, JSONException {
        log.debug("Entering StringCoordinatesArray(String, Map<String, Object>, int)");

        InputVerifier iv = new InputVerifier();
        StringCoordinates[] coordsToSet;

        if( input.get(prefix + GeoTransConstants.COORDINATES) != null) {
            JSONArray sourceCoords = new JSONArray(input.get(prefix + GeoTransConstants.COORDINATES).toString());

            coordsToSet = new StringCoordinates[sourceCoords.length()];

            for( int i = 0; i < sourceCoords.length(); i++ ) {
                coordsToSet[i] = buildFromMap( iv.convertJSONToMap(sourceCoords.getJSONObject(i)), prefix, projectionType, precision);
            }
        }
        else {
            coordsToSet = new StringCoordinates[1];
            coordsToSet[0] = buildFromMap( input, prefix, projectionType, precision );
        }

        setCoordinates(coordsToSet);

        log.debug("Leaving StringCoordinatesArray(String, Map<String, Object>, int)");
    }

    // Json output
    public JSONObject toJson(FormatOptions format) throws JSONException, CoordinateConversionException {
        log.debug("Entering toJson(FormatOptions)");

        StringCoordinates[] stringCoordinates = (StringCoordinates[]) getCoordinates();
        JSONObject jsonToReturn = new JSONObject();
        JSONArray arrayToBuild = new JSONArray();

        if( stringCoordinates.length > 1 ) {
            for( int i = 0; i < stringCoordinates.length; i++ ) {
                arrayToBuild = arrayToBuild.put( buildJSONOutput( stringCoordinates[i] ) );
            }
            jsonToReturn.put(GeoTransConstants.COORDINATES,  arrayToBuild);
        }
        else {
            jsonToReturn = buildJSONOutput( stringCoordinates[0] );
        }

        log.debug("Leaving toJson(FormatOptions) with {}", jsonToReturn.toString());
        return jsonToReturn;
    }

    // File output
    public String toString(FormatOptions format) throws IOException {
        log.debug("Entering toString(FormatOptions)");

        StringWriter sw = new StringWriter();
        BufferedWriter writer = new BufferedWriter(sw);

        StringCoordinates[] coords = (StringCoordinates[]) getCoordinates();

        for (int i = 0; i < coords.length; i++) {
            writer.write(coords[i].getCoordinateString());
            writer.newLine();
        }

        writer.close();

        log.debug("Leaving toString(FormatOptions)");
        return sw.toString();
    }

    private StringCoordinates buildFromMap(Map<String,Object> input, String prefix, int projectionType, int precision) throws CoordinateConversionException {

        InputVerifier iv = new InputVerifier();

        log.debug("Switching on Coordinate Type {}", projectionType);
        switch (projectionType) {
        case CoordinateType.F16GRS:
            if( prefix.equals(GeoTransConstants.TARGET_PREFIX) ) {        
                return new MGRSorUSNGCoordinates(CoordinateType.MGRS, precision);
            }
            
            String coordinateString = iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.COORDINATE_STRING);
            int length = coordinateString.length();


            if ((coordinateString.charAt(length - 1) == ('0')) && (coordinateString.charAt(length - 2) == ('0'))) {
                coordinateString = coordinateString.substring(0, length - 2);
            }

            return new MGRSorUSNGCoordinates(CoordinateType.MGRS, coordinateString, precision);

        case CoordinateType.USNG:
        case CoordinateType.MGRS:
            if( prefix.equals(GeoTransConstants.TARGET_PREFIX) ) {        
                return new MGRSorUSNGCoordinates(CoordinateType.MGRS, precision);
            }        	
            return new MGRSorUSNGCoordinates(projectionType, iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.COORDINATE_STRING), precision);

        case CoordinateType.BNG:
            if( prefix.equals(GeoTransConstants.TARGET_PREFIX) ) {        
                return new BNGCoordinates(projectionType, precision);
            }          	
            return new BNGCoordinates(projectionType, iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.COORDINATE_STRING), precision);

        case CoordinateType.GARS:
            if( prefix.equals(GeoTransConstants.TARGET_PREFIX) ) {        
                return new GARSCoordinates(projectionType, precision);
            }          	
            return new GARSCoordinates(projectionType, iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.COORDINATE_STRING), precision);

        case CoordinateType.GEOREF:
            if( prefix.equals(GeoTransConstants.TARGET_PREFIX) ) {        
                return new GEOREFCoordinates(projectionType, precision);
            }         	
            return new GEOREFCoordinates(projectionType, iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.COORDINATE_STRING), precision);

        default:
            throw new CoordinateConversionException("Invalid string coordinate type");
        }
    }

    private JSONObject buildJSONOutput(StringCoordinates stringCoordinates) throws CoordinateConversionException, JSONException {

        JSONObject jsonToReturn = new JSONObject();
        jsonToReturn.put(GeoTransConstants.COORDINATE_STRING, stringCoordinates.getCoordinateString());

        return jsonToReturn;
    }
}