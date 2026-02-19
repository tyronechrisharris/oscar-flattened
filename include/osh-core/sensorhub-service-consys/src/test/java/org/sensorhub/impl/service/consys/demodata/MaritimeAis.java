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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jglue.fluentjson.JsonArrayBuilder;
import org.jglue.fluentjson.JsonBuilderFactory;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.feature.FeatureId;
import org.vast.sensorML.SMLBuilders.PhysicalSystemBuilder;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.helper.CommonIdentifiers;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.opengis.sensorml.v20.AbstractProcess;


public class MaritimeAis
{
    static final String TEST_FILE1 = "/media/NAS/Media/Data/AIS/AIS_2020_01_01.csv";

    public static final String AIS_PROC_UID = "urn:mrn:iala:systems:ais";
    public static final String VTS_SYS_UID_PREFIX = "urn:mrn:iala:vts:";
    public static final String AIS_DENMARK_SYS_UID = "urn:mrn:iala:vts:dk:ais";
    public static final String AIS_US_SYS_UID = "urn:mrn:iala:vts:us:ais";
    
    static Map<String,String> VESSEL_TYPES = ImmutableMap.<String, String>builder()
        .put("30", "Fishing")
        .put("31", "Towing")
        .put("32", "Towing (Large)")
        .put("33", "Dredging or Underwater Ops")
        .put("34", "Diving Ops")
        .put("35", "Military Ops")
        .put("36", "Sailing")
        .put("37", "Pleasure Craft")
        .put("40", "High speed craft (HSC)")
        .put("50", "Pilot Vessel")
        .put("51", "Search and Rescue Vessel")
        .put("52", "Tug")
        .put("53", "Port Tender")
        .put("54", "Anti-pollution Equipment")
        .put("55", "Law Enforcement")
        .put("56", "Spare - Local Vessel")
        .put("57", "Spare - Local Vessel")
        .put("58", "Medical Transport")
        .put("60", "Passenger")
        .put("61", "Passenger, Hazardous Cat A")
        .put("62", "Passenger, Hazardous Cat B")
        .put("63", "Passenger, Hazardous Cat C")
        .put("64", "Passenger, Hazardous Cat D")
        .put("65", "Passenger")
        .put("66", "Passenger")
        .put("67", "Passenger")
        .put("68", "Passenger")
        .put("69", "Passenger")
        .put("70", "Cargo")
        .put("71", "Cargo, Hazardous Cat A")
        .put("72", "Cargo, Hazardous Cat B")
        .put("73", "Cargo, Hazardous Cat C")
        .put("74", "Cargo, Hazardous Cat D")
        .put("75", "Cargo")
        .put("76", "Cargo")
        .put("77", "Cargo")
        .put("78", "Cargo")
        .put("79", "Cargo")
        .put("80", "Tanker")
        .put("81", "Tanker, Hazardous Cat A")
        .put("82", "Tanker, Hazardous Cat B")
        .put("83", "Tanker, Hazardous Cat C")
        .put("84", "Tanker, Hazardous Cat D")
        .put("85", "Tanker")
        .put("86", "Tanker")
        .put("87", "Tanker")
        .put("88", "Tanker")
        .put("89", "Tanker")
        .build();
    
    static Map<String,String> STATUS_CODES = ImmutableMap.<String, String>builder()
        .put("0", "Under way using engine")
        .put("1", "At anchor")
        .put("2", "Not under command")
        .put("3", "Restricted manoeuverability")
        .put("4", "Constrained by her draught")
        .put("5", "Moored")
        .put("6", "Aground")
        .put("7", "Engaged in fishing")
        .put("8", "Under way sailing")
        .put("9", "Reserved for future amendment of Navigational Status for HSC")
        .put("10", "Reserved for future amendment of Navigational Status for WIG")
        .put("11", "Reserved for future use")
        .put("12", "Reserved for future use")
        .put("13", "Reserved for future use")
        .put("14", "AIS-SART is active")
        .put("15", "Not defined (default)")
        .build();
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    
    
