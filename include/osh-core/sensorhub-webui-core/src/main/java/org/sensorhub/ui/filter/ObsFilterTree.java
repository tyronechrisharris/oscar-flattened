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

import java.util.List;
import org.sensorhub.api.datastore.obs.ObsFilter;
import com.vaadin.event.Action;
import com.vaadin.v7.ui.TreeTable;


@SuppressWarnings({"deprecation"})
public class ObsFilterTree extends FilterTree<ObsFilter, ObsFilter.Builder>
{
    private static final String PHENTIME_PROPERTY = "Phenomenon Time";
    private static final String RESULTTIME_PROPERTY = "Result Time";
    private static final String DATASTREAMFILTER_PROPERTY = "With Datastreams";
    private static final String FOIFILTER_PROPERTY = "With Fois";
    
    
    static void getActions(Class<?> filterClass, List<Action> actions)
    {
        ResourceFilterTree.getActions(filterClass, actions);
    }
    
    
    static ObsFilter newFilter()
    {
        return new ObsFilter.Builder().build();
    }
    
    
    @Override
    protected Object renderFilterAsTree(TreeTable tree, Object parentId, ObsFilter filter)
    {
        tree.setPageLength(tree.getPageLength()+5);
        var id = tree.addItem(new Object[] {"Obs Filter", null}, null);
        if (parentId != null)
            tree.setParent(id, parentId);
        
        toTreeItem(tree, id, PHENTIME_PROPERTY, filter.getPhenomenonTime());
        toTreeItem(tree, id, RESULTTIME_PROPERTY, filter.getResultTime());
        toTreeItem(tree, id, DATASTREAMFILTER_PROPERTY, DataStreamFilterTree::newFilter, filter.getDataStreamFilter());
        toTreeItem(tree, id, FOIFILTER_PROPERTY, FoiFilterTree::newFilter, filter.getFoiFilter());
        
        return id;
    }
    

    @Override
    protected ObsFilter buildFilterFromTree(TreeTable tree, Object parent)
    {
        var builder = new ObsFilter.Builder();
        super.buildFilterFromTree(tree, parent, builder);
        return builder.build();
    }


    @Override
    protected void fromTreeItem(TreeTable tree, Object itemId, String itemName, String itemValue, ObsFilter.Builder builder)
    {
        if (PHENTIME_PROPERTY.equals(itemName))
        {
            var tf = readTemporalFilter(itemValue);
            builder.withPhenomenonTime(tf);
        }
        else if (RESULTTIME_PROPERTY.equals(itemName))
        {
            var tf = readTemporalFilter(itemValue);
            builder.withResultTime(tf);
        }
        else if (DATASTREAMFILTER_PROPERTY.equals(itemName) && Boolean.parseBoolean(itemValue))
        {
            var rootItemId = tree.getChildren(itemId).iterator().next();
            var subTree = new DataStreamFilterTree();
            var filter = subTree.buildFilterFromTree(tree, rootItemId);
            builder.withDataStreams(filter);
        }
        else if (FOIFILTER_PROPERTY.equals(itemName) && Boolean.parseBoolean(itemValue))
        {
            var rootItemId = tree.getChildren(itemId).iterator().next();
            var subTree = new FoiFilterTree();
            var filter = subTree.buildFilterFromTree(tree, rootItemId);
            builder.withFois(filter);
        }
        
        
    }

}
