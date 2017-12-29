package mil.nga.ods.geotrans.utils;

import geotrans3.coordinates.Accuracy;
import geotrans3.coordinates.ConvertResults;
import geotrans3.coordinates.CoordinateTuple;
import geotrans3.enumerations.DatumType;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.jni.JNICoordinateConversionService;
import geotrans3.jni.JNIDatumLibrary;
import geotrans3.jni.JNIEllipsoidLibrary;
import geotrans3.misc.StringToVal;
import geotrans3.parameters.CoordinateSystemParameters;
import geotrans3.utility.Constants;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of static, synchronized methods for executing a geospatial
 * conversion involving at least one User-Defined Datum.
 * 
 * @since BAG SP6
 */
public class CustomConversion {

    private static final Logger log = LoggerFactory.getLogger(CustomConversion.class.getName());

    /**
     * Utility method for reading the custom datum inputs required for a
     * creating a User-Defined Datum, and for defining that datum via the
     * GeoTrans JNI service. This method will write to either the 3_param.dat or
     * 7_param.dat file located under the /data directory of the GeoTrans
     * utility installation. As a result of writing to a file, this method is
     * defined as synchronized static, so that only one thread of the JVM will
     * be able to execute this method at any given time to prevent overwriting
     * changes from other threads.
     * 
     * @param jniDatumLibrary
     *            Instance of the JNI library required for creating a new datum.
     * @param datumCode
     *            Code to create the new custom datum under. E.g., SRC for
     *            source or TGT for target.
     * @param ellipsoidCode
     *            Code of a pre-existing ellipsoid that this custom datum will
     *            reference.
     * @param prefix
     *            Prefix for retrieving the user-defined datum parameters from
     *            the request input.
     * @param inputFields
     *            The request input, containing all fields needed for the custom
     *            conversion.
     * @throws CoordinateConversionException
     * @since BAG SP6
     */
    private synchronized static void createDatum(JNIDatumLibrary jniDatumLibrary, String datumCode, String ellipsoidCode, String prefix,
            Map<String, Object> input) throws CoordinateConversionException {
        Object[] toLog = { prefix, datumCode, ellipsoidCode };
        log.debug("Entering createDatum() with prefix {}, datumCode {}, ellipsoidCode {}", toLog);

        double rotationX = 0;
        double rotationY = 0;
        double rotationZ = 0;
        double scaleFactor = 0;
        double sigmaX = -1;
        double sigmaY = -1;
        double sigmaZ = -1;
        double westLon = 0;
        double eastLon = 0;
        double southLat = 0;
        double northLat = 0;

        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();

        // These are common to both Datum types
        int datumType = stringToVal.stringToInt( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_TYPE) );
        double deltaX = stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_DELTA_X) );
        double deltaY = stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_DELTA_Y) );
        double deltaZ = stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_DELTA_Z) );

        log.debug("Switching on datum type {}", datumType);
        switch (datumType) {
        case DatumType.threeParamDatum:
            log.debug("Found 3 Parameter Datum Type");

            westLon = stringToVal.stringToLongitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_WESTERN_LONGITUDE) )
                    * Constants.PI_OVER_180;
            eastLon = stringToVal.stringToLongitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_EASTERN_LONGITUDE) )
                    * Constants.PI_OVER_180;
            southLat = stringToVal.stringToLatitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_SOUTHERN_LATITUDE) )
                    * Constants.PI_OVER_180;
            northLat = stringToVal.stringToLatitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_NORTHERN_LATITUDE) )
                    * Constants.PI_OVER_180;
            break;

        case DatumType.sevenParamDatum:
            log.debug("Found 7 Parameter Datum Type");

            rotationX = stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_ROTATION_X) );
            rotationY = stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_ROTATION_Y) );
            rotationZ = stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_ROTATION_Z) );
            scaleFactor = stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.DATUM_SCALE_FACTOR) );
            break;

        default:
            throw new CoordinateConversionException("Invalid Datum Type!");
        }

        jniDatumLibrary.defineDatum(datumType, datumCode, "TempName", ellipsoidCode, deltaX, deltaY, deltaZ, sigmaX, sigmaY, sigmaZ, westLon,
                eastLon, southLat, northLat, rotationX, rotationY, rotationZ, scaleFactor);

        log.debug("Leaving createDatum()");
    }

    /**
     * Utility for reading the custom ellipsoid inputs for creating a
     * User-Defined Ellipsoid, and for defining that ellipsoid via the GeoTrans
     * JNI service. This method will write to ellips.dat file located under the
     * /data directory of the GeoTrans utility installation. As a result of
     * writing to a file, this method is defined as synchronized static, so that
     * only one thread of the JVM will be able to execute this method at any
     * given time to prevent overwriting changes from other threads.
     * 
     * @param jniEllipsoidLibrary
     *            Instance of the JNI library required for creating a new
     *            ellipsoid.
     * @param ellipsoidCode
     *            Code to create the new custom ellipsoid under. E.g, SC for
     *            source, TG for target.
     * @param prefix
     *            Prefix for retrieving the user-defined ellipsoid parameters
     *            from the request input.
     * @param inputFields
     *            The request input, containing all fields needed for the custom
     *            conversion.
     * @throws CoordinateConversionException
     * @since BAG SP6
     */
    private synchronized static void createEllipsoid(JNIEllipsoidLibrary jniEllipsoidLibrary, String ellipsoidCode, String prefix,
            Map<String, Object> input) throws CoordinateConversionException {
        log.debug("Entering createEllipsoid() with prefix {}, ellipsoidCode {}", prefix, ellipsoidCode);

        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();

        jniEllipsoidLibrary.defineEllipsoid(
                ellipsoidCode,
                "TempName",
                stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.ELLIPSOID_AXIS) ),
                1.0 / stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.ELLIPSOID_FLATTENING) ));

        log.debug("Leaving createEllipsoid()");
    }

    /**
     * Main method for performing a geospatial conversion involving at least one
     * User-Defined Datum. The procedure for performing a custom conversion
     * involves optionally creating an ellipsoid for both the custom source
     * datum and custom target datum, creating a custom source and / or target
     * datum, executing the conversion with the custom datum(s), deleting the
     * custom datum(s), then deleting the custom ellipsoid(s) if created. <br>
     * As this is a static method, all objects needed for the conversion are
     * passed in as parameters from the client.
     * 
     * @param sourceParams
     *            Parameters for the source coordinate system.
     * @param targetParams
     *            Parameters for the target coordinate system.
     * @param sourceCoords
     *            The source coordinate system fields.
     * @param targetCoords
     *            The target coordinate system fields.
     * @param sourceAccuracy
     *            The source accuracy fields.
     * @param targetAccuracy
     *            The target accuracy fields.
     * @param input
     *            The request input, containing all fields needed for the custom
     *            conversion.
     * @return The results of the custom conversion, either as a single object,
     *         or a tuple if a bulk conversion.
     * @throws Exception
     * @since BAG SP6
     */
    public synchronized static ConvertResults[] performCustomConversion(CoordinateSystemParameters sourceParams,
            CoordinateSystemParameters targetParams, CoordinateTuple[] sourceCoords, CoordinateTuple targetCoords, Accuracy sourceAccuracy,
            Accuracy targetAccuracy, Map<String, Object> input) throws Exception {
        log.debug("Entering performCustomConversion()");

        ConvertResults[] results = new ConvertResults[sourceCoords.length];
        InputVerifier iv = new InputVerifier();

        // Initialize CCS with known datum to access the ellipsoid library,
        // datum library JNI classes.
        JNICoordinateConversionService jniCoordinateConversionService = new JNICoordinateConversionService(GeoTransConstants.WGS84_DATUM_CODE,
                sourceParams, GeoTransConstants.WGS84_DATUM_CODE, targetParams);

        JNIEllipsoidLibrary jniEllipsoidLibrary = new JNIEllipsoidLibrary(jniCoordinateConversionService.getEllipsoidLibrary());
        JNIDatumLibrary jniDatumLibrary = new JNIDatumLibrary(jniCoordinateConversionService.getDatumLibrary());

        String sourceDatum = iv.verifyInputStringIsValid(input, GeoTransConstants.SOURCE_PREFIX + GeoTransConstants.DATUM);
        String targetDatum = iv.verifyInputStringIsValid(input, GeoTransConstants.TARGET_PREFIX + GeoTransConstants.DATUM);
        String sourceEllipsoidCode = new String();
        String targetEllipsoidCode = new String();

        boolean isSourceUsingCustomDatum = sourceDatum.equalsIgnoreCase(GeoTransConstants.USER_DEFINED_DATUM_CODE);
        boolean isSourceUsingCustomEllipsoid = false;
        boolean isSourceDatumCreated = false;
        boolean isSourceEllipsoidCreated = false;

        boolean isTargetUsingCustomDatum = targetDatum.equalsIgnoreCase(GeoTransConstants.USER_DEFINED_DATUM_CODE);
        boolean isTargetUsingCustomEllipsoid = false;
        boolean isTargetDatumCreated = false;
        boolean isTargetEllipsoidCreated = false;

        try {
            if (isSourceUsingCustomDatum) {
                log.debug("Using custom datum for source.");

                sourceDatum = "SRC";
                sourceEllipsoidCode = iv.verifyInputStringIsValid(input, GeoTransConstants.SOURCE_PREFIX + GeoTransConstants.DATUM_ELLIPSOID_CODE);
                isSourceUsingCustomEllipsoid = sourceEllipsoidCode.equalsIgnoreCase(GeoTransConstants.USER_DEFINED_ELLIPSOID_CODE);

                if (isSourceUsingCustomEllipsoid) {
                    log.debug("Using custom ellipsoid for source custom datum.");

                    sourceEllipsoidCode = "SC";
                    createEllipsoid(jniEllipsoidLibrary, sourceEllipsoidCode, GeoTransConstants.SOURCE_PREFIX, input);
                    isSourceEllipsoidCreated = true;
                }

                createDatum(jniDatumLibrary, sourceDatum, sourceEllipsoidCode, GeoTransConstants.SOURCE_PREFIX, input);
                isSourceDatumCreated = true;
            }

            if (isTargetUsingCustomDatum) {
                log.debug("Using custom datum for target.");

                targetDatum = "TGT";
                targetEllipsoidCode =  iv.verifyInputStringIsValid(input, GeoTransConstants.TARGET_PREFIX + GeoTransConstants.DATUM_ELLIPSOID_CODE);
                isTargetUsingCustomEllipsoid = targetEllipsoidCode.equalsIgnoreCase(GeoTransConstants.USER_DEFINED_ELLIPSOID_CODE);

                if (isTargetUsingCustomEllipsoid) {
                    log.debug("Using custom ellipsoid for target custom datum.");

                    targetEllipsoidCode = "TG";
                    createEllipsoid(jniEllipsoidLibrary, targetEllipsoidCode, GeoTransConstants.TARGET_PREFIX, input);
                    isTargetEllipsoidCreated = true;
                }

                createDatum(jniDatumLibrary, targetDatum, targetEllipsoidCode, GeoTransConstants.TARGET_PREFIX, input);
                isTargetDatumCreated = true;
            }

            // Redefine CCS with newly created datum(s)
            jniCoordinateConversionService = new JNICoordinateConversionService(sourceDatum, sourceParams, targetDatum, targetParams);

            for (int i = 0; i < sourceCoords.length; i++) {
                results[i] = jniCoordinateConversionService.convertSourceToTarget(sourceCoords[i], sourceAccuracy, targetCoords, targetAccuracy);
            }
        } finally {
            if (isSourceDatumCreated) {
                log.debug("Deleting custom datum for source.");
                jniDatumLibrary.removeDatum(sourceDatum);
            }
            if (isSourceEllipsoidCreated) {
                log.debug("Deleting custom ellipsoid for source custom datum.");
                jniEllipsoidLibrary.removeEllipsoid(sourceEllipsoidCode);
            }

            if (isTargetDatumCreated) {
                log.debug("Deleting custom datum for target.");
                jniDatumLibrary.removeDatum(targetDatum);
            }
            if (isTargetEllipsoidCreated) {
                log.debug("Deleting custom ellipsoid for target custom datum.");
                jniEllipsoidLibrary.removeEllipsoid(targetEllipsoidCode);
            }
        }

        log.debug("Leaving performCustomConversion() with {} custom conversion result(s)", results.length);
        return results;
    }
}