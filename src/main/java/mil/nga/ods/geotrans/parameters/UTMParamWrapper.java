package mil.nga.ods.geotrans.parameters;

import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.StringToVal;
import geotrans3.parameters.UTMParameters;
import mil.nga.ods.geotrans.utils.GeoTransConstants;
import mil.nga.ods.geotrans.utils.InputVerifier;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UTMParamWrapper extends CoordinateSystemParamWrapper {

    private static final Logger log = LoggerFactory.getLogger(UTMParamWrapper.class.getName());

    public UTMParamWrapper(String prefix, Map<String, Object> input) throws CoordinateConversionException {
        log.debug("Entering UTMParamWrapper");

        long zone = 0;
        int override = 0;

        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();

        if ( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.ZONE_OVERRIDE).equalsIgnoreCase("true") ) {
            override = 1;

            zone = stringToVal.stringToInt( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.ZONE_NUMBER) );

            if ((zone < 1) || (zone > 60)) {
                throw new CoordinateConversionException("Zone out of range (1-60)");
            }
        }

        setParameters(new UTMParameters(
                stringToVal.stringToInt( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.COORDINATE_TYPE) ),
                zone,
                override));

        log.debug("Leaving UTMParamWrapper");
    }
}
