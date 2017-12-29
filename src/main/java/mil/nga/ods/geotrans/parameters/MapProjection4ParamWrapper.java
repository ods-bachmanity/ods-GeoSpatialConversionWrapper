package mil.nga.ods.geotrans.parameters;

import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.StringToVal;
import geotrans3.parameters.MapProjection4Parameters;
import geotrans3.utility.Constants;
import mil.nga.ods.geotrans.utils.GeoTransConstants;
import mil.nga.ods.geotrans.utils.InputVerifier;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapProjection4ParamWrapper extends CoordinateSystemParamWrapper {

    private static final Logger log = LoggerFactory.getLogger(MapProjection4ParamWrapper.class.getName());

    public MapProjection4ParamWrapper(String prefix, Map<String, Object> input) throws CoordinateConversionException {
        log.debug("Entering MapProjection4ParamWrapper");

        InputVerifier iv = new InputVerifier();
        StringToVal stringToVal = new StringToVal();

        setParameters(new MapProjection4Parameters(
                stringToVal.stringToInt( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.COORDINATE_TYPE) ),
                stringToVal.stringToLongitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.CENTRAL_MERIDIAN) ) * Constants.PI_OVER_180,
                stringToVal.stringToLatitude( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.ORIGIN_LATITUDE) ) * Constants.PI_OVER_180,
                stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.FALSE_EASTING) ),
                stringToVal.stringToDouble( iv.verifyInputStringIsValid(input, prefix + GeoTransConstants.FALSE_NORTHING) )));        	

        log.debug("Leaving MapProjection4ParamWrapper");
    }
}