    static void addResources() throws IOException
    {
        // add AIS systme specs
        Api.addOrUpdateProcedure(createAisProcedure(), true);
        
        // add monitoring system instances
        for (var sys: getAllAisMonitoringSystems())
        {
            var sysId = Api.addOrUpdateSystem(sys, true);
            
            var navDs = createDataStream(sys.getUniqueIdentifier());
            var dsId = Api.addOrUpdateDataStream(navDs, true);
            //.ingestFoisAndObs(sysId, dsId);
        }
    }
    
    
    static AbstractProcess createAisProcedure()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SYSTEM)
            .uniqueID(AIS_PROC_UID)
            .name("AIS Monitoring System")
            .description("The automatic identification system (AIS) is an automatic tracking "
                + "system that uses transceivers on ships and is used by vessel traffic services (VTS). "
                + "When satellites are used to receive AIS signatures, the term Satellite-AIS (S-AIS) "
                + "is used. AIS information supplements marine radar, which continues to be the primary "
                + "method of collision avoidance for water transport.[citation needed] Although technically "
                + "and operationally distinct, the ADS-B system is analogous to AIS and performs a similar "
                + "function for aircraft.")
            
            .addIdentifier(sml.identifiers.shortName("AIS"))
            .addIdentifier(sml.identifiers.longName("Automatic Identification System"))
            
            .validFrom(OffsetDateTime.parse("2002-06-01T00:00:00Z"))
            
            .addInput("identifier", sml.createObservableProperty()
                .definition(SWEHelper.getPropertyUri("Identifier"))
                .label("Vessel Identification")
                .build())
            .addInput("position", sml.createObservableProperty()
                .definition(GeoPosHelper.DEF_LOCATION)
                .label("Vessel Position")
                .build())
            .addInput("course", sml.createObservableProperty()
                .definition(SWEHelper.getPropertyUri("CourseAngle"))
                .label("Vessel Course")
                .build())
            .addInput("speed", sml.createObservableProperty()
                .definition(GeoPosHelper.getQudtUri("Speed"))
                .label("Vessel Speed")
                .build())
            
            .addContact(sml.createContact()
                .role(CommonIdentifiers.AUTHOR_DEF)
                .organisationName("Internal Association of Marine Aids to Navigation and Lighthouse Authorities (IALA)")
                .deliveryPoint("10 rue des Gaudines")
                .city("St Germain en Laye")
                .postalCode("78100")
                .country("France")
                .email("contact@iala-aism.org")
            )
            
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Wikipedia Page")
                .description("Wikipedia page for Automatic Identification System (AIS)")
                .url("https://en.wikipedia.org/wiki/Automatic_identification_system")
                .mediaType("text/html")
            )
            .build();
    }
    
    
    static PhysicalSystemBuilder createAisMonitoringSystem(String uid, String countryName)
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SYSTEM)
            .uniqueID(uid)
            .name(countryName + " AIS Monitoring Network")
            .typeOf(AIS_PROC_UID)
            .validFrom(OffsetDateTime.parse("2006-01-01T00:00:00Z"));
    }
    
    
    static Collection<AbstractProcess> getAllAisMonitoringSystems()
    {
        var list = new ArrayList<AbstractProcess>(100);
        
        list.add(createAisMonitoringSystem(AIS_DENMARK_SYS_UID, "Denmark")
            .description("Maritime AIS traffic monitoring network collecting madatory AIS reports from ships crossing the straights "
                + "that connects the Baltic Sea and the North Sea")
            .addContact(sml.createContact()
                .organisationName("Danish Maritime Authority")
                .deliveryPoint("Caspar Brands Plads 9")
                .city("Korsør")
                .postalCode("4220")
                .country("Denmark")
                .phone("+45 72 19 60 00")
                .email("sfs@dma.dk")
            )
            .addComponent("vts1", sml.createPhysicalSystem()
                .definition(SWEConstants.DEF_SYSTEM)
                .uniqueID(VTS_SYS_UID_PREFIX + "dk:beltrep")
                .name("BELTREP VTS")
                .addIdentifier(sml.identifiers.callsign("BELT TRAFFIC"))
                .addContact(sml.createContact()
                    .role(CommonIdentifiers.OPERATOR_DEF)
                    .organisationName("Great Belt VTS")
                    .deliveryPoint("Naval Base Korsoer, Sylowsvej 8")
                    .city("Korsør")
                    .postalCode("4220")
                    .country("Denmark")
                    .phone("+45 58 37 68 68")
                    .email("vts@beltrep.org")
                 ))
            .addComponent("vts2", sml.createPhysicalSystem()
                .definition(SWEConstants.DEF_SYSTEM)
                .uniqueID(VTS_SYS_UID_PREFIX + "dk:soundrep")
                .name("SOUNDREP VTS")
                .addIdentifier(sml.identifiers.callsign("SOUND TRAFFIC"))
                .addContact(sml.createContact()
                    .role(CommonIdentifiers.OPERATOR_DEF)
                    .organisationName("Sjöfartsverket, Sound VTS")
                    .deliveryPoint("Hans Michelsensgatan 9")
                    .city("Malmö")
                    .postalCode("211 20")
                    .country("Sweden")
                    .phone("+46 771 63 06 00")
                    .email("contact@soundvts.org")
                ))
            .build());
        
        return list;
    }
    
    
    static IDataStreamInfo createDataStream(String sysUid)
    {
        var geopos = new GeoPosHelper();
        
        return new DataStreamInfo.Builder()
            .withSystem(new FeatureId(BigId.NONE, sysUid))
            .withName("AIS Navigation Data")
            .withDescription("Vessel navigation data received by AIS monitoring network")
            .withRecordEncoding(sml.newTextEncoding())
            .withRecordDescription(sml.createRecord()
                .name("ais_nav_data")
                .label("AIS Data Record")
                .addField("time", sml.createTime()
                    .asPhenomenonTimeIsoUTC()
                )
                .addField("mmsi", sml.createText()
                    .definition(SWEHelper.getDBpediaUri("MMSI"))
                    .label("MMSI")
                    .description("Maritime Mobile Service Identity (MMSI) number assigned to the vessel")
                )
                .addField("location", geopos.createLocationVectorLatLon()
                    .label("Vessel Location")
                )
                .addField("sog", sml.createQuantity()
                    .definition(SWEHelper.getPropertyUri("SpeedOverGround"))
                    .label("Speed Over Ground (SOG)")
                    .description("Vessel speed relative to ground")
                    .uomCode("[kn_i]")
                )
                .addField("cog", sml.createQuantity()
                    .definition(SWEHelper.getPropertyUri("CourseOverGround"))
                    .refFrame(SWEConstants.REF_FRAME_NED)
                    .axisId("Z")
                    .label("Course Over Ground (COG)")
                    .description("Vessel travel direction relative to true north, measured clockwise")
                    .uomCode("deg")
                )
                .addField("heading", sml.createQuantity()
                    .definition(SWEHelper.getPropertyUri("TrueHeading"))
                    .refFrame(SWEConstants.REF_FRAME_NED)
                    .axisId("Z")
                    .label("True Heading")
                    .description("Vessel heading direction relative to true north, measured clockwise")
                    .uomCode("deg")
                )
                .addField("status", sml.createCategory()
                    .definition(SWEHelper.getPropertyUri("SystemStatus"))
                    .codeSpace("https://api.vtexplorer.com/docs/ref-navstat.html")
                    .label("Navigational Status")
                    .description("Code providing current vessel status")
                )
                .build())
            .build();
    }
    
    
    static void ingestFoisAndObs(String sysId, String dsId) throws IOException
    {
        int maxFois = 2;
        var foiMap = new HashMap<String, String>();
        var sysUrl = "systems/"+ sysId;
        var dsUrl = "datastreams/" + dsId;
        
        // prefetch known FOIs
        int offset = 0;
        while (offset >= 0)
        {
            var respJson = Api.sendGetRequest(sysUrl + "/fois?limit=10000&offset=" + offset);
            var shipFois = respJson.getAsJsonObject().get("items").getAsJsonArray();
            shipFois.forEach(elt -> {
                var f = elt.getAsJsonObject();
                var id = f.get("id").getAsString();
                var mmsi = f.getAsJsonObject("properties").get("mmsi");
                var uid = f.getAsJsonObject("properties").get("uid").getAsString();
                if (mmsi != null && uid.startsWith("urn:osh")) // only our fois
                    foiMap.put(mmsi.getAsString(), id);
            });
            offset = shipFois.size() == 10000 ? offset+10000 : -1;
        }
        System.out.println("Already " + foiMap.size() + " vessels registered");
        
        int foiCount = 0;
        int obsCount = 0;
        JsonArrayBuilder<?, JsonArray> foiArrayJson = JsonBuilderFactory.buildArray();
        JsonArrayBuilder<?, JsonArray> obsArrayJson = JsonBuilderFactory.buildArray();
        var mmsiList = new ArrayList<String>();
        
        // first pass: register all fois
        // second pass: push observations
        passLoop: for (int pass = 1; pass <= 2; pass++)
        {
            System.out.println("Pass " + pass);
            try (var reader = new BufferedReader(new FileReader(TEST_FILE1)))
            {
                var line = reader.readLine();
                
                while ((line = reader.readLine()) != null)
                {
                    var fields = line.split(",");
                    
                    int i = 0;
                    var mmsi = fields[i++];
                    var time = fields[i++];
                    var lat = Double.parseDouble(fields[i++]);
                    var lon = Double.parseDouble(fields[i++]);
                    var sog = Float.parseFloat(fields[i++]);
                    var cog = Float.parseFloat(fields[i++]);
                    var heading = Float.parseFloat(fields[i++]);
                    var vesselName = fields[i++];
                    var imo = fields[i++];
                    var callSign = fields[i++];
                    var vesselType = fields[i++];
                    var status = fields[i++];
                    var length = fields[i++];
                    var width = fields[i++];
                    var draft = fields[i++];
                    
                    //if (!"338531000".equals(mmsi))
                    //    continue;
                    
                    var vesselTypeTxt = VESSEL_TYPES.get(vesselType);
                    if (vesselTypeTxt != null)
                        vesselType = vesselTypeTxt;
                    else
                        vesselType = "Other";
                    
                    // lookup FOI ID and register it if needed
                    var foiId = foiMap.get(mmsi);
                    if (pass == 1 && foiId == null)
                    {
                        // skip if no vessel name is provided
                        // wait for metadata message
                        if (vesselName.length() == 0)
                            continue;
                        
                        // register new FOI
                        var foiJson = JsonBuilderFactory.buildObject()
                            .add("type", "Feature")
                            .addObject("properties")
                                .add("featureType", "http://www.opengis.net/def/featureType/x-T18/Vessel")
                                .add("uid", "urn:osh:foi:vessel:" + mmsi)
                                .add("name", vesselName)
                                .add("description", "Proxy feature for vessel " + mmsi)
                                .add("mmsi", mmsi)
                                .add("imo", imo)
                                .add("callSign", callSign)
                                .add("vesselType", vesselType)
                                .add("length", Strings.isNullOrEmpty(length) ? null : Float.parseFloat(length))
                                .add("width", Strings.isNullOrEmpty(width) ? null : Float.parseFloat(width))
                                .add("draft", Strings.isNullOrEmpty(draft) ? null : Float.parseFloat(draft))
                            .end();
                        
                        foiMap.putIfAbsent(mmsi, ""); // add this temporarily so we don't try to add it again
                        mmsiList.add(mmsi);
                        foiArrayJson.add(foiJson);
                        foiCount++;
                        
                        // push to server once we collected 100 fois
                        if (foiCount % 100 == 0)
                        {
                            if (foiArrayJson != null)
                            {
                                var featureJson = foiArrayJson.getJson().toString();
                                var resp = Api.sendPostRequest(sysUrl + "/fois", featureJson, "application/json");
                                
                                // parse all feature urls from response and extract id part
                                var json = JsonParser.parseString(resp.body());
                                int k = 0;
                                for (var elt: json.getAsJsonArray())
                                {
                                    var foiUrl = elt.getAsString();
                                    var fid = foiUrl.substring(foiUrl.lastIndexOf('/')+1);
                                    foiMap.put(mmsiList.get(k++), fid);
                                }
                                
                                System.out.println(foiCount + " fois");
                            }
                            
                            // start new array
                            foiArrayJson = JsonBuilderFactory.buildArray();
                            mmsiList.clear();
                        }
                        
                        // stop collecting fois if we reached max
                        if (foiMap.size() >= maxFois)
                        {
                            // remove orphan mmsi if any
                            if ("".equals(foiMap.get(mmsi)))
                                foiMap.remove(mmsi);
                            continue passLoop;
                        }
                    }
                    
                    else if (pass == 2)
                    {
                        if (!foiMap.containsKey(mmsi))
                            continue;
                        
                        // push observation
                        var obsJson = JsonBuilderFactory.buildObject()
                            .add("foi@id", foiId)
                            .add("phenomenonTime", time + "Z")
                            .addObject("result")
                                .add("mmsi", mmsi)
                                .addObject("location")
                                    .add("lat", lat)
                                    .add("lon", lon)
                                    .end()
                                .add("sog", sog)
                                .add("cog", cog)
                                .add("heading", heading)
                                .add("status", status)
                            .end();
                        obsArrayJson.add(obsJson);
                        obsCount++;
                        
                        // push to server once we collected 100 obs
                        if (obsCount % 100 == 0)
                        {
                            if (obsArrayJson != null)
                            {
                                Api.sendPostRequest(dsUrl + "/observations", obsArrayJson.getJson(), "application/om+json");
                                System.out.println(obsCount + " obs");
                            }
                            
                            // start new array
                            obsArrayJson = JsonBuilderFactory.buildArray();
                        }
                    }
                }
            }
        }
    }
}
