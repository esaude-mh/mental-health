package org.openmrs.module.mentalhealth;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsClassLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HealthFacilities {

    protected Log log = LogFactory.getLog(getClass());
    public static void createLocationAttributeType() {
        LocationService locationService = Context.getLocationService();
        LocationAttributeType locationAttributeType = locationService.getLocationAttributeTypeByUuid("0f78d4fa-f875-11e8-ab37-9774005faaf6");
        //if missing create one here
        if (locationAttributeType == null) {
            LocationAttributeType type = new LocationAttributeType();
            type.setName("Facility code");
            type.setCreator(Context.getAuthenticatedUser());
            type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
            type.setDescription("Attribute that hold the unique code for the facility");
            type.setUuid("0f78d4fa-f875-11e8-ab37-9774005faaf6");
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
                health_facility_code = records[2];
                province = records[0];
                district = records[1];
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
        LocationAttributeType locationAttributeType = locationService.getLocationAttributeTypeByUuid("0f78d4fa-f875-11e8-ab37-9774005faaf6");
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


    public static void retireUnwantedLocations() {
        LocationService locationService = Context.getLocationService();
        List<String> locationUuid = Arrays.asList(
                "2131aff8-2e2a-480a-b7ab-4ac53250262b",
                "b1a8b05e-3542-4037-bbd3-998ee9c40574",
                "aff27d58-a15c-49a6-9beb-d30dcfc0c66e",
                "7fdfa2cb-bc95-405a-88c6-32b7673c0453",
                "58c57d25-8d39-41ab-8422-108a0c277d98",
                "7f65d926-57d6-4402-ae10-a5b3bcbf7986",
                "6351fcf4-e311-4a19-90f9-35667d99a8af"
        );
        for(String s:locationUuid){
            Location location = locationService.getLocationByUuid(s);
            if(location != null && !location.isRetired()){
                location.setRetired(true);
                location.setRetireReason("Not needed");
                location.setRetiredBy(Context.getAuthenticatedUser());
                location.setDateRetired(new Date());
                locationService.saveLocation(location);
            }
        }
    }

    public static void retireUnWantedUsers() {
        UserService userService = Context.getUserService();
        ProviderService providerService = Context.getProviderService();
        List<String> usersUuids = Arrays.asList(
                "doctor",
                "nurse",
                "clerk",
                "sysadmin"
        );
        for(String us:usersUuids){
            User user = userService.getUserByUsername(us);
            if(user != null && !user.isRetired()) {
                user.setRetired(true);
                user.setRetireReason("Not needed");
                user.setRetiredBy(Context.getAuthenticatedUser());
                user.setDateRetired(new Date());
                //save the user
                userService.retireUser(user, "Retiring");
            }
        }

        for(String pr:usersUuids){
            Provider provider = providerService.getProviderByIdentifier(pr);
            if(provider != null && !provider.isRetired()) {
                System.out.println("The provider is :::"+provider.getIdentifier());
                provider.setRetired(true);
                provider.setRetireReason("Not needed");
                provider.setRetiredBy(Context.getAuthenticatedUser());
                provider.setDateRetired(new Date());
                //save the user
                providerService.retireProvider(provider, "Retiring");
            }
        }
    }
}
