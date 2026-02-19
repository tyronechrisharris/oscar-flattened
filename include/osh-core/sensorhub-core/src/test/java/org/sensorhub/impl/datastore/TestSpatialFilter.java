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
import org.junit.Test;
import org.sensorhub.api.datastore.SpatialFilter;
import org.sensorhub.api.datastore.SpatialFilter.SpatialOp;
import org.vast.util.Bbox;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;


public class TestSpatialFilter
{
    
    protected void checkConsistent(SpatialFilter filter)
    {
        assertNotNull(filter.getRoi());
        
        switch (filter.getOperator())
        {
            case WITHIN_DISTANCE:
                assertNotNull(filter.getCenter());
                assertTrue(!Double.isNaN(filter.getDistance()));
                break;
                
            default:
                assertNull(filter.getCenter());
                assertTrue(Double.isNaN(filter.getDistance()));
        }
    }
    
    
    @Test
    public void testBuildFromBbox()
    {
        Geometry roi = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(-10,-10),
            new Coordinate(-10,10),
            new Coordinate(10,10),
            new Coordinate(10,-10),
            new Coordinate(-10,-10)
        });
        
        var bbox = Bbox.fromJtsEnvelope(roi.getEnvelopeInternal());
        
        var filter = new SpatialFilter.Builder()
            .withBbox(bbox)
            .build();
        
        assertEquals(roi, filter.getRoi());
        assertEquals(SpatialOp.INTERSECTS, filter.getOperator());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildFromPolygon()
    {
        Geometry roi = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(-10,-10),
            new Coordinate(-10,10),
            new Coordinate(10,10),
            new Coordinate(10,-10),
            new Coordinate(-10,-10)
        });
        
        var filter = new SpatialFilter.Builder()
            .withRoi(roi)
            .build();
        
        assertEquals(roi, filter.getRoi());
        assertEquals(SpatialOp.INTERSECTS, filter.getOperator());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildFromPoint()
    {
        Geometry p = new GeometryFactory().createPoint(
            new Coordinate(-23,-56));
        
        var filter = new SpatialFilter.Builder()
            .withRoi(p)
            .build();
        
        assertEquals(p, filter.getRoi());
        assertEquals(SpatialOp.INTERSECTS, filter.getOperator());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildFromCenterAndDistance()
    {
        var center = new GeometryFactory().createPoint(
            new Coordinate(52.88, -89.23));
        var dist = 25.6;
        
        var filter = new SpatialFilter.Builder()
            .withDistanceToPoint(center, dist)
            .build();
        
        assertEquals(center, filter.getCenter());
        assertEquals(dist, filter.getDistance(), 1e-12);
        assertEquals(SpatialOp.WITHIN_DISTANCE, filter.getOperator());
        
        // check generated roi contains circle
        var roi = filter.getRoi();
        var env = roi.getEnvelopeInternal();
        assertTrue(roi.getArea() >= Math.PI * dist * dist);
        assertEquals(center, roi.getCentroid());
        assertTrue(env.getWidth() >= dist*2);
        assertTrue(env.getHeight() >= dist*2);
        checkConsistent(filter);
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void testBuildFromCenterAndDistanceIncompatibleOp()
    {
        var center = new GeometryFactory().createPoint(
            new Coordinate(52.88, -89.23));
        var dist = 25.6;
        
        new SpatialFilter.Builder()
            .withDistanceToPoint(center, dist)
            .withOperator(SpatialOp.DISJOINT)
            .build();
    }
    
    
    @Test
    public void testBuildWithOperator()
    {
        Geometry roi = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(-10,-10),
            new Coordinate(-10,10),
            new Coordinate(10,10),
            new Coordinate(10,-10),
            new Coordinate(-10,-10)
        });
        
        var spatialOps = new SpatialOp[] {
            SpatialOp.INTERSECTS,
            SpatialOp.CONTAINS,
            SpatialOp.DISJOINT,
            SpatialOp.WITHIN,
            SpatialOp.EQUALS
        };
        
        for (var spatialOp: spatialOps)
        {        
            var filter = new SpatialFilter.Builder()
                .withRoi(roi)
                .withOperator(spatialOp)
                .build();
            
            assertEquals(roi, filter.getRoi());
            assertEquals(spatialOp, filter.getOperator());
            checkConsistent(filter);
        }
    }
    
    
    /* predicate tests */
    
    
    @Test
    public void testPredicate_Intersect_PolygonPoint() throws Exception
    {
        var roi = new WKTReader().read(
            "POLYGON((10.689697265625 -25.0927734375, " +
            "34.595947265625 -20.1708984375, " +
            "38.814697265625 -35.6396484375, " +
            "13.502197265625 -39.1552734375, " +
            "10.689697265625 -25.0927734375))");
        
        var filter = new SpatialFilter.Builder()
            .withRoi(roi)
            .build();
        
        // inside
        var p1 = new GeometryFactory().createPoint(
            new Coordinate(20, -30));
        assertTrue(filter.test(p1));
        
        // on vertex
        var p2 = new GeometryFactory().createPoint(
            new Coordinate(34.595947265625, -20.1708984375));
        assertTrue(filter.test(p2));
        
        // outside
        var p3 = new GeometryFactory().createPoint(
            new Coordinate(38, -20));
        assertFalse(filter.test(p3));        
    }
    
    
    @Test
    public void testPredicate_Within_PolygonPoint() throws Exception
    {
        var roi = new WKTReader().read(
            "POLYGON((10.689697265625 -25.0927734375, " +
            "34.595947265625 -20.1708984375, " +
            "38.814697265625 -35.6396484375, " +
            "13.502197265625 -39.1552734375, " +
            "10.689697265625 -25.0927734375))");
        
        var filter = new SpatialFilter.Builder()
            .withRoi(roi)
            .withOperator(SpatialOp.WITHIN)
            .build();
        
        // inside
        var p1 = new GeometryFactory().createPoint(
            new Coordinate(20, -30));
        assertTrue(filter.test(p1));
        
        // on vertex
        var p2 = new GeometryFactory().createPoint(
            new Coordinate(34.595947265625, -20.1708984375));
        assertFalse(filter.test(p2));
        
        // outside
        var p3 = new GeometryFactory().createPoint(
            new Coordinate(38, -20));
        assertFalse(filter.test(p3));
    }
    
    
    @Test
    public void testPredicate_Intersect_PolygonPolygon() throws Exception
    {
        var roi = new WKTReader().read(
            "POLYGON((10.689697265625 -25.0927734375, " +
            "34.595947265625 -20.1708984375, " +
            "38.814697265625 -35.6396484375, " +
            "13.502197265625 -39.1552734375, " +
            "10.689697265625 -25.0927734375))");
        
        var filter = new SpatialFilter.Builder()
            .withRoi(roi)
            .build();
        
        // inside
        var poly1 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(20,-30),
            new Coordinate(30,-30),
            new Coordinate(31,-31),
            new Coordinate(20,-32),
            new Coordinate(20,-30)
        });
        assertTrue(filter.test(poly1));
        
        // crossing
        var poly2 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(10,-10),
            new Coordinate(20,-10),
            new Coordinate(30,-35),
            new Coordinate(10,-10)
        });
        assertTrue(filter.test(poly2));
        
        // outside
        var poly3 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(10,-10),
            new Coordinate(20,-10),
            new Coordinate(10,-20),
            new Coordinate(10,-10)
        });
        assertFalse(filter.test(poly3));
        
        // contains
        var poly4 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(10,-10),
            new Coordinate(50,-10),
            new Coordinate(50,-40),
            new Coordinate(10,-40),
            new Coordinate(10,-10)
        });
        assertTrue(filter.test(poly4));
    }
    
    
    @Test
    public void testPredicate_Within_PolygonPolygon() throws Exception
    {
        var roi = new WKTReader().read(
            "POLYGON((10.689697265625 -25.0927734375, " +
            "34.595947265625 -20.1708984375, " +
            "38.814697265625 -35.6396484375, " +
            "13.502197265625 -39.1552734375, " +
            "10.689697265625 -25.0927734375))");
        
        var filter = new SpatialFilter.Builder()
            .withRoi(roi)
            .withOperator(SpatialOp.WITHIN)
            .build();
        
        // inside
        var poly1 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(20,-30),
            new Coordinate(30,-30),
            new Coordinate(31,-31),
            new Coordinate(20,-32),
            new Coordinate(20,-30)
        });
        assertTrue(filter.test(poly1));
        
        // crossing
        var poly2 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(10,-10),
            new Coordinate(20,-10),
            new Coordinate(30,-35),
            new Coordinate(10,-10)
        });
        assertFalse(filter.test(poly2));
        
        // outside
        var poly3 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(10,-10),
            new Coordinate(20,-10),
            new Coordinate(10,-20),
            new Coordinate(10,-10)
        });
        assertFalse(filter.test(poly3));
        
        // within
        var poly4 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(10,-10),
            new Coordinate(50,-10),
            new Coordinate(50,-40),
            new Coordinate(10,-40),
            new Coordinate(10,-10)
        });
        assertFalse(filter.test(poly4));       
    }
    
    
    @Test
    public void testPredicate_Contains_PolygonPolygon() throws Exception
    {
        var roi = new WKTReader().read(
            "POLYGON((10.689697265625 -25.0927734375, " +
            "34.595947265625 -20.1708984375, " +
            "38.814697265625 -35.6396484375, " +
            "13.502197265625 -39.1552734375, " +
            "10.689697265625 -25.0927734375))");
        
        var filter = new SpatialFilter.Builder()
            .withRoi(roi)
            .withOperator(SpatialOp.CONTAINS)
            .build();
        
        // inside
        var poly1 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(20,-30),
            new Coordinate(30,-30),
            new Coordinate(31,-31),
            new Coordinate(20,-32),
            new Coordinate(20,-30)
        });
        assertFalse(filter.test(poly1));
        
        // crossing
        var poly2 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(10,-10),
            new Coordinate(20,-10),
            new Coordinate(30,-35),
            new Coordinate(10,-10)
        });
        assertFalse(filter.test(poly2));
        
        // outside
        var poly3 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(10,-10),
            new Coordinate(20,-10),
            new Coordinate(10,-20),
            new Coordinate(10,-10)
        });
        assertFalse(filter.test(poly3));
        
        // contains
        var poly4 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(10,-10),
            new Coordinate(50,-10),
            new Coordinate(50,-40),
            new Coordinate(10,-40),
            new Coordinate(10,-10)
        });
        assertTrue(filter.test(poly4));
    }
    
    
    @Test
    public void testPredicate_WithinDistance_Polygon() throws Exception
    {
        var center = new GeometryFactory().createPoint(
            new Coordinate(0.0, 0.0));
        var dist = 40;
        
        var filter = new SpatialFilter.Builder()
            .withDistanceToPoint(center, dist)
            .build();
        
        // inside
        var poly1 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(20,-30),
            new Coordinate(30,-30),
            new Coordinate(31,-31),
            new Coordinate(20,-32),
            new Coordinate(20,-30)
        });
        assertTrue(filter.test(poly1));
        
        // crossing
        var poly2 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(10,-10),
            new Coordinate(50,-10),
            new Coordinate(40,-35),
            new Coordinate(10,-10)
        });
        assertTrue(filter.test(poly2));
        
        // outside
        var poly3 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(100,-10),
            new Coordinate(150,-10),
            new Coordinate(110,-20),
            new Coordinate(100,-10)
        });
        assertFalse(filter.test(poly3));
        
        // contains
        var poly4 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(-50,50),
            new Coordinate(50,50),
            new Coordinate(50,-50),
            new Coordinate(-50,-50),
            new Coordinate(-50,50)
        });
        assertTrue(filter.test(poly4));       
    }
    
    
    @Test
    public void testPredicate_WithinDistance_Point() throws Exception
    {
        var center = new GeometryFactory().createPoint(
            new Coordinate(1.3, 41.2));
        var dist = 25;
        
        var filter = new SpatialFilter.Builder()
            .withDistanceToPoint(center, dist)
            .build();
        
        // inside
        var p1 = new GeometryFactory().createPoint(
            new Coordinate(20, 30));
        assertTrue(filter.test(p1));
        p1 = new GeometryFactory().createPoint(
            new Coordinate(1.3, 41.2-0.000001));
        assertTrue(filter.test(p1));
        
        // on circle
        var p2 = new GeometryFactory().createPoint(
            new Coordinate(1.3+25, 41.2));
        assertTrue(filter.test(p2));
        
        // outside
        var p3 = new GeometryFactory().createPoint(
            new Coordinate(-8, -20));
        assertFalse(filter.test(p3));
        p3 = new GeometryFactory().createPoint(
            new Coordinate(1.3*25+0.000001, 41.2));
        assertFalse(filter.test(p3));
    }

}
