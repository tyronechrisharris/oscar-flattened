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
import org.sensorhub.api.resource.ResourceFilter;
import org.sensorhub.api.resource.ResourceFilter.ResourceFilterBuilder;
import com.vaadin.event.Action;
import com.vaadin.server.ThemeResource;
import com.vaadin.v7.ui.TreeTable;


@SuppressWarnings({"deprecation"})
public abstract class ResourceFilterTree<T extends ResourceFilter<?>, B extends ResourceFilterBuilder<B,?,T>> extends FilterTree<T, B>
{
    private static final Action ADD_IDS_ACTION = new Action("Add Resource IDs", new ThemeResource("icons/add.gif"));
    private static final Action ADD_KEYWORDS_ACTION = new Action("Add Keywords", new ThemeResource("icons/add.gif"));
    
    private static final String INTERNALIDS_PROPERTY = "Internal IDs";
    private static final String FULLTEXT_PROPERTY = "Keywords";
    
    
    static void getActions(Class<?> filterClass, List<Action> actions)
    {
        actions.add(ADD_IDS_ACTION);
        actions.add(ADD_KEYWORDS_ACTION);
    }
    
    
    @Override
    protected Object renderFilterAsTree(TreeTable tree, Object parentId, T filter)
    {
        //toTreeItem(tree, parentId, INTERNALIDS_PROPERTY, filter.getInternalIDs());
        toTreeItem(tree, parentId, FULLTEXT_PROPERTY, filter.getFullTextFilter());
        return null;
    }
    
    
    @Override
    protected void fromTreeItem(TreeTable tree, Object itemId, String itemName, String itemValue, B builder)
    {
        if (INTERNALIDS_PROPERTY.equals(itemName))
        {
            var ids = readIdList(itemValue);
            builder.withInternalIDs(ids);
        }
        else if (FULLTEXT_PROPERTY.equals(itemName))
        {
            var kw = readStringList(itemValue);
            builder.withKeywords(kw);
        }
    }

}
