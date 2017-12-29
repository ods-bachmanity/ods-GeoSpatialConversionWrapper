package mil.nga.ods.geotrans.utils;

import geotrans3.enumerations.Range;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides formatting routines for converting the raw geodetic coordinate
 * output from the GeoTrans utility into either Degrees Minutes Seconds format,
 * Degrees Minutes format, or Decimal Degrees. All output is rounded to a
 * maximum of 12 significant digits.
 * 
 * @since BAG SP6
 */
public class StringToRawVal {

    private static final int LATITUDE_STRING = 1;
    private static final int LONGITUDE_STRING = 2;
    private static final BigDecimal NUM_10 = BigDecimal.TEN;
    private static final BigDecimal NUM_60 = new BigDecimal("60");
    private static final BigDecimal NUM_100 = new BigDecimal("100");
    private static final BigDecimal NUM_180 = new BigDecimal("180");
    private static final BigDecimal NUM_360 = new BigDecimal("360");

    private static final MathContext MC_EXACT = new MathContext(38, RoundingMode.HALF_UP);
    private static final MathContext MC_FINAL = new MathContext(12, RoundingMode.HALF_UP);

    private int lonRange;
    private boolean leadingZeros;

    private char latLonSeparator;

    private static final Logger log = LoggerFactory.getLogger(StringToRawVal.class.getName());

    /**
     * Constructor for the StringToRawVal class. This initializes the formatting
     * options available for the geodetic coordinate output.
     * 
     * @param longitudeRange
     *            The option for selecting which longitude range to format the
     *            output. 0 is for (-180, 180), 1 is for (0, 360).
     * @param leadingZ
     *            The option for displaying leading zeros on the output.
     * @param separator
     *            The option for selecting which separator to use for DMS and DM
     *            formatted output.
     * @see geotrans3.enumerations.Range
     * @since BAG SP6
     */
    public StringToRawVal(int longitudeRange, boolean leadingZ, char separator) {
        log.debug("Entering StringToRawVal() with longitude range: {}, leading zeros: {}, and geodetic separator: '{}'", longitudeRange, leadingZ,
                separator);

        lonRange = longitudeRange;
        leadingZeros = leadingZ;
        latLonSeparator = separator;

        log.debug("Leaving StringToRawVal()");
    }

