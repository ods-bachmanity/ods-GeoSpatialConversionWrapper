package mil.nga.ods.geotrans.parameters;

import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.StringToVal;
import geotrans3.parameters.NeysParameters;
import geotrans3.utility.Constants;
import mil.nga.ods.geotrans.utils.GeoTransConstants;
import mil.nga.ods.geotrans.utils.InputVerifier;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeysParamWrapper extends CoordinateSystemParamWrapper {

    private static final Logger log = LoggerFactory.getLogger(NeysParamWrapper.class.getName());

    public NeysParamWrapper(String prefix, Map<String, Object> input) throws CoordinateConversionException {
        log.debug("Entering NeysParamWrapper");

        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();

        int neysStandardParallel = stringToVal.stringToInt(iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.NEYS_STANDARD_PARALLEL1));
        double std_par_1 = 71.0;

        if (neysStandardParallel == 71) {
            std_par_1 = 71.0;
        } else if (neysStandardParallel == 74) {
            std_par_1 = 74.0;
        }

        setParameters(new NeysParameters(
                stringToVal.stringToInt( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.COORDINATE_TYPE) ),
                stringToVal.stringToLongitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.CENTRAL_MERIDIAN) ) * Constants.PI_OVER_180,
                stringToVal.stringToLatitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.ORIGIN_LATITUDE) ) * Constants.PI_OVER_180,
                std_par_1 * Constants.PI_OVER_180,
                stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.FALSE_EASTING) ),
                stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix +  GeoTransConstants.FALSE_NORTHING) )));        	

        log.debug("Leaving NeysParamWrapper");
    }
}