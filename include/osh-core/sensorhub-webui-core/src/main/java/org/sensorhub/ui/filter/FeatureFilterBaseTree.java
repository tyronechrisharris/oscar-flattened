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
import org.sensorhub.api.datastore.feature.FeatureFilterBase;
import org.sensorhub.api.datastore.feature.FeatureFilterBase.FeatureFilterBaseBuilder;
import com.vaadin.event.Action;
import com.vaadin.server.ThemeResource;
import com.vaadin.v7.ui.TreeTable;


@SuppressWarnings({"deprecation"})
public abstract class FeatureFilterBaseTree<T extends FeatureFilterBase<?>, B extends FeatureFilterBaseBuilder<B,?,T>> extends ResourceFilterTree<T,B>
{
    private static final Action ADD_UIDS_ACTION = new Action("Add System UIDs", new ThemeResource("icons/add.gif"));
    private static final Action ADD_VALIDTIME_ACTION = new Action("Add Validity Time Range", new ThemeResource("icons/add.gif"));
    
    private static final String UNIQUEIDS_PROPERTY = "Unique IDs";
    private static final String VALIDTIME_PROPERTY = "Valid Time";
    private static final String LOCATION_PROPERTY = "Location";
    
    

    static void getActions(Class<?> filterClass, List<Action> actions)
    {
        ResourceFilterTree.getActions(filterClass, actions);
        actions.add(ADD_UIDS_ACTION);
        actions.add(ADD_VALIDTIME_ACTION);
    }
    

    @Override
    protected Object renderFilterAsTree(TreeTable tree, Object parentId, T filter)
    {
        super.renderFilterAsTree(tree, parentId, filter);
        toTreeItem(tree, parentId, UNIQUEIDS_PROPERTY, filter.getUniqueIDs());
        toTreeItem(tree, parentId, VALIDTIME_PROPERTY, filter.getValidTime());
        toTreeItem(tree, parentId, LOCATION_PROPERTY, filter.getLocationFilter());
        return null;
    }
    

    @Override
    protected void fromTreeItem(TreeTable tree, Object itemId, String itemName, String itemValue, B builder)
    {
        super.fromTreeItem(tree, itemId, itemName, itemValue, builder);
        
        if (UNIQUEIDS_PROPERTY.equals(itemName))
        {
            var uids = readStringList(itemValue);
            builder.withUniqueIDs(uids);
        }
        else if (VALIDTIME_PROPERTY.equals(itemName))
        {
            var tf = readTemporalFilter(itemValue);
            builder.withValidTime(tf);
        }
        else if (LOCATION_PROPERTY.equals(itemName))
        {
            var sf = readSpatialFilter(itemValue);
            builder.withLocation(sf);
        }
    }

}
