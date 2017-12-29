package mil.nga.ods.geotrans;

import geotrans3.coordinates.Accuracy;
import geotrans3.coordinates.ConvertResults;
import geotrans3.coordinates.CoordinateTuple;
import geotrans3.enumerations.CoordinateType;
import geotrans3.enumerations.HeightType;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.jni.JNICoordinateConversionService;
import geotrans3.jni.JNIDatumLibrary;
import geotrans3.jni.JNIEllipsoidLibrary;
import geotrans3.misc.Info;
import geotrans3.parameters.CoordinateSystemParameters;
import geotrans3.parameters.GeodeticParameters;
import mil.nga.ods.geotrans.utils.CustomConversion;
import mil.nga.ods.geotrans.utils.GeoTransConstants;
import mil.nga.ods.geotrans.utils.GeoTransUtility;

import java.io.InputStream;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoTransMaster {

    private static final Logger log = LoggerFactory.getLogger(GeoTransMaster.class.getName());
    private static final String JNIMSP_LIB_NAME = "jnimsp_ccs";
    private static final String MSPDTCC_LIB_NAME = "MSPdtcc";

    static {
        log.debug("Loading C++ libraries {} and {}", MSPDTCC_LIB_NAME, JNIMSP_LIB_NAME);

        System.loadLibrary(MSPDTCC_LIB_NAME);
        System.loadLibrary(JNIMSP_LIB_NAME);

        log.debug("Native libraries loaded.");
    }

    public String doBulkConversion(InputStream fileInput) throws Exception {
        log.debug("Entering doBulkConversion()");

        return (String) assembleAndExecuteConversion(new GeoTransUtility(fileInput), true);
    }

    public JSONObject doConversion(String jsonInput) throws Exception {
        log.debug("Entering doConversion()");

        return (JSONObject) assembleAndExecuteConversion(new GeoTransUtility(new JSONObject(jsonInput)), false);
    }

    private Object assembleAndExecuteConversion(GeoTransUtility gtUtility, boolean isBulk) throws Exception {
        log.debug("Entering assembleAndExecuteConversion()");

        gtUtility.initializeForConversion();

        CoordinateSystemParameters sourceParameters = gtUtility.retrieveParameters(GeoTransConstants.SOURCE_PREFIX);
        CoordinateSystemParameters targetParameters = gtUtility.retrieveParameters(GeoTransConstants.TARGET_PREFIX);

        CoordinateTuple[] sourceTuples = gtUtility.retrieveCoordinates(GeoTransConstants.SOURCE_PREFIX, isBulk);
        CoordinateTuple targetTuple = gtUtility.retrieveCoordinates(GeoTransConstants.TARGET_PREFIX, false)[0];

        Accuracy sourceAccuracy = gtUtility.retrieveAccuracy(GeoTransConstants.SOURCE_PREFIX);
        Accuracy targetAccuracy = gtUtility.retrieveAccuracy(GeoTransConstants.TARGET_PREFIX);

        ConvertResults[] results = new ConvertResults[sourceTuples.length];

        if (gtUtility.isUsingCustomDatum()) {
            log.debug("Calling custom conversion method!");
            results = CustomConversion.performCustomConversion(sourceParameters, targetParameters, sourceTuples, targetTuple, sourceAccuracy,
                    targetAccuracy, gtUtility.getHeaderFields());
        } else {
            JNICoordinateConversionService jniCoordinateConversionService = new JNICoordinateConversionService(gtUtility.getSourceDatum(),
                    sourceParameters, gtUtility.getTargetDatum(), targetParameters);

            for (int i = 0; i < sourceTuples.length; i++) {
                results[i] = jniCoordinateConversionService.convertSourceToTarget(sourceTuples[i], sourceAccuracy, targetTuple, targetAccuracy);
            }
        }

        log.debug("Leaving assembleAndExecuteConversion()");
        return gtUtility.buildResponse(results, isBulk);
    }

    public JSONObject doCoordinateTranslation(String jsonInput) throws CoordinateConversionException, JSONException {
        log.debug("Entering doCoordinateTranslation()");

        GeoTransUtility gtUtility = new GeoTransUtility(new JSONObject(jsonInput));

        JSONObject jsonToReturn = new JSONObject();
        jsonToReturn.put(GeoTransConstants.COORDINATE_STRING, gtUtility.translateCoordinates());

        log.debug("Leaving doCoordinateTranslation()");
        return jsonToReturn;
    }

    public JSONObject retrieveAvailableDatums() throws Exception {
        CoordinateSystemParameters tmp = new GeodeticParameters( CoordinateType.GEODETIC, HeightType.NO_HEIGHT );
        JNICoordinateConversionService jniCoordinateConversionService = new JNICoordinateConversionService(GeoTransConstants.WGS84_DATUM_CODE, tmp,
                GeoTransConstants.WGS84_DATUM_CODE, tmp);
        JNIDatumLibrary jniDatumLibrary = new JNIDatumLibrary(jniCoordinateConversionService.getDatumLibrary());

        Info datumInfo;
        JSONObject currentDatum;
        JSONArray availableDatums = new JSONArray();

        for( int i = 0; i < jniDatumLibrary.getDatumCount(); i++ ) {
            datumInfo = jniDatumLibrary.getDatumInfo( i );
            currentDatum = new JSONObject();
            currentDatum.put("code",  datumInfo.getCode());
            currentDatum.put("name",  datumInfo.getName());
            availableDatums.put( currentDatum );
        }

        return new JSONObject().put("availableDatums",  availableDatums);
    }

    public JSONObject retrieveAvailableEllipsoids() throws Exception {
        CoordinateSystemParameters tmp = new GeodeticParameters( CoordinateType.GEODETIC, HeightType.NO_HEIGHT );
        JNICoordinateConversionService jniCoordinateConversionService = new JNICoordinateConversionService(GeoTransConstants.WGS84_DATUM_CODE, tmp,
                GeoTransConstants.WGS84_DATUM_CODE, tmp);
        JNIEllipsoidLibrary jniEllipsoidLibrary = new JNIEllipsoidLibrary(jniCoordinateConversionService.getEllipsoidLibrary());

        Info ellipsoidInfo;
        JSONObject currentEllipsoid;
        JSONArray availableEllipsoids = new JSONArray();

        for( int i = 0; i < jniEllipsoidLibrary.getEllipsoidCount(); i++ ) {
            ellipsoidInfo = jniEllipsoidLibrary.getEllipsoidInfo(i);
            currentEllipsoid = new JSONObject();
            currentEllipsoid.put("code",  ellipsoidInfo.getCode());
            currentEllipsoid.put("name",  ellipsoidInfo.getName().trim());
            availableEllipsoids.put( currentEllipsoid );
        }

        return new JSONObject().put("availableEllipsoids",  availableEllipsoids);
    }
}