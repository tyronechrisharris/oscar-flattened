/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore;

import java.util.function.Predicate;
import org.vast.util.Asserts;
import org.vast.util.BaseBuilder;
import org.vast.util.Bbox;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;


/**
 * <p>
 * Immutable spatial filter definition
 * </p>
 *
 * @author Alex Robin
 * @date Apr 3, 2018
 */
public class SpatialFilter implements Predicate<Geometry>
{
    public enum SpatialOp
    {
        INTERSECTS, OVERLAPS, CROSSES, TOUCHES, CONTAINS, EQUALS, WITHIN, DISJOINT, WITHIN_DISTANCE
    }

    protected SpatialOp operator;
    protected Geometry roi;
    protected Point center;
    protected double distance = Double.NaN;
    
    protected transient PreparedGeometry preparedGeom;
    protected transient Predicate<Geometry> geomTest;


    /*
     * this class can only be instantiated using builder
     */
    protected SpatialFilter()
    {        
    }


    public SpatialOp getOperator()
    {
        return operator;
    }
    
    
    public Geometry getRoi()
    {
        return roi;
    }
    
    
    public Point getCenter()
    {
        return center;
    }
    
    
    public double getDistance()
    {
        return distance;
    }


    @Override
    public boolean test(Geometry geom)
    {
        Asserts.checkNotNull(geom, Geometry.class);
        return geomTest.test(geom);
    }
    
    
    /**
     * Computes the intersection (logical AND) between this filter and another filter of the same kind
     * @param filter The other filter to AND with
     * @return The new composite filter
     * @throws EmptyFilterIntersection if the intersection doesn't exist
     */
    public SpatialFilter intersect(SpatialFilter filter) throws EmptyFilterIntersection
    {
        if (filter == null)
            return this;
        return intersect(filter, new Builder()).build();
    }
    
    
    protected <F extends SpatialFilter, B extends SpatialFilterBuilder<B, F>> B intersect(F otherFilter, B builder) throws EmptyFilterIntersection
    {
        // we're handling only INTERSECTION operator for now        
        return builder.withRoi(roi.intersection(otherFilter.roi));
    }
    
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append(operator).append(' ');
        if (!Double.isNaN(distance))
        {
            buf.append(distance)
               .append(" FROM ")
               .append(center);
        }
        else
            buf.append(roi);
        return buf.toString();
    }
    
    
    public static class Builder extends SpatialFilterBuilder<Builder, SpatialFilter>
    {
        public Builder()
        {
            super(new SpatialFilter());
        }
    }
    
    
    /*
     * Nested builder for use within another builder
     */
    public static abstract class NestedBuilder<B> extends SpatialFilterBuilder<NestedBuilder<B>, SpatialFilter>
    {
        B parent;
        
        public NestedBuilder(B parent)
        {
            this.parent = parent;
            this.instance = new SpatialFilter();
        }
                
        public abstract B done();
    }
    
    
    @SuppressWarnings("unchecked")
    public static abstract class SpatialFilterBuilder<
            B extends SpatialFilterBuilder<B, T>,
            T extends SpatialFilter>
        extends BaseBuilder<T>
    {
        
        protected SpatialFilterBuilder()
        {
        }
        
        
        protected SpatialFilterBuilder(T instance)
        {
            super(instance);
        }
        
        
        public B withRoi(Geometry geom)
        {
            instance.roi = geom;
            instance.preparedGeom = PreparedGeometryFactory.prepare(geom);
            return (B)this;
        }
        
        
        public B withDistanceToPoint(Point center, double distance)
        {
            instance.center = center;
            instance.distance = distance;
            withRoi(center.buffer(distance, 1).getEnvelope());
            withOperator(SpatialOp.WITHIN_DISTANCE);
            return (B)this;
        }
        
        
        public B withDistanceToGeom(Geometry geom, double distance)
        {
            instance.center = geom.getCentroid();
            instance.distance = distance;
            withRoi(geom.getEnvelope().buffer(distance, 0));
            withOperator(SpatialOp.WITHIN_DISTANCE);
            return (B)this;
        }
        
        
        public B withBbox(Bbox bbox)
        {
            withRoi(bbox.toJtsPolygon());
            return (B)this;
        }
        
        
        public B withOperator(SpatialOp op)
        {
            if (instance.operator == op)
                return (B)this;
            
            if (instance.operator != null)
                throw new IllegalStateException("Operator is already set");
            
            instance.operator = op;
            final T instanceLocal = instance;
            
            switch (op)
            {
                case INTERSECTS:
                    instance.geomTest = (g -> instanceLocal.preparedGeom.intersects(g));
                    break;
                    
                case OVERLAPS:
                    instance.geomTest = (g -> instanceLocal.preparedGeom.overlaps(g));
                    break;
                    
                case CROSSES:
                    instance.geomTest = (g -> instanceLocal.preparedGeom.crosses(g));
                    break;
                    
                case TOUCHES:
                    instance.geomTest = (g -> instanceLocal.preparedGeom.touches(g));
                    break;
                    
                case EQUALS:
                    instance.geomTest = (g -> instanceLocal.preparedGeom.equals(g));
                    break;
                    
                case CONTAINS:
                    // use reverse operator so we can prepare the fixed geom
                    instance.geomTest = (g -> instanceLocal.preparedGeom.within(g));
                    break;
                    
                case WITHIN:
                    // use reverse operator so we can prepare the fixed geom
                    instance.geomTest = (g -> instanceLocal.preparedGeom.contains(g));
                    break;
                    
                case DISJOINT:
                    instance.geomTest = (g -> instanceLocal.preparedGeom.disjoint(g));
                    break;
                    
                case WITHIN_DISTANCE:
                    instance.geomTest = (g -> instanceLocal.center.isWithinDistance(g, instanceLocal.distance));
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unsupported operator " + op);
            }
            
            return (B)this;
        }
        
        
        @Override
        public T build()
        {
            Asserts.checkNotNull(instance.roi, "roi");
            
            if (instance.operator == null)
                withOperator(SpatialOp.INTERSECTS);
            
            if (instance.operator == SpatialOp.WITHIN_DISTANCE && Double.isNaN(instance.distance))
                throw new IllegalStateException(SpatialOp.WITHIN_DISTANCE + " operator must be set along with a distance value");
                   
            return super.build();
        }
    }
}
