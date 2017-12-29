package mil.nga.ods.geotrans.parameters;

import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.StringToVal;
import geotrans3.parameters.LocalCartesianParameters;
import geotrans3.utility.Constants;
import mil.nga.ods.geotrans.utils.GeoTransConstants;
import mil.nga.ods.geotrans.utils.InputVerifier;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalCartesianParamWrapper extends CoordinateSystemParamWrapper {

    private static final Logger log = LoggerFactory.getLogger(LocalCartesianParamWrapper.class.getName());

    public LocalCartesianParamWrapper(String prefix, Map<String, Object> input) throws CoordinateConversionException {
        log.debug("Entering LocalCartesianParamWrapper");

        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();

        setParameters(new LocalCartesianParameters(
                stringToVal.stringToInt( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.COORDINATE_TYPE) ),
                stringToVal.stringToLongitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.ORIGIN_LONGITUDE) ) * Constants.PI_OVER_180,
                stringToVal.stringToLatitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.ORIGIN_LATITUDE) ) * Constants.PI_OVER_180,
                stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.ORIGIN_HEIGHT) ),
                stringToVal.stringToLongitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.ORIENTATION) ) * Constants.PI_OVER_180));
        
        log.debug("Leaving LocalCartesianParamWrapper");
    }
}