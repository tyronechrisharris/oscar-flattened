/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.demodata;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import org.isotc211.v2005.gmd.CIResponsibleParty;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.feature.FeatureId;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLMetadataBuilders.CIResponsiblePartyBuilder;
import org.vast.sensorML.helper.CommonIdentifiers;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;
import net.opengis.sensorml.v20.AbstractProcess;


public class LaserTech
{
    public static final String TP360_PROC_UID = "urn:x-lasertech:lrf:TP360R:v2";
    public static final String TP360_SYS_UID_PREFIX = "urn:x-nypd:lrf:";
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    
    
    static void addResources() throws IOException
    {
        // add PTZ cam datasheet
        Api.addOrUpdateProcedure(createTP360Datasheet(), true);
        
        // add camera instances
        for (var sys: getTrupulseInstances())
        {
            Api.addOrUpdateSystem(sys, true);
            Api.addOrUpdateDataStream(createLrfDataStream(sys), true);
        }
    }
    
    
    static AbstractProcess createTP360Datasheet()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(TP360_PROC_UID)
            .name("Trupulse 360°R rugged Laser Range Finder")
            .description("The TruPulse® 360°R laser rangefinder measures distance, inclination angles "
                + "and azimuth with high accuracy and target acquisition. This model offers a "
                + "horizontally hand-held waterproof rugged design for the harshest environments.")
            
            .addIdentifier(sml.identifiers.shortName("Trupulse 360°R"))
            .addIdentifier(sml.identifiers.longName("LaserTech Trupulse 360°R Laser Range Finder"))
            .addIdentifier(sml.identifiers.manufacturer("Laser Tech"))
            .addIdentifier(sml.identifiers.modelNumber("TP360R"))
            .addClassifier(sml.classifiers.sensorType("Laser Range Finder"))
            
            /*.addInput("light", sml.createObservableProperty()
                .definition(SWEHelper.getDBpediaUri("Electromagnetic_radiation"))
                .label("Visible Light")
                .build()
            )*/
            
            .validFrom(OffsetDateTime.parse("2015-03-05T00:00:00Z"))
            
            .addCharacteristicList("mech_specs", sml.createCharacteristicList()
                .label("Mechanical Characteristics")
                .add("weight", sml.characteristics.mass(285, "g"))
                .add("width", sml.characteristics.width(120, "mm"))
                .add("length", sml.characteristics.length(90, "mm"))
                .add("height", sml.characteristics.height(50, "mm"))
                .add("material", sml.characteristics.material("Plastic")
                    .label("Casing Material"))
            )
            
            .addCharacteristicList("elec_specs", sml.createCharacteristicList()
                .label("Electrical Characteristics")
                .add("bat_life", sml.characteristics.batteryLifetime(8, "h"))
                .add("bat_type", sml.createCategory()
                    .definition(SWEHelper.getDBpediaUri("Battery_types"))
                    .label("Battery Type")
                    .value("CRV3"))
                .add("if_type", sml.createText()
                    .definition(SWEHelper.getDBpediaUri("Interface_(computing)"))
                    .label("Interface Types")
                    .value("RS-232, Bluetooth"))
            )
            
            .addCapabilityList("range_caps1", sml.capabilities.systemCapabilities()
                .label("Range Measurement Capabilities (Reflective Targets)")
                .add("meas_range", sml.capabilities.measurementRange(0, 2000, "m"))
                .add("resolution", sml.capabilities.resolution(0.1, "m"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.2, "m"))
            )
            
