package mil.nga.ods.geotrans.utils;

import geotrans3.exception.CoordinateConversionException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputVerifier {

    private static final Logger log = LoggerFactory.getLogger(InputVerifier.class.getName());

    public double verifyDoubleIsValid(double toVerify) throws CoordinateConversionException {
        if (Double.isNaN(toVerify)) {
            throw new CoordinateConversionException(toVerify + " is not a valid number!");
        }
        return toVerify;
    }

    public String verifyInputStringIsValid(Map<String, Object> input, String element) throws CoordinateConversionException {
        if (input.get(element) == null) {
            throw new CoordinateConversionException(element + " is missing!");
        }

        log.debug("Verified {} input exists with value {}", element, input.get(element).toString().trim());

        return input.get(element).toString().trim();
    }

    public Map<String,Object> convertJSONToMap(JSONObject jObj) throws JSONException {

        Map<String, Object> mapToReturn = new HashMap<String, Object>();

        String element;
        for (@SuppressWarnings("unchecked") Iterator<String> iter = jObj.keys(); iter.hasNext();) {
            element = iter.next();
            mapToReturn.put(element, jObj.get(element));
        }

        return mapToReturn;
    }
}