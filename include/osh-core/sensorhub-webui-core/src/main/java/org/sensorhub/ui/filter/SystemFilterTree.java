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
import org.sensorhub.api.datastore.system.SystemFilter;
import com.vaadin.event.Action;
import com.vaadin.v7.ui.TreeTable;


@SuppressWarnings({"deprecation"})
public class SystemFilterTree extends FeatureFilterBaseTree<SystemFilter, SystemFilter.Builder>
{
    private static final String PARENTFILTER_PROPERTY = "With Parent Systems";
    private static final String DATASTREAMFILTER_PROPERTY = "With Datastreams";
    
    
    static void getActions(Class<?> filterClass, List<Action> actions)
    {
        FeatureFilterBaseTree.getActions(filterClass, actions);
    }
    
    
    static SystemFilter newFilter()
    {
        return new SystemFilter.Builder().build();
    }
    
    
    @Override
    protected Object renderFilterAsTree(TreeTable tree, Object parentId, SystemFilter filter)
    {
        tree.setPageLength(tree.getPageLength()+7);
        var id = tree.addItem(new Object[] {"System Filter", null}, null);
        if (parentId != null)
            tree.setParent(id, parentId);
        
        super.renderFilterAsTree(tree, id, filter);
        toTreeItem(tree, id, PARENTFILTER_PROPERTY, SystemFilterTree::newFilter, filter.getParentFilter());
        toTreeItem(tree, id, DATASTREAMFILTER_PROPERTY, DataStreamFilterTree::newFilter, filter.getDataStreamFilter());
        
        return id;
    }
    

    @Override
    protected SystemFilter buildFilterFromTree(TreeTable tree, Object parentId)
    {
        var builder = new SystemFilter.Builder();
        super.buildFilterFromTree(tree, parentId, builder);
        return builder.build();
    }


    @Override
    protected void fromTreeItem(TreeTable tree, Object itemId, String itemName, String itemValue, SystemFilter.Builder builder)
    {
        super.fromTreeItem(tree, itemId, itemName, itemValue, builder);
        
        if (PARENTFILTER_PROPERTY.equals(itemName) && Boolean.parseBoolean(itemValue))
        {
            var rootItemId = tree.getChildren(itemId).iterator().next();
            var subTree = new SystemFilterTree();
            var filter = subTree.buildFilterFromTree(tree, rootItemId);
            builder.withParents(filter);
        }
        else if (DATASTREAMFILTER_PROPERTY.equals(itemName) && Boolean.parseBoolean(itemValue))
        {
            var rootItemId = tree.getChildren(itemId).iterator().next();
            var subTree = new DataStreamFilterTree();
            var filter = subTree.buildFilterFromTree(tree, rootItemId);
            builder.withDataStreams(filter);
        }
    }

}