            .addCapabilityList("range_caps2", sml.capabilities.systemCapabilities()
                .label("Range Measurement Capabilities (Non-Reflective Targets)")
                .add("meas_range", sml.capabilities.measurementRange(0, 1000, "m"))
                .add("resolution", sml.capabilities.resolution(0.1, "m"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(1, "m"))
                
            )
            
            .addCapabilityList("azim_caps", sml.capabilities.systemCapabilities()
                .label("Azimuth Measurement Capabilities")
                .add("meas_range", sml.capabilities.measurementRange(0, 360, "deg"))
                .add("resolution", sml.capabilities.resolution(0.1, "deg"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.5, "deg"))
            )
            
            .addCapabilityList("elev_caps", sml.capabilities.systemCapabilities()
                .label("Azimuth Measurement Capabilities")
                .add("meas_range", sml.capabilities.measurementRange(0, 90, "deg"))
                .add("resolution", sml.capabilities.resolution(0.1, "deg"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.25, "deg"))
            )
            
            .addCapabilityList("op_range", sml.capabilities.operatingProperties()
                .add("temperature", sml.conditions.temperatureRange(-20, 60, "Cel"))
                .add("ingress", sml.createCategory()
                    .definition(SWEHelper.getDBpediaUri("IP_code"))
                    .label("Ingress Protection")
                    .value("IP56"))
            )
            
            .addContact(getLaserTechContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Product Webpage")
                .description("Product webpage on manufacturer's website")
                .url("https://lasertech.com/product/trupulse-360-r-laser-rangefinder")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.SPECSHEET_DEF, sml.createDocument()
                .name("Datasheet")
                .url("https://lasertech.com/wp-content/uploads/1301_productsolutions_2023_e.pdf")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.USER_MANUAL_DEF, sml.createDocument()
                .name("User Manual")
                .url("https://lasertech.com/wp-content/uploads/LTI-TruPulse-360-R-UM.3.pdf")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.PHOTO_DEF, sml.createDocument()
                .name("Photo")
                .url("https://i0.wp.com/muller.sarl/wp-content/uploads/2019/09/TP360R_005.png")
                .mediaType("image/png")
            )
            
            .build();
    }
    
    
    static AbstractProcess createTrupulseInstance(String id, Instant startTime)
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(TP360_SYS_UID_PREFIX + id)
            .name("Trupulse Laser Range Finder " + id)
            .typeOf(TP360_PROC_UID)
            .addIdentifier(sml.identifiers.shortName("Trupulse Laser Range Finder " + id))
            .addContact(getOperatorContactInfo())
            .validFrom(startTime.atOffset(ZoneOffset.UTC))
            .build();
    }
    
    
    static Collection<AbstractProcess> getTrupulseInstances()
    {
        var list = new ArrayList<AbstractProcess>(100);
        
        for (int i = 0; i < 3; i++)
        {
            list.add(createTrupulseInstance(
                String.format("TP%05d", (i+10)),
                Instant.parse("2020-04-28T08:00:00Z").plus((i+1)*(i-1), ChronoUnit.DAYS)
            ));
        }
        
        return list;
    }
    
    
    static CIResponsiblePartyBuilder getLaserTechContactInfo()
    {
        return sml.createContact()
            .organisationName("Laser Technology, Inc.")
            .website("https://lasertech.com")
            .deliveryPoint("6912 South Quentin Street, Suite A")
            .city("Centennial")
            .postalCode("80112")
            .administrativeArea("CO")
            .country("USA")
            .phone("+1 877 696 2584");
    }
    
    
    static CIResponsibleParty getOperatorContactInfo()
    {
        return sml.createContact()
            .role(CommonIdentifiers.OPERATOR_DEF)
            .organisationName("City of New York - Police Department")
            .website("https://www.nyc.gov/site/nypd")
            .city("New York")
            .administrativeArea("NY")
            .country("USA")
            .build();
    }
    
    
    static IDataStreamInfo createLrfDataStream(AbstractProcess sys)
    {
        return new DataStreamInfo.Builder()
            .withSystem(new FeatureId(BigId.NONE, sys.getUniqueIdentifier()))
            .withName(sys.getName() + " - Target Location")
            .withDescription("3D spherical coordinates of targer relative to LRF location")
            .withRecordEncoding(sml.newTextEncoding())
            .withRecordDescription(sml.createRecord()
                .name("lrf_data")
                .addField("time", sml.createTime()
                    .asPhenomenonTimeIsoUTC()
                )
                .addField("range", sml.createQuantity()
                    .definition(GeoPosHelper.DEF_DISTANCE)
                    .label("Range")
                    .description("Distance to target from LRF location")
                    .uomCode("m")
                )
                .addField("azim", sml.createQuantity()
                    .definition(GeoPosHelper.DEF_AZIMUTH_ANGLE)
                    .refFrame(SWEConstants.REF_FRAME_NED)
                    .axisId("Z")
                    .label("Azimuth")
                    .description("Azimuth of target relative to LRF location, measured clockwise from north")
                    .uomCode("deg")
                )
                .addField("elev", sml.createQuantity()
                    .definition(GeoPosHelper.DEF_ELEVATION_ANGLE)
                    .refFrame(SWEConstants.REF_FRAME_NED)
                    .axisId("Y")
                    .label("Elevation")
                    .description("Elevation of target relative to LRF location, measured from local horizontal")
                    .uomCode("deg")
                )
                .build()
            )
            .build();
    }

}
