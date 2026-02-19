/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.FullTextFilter;
import org.sensorhub.api.datastore.IQueryFilter;
import org.sensorhub.api.datastore.SpatialFilter;
import org.sensorhub.api.datastore.TemporalFilter;
import org.sensorhub.api.datastore.command.CommandFilter;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.feature.FoiFilter;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.vast.util.BaseBuilder;
import org.vast.util.TimeExtent;
import com.google.common.collect.Lists;
import com.vaadin.event.Action;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.ui.TreeTable;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;


@SuppressWarnings({"deprecation"})
public abstract class FilterTree<T extends IQueryFilter, B extends BaseBuilder<T>>
{
    static final String PROP_NAME = "name";
    static final String PROP_VALUE = "val";
    static final String PROP_CALLBACK = "callback";
    
    
    /*
    Methods to render filter values as a tree
    */
    
    protected abstract Object renderFilterAsTree(TreeTable tree, Object parentId, T filter);
    
    
    static void getActions(Class<?> filterClass, List<Action> actions)
    {
        if (SystemFilter.class.equals(filterClass))
            SystemFilterTree.getActions(filterClass, actions);
        else
            throw new IllegalStateException("Unsupported filter type: " + filterClass);
    }
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static FilterTree<IQueryFilter,?> create(Class<?> filterClass)
    {
        if (SystemFilter.class.equals(filterClass))
            return (FilterTree)new SystemFilterTree();
        else if (DataStreamFilter.class.equals(filterClass))
            return (FilterTree)new DataStreamFilterTree();
        else if (CommandStreamFilter.class.equals(filterClass))
            return (FilterTree)new CommandStreamFilterTree();
        else if (ObsFilter.class.equals(filterClass))
            return (FilterTree)new ObsFilterTree();
        else if (CommandFilter.class.equals(filterClass))
            return (FilterTree)new CommandFilterTree();
        else if (FoiFilter.class.equals(filterClass))
            return (FilterTree)new FoiFilterTree();
        else
            throw new IllegalStateException("Unsupported filter type: " + filterClass);
    }
    
    
    protected Object toTreeItem(TreeTable tree, Object parentId, String name, String value)
    {
        var id = tree.addItem(new Object[]{name, value}, null);
        tree.setParent(id, parentId);
        tree.setChildrenAllowed(id, false);
        return id;
    }
    
    
    protected Object toTreeItem(TreeTable tree, Object parentId, String name, Collection<? extends Object> prop)
    {
        if (prop != null && !prop.isEmpty())
        {
            var sb = new StringBuilder();
            prop.stream().forEach(elt -> {
                String s = elt instanceof BigId ? BigId.toString32((BigId)elt) : elt.toString();
                sb.append(s).append('\n');
            });
            sb.setLength(sb.length()-1);
            return toTreeItem(tree, parentId, name, sb.toString());
        }
        else 
            return toTreeItem(tree, parentId, name, "");
    }
    
    
    protected Object toTreeItem(TreeTable tree, Object parentId, String name, FullTextFilter textFilter)
    {
        if (textFilter != null)
            return toTreeItem(tree, parentId, name, textFilter.getKeywords());
        else
            return toTreeItem(tree, parentId, name, "");
    }
    
    
    protected Object toTreeItem(TreeTable tree, Object parentId, String name, TemporalFilter timeFilter)
    {
        if (timeFilter != null)
        {
            var sb = new StringBuilder();
            
            if (timeFilter.isLatestTime())
                sb.append("latest");
            else if (timeFilter.isCurrentTime())
                sb.append("current");
            else
                sb.append(timeFilter.asTimeExtent().isoStringUTC(true));
            
            return toTreeItem(tree, parentId, name, sb.toString());
        }
        else 
            return toTreeItem(tree, parentId, name, "");
    }
    
    
    protected Object toTreeItem(TreeTable tree, Object parentId, String name, SpatialFilter roiFilter)
    {
        if (roiFilter != null)
        {
            var sb = new StringBuilder();
            sb.append(new WKTWriter().write(roiFilter.getRoi()));
            return toTreeItem(tree, parentId, name, sb.toString());
        }
        else 
            return toTreeItem(tree, parentId, name, "");
    }
    
    
    @SuppressWarnings("unchecked")
    protected Object toTreeItem(TreeTable tree, Object parentId, String name, Supplier<? extends IQueryFilter> newFilter, IQueryFilter nestedFilter)
    {
        boolean hasFilter = nestedFilter != null;
        
        var id = tree.addItem();
        var newItem = tree.getItem(id);
        newItem.getItemProperty(PROP_NAME).setValue(name);
        newItem.getItemProperty(PROP_VALUE).setValue(Boolean.toString(hasFilter));
        tree.setParent(id, parentId);
        tree.setChildrenAllowed(id, false);
        
        newItem.getItemProperty(PROP_CALLBACK).setValue(new Consumer<Boolean>() {
            boolean enabled;
            
            @Override
            public void accept(Boolean enable)
            {
                // use enabled flag since we need to break cycle due to
                // checkbox redrawing when items are added/removed
                if (enable && !enabled)
                {
                    enabled = true;
                    if (!tree.hasChildren(id))
                    {
                        var f = newFilter.get();
                        renderSubFilterTree(tree, id, f);
                    }
                }
                else if (!enable && enabled)
                {
                    enabled = false;
                    int count = removeChildrenRecursively(tree, id);
                    tree.setPageLength(tree.getPageLength()-count);
                    tree.setChildrenAllowed(id, false);
                }
            }
        });
        
        if (nestedFilter != null)
            renderSubFilterTree(tree, id, nestedFilter);
        
        return id;
    }
    
    
    int removeChildrenRecursively(TreeTable tree, Object parentId)
    {
        int count = 0;
        if (tree.hasChildren(parentId))
        {
            var children = new ArrayList<>(tree.getChildren(parentId));
            for (var itemId: children)
            {
                count += removeChildrenRecursively(tree, itemId);
                tree.removeItem(itemId);
                count++;
            }
        }
        return count;
    }
    
    
    Object renderSubFilterTree(TreeTable tree, Object parentId, IQueryFilter filter)
    {
        tree.setChildrenAllowed(parentId, true);
        var ft = FilterTree.create(filter.getClass());
        var id = ft.renderFilterAsTree(tree, parentId, filter);
        
        // expand all items
        tree.setCollapsed(parentId, false);
        for (Object itemId: tree.getChildren(parentId))
            tree.setCollapsed(itemId, false);
        
        return id;
    }
    
    
    /*
     * Methods to read filter values from tree
     */
    
