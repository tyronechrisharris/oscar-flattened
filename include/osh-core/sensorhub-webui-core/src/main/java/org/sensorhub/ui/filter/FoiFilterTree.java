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
import org.sensorhub.api.datastore.feature.FoiFilter;
import com.vaadin.event.Action;
import com.vaadin.v7.ui.TreeTable;


@SuppressWarnings({"deprecation"})
public class FoiFilterTree extends FeatureFilterBaseTree<FoiFilter, FoiFilter.Builder>
{
    
    static void getActions(Class<?> filterClass, List<Action> actions)
    {
        FeatureFilterBaseTree.getActions(filterClass, actions);
    }
    
    
    static FoiFilter newFilter()
    {
        return new FoiFilter.Builder().build();
    }
    
    
    @Override
    protected Object renderFilterAsTree(TreeTable tree, Object parentId, FoiFilter filter)
    {
        tree.setPageLength(tree.getPageLength()+5);
        var id = tree.addItem(new Object[] {"Foi Filter", null}, null);
        if (parentId != null)
            tree.setParent(id, parentId);
        
        super.renderFilterAsTree(tree, id, filter);
        
        return id;
    }
    

    @Override
    protected FoiFilter buildFilterFromTree(TreeTable tree, Object parentId)
    {
        var builder = new FoiFilter.Builder();
        super.buildFilterFromTree(tree, parentId, builder);
        return builder.build();
    }


    @Override
    protected void fromTreeItem(TreeTable tree, Object itemId, String itemName, String itemValue, FoiFilter.Builder builder)
    {
        super.fromTreeItem(tree, itemId, itemName, itemValue, builder);
    }

}
