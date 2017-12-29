package mil.nga.ods.geotrans.utils;

import geotrans3.coordinates.Accuracy;
import geotrans3.coordinates.ConvertResults;
import geotrans3.coordinates.CoordinateTuple;
import geotrans3.coordinates.GeodeticCoordinates;
import geotrans3.enumerations.CoordinateType;
import geotrans3.enumerations.HeightType;
import geotrans3.enumerations.Precision;
import geotrans3.enumerations.Range;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.FormatOptions;
import geotrans3.misc.StringToVal;
import geotrans3.parameters.CoordinateSystemParameters;
import geotrans3.parameters.GeodeticParameters;
import mil.nga.ods.geotrans.coordinates.CartesianCoordinatesArray;
import mil.nga.ods.geotrans.coordinates.CoordinateAccuracy;
import mil.nga.ods.geotrans.coordinates.CoordinatesArray;
import mil.nga.ods.geotrans.coordinates.GeodeticCoordinatesArray;
import mil.nga.ods.geotrans.coordinates.MapProjectionCoordinatesArray;
import mil.nga.ods.geotrans.coordinates.StringCoordinatesArray;
import mil.nga.ods.geotrans.coordinates.UPSCoordinatesArray;
import mil.nga.ods.geotrans.coordinates.UTMCoordinatesArray;
import mil.nga.ods.geotrans.parameters.EquidistantCylindricalParamWrapper;
import mil.nga.ods.geotrans.parameters.GeodeticParamWrapper;
import mil.nga.ods.geotrans.parameters.LocalCartesianParamWrapper;
import mil.nga.ods.geotrans.parameters.MapProjection3ParamWrapper;
import mil.nga.ods.geotrans.parameters.MapProjection4ParamWrapper;
import mil.nga.ods.geotrans.parameters.MapProjection5ParamWrapper;
import mil.nga.ods.geotrans.parameters.MapProjection6ParamWrapper;
import mil.nga.ods.geotrans.parameters.MercatorScaleFactorParamWrapper;
import mil.nga.ods.geotrans.parameters.MercatorStandardParallelParamWrapper;
import mil.nga.ods.geotrans.parameters.NeysParamWrapper;
import mil.nga.ods.geotrans.parameters.ObliqueMercatorParamWrapper;
import mil.nga.ods.geotrans.parameters.PolarStereographicScaleFactorParamWrapper;
import mil.nga.ods.geotrans.parameters.PolarStereographicStandardParallelParamWrapper;
import mil.nga.ods.geotrans.parameters.UTMParamWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing the common utility methods for performing a
 * geospatial conversion and a coordinate translation for the GeoTransService
 * RESTful web service, regardless of input format.
 * 
 * 
 * @since BAG SP6
 */
public class GeoTransUtility {

    private static final Logger log = LoggerFactory.getLogger(GeoTransUtility.class.getName());

    private Map<String, Object> headerFields = new HashMap<String, Object>();
    private StringToVal stringToVal = new StringToVal();
    private InputVerifier iv = new InputVerifier();

    private ArrayList<String> projectionFields;
    private FormatOptions format;

    private boolean usingCustomDatum;
    private boolean usingDefaultTargetDatum;
    private String sourceDatum;
    private String targetDatum;

    public Map<String, Object> getHeaderFields() {
        return headerFields;
    }

    public boolean isUsingCustomDatum() {
        return usingCustomDatum;
    }

    public boolean isUsingDefaultTargetDatum() {
        return usingDefaultTargetDatum;
    }

    public String getSourceDatum() {
        return sourceDatum;
    }

    public String getTargetDatum() {
        return targetDatum;
    }

    public GeoTransUtility(JSONObject jObj) throws CoordinateConversionException, JSONException {
        log.debug("Entering GeoTransUtility(JSONObject) with: {}", jObj);

        headerFields = iv.convertJSONToMap(jObj);

        log.debug("Leaving GeoTransUtility()");
    }

    public GeoTransUtility(InputStream file) throws CoordinateConversionException, IOException {
        log.debug("Entering GeoTransUtility(InputStream) with: {}", file);

        projectionFields = new ArrayList<String>();
        String[] headerValuePair = null;
        String line = null;
        boolean headerPassed = false;

        BufferedReader reader = new BufferedReader(new InputStreamReader(file));

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#") || line.trim().length() == 0) {
                continue;
            }

