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
import java.util.ArrayList;
import java.util.Collection;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.feature.FeatureId;
import org.vast.ogc.gml.GMLBuilders;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.om.SamplingSurface;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLMetadataBuilders.CIResponsiblePartyBuilder;
import org.vast.sensorML.helper.CommonIdentifiers;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;
import org.vast.swe.helper.RasterHelper;
import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.swe.v20.Count;
import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.ScalarComponent;


public class Pleiades
{
    public static final String PHR_PROC_UID = "urn:x-cnes:sat:phr";
    public static final String PHR1A_SYS_UID = "urn:x-cnes:sat:phr:1a";
    public static final String PHR1B_SYS_UID = "urn:x-cnes:sat:phr:1b";
    public static final String HIRI_PROC_UID = "urn:x-cnes:ins:hiri";
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    
    
    static void addResources() throws IOException
    {
        // add Pleiades HR specs
        Api.addOrUpdateProcedure(createPHRSpecs(), true);
        
        // add PHR satellite instances
        for (var sys: getPHRInstances())
        {
            Api.addOrUpdateSystem(sys, true);
            Api.addOrUpdateDataStream(createImageDataStream(sys), true);
            
            for (int i = 1; i <= 10; i++)
            {
                var ts = Instant.parse("2023-09-15T15:36:24Z").toEpochMilli() + i * 32000;
                Api.addOrUpdateSF(sys.getUniqueIdentifier(), createFootprintSf(sys, i, ts), true);
            }
        }
    }
    
    
    static AbstractProcess createPHRSpecs()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_PLATFORM)
            .uniqueID(PHR_PROC_UID)
            .name("Pleiades-HR")
            .description("Pléiades is the successor of the SPOT program, with a focus on cost reduction, "
                + "technological innovation, user services and performance upgrades. The constellation provides "
                + "global coverage and daily observation accessibility to any point on Earth in several modes "
                + "of operation. Pléiades satellites are based on the agile Astrosat-1000 platform and fitted with "
                + "CNES high-resolution imager (HiRI) developed by Thales Alenia Space (TAS-F). The instrument's "
                + "prime objective is to provide  high-resolution multispectral imagery with high geo-location "
                + "accuracy. It offers several types of acquisition modes to fulfil various application fields "
                + "of cartography, agriculture, forestry, hydrology, geological prospecting, dynamic geology, "
                + "risk management and defence.")
            
            .addIdentifier(sml.identifiers.shortName("Pleiades-HR"))
            .addIdentifier(sml.identifiers.longName("Pleiades-HR Satellite Platform"))
            .addIdentifier(sml.identifiers.author("CNES"))
            
            .addCharacteristicList("physical", sml.createCharacteristicList()
                .label("Physical Characteristics")
                .add("weight", sml.characteristics.mass(970, "kg"))
            )
            
            .addCharacteristicList("orbit", sml.createCharacteristicList()
                .label("Orbit Characteristics")
                .add("type", sml.createCategory()
                    .definition(SWEHelper.getPropertyUri("OrbitType"))
                    .label("Orbit Type")
                    .value("Sun-synchronous"))
                .add("height", sml.characteristics.height(694, "km"))
                .add("inclination", sml.createQuantity()
                    .definition(SWEHelper.getPropertyUri("OrbitInclination"))
                    .label("Inclination")
                    .uomCode("deg")
                    .value(98.2))
                .add("repeat_cycle", sml.createQuantity()
                    .definition(SWEHelper.getPropertyUri("OrbitRepeatCycle"))
                    .label("Repeat Cycle")
                    .uomCode("d")
                    .value(26))
            )
            
            .addCapabilityList("pointing_specs", sml.capabilities.systemCapabilities()
                .label("Pointing Capabilities")
                .add("roll_range", sml.capabilities.pointingRange(-30, 30, "deg")
                    .label("Across-Track Pointing Range (Roll)"))
                .add("pitch_range", sml.capabilities.pointingRange(-30, 30, "deg")
                    .label("Along-Track Pointing Range (Pitch)"))
                .add("maneuver_speed", sml.createQuantity()
                    .definition(GeoPosHelper.DEF_ANGULAR_VELOCITY)
                    .label("Maneuver Speed")
                    .uom("deg/s")
                    .value(2.0))
            )
            
            .addCapabilityList("imaging_specs", sml.capabilities.systemCapabilities()
                .label("Imaging Capabilities")
                .add("swath_width", sml.createQuantity()
                    .definition(SWEHelper.getPropertyUri("SwathWidth"))
                    .label("Swath Width")
                    .description("Swath width at nadir")
                    .uom("km")
                    .value(20.0))
                .add("res_pan", sml.createQuantity()
                    .definition(SWEHelper.getPropertyUri("GroundSamplingDistance"))
                    .label("Spatial Resolution, GSD (PAN)")
                    .uom("m")
                    .value(0.7))
                .add("res_ms", sml.createQuantity()
                    .definition(SWEHelper.getPropertyUri("GroundSamplingDistance"))
                    .label("Spatial Resolution, GSD (MS)")
                    .uom("m")
                    .value(2.8))
            )
            
            .addContact(SpotSat.getCnesContactInfo()
                .role(CommonIdentifiers.AUTHOR_DEF)
            )
            .addContact(SpotSat.getAirbusContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Pléiades EO-Portal Page")
                .description("Pleiades page with specs on eo-portal website")
                .url("https://www.eoportal.org/satellite-missions/pleiades")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Pléiades Wikipedia Page")
                .url("https://en.wikipedia.org/wiki/Pleiades_(satellite)")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.PHOTO_DEF, sml.createDocument()
                .name("Photo")
                .url("https://www.eoportal.org/api/cms/documents/163813/5980730/Pleiades_Auto37.jpeg")
                .mediaType("image/jpg")
            )
            
            .addLocalReferenceFrame(sml.createSpatialFrame()
                .id("PLATFORM_FRAME")
                .label("Platform Frame")
                .description("Local reference frame attached to the satellite platform")
                .origin("Center of mass of the satellite")
                .addAxis("X", "In the plane formed by the solar panels, along the solar panel #1, pointing outward")
                .addAxis("Y", "Orthogonal to both X and Z, forming a right handed frame")
                .addAxis("Z", "Along the telescope line of sight, pointing from the optical center outward")
            )
            
            .addComponent("HiRI", createHIRISpecs())
            
            .build();
    }
    
    
    static AbstractProcess createHIRISpecs()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(HIRI_PROC_UID)
            .name("Pleiades High Resolution Imager")
            .description("HiRI captures images in one panchromatic (PAN) and four multispectral bands: blue, green, "
                + "red and near-infrared. The PAN band has a spatial resolution of 0.7 m whilst the multispectral "
                + "bands have a spatial resolution of 2.8 m. The swath width for each is 20 km at nadir.")
            
            .addIdentifier(sml.identifiers.shortName("HiRI"))
            .addIdentifier(sml.identifiers.longName("Pleiades High Resolution Imager"))
            .addClassifier(sml.classifiers.sensorType("Optical Pushbroom Imager"))
            
            .addInput("radiance", sml.createObservableProperty()
                .definition(SWEHelper.getQudtUri("Radiance"))
                .label("Earth Radiance")
                .build()
            )
            
            .addCharacteristicList("physical", sml.createCharacteristicList()
                .label("Physical Characteristics")
                .add("weight", sml.characteristics.mass(195, "kg"))
                .add("length", sml.characteristics.length(1594, "mm"))
                .add("width", sml.characteristics.width(980, "mm"))
                .add("height", sml.characteristics.height(2235, "mm"))
            )
            
            .addCapabilityList("optical_specs", sml.capabilities.systemCapabilities()
                .label("Optical Capabilities")
                .add("resolution", sml.capabilities.resolution(12, "bit"))
                .add("focal", sml.capabilities.focalLength(12.905, "m"))
                .add("fov", sml.capabilities.fov(1.65, "deg"))
            )
            
            .addCapabilityList("pan_specs", sml.capabilities.systemCapabilities()
                .label("Panchromatic Band Capabilities")
                .add("spectal_range", sml.createQuantityRange()
                    .definition(SWEHelper.getQudtUri("Wavelength"))
                    .label("Spectral Range")
                    .uom("nm")
                    .value(480, 820))
                .add("snr", sml.capabilities.snr(147))
            )
            
            .addCapabilityList("band0_specs", sml.capabilities.systemCapabilities()
                .label("MS Band 0 Capabilities (blue)")
                .add("spectal_range", sml.createQuantityRange()
                    .definition(SWEHelper.getQudtUri("Wavelength"))
                    .label("Spectral Range")
                    .uom("nm")
                    .value(450, 530))
                .add("snr", sml.capabilities.snr(130))
            )
            
            .addCapabilityList("band1_specs", sml.capabilities.systemCapabilities()
                .label("MS Band 1 Capabilities (green)")
                .add("spectal_range", sml.createQuantityRange()
                    .definition(SWEHelper.getQudtUri("Wavelength"))
                    .label("Spectral Range")
                    .uom("nm")
                    .value(510, 590))
                .add("snr", sml.capabilities.snr(130))
            )
            
            .addCapabilityList("band2_specs", sml.capabilities.systemCapabilities()
                .label("MS Band 2 Capabilities (red)")
                .add("spectal_range", sml.createQuantityRange()
                    .definition(SWEHelper.getQudtUri("Wavelength"))
                    .label("Spectral Range")
                    .uom("nm")
                    .value(620, 700))
                .add("snr", sml.capabilities.snr(130))
            )
            
            .addCapabilityList("band3_specs", sml.capabilities.systemCapabilities()
                .label("MS Band 3 Capabilities (NIR)")
                .add("spectal_range", sml.createQuantityRange()
                    .definition(SWEHelper.getQudtUri("Wavelength"))
                    .label("Spectral Range")
                    .uom("nm")
                    .value(775, 915))
                .add("snr", sml.capabilities.snr(130))
            )
            
            .addContact(getThalesContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .build();
    }
    
    
    static Collection<AbstractProcess> getPHRInstances()
    {
        var list = new ArrayList<AbstractProcess>(100);
        
        list.add(sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_PLATFORM)
            .uniqueID(PHR1A_SYS_UID)
            .name("Pléiades-1A Satellite")
            .description("First satellite of the Pléiades-HR constellation developped by CNES.")
            .typeOf(PHR_PROC_UID)
            .addIdentifier(sml.identifiers.shortName("Pléiades-1A"))
            .addIdentifier(sml.identifiers.operator("CNES"))
            .addIdentifier(sml.createTerm()
                .definition(SWEHelper.getDBpediaUri("International_Designator"))
                .label("International Designator (COSPAR ID)")
                .value("2011-076F")
            )
            .addIdentifier(sml.createTerm()
                .definition(SWEHelper.getDBpediaUri("Satellite_Catalog_Number"))
                .label("Satellite Catalog Number (SATCAT, NORAD)")
                .value("38012")
            )
            .addContact(SpotSat.getCnesContactInfo()
                .role(CommonIdentifiers.OPERATOR_DEF))
            .validFrom(OffsetDateTime.parse("2011-12-17T02:03:00Z"))
            .build());
        
        list.add(sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_PLATFORM)
            .uniqueID(PHR1B_SYS_UID)
            .name("Pléiades-1B Satellite")
            .description("Second satellite of the Pléiades-HR constellation developped by CNES.")
            .typeOf(PHR_PROC_UID)
            .addIdentifier(sml.identifiers.shortName("Pléiades-1B"))
            .addIdentifier(sml.identifiers.operator("CNES"))
            .addIdentifier(sml.createTerm()
                .definition(SWEHelper.getDBpediaUri("International_Designator"))
                .label("International Designator (COSPAR ID)")
                .value("2012-068A")
            )
            .addIdentifier(sml.createTerm()
                .definition(SWEHelper.getDBpediaUri("Satellite_Catalog_Number"))
                .label("Satellite Catalog Number (SATCAT/NORAD)")
                .value("39019")
            )
            .addContact(SpotSat.getCnesContactInfo()
                .role(CommonIdentifiers.OPERATOR_DEF))
            .validFrom(OffsetDateTime.parse("2012-12-02T02:02:00Z"))
            .build());
        
        return list;
    }
    
    
    static CIResponsiblePartyBuilder getThalesContactInfo()
    {
        return sml.createContact()
            .organisationName("Thales Alenia Space")
            .website("https://www.thalesgroup.com")
            .deliveryPoint("26 Av. Jean François Champollion")
            .city("Toulouse")
            .postalCode("31100")
            .country("France")
            .phone("+ 33 (0)5 34 35 36 37");
    }
    
    
    static IDataStreamInfo createImageDataStream(AbstractProcess sys)
    {
        var raster = new RasterHelper();
        Count width, height;
        
        // spectral bands
        ScalarComponent blue = raster.createCount()
            .name("blue")
            .definition(RasterHelper.DEF_BLUE_CHANNEL)
            .label("Blue Channel")
            .dataType(DataType.USHORT)
            .build();
        
        ScalarComponent green = raster.createCount()
            .name("green")
            .definition(RasterHelper.DEF_GREEN_CHANNEL)
            .label("Green Channel")
            .dataType(DataType.USHORT)
            .build();
        
        ScalarComponent red = raster.createCount()
            .name("red")
            .definition(RasterHelper.DEF_RED_CHANNEL)
            .label("Red Channel")
            .dataType(DataType.USHORT)
            .build();
        
        ScalarComponent nir = raster.createCount()
            .name("nir")
            .definition("http://sensorml.com/ont/swe/spectrum/NIR")
            .label("NIR Channel")
            .dataType(DataType.USHORT)
            .build();
        
        return new DataStreamInfo.Builder()
            .withSystem(new FeatureId(BigId.NONE, sys.getUniqueIdentifier()))
            .withName(sys.getName() + " - Sensor Level Imagery")
            .withDescription("Sensor level imagery product (not projected or orthorectified)")
            .withRecordEncoding(sml.newTextEncoding())
            .withRecordDescription(sml.createRecord()
                .name("image")
                .label("Scene")
                .addField("time", sml.createTime().asPhenomenonTimeIsoUTC())
                .addField("width", width = sml.createCount()
                    .definition(RasterHelper.DEF_RASTER_WIDTH)
                    .label("Image Width")
                    .id("ING_WIDTH")
                    .build())
                .addField("height", height = sml.createCount()
                    .definition(RasterHelper.DEF_RASTER_HEIGHT)
                    .label("Image Height")
                    .id("IMG_HEIGHT")
                    .build())
                .addField("image", raster.newRasterImage(width, height, blue, green, red, nir))
                .build())
            .build();
    }
    
    
    static IFeature createFootprintSf(AbstractProcess sys, int num, long ts)
    {
        var sysUid = sys.getUniqueIdentifier();
        
        var c = num - 5;
        var s = PHR1A_SYS_UID.equals(sys.getUniqueIdentifier()) ? 1 : 0;
        var lonOffset = c*0.03 + s*6.5;
        var latOffset = c*0.19 - s*13.3;
        
        var sf = new SamplingSurface();
        sf.setUniqueIdentifier(sysUid + String.format(":sf%03d", num));
        sf.setName("IMG_" + (s == 1 ? "PHR1A" : "PHR1B") + "_PMS_" + ts + " Footprint");
        sf.setSampledFeature("Earth", SWEHelper.getDBpediaUri("Earth"));
        sf.setShape(new GMLBuilders().createPolygon()
            .exterior(
                -78.58+lonOffset, -0.23+latOffset,
                -78.40+lonOffset, -0.26+latOffset,
                -78.37+lonOffset, -0.07+latOffset,
                -78.55+lonOffset, -0.04+latOffset,
                -78.58+lonOffset, -0.23+latOffset
            )
            .build());
        return sf;
    }

}