    protected abstract T buildFilterFromTree(TreeTable tree, Object parentId);
    
    
    protected B buildFilterFromTree(TreeTable tree, Object parentId, B builder)
    {
        for (var itemId: tree.getChildren(parentId))
        {
            Item item = tree.getItem(itemId);
            String propName = (String)item.getItemProperty(PROP_NAME).getValue();
            String propStr = (String)item.getItemProperty(PROP_VALUE).getValue();
            if (propStr != null && !propStr.isBlank())
                fromTreeItem(tree, itemId, propName, propStr, builder);
        }
        
        return builder;
    }
    
    
    protected abstract void fromTreeItem(TreeTable tree, Object parentId, String itemName, String itemValue, B builder);
    
    
    protected Collection<BigId> readIdList(String itemValue)
    {
        var tokens = itemValue.split(",|\n");
        return Lists.transform(Arrays.asList(tokens), s -> BigId.fromString32(s));
    }
    
    
    protected Collection<String> readStringList(String itemValue)
    {
        var tokens = itemValue.split(",|\n");
        return Arrays.asList(tokens);
    }
    
    
    protected TemporalFilter readTemporalFilter(String itemValue)
    {
        if ("latest".equals(itemValue))
        {
            return new TemporalFilter.Builder()
                .withLatestTime()
                .build();
        }
        else if ("current".equals(itemValue))
        {
            return new TemporalFilter.Builder()
                .withCurrentTime()
                .build();
        }
        else
        {
            return new TemporalFilter.Builder()
                .fromTimeExtent(TimeExtent.parse(itemValue))
                .build();
        }
    }
    
    
    protected SpatialFilter readSpatialFilter(String itemValue)
    {
        try
        {
            var roi = new WKTReader().read(itemValue);
            return new SpatialFilter.Builder()
                .withRoi(roi)
                .build();
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Invalid filter property", e);
        }
    }
}