    /**
     * Private method for converting a latitude or longitude value into the
     * desired format, using the BigDecimal class for all mathematical
     * operations to maintain the values output from the GeoTrans utility.
     * 
     * @param degreesBD
     *            The raw value of a latitude or longitude.
     * @param useMinutes
     *            Option for including Minutes in the result format.
     * @param useSeconds
     *            Option for including Seconds in the result format.
     * @param type
     *            Flag to indicate whether input is a latitude (1) or longitude
     *            (2)
     * @return String representation of the latitude or longitude per the
     *         specified formatting options.
     * @since BAG SP6
     */
    private String degreesToString(BigDecimal degreesBD, boolean useMinutes, boolean useSeconds, int type) {
        Object[] toLog = { degreesBD, useMinutes, useSeconds, type };
        log.debug("Entering degreesToString() with {} degrees, useMinutes: {}, useSeconds: {}, lat(1) or long(2): {}", toLog);

        BigDecimal minutesBD = BigDecimal.ZERO;
        BigDecimal secondsBD = BigDecimal.ZERO;
        int degreesInt = 0;
        int minutesInt = 0;
        int secondsInt = 0;
        String degreesString = "";
        String minutesString = "";
        String secondsString = "";

        String valueToReturn = "";

        // Decimal Degrees
        if (!useMinutes) {
            if (leadingZeros) {
                if (type == LATITUDE_STRING) {
                    if (degreesBD.abs(MC_EXACT).compareTo(NUM_10) == -1) {
                        degreesString = "0" + degreesBD.toPlainString();
                    } else {
                        degreesString = degreesBD.toPlainString();
                    }
                } else {
                    if (degreesBD.abs(MC_EXACT).compareTo(NUM_10) == -1) {
                        degreesString = "00" + degreesBD.toPlainString();
                    } else if (degreesBD.abs(MC_EXACT).compareTo(NUM_100) == -1) {
                        degreesString = "0" + degreesBD.toPlainString();
                    } else {
                        degreesString = degreesBD.toPlainString();
                    }
                }
            } else {
                degreesString = degreesBD.toPlainString();
            }

            valueToReturn = degreesString;
        }
        // Degrees & Minutes
        else if (useMinutes && !useSeconds) {
            degreesInt = degreesBD.intValue();
            minutesBD = (degreesBD.subtract(new BigDecimal(degreesInt), MC_EXACT)).multiply(NUM_60, MC_EXACT);
            minutesBD = minutesBD.round(MC_FINAL);
            minutesInt = minutesBD.intValue();

            if (minutesInt >= 60) {
                minutesBD = minutesBD.subtract(NUM_60, MC_EXACT);
                degreesInt += 1;
            }

            // Only display 12 digits after decimal if less than 1.
            if (minutesBD.compareTo(BigDecimal.ONE) == -1 && minutesBD.scale() > MC_FINAL.getPrecision()) {
                minutesBD = minutesBD.setScale(MC_FINAL.getPrecision(), MC_FINAL.getRoundingMode());
            }

            if (leadingZeros) {
                if (type == LATITUDE_STRING) {
                    if (Math.abs(degreesInt) < 10) {
                        degreesString = "0" + Integer.toString(degreesInt) + latLonSeparator;
                    } else {
                        degreesString = Integer.toString(degreesInt) + latLonSeparator;
                    }
                } else {
                    if (Math.abs(degreesInt) < 10) {
                        degreesString = "00" + Integer.toString(degreesInt) + latLonSeparator;
                    } else if (Math.abs(degreesInt) < 100) {
                        degreesString = "0" + Integer.toString(degreesInt) + latLonSeparator;
                    } else {
                        degreesString = Integer.toString(degreesInt) + latLonSeparator;
                    }
                }

                if (minutesInt < 10) {
                    minutesString = "0" + minutesBD.toPlainString();
                } else {
                    minutesString = minutesBD.toPlainString();
                }
            } else {
                degreesString = Integer.toString(degreesInt) + latLonSeparator;
                minutesString = minutesBD.toPlainString();
            }

            valueToReturn = degreesString + minutesString;
        }
        // Degrees, Minutes, & Seconds
        else {
            degreesInt = degreesBD.intValue();

            minutesBD = (degreesBD.subtract(new BigDecimal(degreesInt), MC_EXACT)).multiply(NUM_60, MC_EXACT);
            minutesInt = minutesBD.intValue();

            secondsBD = (minutesBD.subtract(new BigDecimal(minutesInt), MC_EXACT)).multiply(NUM_60, MC_EXACT);
            secondsBD = secondsBD.round(MC_FINAL);
            secondsInt = secondsBD.intValue();

            if (secondsInt >= 60) {
                secondsBD = secondsBD.subtract(NUM_60, MC_EXACT);
                minutesInt += 1;

                if (minutesInt >= 60) {
                    degreesInt += 1;
                    minutesInt -= 60;
                }
            }

            // Only display 12 digits after decimal if less than 1.
            if (secondsBD.compareTo(BigDecimal.ONE) == -1 && secondsBD.scale() > MC_FINAL.getPrecision()) {
                secondsBD = secondsBD.setScale(MC_FINAL.getPrecision(), MC_FINAL.getRoundingMode());
            }

            if (leadingZeros) {
                if (type == LATITUDE_STRING) {
                    if (Math.abs(degreesInt) < 10) {
                        degreesString = "0" + Long.toString(degreesInt) + latLonSeparator;
                    } else {
                        degreesString = Long.toString(degreesInt) + latLonSeparator;
                    }
                } else {
                    if (Math.abs(degreesInt) < 10) {
                        degreesString = "00" + Long.toString(degreesInt) + latLonSeparator;
                    } else if (Math.abs(degreesInt) < 100) {
                        degreesString = "0" + Long.toString(degreesInt) + latLonSeparator;
                    } else {
                        degreesString = Long.toString(degreesInt) + latLonSeparator;
                    }
                }

                if (minutesInt < 10) {
                    minutesString = "0" + Long.toString(minutesInt) + latLonSeparator;
                } else {
                    minutesString = Long.toString(minutesInt) + latLonSeparator;
                }

                if (secondsInt < 10) {
                    secondsString = "0" + secondsBD.toPlainString();
                } else {
                    secondsString = secondsBD.toPlainString();
                }
            } else {
                degreesString = Long.toString(degreesInt) + latLonSeparator;
                minutesString = Long.toString(minutesInt) + latLonSeparator;
                secondsString = secondsBD.toPlainString();
            }

            valueToReturn = degreesString + minutesString + secondsString;
        }

        log.debug("Leaving degreesToString() with {}", valueToReturn);
        return valueToReturn;
    }

