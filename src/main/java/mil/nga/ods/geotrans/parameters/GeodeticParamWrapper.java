package mil.nga.ods.geotrans.parameters;

import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.StringToVal;
import geotrans3.parameters.GeodeticParameters;
import mil.nga.ods.geotrans.utils.GeoTransConstants;
import mil.nga.ods.geotrans.utils.InputVerifier;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeodeticParamWrapper extends CoordinateSystemParamWrapper {

    private static final Logger log = LoggerFactory.getLogger(GeodeticParamWrapper.class.getName());

    public GeodeticParamWrapper(String prefix, Map<String, Object> input) throws CoordinateConversionException {
        log.debug("Entering GeodeticParamWrapper");

        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();

        setParameters(new GeodeticParameters(
                stringToVal.stringToInt( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.COORDINATE_TYPE) ),
                stringToVal.stringToInt( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.HEIGHT_TYPE) )));

        log.debug("Leaving GeodeticParamWrapper");
    }
}