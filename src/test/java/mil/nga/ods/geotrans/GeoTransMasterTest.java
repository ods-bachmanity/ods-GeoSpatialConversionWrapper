package mil.nga.ods.geotrans;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

public class GeoTransMasterTest {
	
	private GeoTransMaster geoTransMaster;
	
	@Before
	public void setUp() {
		geoTransMaster = new GeoTransMaster();
	}
	
	@Test
	public void getAvailableDatums_ShouldReturnJSON() throws Exception {
		JSONObject jsonDatumsResult;
		
		jsonDatumsResult = geoTransMaster.retrieveAvailableDatums();
		
		assertNotNull(jsonDatumsResult);
		
		if (jsonDatumsResult != null) {
			System.out.println("\ngetAvailableDatums output:");
			System.out.println(jsonDatumsResult.toString());
		}
	}
	
	@Test
	public void getAvailableEllipsoids_ShouldReturnJSON() throws Exception {
		JSONObject jsonEllipsoidResult;
		
		jsonEllipsoidResult = geoTransMaster.retrieveAvailableEllipsoids();
		
		assertNotNull(jsonEllipsoidResult);
		
		if (jsonEllipsoidResult != null) {
			System.out.println("\ngetAvailableEllipsoids output:");
			System.out.println(jsonEllipsoidResult.toString());
		}
	}
	
	@Test
	public void getJsonOutput_ShouldReturnJSON() throws Exception {
		String jsonStr = "";
		try {
			jsonStr = FileUtils.readFileToString(new File("test\\inputs\\Test_multi.json"));
			System.out.println("\nJSON input from file:");
			System.out.println(jsonStr);
			
			JSONObject json = new JSONObject(jsonStr);
			System.out.println("\nJSON input as String:");
			System.out.println(json.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JSONObject jsonResult;
		
		jsonResult = geoTransMaster.doConversion(jsonStr);
		assertNotNull(jsonResult);
		
		if (jsonResult != null) {
			System.out.println("\ndoConversion output:");
			System.out.println(jsonResult.toString());
		}
	}
	
	/* Commented out, doBulkConversion not working correctly yet.
	@Test
	public void getStringOutput_ShouldReturnString() throws Exception {
		// String stringResult = geoTransMaster.doBulkConversion(new FileInputStream("test\\inputs\\Lat_Lon.csv"));
		
		String stringResult = geoTransMaster.doBulkConversion(new FileInputStream("test\\inputs\\BatchTest.csv"));
		
		assertNotNull(stringResult);
		
		if (stringResult != null) {
			System.out.println(stringResult);
		}
	}
	*/
}