    /**
     * Utility method for converting a double value into a String for returning
     * to the client.
     * 
     * @param value
     *            Value to convert to a String.
     * @return Initial value in Plain String form, not in exponent notation.
     * @since BAG SP6
     */
    public String doubleToString(final double value) {
        log.debug("Entering doubleToString() with {}", value);

        BigDecimal exact = new BigDecimal(String.valueOf(value));
        String exactToReturn = exact.toPlainString();

        log.debug("Leaving doubleToString() with {}", exactToReturn);
        return exactToReturn;
    }

    /**
     * Utility method for converting an input latitude value into the desired
     * format.
     * 
     * @param latitude
     *            Raw value of the latitude.
     * @param useNSEW
     *            Option for using the North N or South S notation for output.
     *            If false, uses + and - for North and South, respectively.
     * @param useMinutes
     *            Option for including Minutes in the result format.
     * @param useSeconds
     *            Option for including Seconds in the result format.
     * @return Latitude formatted per the specified options.
     * @since BAG SP6
     */
    public String latitudeToString(BigDecimal latitude, boolean useNSEW, boolean useMinutes, boolean useSeconds) {
        Object[] toLog = { latitude, useNSEW, useMinutes, useSeconds };
        log.debug("Entering latitudeToString() with {} latitude, useNSEW: {}, useMinutes: {}, useSeconds: {}", toLog);

        BigDecimal degrees = latitude.abs(MC_EXACT);
        String degreesAsString = degreesToString(degrees, useMinutes, useSeconds, LATITUDE_STRING);

        if (useNSEW) {
            if (latitude.compareTo(BigDecimal.ZERO) == -1) {
                degreesAsString += 'S';
            } else {
                degreesAsString += 'N';
            }
        } else {
            if (latitude.compareTo(BigDecimal.ZERO) == -1) {
                String temp = degreesAsString;
                degreesAsString = '-' + temp;
            }
        }

        log.debug("Leaving latitudeToString() with {}", degreesAsString);
        return degreesAsString;
    }

    /**
     * Utility method for converting an input longitude value into the desired
     * format.
     * 
     * @param longitude
     *            Raw value of the longitude
     * @param useNSEW
     *            Option for using the East E or West W notation for output. If
     *            false, uses + and - for East and West, respectively.
     * @param useMinutes
     *            Option for including Minutes in the result format.
     * @param useSeconds
     *            Option for including Seconds in the result format.
     * @return Longitude formatted per the specified options.
     * @since BAG SP6
     */
    public String longitudeToString(BigDecimal longitude, boolean useNSEW, boolean useMinutes, boolean useSeconds) {
        Object[] toLog = { longitude, useNSEW, useMinutes, useSeconds };
        log.debug("Entering longitudeToString() with {} longitude, useNSEW: {}, useMinutes: {}, useSeconds: {}", toLog);

        BigDecimal degrees = BigDecimal.ZERO;

        log.debug("Switching on {}", lonRange);
        switch (lonRange) {
        case Range._180_180:
            if (longitude.compareTo(NUM_180) == 1) {
                degrees = longitude.subtract(NUM_360, MC_EXACT).abs(MC_EXACT);
            } else {
                degrees = longitude.abs(MC_EXACT);
            }
            break;

        case Range._0_360:
            if (longitude.compareTo(BigDecimal.ZERO) == -1) {
                degrees = longitude.add(NUM_360, MC_EXACT);
            } else {
                degrees = longitude;
            }
            break;
        }

        String degreesAsString = degreesToString(degrees, useMinutes, useSeconds, LONGITUDE_STRING);

        switch (lonRange) {
        case Range._180_180:
            if (useNSEW) {
                if ((longitude.compareTo(NUM_180) == 1) || (longitude.compareTo(BigDecimal.ZERO) == -1)) {
                    degreesAsString += 'W';
                } else {
                    degreesAsString += 'E';
                }
            } else {
                if ((longitude.compareTo(NUM_180) == 1) || (longitude.compareTo(BigDecimal.ZERO) == -1)) {
                    String temp = degreesAsString;
                    degreesAsString = '-' + temp;
                }
            }
            break;

        case Range._0_360:
            if (useNSEW) {
                degreesAsString += 'E';
            }
            break;
        }

        log.debug("Leaving longitudeToString() with {}", degreesAsString);
        return degreesAsString;
    }
}