            if (!headerPassed) {
                if (!line.startsWith(GeoTransConstants.END_OF_HEADER)) {
                    headerValuePair = line.split(":");
                    if (headerValuePair.length == 2) {
                        headerFields.put(headerValuePair[0].trim(), headerValuePair[1].trim());
                    }
                } else if (line.startsWith(GeoTransConstants.END_OF_HEADER)) {
                    headerPassed = true;
                }
            } else {
                projectionFields.add(line);
            }
        }

        log.debug("Leaving GeoTransUtility()");
    }

    /**
     * Method for initializing the formatting options and datum info required
     * for performing a geospatial conversion. This reads the headerFields
     * HashMap object populated by the child classes constructors.
     * 
     * @throws CoordinateConversionException
     * @since BAG SP6
     */
    public void initializeForConversion() throws CoordinateConversionException {
        log.debug("Entering initializeForConversion()");

        sourceDatum = iv.verifyInputStringIsValid(headerFields, GeoTransConstants.SOURCE_PREFIX + GeoTransConstants.DATUM);
        targetDatum = determineTargetDatum();

        usingCustomDatum = (sourceDatum.equalsIgnoreCase(GeoTransConstants.USER_DEFINED_DATUM_CODE) ||
                targetDatum.equalsIgnoreCase(GeoTransConstants.USER_DEFINED_DATUM_CODE));

        initializeFormat();

        log.debug("Leaving initializeForConversion()");
    }

    private String determineTargetDatum() {

        if (headerFields.get(GeoTransConstants.TARGET_PREFIX + GeoTransConstants.DATUM) == null) {
            usingDefaultTargetDatum = true;
            return GeoTransConstants.WGS84_DATUM_CODE;
        }

        usingDefaultTargetDatum = false;
        return headerFields.get(GeoTransConstants.TARGET_PREFIX + GeoTransConstants.DATUM).toString();
    }

    /**
     * Method for initializing the format options available for the geospatial
     * conversion result. This is a private method called only by the
     * initializeForConversion() method. This reads the headerFields HashMap
     * object populated by the child classes constructors.
     * 
     * @throws CoordinateConversionException
     * @since BAG SP6
     */
    private void initializeFormat() throws CoordinateConversionException {
        log.debug("Entering initializeFormat()");

        // Precision set to max.
        int precision = Precision.TEN_THOUSANDTH_OF_SECOND;

        // GeoDetic Separator defaults to a space (' ').
        char separator = ' ';
        if (headerFields.get(GeoTransConstants.GEODETIC_SEPARATOR) != null) {
            separator = headerFields.get(GeoTransConstants.GEODETIC_SEPARATOR).toString().charAt(0);
        }

        // Longitude Range defaults to (-180, 180)
        int longitudeRange = Range._180_180;
        if (headerFields.get(GeoTransConstants.LONGITUDE_RANGE) != null) {
            longitudeRange = stringToVal.stringToInt(headerFields.get(GeoTransConstants.LONGITUDE_RANGE).toString().trim());
        }

        // Leading Zeros defaults to false
        boolean leadingZeros = false;
        if (headerFields.get(GeoTransConstants.LEADING_ZEROS) != null) {
            leadingZeros = Boolean.parseBoolean(headerFields.get(GeoTransConstants.LEADING_ZEROS).toString().trim());
        }

        // Hemisphere indicator defaults to +/-
        int signHemi = 0;
        if (headerFields.get(GeoTransConstants.SIGN_HEMISPHERE) != null) {
            signHemi = stringToVal.stringToInt(headerFields.get(GeoTransConstants.SIGN_HEMISPHERE).toString().trim());
        }

        // Geodetic format defaults to Decimal Degrees.
        int geodeticUnits = 2;
        if (headerFields.get(GeoTransConstants.GEODETIC_UNITS) != null) {
            geodeticUnits = stringToVal.stringToInt(headerFields.get(GeoTransConstants.GEODETIC_UNITS).toString().trim());
        }

        format = new FormatOptions();
        format.setLeadingZeros(leadingZeros);
        format.setPrecision(precision);
        format.setRange(longitudeRange);
        format.setSeparator(separator);
        format.setSignHemi(signHemi);
        format.setUnits(geodeticUnits);

        log.debug("Leaving initializeFormat()");
    }

    /**
     * Utility method for constructing the source and target coordinate system
     * coordinates. These coordinates will be input to the GeoTrans JNI
     * conversion service for a geospatial conversion.
     * 
     * @param prefix
     *            The prefix of either 'source' or 'target', indicating whether
     *            to pull the data from the source or target input parameters.
     * @return The CoordinateTuple object containing the coordinate system
     *         coordinates for either the source or target coordinate system.
     * @throws CoordinateConversionException
     * @throws JSONException
     * @since BAG SP6
     */
    public CoordinateTuple[] retrieveCoordinates(String prefix, boolean isBulk) throws CoordinateConversionException, JSONException {
        log.debug("Entering retrieveCoordinates() with prefix: {}", prefix);

        if( prefix.equals(GeoTransConstants.TARGET_PREFIX) && isUsingDefaultTargetDatum() ) {
            return new GeodeticCoordinatesArray( new GeodeticCoordinates( CoordinateType.GEODETIC ) ).getCoordinates();
        }

        int projectionType = stringToVal.stringToInt( iv.verifyInputStringIsValid(headerFields, prefix + GeoTransConstants.COORDINATE_TYPE) );

        CoordinatesArray coordsArray;

        log.debug("Switching on Coordinate Type {}", projectionType);
        switch (projectionType) {
        case CoordinateType.ALBERS:
        case CoordinateType.AZIMUTHAL:
        case CoordinateType.BONNE:
        case CoordinateType.CASSINI:
        case CoordinateType.CYLEQA:
        case CoordinateType.ECKERT4:
        case CoordinateType.ECKERT6:
        case CoordinateType.EQDCYL:
        case CoordinateType.GNOMONIC:
        case CoordinateType.LAMBERT_1:
        case CoordinateType.LAMBERT_2:
        case CoordinateType.MERCATOR_SP:
        case CoordinateType.MERCATOR_SF:
        case CoordinateType.MILLER:
        case CoordinateType.MOLLWEIDE:
        case CoordinateType.NEYS:
        case CoordinateType.NZMG:
        case CoordinateType.OMERC:
        case CoordinateType.ORTHOGRAPHIC:
        case CoordinateType.POLARSTEREO_SP:
        case CoordinateType.POLARSTEREO_SF:
        case CoordinateType.POLYCONIC:
        case CoordinateType.SINUSOIDAL:
        case CoordinateType.STEREOGRAPHIC:
        case CoordinateType.TRCYLEQA:
        case CoordinateType.TRANMERC:
        case CoordinateType.GRINTEN:
            coordsArray = isBulk ? new MapProjectionCoordinatesArray(projectionFields, projectionType) : new MapProjectionCoordinatesArray(prefix,
                    headerFields, projectionType);
            break;

        case CoordinateType.USNG:
        case CoordinateType.MGRS:
        case CoordinateType.BNG:
        case CoordinateType.GARS:
        case CoordinateType.GEOREF:
        case CoordinateType.F16GRS:
            coordsArray = isBulk ? new StringCoordinatesArray(projectionFields, projectionType, format.getPrecision()) : new StringCoordinatesArray(
                    prefix, headerFields, projectionType, format.getPrecision());
            break;

        case CoordinateType.GEOCENTRIC:
        case CoordinateType.LOCCART:
            coordsArray = isBulk ? new CartesianCoordinatesArray(projectionFields, projectionType) : new CartesianCoordinatesArray(prefix, headerFields, projectionType);
            break;

        case CoordinateType.GEODETIC:
            coordsArray = isBulk ? new GeodeticCoordinatesArray(projectionFields, projectionType) : new GeodeticCoordinatesArray(prefix, headerFields);
            break;

        case CoordinateType.UPS:
            coordsArray = isBulk ? new UPSCoordinatesArray(projectionFields, projectionType) : new UPSCoordinatesArray(prefix, headerFields);
            break;

        case CoordinateType.UTM:
            coordsArray = isBulk ? new UTMCoordinatesArray(projectionFields, projectionType) : new UTMCoordinatesArray(prefix, headerFields);
            break;

        default:
            throw new CoordinateConversionException("Invalid coordinate type");
        }

        log.debug("Leaving retrieveCoordinates()");
        return coordsArray.getCoordinates();
    }

    /**
     * Utility method for constructing the source and target coordinate system
     * parameters. These parameters will be input to the GeoTrans JNI conversion
     * service for a geospatial conversion.
     * 
     * @param prefix
     *            The prefix of either 'source' or 'target', indicating whether
     *            to pull the data from the source or target input coordinates.
     * @return The CoordinateSystemParameter object containing the coordinate
     *         system parameters for either the source or target coordinate
     *         system parameters.
     * @throws CoordinateConversionException
     * @since BAG SP6
     */
    public CoordinateSystemParameters retrieveParameters(String prefix) throws CoordinateConversionException {
        log.debug("Entering retrieveParameters() with prefix: {}", prefix);

        if( prefix.equals(GeoTransConstants.TARGET_PREFIX) && isUsingDefaultTargetDatum() ) {
            return new GeodeticParameters( CoordinateType.GEODETIC, HeightType.NO_HEIGHT );
        }

        int projectionType = stringToVal.stringToInt(iv.verifyInputStringIsValid(headerFields, prefix + GeoTransConstants.COORDINATE_TYPE));

        CoordinateSystemParameters paramsToReturn;

        log.debug("Switching on Coordinate type{}", projectionType);
        switch (projectionType) {
        case CoordinateType.ECKERT4:
        case CoordinateType.ECKERT6:
        case CoordinateType.MILLER:
        case CoordinateType.MOLLWEIDE:
        case CoordinateType.SINUSOIDAL:
        case CoordinateType.GRINTEN:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new MapProjection3ParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.AZIMUTHAL:
        case CoordinateType.BONNE:
        case CoordinateType.CASSINI:
        case CoordinateType.CYLEQA:
        case CoordinateType.GNOMONIC:
        case CoordinateType.ORTHOGRAPHIC:
        case CoordinateType.POLYCONIC:
        case CoordinateType.STEREOGRAPHIC:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new MapProjection4ParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.LAMBERT_1:
        case CoordinateType.TRCYLEQA:
        case CoordinateType.TRANMERC:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new MapProjection5ParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.ALBERS:
        case CoordinateType.LAMBERT_2:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new MapProjection6ParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.EQDCYL:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new EquidistantCylindricalParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.GEODETIC:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new GeodeticParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.LOCCART:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new LocalCartesianParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.MERCATOR_SP:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new MercatorStandardParallelParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.MERCATOR_SF:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new MercatorScaleFactorParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.NEYS:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new NeysParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.OMERC:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new ObliqueMercatorParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.POLARSTEREO_SP:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new PolarStereographicStandardParallelParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.POLARSTEREO_SF:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new PolarStereographicScaleFactorParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.UTM:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = (new UTMParamWrapper(prefix, headerFields)).getParameters();
            break;

        case CoordinateType.BNG:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = new CoordinateSystemParameters(CoordinateType.BNG);
            break;

        case CoordinateType.GARS:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = new CoordinateSystemParameters(CoordinateType.GARS);
            break;

        case CoordinateType.GEOCENTRIC:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = new CoordinateSystemParameters(CoordinateType.GEOCENTRIC);
            break;

        case CoordinateType.GEOREF:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = new CoordinateSystemParameters(CoordinateType.GEOREF);
            break;

        case CoordinateType.F16GRS:
        case CoordinateType.MGRS:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = new CoordinateSystemParameters(CoordinateType.MGRS);
            break;

        case CoordinateType.NZMG:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = new CoordinateSystemParameters(CoordinateType.NZMG);
            break;

        case CoordinateType.UPS:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = new CoordinateSystemParameters(CoordinateType.UPS);
            break;

        case CoordinateType.USNG:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            paramsToReturn = new CoordinateSystemParameters(CoordinateType.USNG);
            break;

        default:
            throw new CoordinateConversionException("Input contains invalid coordinate type");
        }

        log.debug("Leaving retrieveParameters()");
        return paramsToReturn;
    }

    /**
     * Method for performing a coordinate translation, without requiring the
     * full format initialization of the geospatial conversion. This method
     * converts from either Degrees, Minutes, Seconds or Degrees, Minutes to
     * Decimal Degrees.
     * 
     * @return a String representation of the latitude or longitude value in
     *         Decimal Degrees.
     * @throws CoordinateConversionException
     * @since BAG SP6
     */
    public String translateCoordinates() throws CoordinateConversionException {
        log.debug("Entering translateCoordinates()");

        String coordinateString = iv.verifyInputStringIsValid(headerFields, GeoTransConstants.INPUT_PREFIX + GeoTransConstants.COORDINATE_STRING);

        int coordinateType = stringToVal.stringToInt(iv.verifyInputStringIsValid(headerFields, GeoTransConstants.INPUT_PREFIX + GeoTransConstants.COORDINATE_TYPE));
        int lonRangeType = 0;

        if (headerFields.containsKey(GeoTransConstants.LONGITUDE_RANGE)) {
            lonRangeType = stringToVal.stringToInt(headerFields.get(GeoTransConstants.LONGITUDE_RANGE).toString().trim());
        }

        StringToRawVal localSTRV = new StringToRawVal(lonRangeType, false, ' ');
        double coordsIntermediate = 0;
        String coordsToReturn = new String();

        if (coordinateType == GeoTransConstants.LATITUDE_COORDINATE) {
            coordsIntermediate = stringToVal.stringToLatitude(coordinateString);
            coordsToReturn = localSTRV.latitudeToString(BigDecimal.valueOf(coordsIntermediate), false, false, false);

        } else if (coordinateType == GeoTransConstants.LONGITUDE_COORDINATE) {
            coordsIntermediate = stringToVal.stringToLongitude(coordinateString);
            coordsToReturn = localSTRV.longitudeToString(BigDecimal.valueOf(coordsIntermediate), false, false, false);

        } else {
            throw new CoordinateConversionException("Invalid Coordinate Type!");
        }

        log.debug("Leaving translateCoordinates() with {}", coordsToReturn);
        return coordsToReturn;
    }

    public Object buildResponse(ConvertResults[] results, boolean isBulk) throws CoordinateConversionException, JSONException, IOException {
        log.debug("Entering buildResponse()");

        int projectionType = results[0].getCoordinateTuple().getCoordinateType();

        CoordinatesArray coords;

        switch (projectionType) {
        case CoordinateType.ALBERS:
        case CoordinateType.AZIMUTHAL:
        case CoordinateType.BONNE:
        case CoordinateType.CASSINI:
        case CoordinateType.CYLEQA:
        case CoordinateType.ECKERT4:
        case CoordinateType.ECKERT6:
        case CoordinateType.EQDCYL:
        case CoordinateType.GNOMONIC:
        case CoordinateType.LAMBERT_1:
        case CoordinateType.LAMBERT_2:
        case CoordinateType.MERCATOR_SP:
        case CoordinateType.MERCATOR_SF:
        case CoordinateType.MILLER:
        case CoordinateType.MOLLWEIDE:
        case CoordinateType.NEYS:
        case CoordinateType.NZMG:
        case CoordinateType.OMERC:
        case CoordinateType.ORTHOGRAPHIC:
        case CoordinateType.POLARSTEREO_SP:
        case CoordinateType.POLARSTEREO_SF:
        case CoordinateType.POLYCONIC:
        case CoordinateType.SINUSOIDAL:
        case CoordinateType.STEREOGRAPHIC:
        case CoordinateType.TRCYLEQA:
        case CoordinateType.TRANMERC:
        case CoordinateType.GRINTEN:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            coords = new MapProjectionCoordinatesArray(results);
            break;

        case CoordinateType.BNG:
        case CoordinateType.GARS:
        case CoordinateType.GEOREF:
        case CoordinateType.MGRS:
        case CoordinateType.USNG:
        case CoordinateType.F16GRS:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            coords = new StringCoordinatesArray(results);
            break;

        case CoordinateType.GEOCENTRIC:
        case CoordinateType.LOCCART:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            coords = new CartesianCoordinatesArray(results);
            break;

        case CoordinateType.GEODETIC:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            coords = new GeodeticCoordinatesArray(results);
            break;

        case CoordinateType.UPS:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            coords = new UPSCoordinatesArray(results);
            break;

        case CoordinateType.UTM:
            log.debug("Found type {}", CoordinateType.name(projectionType));

            coords = new UTMCoordinatesArray(results);
            break;

        default:
            throw new CoordinateConversionException("Invalid coordinate type");
        }

        Object objectToReturn;

        if (!isBulk) {
            objectToReturn = coords.toJson(format);

            if( results.length == 1 ) {
                CoordinateAccuracy coordAccuracy = new CoordinateAccuracy(results[0].getAccuracy());
                objectToReturn = coordAccuracy.appendToJson( (JSONObject)objectToReturn );
            }
        }
        else {
            objectToReturn = coords.toString(format);
        }

        log.debug("Leaving buildResponse() with {}", objectToReturn);
        return objectToReturn;
    }

    public Accuracy retrieveAccuracy(String prefix) throws CoordinateConversionException {
        log.debug("Entering retrieveAccuracy()");

        CoordinateAccuracy coordAccuracy = new CoordinateAccuracy(prefix, headerFields);

        log.debug("Leaving retrieveAccuracy()");
        return coordAccuracy.getAccuracy();
    }
}