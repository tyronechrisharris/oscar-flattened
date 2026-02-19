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
import org.sensorhub.api.datastore.command.CommandFilter;
import com.vaadin.event.Action;
import com.vaadin.v7.ui.TreeTable;


@SuppressWarnings({"deprecation"})
public class CommandFilterTree extends FilterTree<CommandFilter, CommandFilter.Builder>
{
    private static final String ISSUETIME_PROPERTY = "Issue Time";
    private static final String CMDSTREAMFILTER_PROPERTY = "With Datastreams";
    
    
    static void getActions(Class<?> filterClass, List<Action> actions)
    {
        ResourceFilterTree.getActions(filterClass, actions);
    }
    
    
    static CommandFilter newFilter()
    {
        return new CommandFilter.Builder().build();
    }
    
    
    @Override
    protected Object renderFilterAsTree(TreeTable tree, Object parentId, CommandFilter filter)
    {
        tree.setPageLength(tree.getPageLength()+3);
        var id = tree.addItem(new Object[] {"Command Filter", null}, null);
        if (parentId != null)
            tree.setParent(id, parentId);
        
        toTreeItem(tree, id, ISSUETIME_PROPERTY, filter.getIssueTime());
        toTreeItem(tree, id, CMDSTREAMFILTER_PROPERTY, DataStreamFilterTree::newFilter, filter.getCommandStreamFilter());
        
        return id;
    }
    

    @Override
    protected CommandFilter buildFilterFromTree(TreeTable tree, Object parent)
    {
        var builder = new CommandFilter.Builder();
        super.buildFilterFromTree(tree, parent, builder);
        return builder.build();
    }


    @Override
    protected void fromTreeItem(TreeTable tree, Object itemId, String itemName, String itemValue, CommandFilter.Builder builder)
    {
        if (ISSUETIME_PROPERTY.equals(itemName))
        {
            var tf = readTemporalFilter(itemValue);
            builder.withIssueTime(tf);
        }
        else if (CMDSTREAMFILTER_PROPERTY.equals(itemName) && Boolean.parseBoolean(itemValue))
        {
            var rootItemId = tree.getChildren(itemId).iterator().next();
            var subTree = new CommandStreamFilterTree();
            var filter = subTree.buildFilterFromTree(tree, rootItemId);
            builder.withCommandStreams(filter);
        }
    }

}
