package mil.nga.ods.geotrans.coordinates;

import geotrans3.coordinates.ConvertResults;
import geotrans3.coordinates.GeodeticCoordinates;
import geotrans3.enumerations.CoordinateType;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.FormatOptions;
import geotrans3.misc.StringToVal;
import geotrans3.utility.Constants;
import mil.nga.ods.geotrans.utils.GeoTransConstants;
import mil.nga.ods.geotrans.utils.InputVerifier;
import mil.nga.ods.geotrans.utils.StringToRawVal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeodeticCoordinatesArray extends CoordinatesArray {

    private static final Logger log = LoggerFactory.getLogger(GeodeticCoordinatesArray.class.getName());

    // Single Results input
    public GeodeticCoordinatesArray(GeodeticCoordinates coords) {
        log.debug("Entering GeodeticCoordinatesArray(GeodeticCoordinates)");

        setCoordinates(new GeodeticCoordinates[] { coords });

        log.debug("Entering GeodeticCoordinatesArray(GeodeticCoordinates)");
    }

    // Bulk Results input
    public GeodeticCoordinatesArray(ConvertResults[] results) {
        log.debug("Entering GeodeticCoordinatesArray(ConvertResults[])");

        GeodeticCoordinates[] coordArray = new GeodeticCoordinates[results.length];

        for (int i = 0; i < results.length; i++) {
            coordArray[i] = (GeodeticCoordinates) results[i].getCoordinateTuple();
        }

        setCoordinates(coordArray);

        log.debug("Leaving GeodeticCoordinatesArray(ConvertResults[])");
    }

    // File input
    public GeodeticCoordinatesArray(ArrayList<String> coords, int projectionType) throws CoordinateConversionException {
        log.debug("Entering GeodeticCoordinatesArray(ArrayList<String>, int)");

        GeodeticCoordinates[] coordinates = new GeodeticCoordinates[coords.size()];
        Iterator<String> iter = coords.iterator();

        String coordinateTuple[];
        StringToVal stringToVal = new StringToVal();

        for (int i = 0; iter.hasNext(); i++) {
            coordinateTuple = iter.next().split(",");

            coordinates[i] = new GeodeticCoordinates(projectionType,
                    stringToVal.stringToLongitude(coordinateTuple[0].trim()) * Constants.PI_OVER_180,
                    stringToVal.stringToLatitude(coordinateTuple[1].trim()) * Constants.PI_OVER_180,
                    stringToVal.stringToDouble(coordinateTuple[2].trim()));
        }

        setCoordinates(coordinates);

        log.debug("Leaving GeodeticCoordinatesArray(ArrayList<String>, int)");
    }

    // Json input
    public GeodeticCoordinatesArray(String prefix, Map<String, Object> input) throws CoordinateConversionException, JSONException {
        log.debug("Entering GeodeticCoordinatesArray(String, Map<String, Object>)");

        InputVerifier iv = new InputVerifier();
        GeodeticCoordinates[] coordsToSet;

        if( input.get(prefix + GeoTransConstants.COORDINATES) != null) {
            JSONArray sourceCoords = new JSONArray(input.get(prefix + GeoTransConstants.COORDINATES).toString());

            coordsToSet = new GeodeticCoordinates[sourceCoords.length()];

            for( int i = 0; i < sourceCoords.length(); i++ ) {
                coordsToSet[i] = buildFromMap( iv.convertJSONToMap(sourceCoords.getJSONObject(i)), prefix);
            }
        }
        else {
            coordsToSet = new GeodeticCoordinates[1];
            coordsToSet[0] = buildFromMap( input, prefix );
        }

        setCoordinates(coordsToSet);

        log.debug("Leaving GeodeticCoordinatesArray(String, Map<String, Object>)");
    }

    // Json output
    public JSONObject toJson(FormatOptions format) throws JSONException, CoordinateConversionException {
        log.debug("Entering toJson(FormatOptions)");

        GeodeticCoordinates[] geodeticCoordinates = (GeodeticCoordinates[]) getCoordinates();
        JSONObject jsonToReturn = new JSONObject();
        JSONArray arrayToBuild = new JSONArray();

        if( geodeticCoordinates.length > 1 ) {
            for( int i = 0; i < geodeticCoordinates.length; i++ ) {
                arrayToBuild = arrayToBuild.put( buildJSONOutput( geodeticCoordinates[i], format ) );
            }
            jsonToReturn.put(GeoTransConstants.COORDINATES,  arrayToBuild);
        }
        else {
            jsonToReturn = buildJSONOutput( geodeticCoordinates[0], format );
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

        GeodeticCoordinates[] coords = (GeodeticCoordinates[]) getCoordinates();

        StringToRawVal stringToRawVal = new StringToRawVal(format.getRange(), format.getLeadingZeros(), format.getSeparator());

        for (int i = 0; i < coords.length; i++) {
            writer.write(stringToRawVal.longitudeToString(BigDecimal.valueOf(iv.verifyDoubleIsValid(coords[i].getLongitude() * Constants._180_OVER_PI)),
                    format.getUseNSEW(), format.getUseMinutes(), format.getUseSeconds()));
            writer.write(GeoTransConstants.COMMA_SPACE);
            writer.write(stringToRawVal.latitudeToString(BigDecimal.valueOf(iv.verifyDoubleIsValid(coords[i].getLatitude() * Constants._180_OVER_PI)),
                    format.getUseNSEW(), format.getUseMinutes(), format.getUseSeconds()));
            writer.write(GeoTransConstants.COMMA_SPACE);
            writer.write(stringToRawVal.doubleToString( iv.verifyDoubleIsValid(coords[i].getHeight() )));
            writer.newLine();
        }

        writer.close();

        log.debug("Leaving toString(FormatOptions)");
        return sw.toString();
    }

    private GeodeticCoordinates buildFromMap(Map<String,Object> input, String prefix) throws CoordinateConversionException {
        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();
        
        if( prefix.equals(GeoTransConstants.TARGET_PREFIX) ) {
	        return new GeodeticCoordinates(CoordinateType.GEODETIC);
        }
        else {
            return new GeodeticCoordinates(
                    CoordinateType.GEODETIC,
                    stringToVal.stringToLongitude(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.LONGITUDE)) * Constants.PI_OVER_180,
                    stringToVal.stringToLatitude(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.LATITUDE)) * Constants.PI_OVER_180,
                    stringToVal.stringToDouble(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.HEIGHT)));        	
        }
    }

    private JSONObject buildJSONOutput(GeodeticCoordinates geodeticCoordinates, FormatOptions format) throws CoordinateConversionException, JSONException {
        InputVerifier iv = new InputVerifier();
        StringToRawVal stringToRawVal = new StringToRawVal(format.getRange(), format.getLeadingZeros(), format.getSeparator());

        JSONObject jsonToReturn = new JSONObject();
        boolean useNSEW = format.getUseNSEW();
        boolean useMinutes = format.getUseMinutes();
        boolean useSeconds = format.getUseSeconds();

        jsonToReturn.put(GeoTransConstants.LONGITUDE,
                stringToRawVal.longitudeToString(
                        BigDecimal.valueOf(iv.verifyDoubleIsValid(geodeticCoordinates.getLongitude() * Constants._180_OVER_PI) ),
                        useNSEW,
                        useMinutes,
                        useSeconds));

        jsonToReturn.put(GeoTransConstants.LATITUDE,
                stringToRawVal.latitudeToString(
                        BigDecimal.valueOf( iv.verifyDoubleIsValid(geodeticCoordinates.getLatitude() * Constants._180_OVER_PI) ),
                        useNSEW,
                        useMinutes,
                        useSeconds));

        jsonToReturn.put(GeoTransConstants.HEIGHT, stringToRawVal.doubleToString( iv.verifyDoubleIsValid(geodeticCoordinates.getHeight() )));

        return jsonToReturn;
    }
}