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
import java.time.Duration;
import java.time.Instant;
import org.junit.Test;
import org.sensorhub.api.datastore.TemporalFilter;
import org.sensorhub.api.datastore.EmptyFilterIntersection;
import org.sensorhub.api.datastore.RangeFilter.RangeOp;
import org.vast.util.TimeExtent;
import com.google.common.collect.Range;


public class TestTemporalFilter
{
    static final int NOW_THRESHOLD_NANOS = 100000000;
    
    
    protected void checkConsistent(TemporalFilter filter)
    {
        assertEquals(filter.getRange().lowerEndpoint(), filter.getMin());
        assertEquals(filter.getRange().upperEndpoint(), filter.getMax());
        
        if (filter.isCurrentTime())
        {
            assertTrue(Duration.between(filter.getMin(), Instant.now()).getNano() < NOW_THRESHOLD_NANOS);
            assertTrue(filter.isSingleValue());
            assertTrue(filter.beginsNow());
            assertTrue(filter.endsNow());
            assertFalse(filter.isLatestTime());
        }
        else if (filter.beginsNow())
        {
            assertTrue(Duration.between(filter.getMin(), Instant.now()).getNano() < NOW_THRESHOLD_NANOS);
            assertFalse(filter.isSingleValue());
            assertFalse(filter.isLatestTime());
        }
        else if (filter.endsNow())
        {
            assertTrue(Duration.between(filter.getMax(), Instant.now()).getNano() < NOW_THRESHOLD_NANOS);
            assertFalse(filter.isSingleValue());
            assertFalse(filter.isLatestTime());
        }
        
        if (filter.isLatestTime())
        {
            assertTrue(filter.isSingleValue());
            assertFalse(filter.isCurrentTime());
        }
        else if (filter.isSingleValue())
        {
            assertEquals(filter.getMin(), filter.getMax());
        }
    }
    
    
    @Test
    public void testBuildFromInstant()
    {
        var instant = Instant.parse("2006-05-07T12:00:00Z");
        var filter = new TemporalFilter.Builder()
            .withSingleValue(instant)
            .build();
        assertEquals(instant, filter.getMin());
        assertEquals(instant, filter.getMax());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildFromTimeExtent()
    {
        var begin = "2006-05-07T12:00:00Z";
        var end = "2015-04-08Z";
        var timeExtent = TimeExtent.parse(begin + "/" + end);
        var filter = new TemporalFilter.Builder()
            .fromTimeExtent(timeExtent)
            .build();
        assertEquals(Instant.parse(begin), filter.getMin());
        assertEquals(Instant.parse(end.replace("Z", "T00:00:00Z")), filter.getMax());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildFromRange()
    {
        var begin = Instant.parse("2006-05-07T12:00:00Z");
        var end = Instant.parse("2026-12-31T12:00:00Z");
        var filter = new TemporalFilter.Builder()
            .withRange(Range.closed(begin, end))
            .build();
        assertEquals(begin, filter.getMin());
        assertEquals(end, filter.getMax());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildWithCurrentTime()
    {
        var filter = new TemporalFilter.Builder()
            .withCurrentTime()
            .build();
        assertTrue(filter.isCurrentTime());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildFromTimeExtentNow()
    {
        var filter = new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.now())
            .build();
        assertTrue(filter.isCurrentTime());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildFromTimeExtentBeginNow()
    {
        var end = Instant.parse("3000-05-07T12:00:00Z");
        var timeExtent = TimeExtent.beginNow(end);
        var filter = new TemporalFilter.Builder()
            .fromTimeExtent(timeExtent)
            .build();
        assertTrue(filter.beginsNow());
        assertEquals(end, filter.getMax());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildWithRangeBeginNow()
    {
        var end = Instant.parse("2126-12-31T12:00:00Z");
        var filter = new TemporalFilter.Builder()
            .withRangeBeginningNow(end)
            .build();
        assertTrue(filter.beginsNow());
        assertEquals(end, filter.getMax());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildFromTimeExtentEndNow()
    {
        var begin = Instant.parse("2006-05-07T12:00:00Z");
        var timeExtent = TimeExtent.endNow(begin);
        var filter = new TemporalFilter.Builder()
            .fromTimeExtent(timeExtent)
            .build();
        assertTrue(filter.endsNow());
        assertEquals(begin, filter.getMin());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildWithRangeEndNow()
    {
        var begin = Instant.parse("2016-02-29T12:00:00Z");
        var filter = new TemporalFilter.Builder()
            .withRangeEndingNow(begin)
            .build();
        assertTrue(filter.endsNow());
        assertEquals(begin, filter.getMin());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildWithLatestTime()
    {
        var filter = new TemporalFilter.Builder()
            .withLatestTime()
            .build();
        assertTrue(filter.isLatestTime());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildWithAllTimes()
    {
        var filter = new TemporalFilter.Builder()
            .withAllTimes()
            .build();
        assertTrue(filter.isAllTimes());
        assertEquals(Instant.MIN, filter.getMin());
        assertEquals(Instant.MAX, filter.getMax());
        checkConsistent(filter);
    }
    
    
    @Test
    public void testBuildWithOperator()
    {
        var instant = Instant.now();
        
        // default is intersect
        var filter = new TemporalFilter.Builder()
            .withSingleValue(instant)
            .build();
        assertEquals(RangeOp.INTERSECTS, filter.getOperator());
        checkConsistent(filter);
                
        filter = new TemporalFilter.Builder()
            .withRange(instant, instant.plusNanos(1))
            .withOperator(RangeOp.CONTAINS)
            .build();
        assertEquals(RangeOp.CONTAINS, filter.getOperator());
        checkConsistent(filter);
        
        filter = new TemporalFilter.Builder()
            .withSingleValue(instant)
            .withOperator(RangeOp.EQUALS)
            .build();
        assertEquals(RangeOp.EQUALS, filter.getOperator());
        checkConsistent(filter);
    }
    
    
    /* predicate tests */
    
    
    @Test
    public void testPredicate_Intersect_RangeRange()
    {
        var now = Instant.now();
        var te = TimeExtent.period(now, now.plusSeconds(33));
        
        // overlap
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(10), now.plusSeconds(10)))
            .build()
            .test(te));
        
        // touch at min
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(10), now))
            .build()
            .test(te));
        
        // touch at max
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(33), now.plusSeconds(56)))
            .build()
            .test(te));
        
        // disjoint
        assertFalse(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(33), now.minusSeconds(15)))
            .build()
            .test(te));
        assertFalse(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.plusSeconds(34), now.plusSeconds(45)))
            .build()
            .test(te));
        
        // with now
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.endNow(now.minusSeconds(10)))
            .build()
            .test(te));
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.beginNow(now.plusSeconds(60)))
            .build()
            .test(te));        
    }
    
    
    @Test
    public void testPredicate_Intersect_RangeInstant()
    {
        var now = Instant.now();
        
        // contains
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(10), now.plusSeconds(10)))
            .build()
            .test(now));
        
        // touch at min
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(10), now))
            .build()
            .test(now));
        
        // touch at max
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now, now.plusSeconds(6)))
            .build()
            .test(now));
        
        // disjoint
        assertFalse(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(33), now.minusSeconds(15)))
            .build()
            .test(now));
        assertFalse(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.plusSeconds(34), now.plusSeconds(45)))
            .build()
            .test(now));
    }
    
    
    @Test
    public void testPredicate_Intersect_InstantInstant()
    {
        var now = Instant.now();
        
        // equals
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.instant(now))
            .build()
            .test(now));
        
        // disjoint
        assertFalse(new TemporalFilter.Builder()
            .withSingleValue(now.plusNanos(1))
            .build()
            .test(now));
        assertFalse(new TemporalFilter.Builder()
            .withSingleValue(now.minusNanos(1))
            .build()
            .test(now));
    }
    
    
    @Test
    public void testPredicate_Contains_RangeRange()
    {
        var now = Instant.now();
        var te = TimeExtent.period(now, now.plusSeconds(33));
        
        // overlap
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(10), now.plusSeconds(40)))
            .withOperator(RangeOp.CONTAINS)
            .build()
            .test(te));
        
        // intersect through min
        assertFalse(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(10), now.plusSeconds(10)))
            .withOperator(RangeOp.CONTAINS)
            .build()
            .test(te));
        
        // intersect through max
        assertFalse(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.plusSeconds(21), now.plusSeconds(56)))
            .withOperator(RangeOp.CONTAINS)
            .build()
            .test(te));
        
        // disjoint
        assertFalse(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(33), now.minusSeconds(15)))
            .withOperator(RangeOp.CONTAINS)
            .build()
            .test(te));
        
        // with now
        assertFalse(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.endNow(now.minusSeconds(10)))
            .withOperator(RangeOp.CONTAINS)
            .build()
            .test(te));
        assertFalse(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.beginNow(now.plusSeconds(60)))
            .withOperator(RangeOp.CONTAINS)
            .build()
            .test(te));        
    }
    
    
    @Test
    public void testPredicate_Contains_RangeInstant()
    {
        var now = Instant.now();
        
        // contains
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(10), now.plusSeconds(10)))
            .build()
            .test(now));
        
        // touch at min
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(10), now))
            .build()
            .test(now));
        
        // touch at max
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now, now.plusSeconds(6)))
            .build()
            .test(now));
        
        // disjoint
        assertFalse(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.minusSeconds(33), now.minusNanos(1)))
            .build()
            .test(now));
        assertFalse(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(now.plusNanos(1), now.plusSeconds(45)))
            .build()
            .test(now));
    }
    
    
    @Test
    public void testPredicate_Contains_InstantInstant()
    {
        var now = Instant.now();
        
        // equals
        assertTrue(new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.instant(now))
            .withOperator(RangeOp.CONTAINS)
            .build()
            .test(now));
        
        // disjoint
        assertFalse(new TemporalFilter.Builder()
            .withSingleValue(now.plusNanos(1))
            .withOperator(RangeOp.CONTAINS)
            .build()
            .test(now));
        assertFalse(new TemporalFilter.Builder()
            .withSingleValue(now.minusNanos(1))
            .withOperator(RangeOp.CONTAINS)
            .build()
            .test(now));
    }
    
    
    @Test
    public void testIntersectionSameOperator() throws Exception
    {
        var t0 = Instant.now();
        TemporalFilter filter1, filter2, xFilter;
        
        // 2 finite ranges w/ instant in common
        filter1 = new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(t0, t0.plusSeconds(10)))
            .build();        
        filter2 = new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(t0.minusSeconds(10), t0))
            .build();        
        xFilter = filter1.intersect(filter2);
        
        assertEquals(xFilter.getMin(), t0);
        System.out.println(xFilter);
        checkConsistent(xFilter);
        
        // 2 finite ranges w/ range in common
        filter1 = new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(t0, t0.plusSeconds(10)))
            .build();        
        filter2 = new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(t0.minusSeconds(10), t0.plusSeconds(5)))
            .build();        
        xFilter = filter1.intersect(filter2);
        
        assertEquals(xFilter.getMin(), t0);
        assertEquals(xFilter.getMax(), t0.plusSeconds(5));
        System.out.println(xFilter);
        checkConsistent(xFilter);
        
        // ranges with 'now' bound
        filter1 = new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.beginNow(t0.plusSeconds(10)))
            .build();        
        filter2 = new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(t0.minusSeconds(1), t0.plusSeconds(1)))
            .build();        
        xFilter = filter1.intersect(filter2);
        
        assertTrue(Duration.between(xFilter.getMin(), t0).getSeconds() < 1);
        assertEquals(xFilter.getMax(), t0.plusSeconds(1));
        System.out.println(xFilter);
        checkConsistent(xFilter);
        
        // 'now' instant and range
        filter1 = new TemporalFilter.Builder()
            .withCurrentTime()
            .build();        
        filter2 = new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.period(t0.minusSeconds(3600*1000), t0.plusSeconds(3600)))
            .build();        
        xFilter = filter1.intersect(filter2);
        
        assertTrue(xFilter.isSingleValue());
        assertTrue(Duration.between(xFilter.getMin(), t0).getSeconds() < 1);
        System.out.println(xFilter);
        checkConsistent(xFilter);
        
        // 'now' instant and range ending 'now'
        filter1 = new TemporalFilter.Builder()
            .withCurrentTime()
            .build();        
        filter2 = new TemporalFilter.Builder()
            .fromTimeExtent(TimeExtent.endNow(t0.minusSeconds(180)))
            .build();        
        xFilter = filter1.intersect(filter2);
        
        assertTrue(xFilter.isSingleValue());
        assertTrue(Duration.between(xFilter.getMin(), t0).getSeconds() < 1);
        System.out.println(xFilter);
        checkConsistent(xFilter);
    }
    
    
    @Test
    public void testErrorEmptyIntersectionSameOperator() throws Exception
    {
        var now = Instant.now();        
        
        // 2 finite ranges w/ nothing in common
        assertThrows(EmptyFilterIntersection.class, () -> {
            var filter1 = new TemporalFilter.Builder()
                .fromTimeExtent(TimeExtent.period(now.plusSeconds(1), now.plusSeconds(10)))
                .build();        
            var filter2 = new TemporalFilter.Builder()
                .fromTimeExtent(TimeExtent.period(now.minusSeconds(10), now))
                .build();        
            filter1.intersect(filter2);
        });
        
        // 1 range + 1 instant w/ nothing in common
        assertThrows(EmptyFilterIntersection.class, () -> {
            var filter1 = new TemporalFilter.Builder()
                .fromTimeExtent(TimeExtent.instant(now.plusMillis(1)))
                .build();        
            var filter2 = new TemporalFilter.Builder()
                .fromTimeExtent(TimeExtent.period(now.minusSeconds(10), now))
                .build();        
            filter1.intersect(filter2);
        });
        
        // range not including now
        assertThrows(EmptyFilterIntersection.class, () -> {
            var filter1 = new TemporalFilter.Builder()
                .withCurrentTime()
                .build();        
            var filter2 = new TemporalFilter.Builder()
                .fromTimeExtent(TimeExtent.period(now.minusSeconds(10), now.minusMillis(1)))
                .build();
            filter1.intersect(filter2);
        });
    }
}
