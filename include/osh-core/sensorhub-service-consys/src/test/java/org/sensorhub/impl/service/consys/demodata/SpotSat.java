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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLMetadataBuilders.CIResponsiblePartyBuilder;
import org.vast.sensorML.helper.CommonIdentifiers;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;
import net.opengis.sensorml.v20.AbstractProcess;


public class SpotSat
{
    public static final String SPOT5_PROC_UID = "urn:x-cnes:sat:spot5";
    public static final String HRG_PROC_UID = "urn:x-cnes:ins:hrg";
    
    public static final String ASTROTERRA_PROC_UID = "urn:x-airbus:sat:astroterra";
    public static final String NAOMI_PROC_UID = "urn:x-airbus:ins:naomi";
    public static final String SPOT6_SYS_UID = "urn:x-airbus:sat:astroterra:spot6";
    public static final String SPOT7_SYS_UID = "urn:x-airbus:sat:astroterra:spot7";
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    
    
    static void addResources() throws IOException
    {
        // add Astroterra specs
        Api.addOrUpdateProcedure(createAstroTerraSpecs(), true);
        
        // add satellite instances
        for (var sys: getSpotInstances())
        {
            Api.addOrUpdateSystem(sys, true);
            Api.addOrUpdateDataStream(Pleiades.createImageDataStream(sys), true);
        }
    }
    
    
    static AbstractProcess createAstroTerraSpecs()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_PLATFORM)
            .uniqueID(ASTROTERRA_PROC_UID)
            .name("SPOT 6/7 AstroTerra EO Platform")
            .description("In 2008, Spot Image of Toulouse, France, and partners (EADS Astrium) started "
                + "an initiative to build a new commercial SPOT mission series, referred to as SPOT-6/7, "
                + "to continue sustainable wide-swath high-resolution observation services as currently "
                + "provided by the SPOT-5 mission. The spacecraft features CMGs (Control Moment Gyroscopes) "
                + "instead of reaction wheels to improve pointing manoeuvrability. The satellite's in-orbit "
                + "design life was 10 years.")
            
            .addIdentifier(sml.identifiers.shortName("AstroTerra"))
            .addIdentifier(sml.identifiers.longName("SPOT 6/7 AstroTerra Earth Observation Platform"))
            
            .addCharacteristicList("physical", sml.createCharacteristicList()
                .label("Physical Characteristics")
                .add("weight", sml.characteristics.mass(712, "kg"))
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
                    .value(60.0))
                .add("res_pan", sml.createQuantity()
                    .definition(SWEHelper.getPropertyUri("GroundSamplingDistance"))
                    .label("Spatial Resolution, GSD (PAN)")
                    .uom("m")
                    .value(1.5))
                .add("res_ms", sml.createQuantity()
                    .definition(SWEHelper.getPropertyUri("GroundSamplingDistance"))
                    .label("Spatial Resolution, GSD (MS)")
                    .uom("m")
                    .value(6))
            )
            
            .addContact(getAirbusContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("SPOT-6/7 EO-Portal Page")
                .description("SPOT-6/7 page with specs on eo-portal website")
                .url("https://www.eoportal.org/satellite-missions/spot-6-7")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.SPECSHEET_DEF, sml.createDocument()
                .name("SPOT-6/7 Specsheet")
                .url("https://www.intelligence-airbusds.com/files/pmedia/edited/r18072_9_spot_6_technical_sheet.pdf")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.PHOTO_DEF, sml.createDocument()
                .name("Photo")
                .url("https://eo.belspo.be/sites/default/files/styles/xlarge/public/satellites/212._spot-6.jpeg")
                .mediaType("image/jpg")
            )
            
            .addComponent("NAOMI", createNAOMISpecs())
            
            .build();
    }
    
    
    static AbstractProcess createNAOMISpecs()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(NAOMI_PROC_UID)
            .name("New Astrosat Optical Modular Imager (NAOMI)")
            .description("NAOMI contains two identical Korsch telescopes in silicon carbide that each "
                + "deliver an aperture of 200mm and a field of regard of ±30°. Each telescope contains "
                + "a detector with a TDI (Time Delay Integration) matrix of 7000 pixels for the "
                + "panchromatic channel and four lines of 1750 pixels for the multispectral channel.")
            
            .addIdentifier(sml.identifiers.shortName("NAOMI"))
            .addIdentifier(sml.identifiers.longName("New Astrosat Optical Modular Imager"))
            .addClassifier(sml.classifiers.sensorType("Optical Pushbroom Imager"))
            
            .addInput("radiance", sml.createObservableProperty()
                .definition(SWEHelper.getQudtUri("Radiance"))
                .label("Earth Radiance")
                .build()
            )
            
            .addCharacteristicList("physical", sml.createCharacteristicList()
                .label("Physical Characteristics")
                .add("weight", sml.characteristics.mass(18.5, "kg"))
            )
            
            .addCapabilityList("optical_specs", sml.capabilities.systemCapabilities()
                .label("Optical Capabilities")
                .add("resolution", sml.capabilities.resolution(12, "bit"))
                .add("focal", sml.capabilities.focalLength(4.3, "m"))
                .add("fov", sml.capabilities.fov(4.95, "deg"))
            )
            
            .addCapabilityList("pan_specs", sml.capabilities.systemCapabilities()
                .label("Panchromatic Band Capabilities")
                .add("spectal_range", sml.createQuantityRange()
                    .definition(SWEHelper.getQudtUri("Wavelength"))
                    .label("Spectral Range")
                    .uom("nm")
                    .value(450, 745))
                .add("snr", sml.capabilities.snr(126))
            )
            
            .addCapabilityList("band0_specs", sml.capabilities.systemCapabilities()
                .label("MS Band 0 Capabilities (blue)")
                .add("spectal_range", sml.createQuantityRange()
                    .definition(SWEHelper.getQudtUri("Wavelength"))
                    .label("Spectral Range")
                    .uom("nm")
                    .value(455, 525))
                .add("snr", sml.capabilities.snr(259))
            )
            
            .addCapabilityList("band1_specs", sml.capabilities.systemCapabilities()
                .label("MS Band 1 Capabilities (green)")
                .add("spectal_range", sml.createQuantityRange()
                    .definition(SWEHelper.getQudtUri("Wavelength"))
                    .label("Spectral Range")
                    .uom("nm")
                    .value(530, 590))
                .add("snr", sml.capabilities.snr(254))
            )
            
            .addCapabilityList("band2_specs", sml.capabilities.systemCapabilities()
                .label("MS Band 2 Capabilities (red)")
                .add("spectal_range", sml.createQuantityRange()
                    .definition(SWEHelper.getQudtUri("Wavelength"))
                    .label("Spectral Range")
                    .uom("nm")
                    .value(625, 695))
                .add("snr", sml.capabilities.snr(267))
            )
            
            .addCapabilityList("band3_specs", sml.capabilities.systemCapabilities()
                .label("MS Band 3 Capabilities (NIR)")
                .add("spectal_range", sml.createQuantityRange()
                    .definition(SWEHelper.getQudtUri("Wavelength"))
                    .label("Spectral Range")
                    .uom("nm")
                    .value(760, 890))
                .add("snr", sml.capabilities.snr(293))
            )
            
            .addContact(getAirbusContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .build();
    }
    
    
    static Collection<AbstractProcess> getSpotInstances()
    {
        var list = new ArrayList<AbstractProcess>(100);
        
        list.add(sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_PLATFORM)
            .uniqueID(SPOT6_SYS_UID)
            .name("SPOT-6 Satellite")
            .typeOf(ASTROTERRA_PROC_UID)
            .addIdentifier(sml.identifiers.shortName("SPOT-6"))
            .addIdentifier(sml.identifiers.operator("Airbus Defence and Space"))
            .addIdentifier(sml.createTerm()
                .definition(SWEHelper.getDBpediaUri("International_Designator"))
                .label("International Designator (COSPAR ID)")
                .value("2012-047A")
            )
            .addIdentifier(sml.createTerm()
                .definition(SWEHelper.getDBpediaUri("Satellite_Catalog_Number"))
                .label("Satellite Catalog Number (SATCAT, NORAD)")
                .value("38755")
            )
            .addContact(getAirbusContactInfo()
                .role(CommonIdentifiers.OPERATOR_DEF))
            .validFrom(OffsetDateTime.parse("2012-09-09T04:23:00Z"))
            .build());
        
        list.add(sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_PLATFORM)
            .uniqueID(SPOT7_SYS_UID)
            .name("SPOT-7 Satellite (Azersky)")
            .typeOf(ASTROTERRA_PROC_UID)
            .addIdentifier(sml.identifiers.shortName("SPOT-7"))
            .addIdentifier(sml.identifiers.shortName("Azersky"))
            .addIdentifier(sml.identifiers.operator("Azercosmos"))
            .addIdentifier(sml.createTerm()
                .definition(SWEHelper.getDBpediaUri("International_Designator"))
                .label("International Designator (COSPAR ID)")
                .value("2014-034A")
            )
            .addIdentifier(sml.createTerm()
                .definition(SWEHelper.getDBpediaUri("Satellite_Catalog_Number"))
                .label("Satellite Catalog Number (SATCAT/NORAD)")
                .value("40053")
            )
            .addContact(getAzercosmosContactInfo()
                .role(CommonIdentifiers.OPERATOR_DEF))
            .validTimePeriod(
                OffsetDateTime.parse("2014-06-30T04:42:00Z"),
                OffsetDateTime.parse("2023-03-17T12:00:00Z"))
            .build());
        
        return list;
    }
    
    
    static CIResponsiblePartyBuilder getCnesContactInfo()
    {
        return sml.createContact()
            .organisationName("CNES - Centre Spatial de Toulouse")
            .website("https://cnes.fr")
            .deliveryPoint("18 avenue Edouard Belin")
            .city("Toulouse")
            .postalCode("31401")
            .country("France")
            .phone("+ 33 (0)5 61 27 31 31");
    }
    
    
    static CIResponsiblePartyBuilder getAirbusContactInfo()
    {
        return sml.createContact()
            .organisationName("Airbus Defence and Space")
            .website("https://www.airbus.com")
            .deliveryPoint("31 Rue des Cosmonautes")
            .city("Toulouse")
            .postalCode("31400")
            .country("France")
            .phone("+ 33 (0)5 62 19 62 19");
    }
    
    
    static CIResponsiblePartyBuilder getAzercosmosContactInfo()
    {
        return sml.createContact()
            .organisationName("Azercosmos")
            .website("http://azercosmos.az")
            .city("Baku")
            .country("Azerbaijan");
    }

}
