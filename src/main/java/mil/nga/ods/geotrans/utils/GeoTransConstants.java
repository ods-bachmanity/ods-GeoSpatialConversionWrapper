package mil.nga.ods.geotrans.utils;

/**
 * This constants file holds the Strings used as JSON and File element names for
 * the incoming and outgoing GeoTransService JSON and File requests.
 * 
 * @since BAG SP6
 */
public class GeoTransConstants {

    // Format Options
    public static final String GEODETIC_SEPARATOR = "geodeticSeparator";
    public static final String GEODETIC_UNITS = "geodeticUnits";
    public static final String LEADING_ZEROS = "leadingZeros";
    public static final String LONGITUDE_RANGE = "lonRange";
    public static final String PRECISION = "precision";
    public static final String SIGN_HEMISPHERE = "signHemisphere";

    // Input field prefixes
    public static final String INPUT_PREFIX = "input";
    public static final String SOURCE_PREFIX = "source";
    public static final String TARGET_PREFIX = "target";

    // Input Parameters, prefixed with source or target
    public static final String ACCURACY_CE = "AccuracyCE";
    public static final String ACCURACY_LE = "AccuracyLE";
    public static final String ACCURACY_SE = "AccuracySE";
    public static final String CENTRAL_MERIDIAN = "CentralMeridian";
    public static final String COORDINATE_TYPE = "CoordinateType";
    public static final String COORDINATES = "Coordinates";
    public static final String DATUM = "Datum";
    public static final String FALSE_EASTING = "FalseEasting";
    public static final String FALSE_NORTHING = "FalseNorthing";
    public static final String HEIGHT_TYPE = "HeightType";
    public static final String HEMISPHERE = "Hemisphere";
    public static final String LATITUDE_ONE = "Latitude1";
    public static final String LATITUDE_TWO = "Latitude2";
    public static final String LONGITUDE_ONE = "Longitude1";
    public static final String LONGITUDE_TWO = "Longitude2";
    public static final String NEYS_STANDARD_PARALLEL1 = "NeysStandardParallel1";
    public static final String ORIENTATION = "Orientation";
    public static final String ORIGIN_HEIGHT = "OriginHeight";
    public static final String ORIGIN_LATITUDE = "OriginLatitude";
    public static final String ORIGIN_LONGITUDE = "OriginLongitude";
    public static final String SCALE_FACTOR = "ScaleFactor";
    public static final String STANDARD_PARALLEL = "StandardParallel";
    public static final String STANDARD_PARALLEL_ONE = "1stStandardParallel";
    public static final String STANDARD_PARALLEL_TWO = "2ndStandardParallel";

    // Input Parameters, prefixed with source or target, for custom datum shift
    public static final String DATUM_TYPE = "DatumType";
    public static final String DATUM_DELTA_X = "DatumDeltaX";
    public static final String DATUM_DELTA_Y = "DatumDeltaY";
    public static final String DATUM_DELTA_Z = "DatumDeltaZ";
    public static final String DATUM_EASTERN_LONGITUDE = "DatumEasternLongitude";
    public static final String DATUM_ELLIPSOID_CODE = "DatumEllipsoidCode";
    public static final String DATUM_NORTHERN_LATITUDE = "DatumNorthernLatitude";
    public static final String DATUM_ROTATION_X = "DatumRotationX";
    public static final String DATUM_ROTATION_Y = "DatumRotationY";
    public static final String DATUM_ROTATION_Z = "DatumRotationZ";
    public static final String DATUM_SCALE_FACTOR = "DatumScaleFactor";
    public static final String DATUM_SOUTHERN_LATITUDE = "DatumSouthernLatitude";
    public static final String DATUM_WESTERN_LONGITUDE = "DatumWesternLongitude";

    public static final String ELLIPSOID_AXIS = "EllipsoidAxis";
    public static final String ELLIPSOID_FLATTENING = "EllipsoidFlattening";

    // Coordinates; prefixed on input, not on output.
    public static final String COORDINATE_STRING = "CoordinateString";
    public static final String EASTING = "Easting";
    public static final String HEIGHT = "Height";
    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";
    public static final String NORTHING = "Northing";
    public static final String X_VALUE = "X";
    public static final String Y_VALUE = "Y";
    public static final String Z_VALUE = "Z";
    public static final String ZONE_OVERRIDE = "Zone";
    public static final String ZONE_NUMBER = "ZoneData";

    public static final String END_OF_HEADER = "END OF HEADER";
    public static final String COMMA_SPACE = ", ";

    // Coordinate Translation types
    public static final int LATITUDE_COORDINATE = 0;
    public static final int LONGITUDE_COORDINATE = 1;

    // Datum Codes
    public static final String WGS84_DATUM_CODE = "WGE";
    public static final String USER_DEFINED_DATUM_CODE = "UDD";
    public static final String USER_DEFINED_ELLIPSOID_CODE = "UDE";
}