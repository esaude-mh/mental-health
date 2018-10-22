package org.openmrs.module.mentalhealth;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsClassLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

public class HealthFacilities {

    protected Log log = LogFactory.getLog(getClass());

    public static void createLocationAttributeType() {
        LocationService locationService = Context.getLocationService();
        LocationAttributeType locationAttributeType = locationService.getLocationAttributeTypeByUuid("132895aa-1c88-11e8-b6fd-7395830b63f3");
        //if missing create one here
        if (locationAttributeType == null) {
            LocationAttributeType type = new LocationAttributeType();
            type.setName("Health facility code");
            type.setCreator(Context.getAuthenticatedUser());
            type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
            type.setDescription("Attribute that hold the unique code for the facility");
            type.setUuid("132895aa-1c88-11e8-b6fd-7395830b63f3");
            locationService.saveLocationAttributeType(type);
        }

    }

    public static void uploadLocations() throws Exception {
        LocationService locationService = Context.getLocationService();
        InputStream path = OpenmrsClassLoader.getInstance().getResourceAsStream("metadata/health_facilities.csv");
        String line = "";
        String cvsSplitBy = ",";
        String headLine = "";
        String health_facility_code = "";
        String facility_name = "";
        String province = "";
        String district = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(path, "UTF-8"));
            //exclude the first line as this holds the column headers
            headLine = br.readLine();
            while ((line = br.readLine()) != null) {

                String[] records = line.split(cvsSplitBy);
                health_facility_code = records[0];
                province = records[1];
                district = records[2];
                facility_name = records[3];

                if (StringUtils.isNotEmpty(facility_name)) {
                    Location location = locationService.getLocation(facility_name);
                    //check if this loaction is null, then create it in the system
                    if(location == null){
                        //create the location and associate it with respective metadata
                        Location newLocation = new Location();
                        newLocation.setName(facility_name);
                        newLocation.setCreator(Context.getAuthenticatedUser());
                        newLocation.setStateProvince(province);
                        newLocation.setCountyDistrict(district);

                        //set the facility unique code for this location
                        setLocationAttribute(health_facility_code, newLocation);
                        locationService.saveLocation(newLocation);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   private static LocationAttribute setLocationAttribute(String facilityCode, Location location) throws Exception {
        LocationService locationService = Context.getLocationService();
        LocationAttributeType locationAttributeType = locationService.getLocationAttributeTypeByUuid("132895aa-1c88-11e8-b6fd-7395830b63f3");
        LocationAttribute attribute = new LocationAttribute();

        if (StringUtils.isNotEmpty(facilityCode) && location != null) {
                attribute.setDateCreated(new Date());
                attribute.setAttributeType(locationAttributeType);
                attribute.setValue(facilityCode);
                attribute.setCreator(Context.getAuthenticatedUser());
                location.addAttribute(attribute);
        }
        else {
            throw new Exception("The MFL code for "+location.getName()+" is NOT provided");
        }
        return attribute;
    }
}
