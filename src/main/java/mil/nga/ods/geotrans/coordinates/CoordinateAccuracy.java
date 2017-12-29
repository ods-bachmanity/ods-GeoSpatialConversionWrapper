package mil.nga.ods.geotrans.coordinates;

import geotrans3.coordinates.Accuracy;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.StringToVal;
import mil.nga.ods.geotrans.utils.GeoTransConstants;

import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoordinateAccuracy extends Accuracy {

    private static final Logger log = LoggerFactory.getLogger(CoordinateAccuracy.class.getName());

    private Accuracy accuracy;

    public CoordinateAccuracy(String prefix, Map<String, Object> input) throws CoordinateConversionException {
        log.debug("Entering retrieveAccuracy() with: {}", prefix);

        StringToVal stringToVal = new StringToVal();

        // All accuracies default to Unknown.
        double ce90 = -1.0;
        double le90 = -1.0;
        double se90 = -1.0;

        if (input.get(prefix + GeoTransConstants.ACCURACY_CE) != null) {
            String ce90String = input.get(prefix + GeoTransConstants.ACCURACY_CE).toString().trim();

            if (!(ce90String.equals("Unk")) && !(ce90String.equals("N/A"))) {
                ce90 = stringToVal.stringToDouble(ce90String);
            }
        }

        if (input.get(prefix + GeoTransConstants.ACCURACY_LE) != null) {
            String le90String = input.get(prefix + GeoTransConstants.ACCURACY_LE).toString().trim();

            if (!(le90String.equals("Unk")) && !(le90String.equals("N/A"))) {
                le90 = stringToVal.stringToDouble(le90String);
            }
        }

        if (input.get(prefix + GeoTransConstants.ACCURACY_SE) != null) {
            String se90String = input.get(prefix + GeoTransConstants.ACCURACY_SE).toString().trim();

            if (!(se90String.equals("Unk")) && !(se90String.equals("N/A"))) {
                se90 = stringToVal.stringToDouble(se90String);
            }
        }

        setAccuracy(new Accuracy(ce90, le90, se90));

        log.debug("Leaving retrieveAccuracy() with ce: {}, le: {}, se: {}", ce90, le90, se90);
    }

    public CoordinateAccuracy(Accuracy a) {
        setAccuracy(a);
    }

    private void setAccuracy(Accuracy a) {
        accuracy = a;
    }

    public Accuracy getAccuracy() {
        return accuracy;
    }

    public JSONObject appendToJson(JSONObject jsonToReturn) throws JSONException {

        jsonToReturn.put(GeoTransConstants.ACCURACY_CE, accuracy.getCE90());
        jsonToReturn.put(GeoTransConstants.ACCURACY_LE, accuracy.getLE90());
        jsonToReturn.put(GeoTransConstants.ACCURACY_SE, accuracy.getSE90());

        return jsonToReturn;
    }
}