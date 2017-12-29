package mil.nga.ods.geotrans.parameters;

import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.StringToVal;
import geotrans3.parameters.PolarStereographicScaleFactorParameters;
import geotrans3.utility.Constants;
import mil.nga.ods.geotrans.utils.GeoTransConstants;
import mil.nga.ods.geotrans.utils.InputVerifier;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolarStereographicScaleFactorParamWrapper extends CoordinateSystemParamWrapper {

    private static final Logger log = LoggerFactory.getLogger(PolarStereographicScaleFactorParamWrapper.class.getName());

    public PolarStereographicScaleFactorParamWrapper(String prefix, Map<String, Object> input) throws CoordinateConversionException {
        log.debug("Entering PolarStereographicScaleFactorParamWrapper");

        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();

        setParameters(new PolarStereographicScaleFactorParameters(
                stringToVal.stringToInt( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.COORDINATE_TYPE) ),
                stringToVal.stringToLongitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.CENTRAL_MERIDIAN) ) * Constants.PI_OVER_180,
                stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.SCALE_FACTOR) ),
                iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.HEMISPHERE).charAt(0),
                stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.FALSE_EASTING) ),
                stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.FALSE_NORTHING) )));        	

        log.debug("Leaving PolarStereographicScaleFactorParamWrapper");
    }
}