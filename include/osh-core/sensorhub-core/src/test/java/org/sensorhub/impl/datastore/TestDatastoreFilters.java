/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore;

import static org.junit.Assert.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.function.Predicate;
import org.junit.Test;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.system.wrapper.SystemWrapper;
import org.vast.sensorML.SMLHelper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;


public class TestDatastoreFilters
{

    @Test
    public void testSystemFilterBuilder()
    {
        var filter = new SystemFilter.Builder()
            .build();
        
        // internal IDs
        var internalIDs = BigId.fromLongs(0, 20L, 20L, 30L);
        filter = new SystemFilter.Builder()
            .withInternalIDs(internalIDs)
            .build();
        assertTrue(filter.getInternalIDs().size() == 2);
        for (var id: internalIDs)
            assertTrue(filter.getInternalIDs().contains(id));
        
        // full text
        String[] keywords = {"word3", "word1", "word2", "word3"};
        filter = new SystemFilter.Builder()
            .withKeywords(keywords)
            .build();
        assertTrue(filter.getFullTextFilter().getKeywords().size() == 3);
        for (var w: keywords)
            assertTrue(filter.getFullTextFilter().getKeywords().contains(w));
        
        // predicate
        Predicate<ISystemWithDesc> predicate = (f -> f.getName().equals("test"));
        filter = new SystemFilter.Builder()
            .withValuePredicate(predicate)
            .build();
        assertEquals(predicate, filter.getValuePredicate());
        
        // feature UIDs
        String[] uids = {"urn:osh:features:F002", "urn:osh:features:F001", "urn:osh:features:F235"};
        filter = new SystemFilter.Builder()
            .withUniqueIDs(uids)
            .build();
        assertTrue(filter.getUniqueIDs().size() == 3);
        for (var uid: uids)
            assertTrue(filter.getUniqueIDs().contains(uid));
                
        // location
        Geometry roi = new GeometryFactory().createPolygon(new Coordinate[] {
           new Coordinate(-10,-10),
           new Coordinate(-10,10),
           new Coordinate(10,10),
           new Coordinate(10,-10),
           new Coordinate(-10,-10)
        });
        filter = new SystemFilter.Builder()
            .withLocationIntersecting(roi)
            .build();
        assertEquals(roi, filter.getLocationFilter().getRoi());
                
        // validTime
        var validTime = Instant.parse("2006-05-07T12:00:00Z");
        filter = new SystemFilter.Builder()
            .validAtTime(validTime)
            .build();
        assertEquals(validTime, filter.getValidTime().getMin());
        assertEquals(validTime, filter.getValidTime().getMax());
        
    }
    
    
    @Test
    public void testSystemFilterAsPredicate()
    {
        var proc = new SystemWrapper(new SMLHelper().createPhysicalComponent()
            .name("Thermometer")
            .description("A thermometer measuring outdoor air temperature on my window")
            .uniqueID("urn:osh:sensor:001")
            .validFrom(OffsetDateTime.parse("2001-05-26T13:45:00Z"))
            .build());
        
        assertTrue(new SystemFilter.Builder().build()
            .test(proc));
        
        assertTrue(new SystemFilter.Builder()
            .withUniqueIDs("urn:osh:sensor:001").build()
            .test(proc));
        
        assertTrue(new SystemFilter.Builder()
            .withUniqueIDs("urn:osh:sensor:001")
            .validAtTime(Instant.parse("2002-01-01T00:00:00Z")).build()
            .test(proc));
        
        assertFalse(new SystemFilter.Builder()
            .withUniqueIDs("urn:osh:sensor:001")
            .validAtTime(Instant.parse("2000-03-15T06:30:00Z")).build()
            .test(proc));
        
        assertTrue(new SystemFilter.Builder()
            .withUniqueIDs("urn:osh:sensor:001")
            .validAtTime(Instant.now()).build()
            .test(proc));
        
        assertFalse(new SystemFilter.Builder()
            .withUniqueIDs("urn:osh:sensor:001")
            .validAtTime(Instant.now().plusSeconds(10)).build()
            .test(proc));
        
        assertTrue(new SystemFilter.Builder()
            .withUniqueIDs("urn:osh:sensor:001")
            .withCurrentVersion().build()
            .test(proc));
        
        assertFalse(new SystemFilter.Builder()
            .withUniqueIDs("urn:osh:sensor:002").build()
            .test(proc));
        
        assertTrue(new SystemFilter.Builder()
            .withUniqueIDs("urn:osh:sensor:001")
            .withKeywords("outdoor")
            .build()
            .test(proc));
        
        assertTrue(new SystemFilter.Builder()
            .withKeywords("Thermo")
            .build()
            .test(proc));
        
        assertFalse(new SystemFilter.Builder()
            .withKeywords("robot")
            .build()
            .test(proc));
    }

}
