window.org_sensorhub_ui_ProcessFlowDiagram = function() {
    
    var self = this;
    var graph = new joint.dia.Graph;        
    var popup = $('<div class="v-tooltip"></div>');
    var contextMenu = $('<div class="v-tooltip"></div>');
    const INPUTS_BLOCK = '__inputs';
    const PARAMS_BLOCK = '__parameters';
    const OUTPUTS_BLOCK = '__outputs';
    const COMPONENTS_PATH = 'components'
    
    var getProcessBlockByName = function(name) {
        
        if (name == INPUTS_BLOCK)
            return {id: INPUTS_BLOCK, inputs: self.getState().inputs}
        else if (name == PARAMS_BLOCK)
            return {id: PARAMS_BLOCK, param: self.getState().params}
        if (name == OUTPUTS_BLOCK)
            return {id: OUTPUTS_BLOCK, outputs: self.getState().outputs}
        
        var processBlock = self.getState().processBlocks[this.name];
        if (processBlock == null)
            processBlock = self.getState().dataSources[this.name];
        
        return processBlock;
    };
    
    // override element view to allow deleting, resizing and showing port popups
    var ProcessElementView = joint.dia.ElementView.extend({
        resizing: null,
        
        render: function () {
            joint.dia.ElementView.prototype.render.apply(this, arguments);

            var toolMarkup = this.model.toolMarkup || this.model.get('toolMarkup');
            if (toolMarkup) {
                var nodes = V(toolMarkup);
                V(this.el).append(nodes);
            }
            
            this.update();
            return this;
        },
        
        pointerdown: function(evt, x, y) {
            
            // block when popup is shown
            if (evt.button != 0 || popup.is(":visible"))
                return;
            
            // intercept resize action
            var pos = this.model.get('position');
            var size = this.model.get('size');
            var right = pos.x + size.width;
            var bottom = pos.y + size.height;
            if (Math.abs(x-right) < 10 && Math.abs(y-bottom) < 10) {
                this.resizing = {x0: x, y0: y};
                return;
            }
            
            // intercept magnet click to show popup
            if (evt.target.getAttribute('magnet') &&
                this.can('addLinkFromMagnet') &&
                this.paper.options.validateMagnet.call(this.paper, this, evt.target)) {

                var portName = evt.target.getAttribute('port');
                var subPorts = this.model.getSubPorts(portName); 
                if (subPorts.length > 1) {
                    popup.css({
                        'background-color': 'white',
                        position: "absolute",
                        top: evt.offsetY,
                        left: evt.offsetX
                    });
                    
                    var view = this;
                    var eventArgs = arguments;
                    popup.empty();
                    subPorts.forEach(function(port) {
                        var link = $('<div><a>' + port + '</a></div>').on("mousedown", function() {
                           eventArgs[0].srcPath = port;
                           popup.hide();
                           // resume normal pointer handling
                           view.paper.sourceView = view;
                           joint.dia.ElementView.prototype.pointerdown.apply(view, eventArgs);
                        });
                        popup.append(link);
                    })
                    
                    popup.show();
                    return;
                }
            }
            
            // default action
            joint.dia.ElementView.prototype.pointerdown.apply(this, arguments);
        },
        
        pointermove: function(evt, x, y) {
            
            // block when popup is shown
            if (evt.button != 0 || popup.is(":visible"))
                return;
                        
            // if resizing
            if (this.resizing != null) {
                var size = this.model.get('size');
                var newWidth = size.width + x-this.resizing.x0;
                var newHeight = size.height + y-this.resizing.y0;
                if (newWidth > 30 && newHeight > 30) {
                    this.model.resize(newWidth, newHeight);
                    this.resizing = {x0: x, y0: y};
                }
                return;
            } 
            
            // default action
            joint.dia.ElementView.prototype.pointermove.apply(this, arguments);
        },

        pointerup: function(evt, x, y) {
            
            // block when popup is shown
            if (evt.button != 0 || popup.is(":visible"))
                return;
            
            // complete resizing
            this.resizing = null;
            var className = evt.target.parentNode.getAttribute('class');
            
            // intercept target magnet click to show popup
            if (this._linkView) {
                var target = this._linkView.model.getTargetElement();
                if (target != null) {
                    var portName = this._linkView.model.get('target').port;
                    var subPorts = target.getSubPorts(portName); 
                    if (subPorts.length > 1) {
                        popup.css({
                            'background-color': 'white',
                            position: "absolute",
                            top: evt.offsetY,
                            left: evt.offsetX
                        });
                        
                        var view = this;
                        var eventArgs = arguments;
                        popup.empty();
                        subPorts.forEach(function(port) {
                            var link = $('<div><a>' + port + '</a></div>').on("mousedown", function() {
                               eventArgs[0].destPath = port;
                               popup.hide();
                               // resume normal pointer handling
                               view.paper.sourceView = view;
                               joint.dia.ElementView.prototype.pointerup.apply(view, eventArgs);
                            });
                            popup.append(link);
                        })
                        
                        popup.show();
                    }
                }
                
                joint.dia.ElementView.prototype.pointerup.apply(this, arguments);
                return;
            }
            
            // default action + update shape
            if (className != 'element-tool-remove') {
                var pos = this.model.get('position');
                var size = this.model.get('size');
                self.onChangeElement(this.model.name, pos.x, pos.y, size.width, size.height);
                joint.dia.ElementView.prototype.pointerup.apply(this, arguments);
            }
        },    

        pointerclick: function (evt, x, y) {
            this._dx = x;
            this._dy = y;
            this._action = '';

            // intercept click on remove icon
            var className = evt.target.parentNode.getAttribute('class');
            if (className == 'element-tool-remove') {
                this.model.remove();
            }

            // default action
            else {
                joint.dia.ElementView.prototype.pointerclick.apply(this, arguments);
            }
        },

        mouseenter: function(evt) {
            var position = this.model.get('position');
            var size = this.model.get('size');
            
            // intercept resize action
            var right = position.x + size.width;
            var bottom = position.y + size.height;
            if (Math.abs(evt.offsetX-right) < 10 && Math.abs(evt.offsetY-bottom) < 10) {
                evt.target.style.cursor = 'se-resize';
                evt.target.onmousemove = function(evt) {
                    if (Math.abs(evt.offsetX-right) > 10 || Math.abs(evt.offsetY-bottom) > 10) {
                        evt.target.style.cursor = null;
                        evt.target.onmousemove = null;
                    }
                }
            } else {
                evt.target.style.cursor = null;
            }

            joint.dia.ElementView.prototype.mouseenter.apply(this, arguments);
        },

        mouseleave: function(evt) {
            evt.target.style.cursor = null;
            joint.dia.ElementView.prototype.mouseleave.apply(this, arguments);
        }
    });
    
    
    // override LinkView to set path to sub-port
    var ProcessLinkView = joint.dia.LinkView.extend({
                
        pointerdown: function(evt, x, y) {
            if (evt.srcPath != 'undefined')
                this.setSourcePath(evt.srcPath);
            if (evt.destPath != 'undefined')
                this.setDestPath(evt.destPath); 
            joint.dia.LinkView.prototype.pointerdown.apply(this, arguments);
        },
        
        pointerup: function(evt, x, y) {
            if (evt.srcPath != 'undefined')
                this.setSourcePath(evt.srcPath);
            if (evt.destPath != 'undefined')
                this.setDestPath(evt.destPath);               
            joint.dia.LinkView.prototype.pointerup.apply(this, arguments);
        },
        
        setSourcePath: function(path) {
            this.model.get('source').path = path;
        },
        
        setDestPath: function(path) {
            this.model.get('target').path = path;
        }
    }); 

    
    // override element model to add remove icon and param ports
    var ProcessBlock = joint.shapes.devs.Atomic.extend({
        
        toolMarkup: ['<g class="element-tools">',
            '<g class="element-tool-remove"><circle fill="red" r="11"/>',
            '<path transform="scale(.8) translate(-16, -16)" d="M24.778,21.419 19.276,15.917 24.777,10.415 21.949,7.585 16.447,13.087 10.945,7.585 8.117,10.415 13.618,15.917 8.116,21.419 10.946,24.248 16.447,18.746 21.948,24.248z"/>',
            '<title>Remove this element from the model</title>',
            '</g>',
            '</g>'].join(''),
            
        defaults: _.defaultsDeep({
           ports: {
               groups: {
                 'in': {
                   attrs: {
                     '.port-body': { magnet: 'passive' }
                   }
                 },
                 'param': {
                   position: {
                     name: 'bottom'
                   },
                   attrs: {
                     '.port-label': { fill: '#000' },
                     '.port-body': { fill: '#fff', stroke: '#000', r: 10, magnet: 'passive' }
                   },
                   label: {
                     position: {
                        name: 'bottom',
                        args: {
                          x: -15,
                          y: 12
                        }
                     }
                   }
                 }
               }
           }
        }, joint.shapes.devs.Atomic.prototype.defaults),

        initialize: function() {
            this.on('change:paramPorts', this.updatePortItems, this);
            joint.shapes.devs.Atomic.prototype.initialize.apply(this, arguments);
        },

        updatePortItems: function(model, changed, opt) {
            // Make sure all ports are unique.
            var inPorts = _.uniq(this.get('inPorts'));
            var outPorts = _.difference(_.uniq(this.get('outPorts')), inPorts);
            var paramPorts = _.uniq(this.get('paramPorts'));

            var inPortItems = this.createPortItems('in', inPorts);            
            var outPortItems = this.createPortItems('out', outPorts);
            var paramPortItems = this.createPortItems('param', paramPorts);

            this.prop('ports/items', inPortItems.concat(outPortItems).concat(paramPortItems), _.extend({ rewrite: true }, opt));
        },

        addParamPort: function(port, opt) {
           return this._addGroupPort(port, 'paramPorts', opt);
        },
        
        getPortType: function(portName) {
            if (this.get('inPorts').indexOf(portName) >= 0)
                return "inputs";
            else if (this.get('outPorts').indexOf(portName) >= 0)
                return "outputs";
            else if (this.get('paramPorts').indexOf(portName) >= 0)
                return "parameters";
        },
        
        getSubPorts: function(portName) {
                        
            // get handle to process block listing all I/Os
            var processBlock = findProcessBlockByName(this.name);
            
            // find actual group this port belongs to
            var portList = processBlock.inputs;
            if (!portList.some(function(port) {return port.path == portName}))
                portList = processBlock.outputs;
            if (!portList.some(function(port) {return port.path == portName}))
                portList = processBlock.params;
            if (!portList.some(function(port) {return port.path == portName}))
                return [];
            
            // collect sub ports
            var subPorts = [];
            for (var i=0; i < portList.length; i++) {
                var port = portList[i];
                if (port.path.startsWith(portName))
                    subPorts.push(port.path);
            }
            
            return subPorts;
        }
    });
    
    
    // create paper area
    var paper = new joint.dia.Paper({

        el: this.getElement(),
        width: 1400,
        height: 800,
        gridSize: 10,
        model: graph,
        linkPinning: false,
        embeddingMode: true,
        elementView: ProcessElementView,
        linkView: ProcessLinkView,
        restrictTranslate: {
            x: 20, y: 10, width: 1350, height: 780
        },
        defaultLink: new joint.dia.Link({
            attrs: { '.marker-target': { d: 'M 10 0 L 0 5 L 10 10 z' } }
        }),
        snapLinks: {
            radius: 75
        },
        highlighting: {
            'default': {
                name: 'addClass',
                options: {
                    className: 'highlighted'
                }
            },
            'embedding': {
                name: 'addClass',
                options: {
                    className: 'highlighted-parent'
                }
            }
        },

        validateEmbedding: function(childView, parentView) {
            return parentView.model instanceof joint.shapes.devs.Coupled;
        },

        validateConnection: function(sourceView, sourceMagnet, targetView, targetMagnet) {
            return sourceMagnet != targetMagnet;
        }
    });
    
    popup.appendTo(paper.$el).hide();
    
    // select element
    self.highlightedCellView = null;
    paper.on('cell:pointerclick', function(cellView) {
        if (self.highlightedCellView != null)
            self.highlightedCellView.unhighlight();
        cellView.highlight();
        self.highlightedCellView = cellView;
    });
    
    // unselect element
    paper.on('blank:pointerclick', function() {
        if (self.highlightedCellView != null)
            self.highlightedCellView.unhighlight();
        self.highlightedCellView = null;
    });
    
    // on link connected
    paper.on('link:pointerup', function(link) {        
        var src = link.model.getSourceElement();
        var dest = link.model.getTargetElement();
        
        if (src != null && dest != null)
        {
            var srcPort = link.model.get('source');
            var destPort = link.model.get('target');
            var srcPortType = src.getPortType(srcPort.port);
            var destPortType = dest.getPortType(destPort.port);
            var srcPath = srcPortType + "/" + ((srcPort.path != 'undefined') ? srcPort.path : srcPort.port);
            var destPath = destPortType + "/" + ((destPort.path != 'undefined') ? destPort.path : destPort.port);
            if (src.name != "inputs" && src.name != "parameters")
                srcPath = "components/" + src.name + "/" + srcPath;
            if (dest.name != "outputs")
                destPath = "components/" + dest.name + "/" + destPath;
            self.onChangeLink(link.model.id, srcPath, destPath);
        }
    });
    
    // context menu
    contextMenu.appendTo(paper.$el).hide();
    paper.on('cell:contextmenu', function (cellView, evt) {
        if (evt.target.getAttribute('magnet')) {
            var processName = cellView.model.name;
            var portName = evt.target.getAttribute('port');
            var portGroup = evt.target.getAttribute('port-group');
            contextMenu.empty();
            
            if (processName == 'inputs') {
                contextMenu.append($('<div><a>Remove input</a></div>').click(function() {
                    self.onContextMenu('delInput', processName, portName);
                    contextMenu.hide();
                }));
            }
            else if (processName == 'params') {
                contextMenu.append($('<div><a>Remove parameter</a></div>').click(function() {
                    self.onContextMenu('delParam', processName, portName);
                    contextMenu.hide();
                }));
            }
            else if (processName == 'outputs') {
                contextMenu.append($('<div><a>Remove output</a></div>').click(function() {
                    self.onContextMenu('delOutput', processName, portName);
                    contextMenu.hide();
                }));
            }
            else if (portGroup == 'in') {
                contextMenu.append($('<div><a>Expose as input</a></div>').click(function() {
                    self.onContextMenu('addInput', processName, portName);
                    contextMenu.hide();
                }));
                contextMenu.append($('<div><a>Set value</a></div>').click(function() {
                    self.onContextMenu('setInput', processName, portName);
                    contextMenu.hide();
                }));
            }
            else if (portGroup == 'param') {
                contextMenu.append($('<div><a>Expose as parameter</a></div>').click(function() {
                    self.onContextMenu('addParam', processName, portName);
                    contextMenu.hide();
                }));
                contextMenu.append($('<div><a>setValue</a></div>').click(function() {
                    self.onContextMenu('setParam', processName, portName);
                    contextMenu.hide();
                }));
            }
            else if (portGroup == 'out') {
                contextMenu.append($('<div><a>Expose as output</a></div>').click(function() {
                    self.onContextMenu('addOutput', processName, portName);
                    contextMenu.hide();
                }));
            }
            
            contextMenu.css({
                'background-color': 'white',
                position: "absolute",
                top: evt.offsetY,
                left: evt.offsetX
            });
            
            contextMenu.show();
        }
    });

    // remove events
    graph.on('remove', function(cell) {
        if (cell.get('type') == 'link') {
            cell.disconnect();
            self.onRemoveLink(cell.id);
        } else {
            self.onRemoveElement(cell.name);
        }
    });
    
    // draw functions
    var drawBlocks = function(blockMap) {
        
        for (var key in blockMap) {
            var b = blockMap[key];
            
            // do nothing if already displayed
            if (graph.getCell(b.id) != null)
                continue;
            
            var shape = new ProcessBlock({
                id: b.id,
                position: {
                    x: b.x,
                    y: b.y
                },
                size: {
                    width: b.w,
                    height: b.h
                },
                attrs: {
                    '.label': {
                        text: b.name + "\n\n(" + b.type + ")"
                    },
                    '.body': {
                        'rx': 5,
                        'ry': 5
                    }
                }
            });
            shape.name = b.name;
            
            // add input ports
            for (var i=0; i < b.inputs.length; i++) {
                var port = b.inputs[i];
                if (!port.path.includes("/"))
                    shape.addInPort(port.path); 
            }
            
            // add output ports
            for (var i=0; i < b.outputs.length; i++) {
                var port = b.outputs[i];
                if (!port.path.includes("/"))
                    shape.addOutPort(port.path); 
            }
            
            // add param ports
            for (var i=0; i < b.params.length; i++) {
                var port = b.params[i];
                if (!port.path.includes("/"))
                    shape.addParamPort(port.path); 
            }

            graph.addCell(shape);
        }
    };
    
    var drawInputPorts = function(portList) {
        
        if (portList.length == 0)
            return;
        
        var shape = new ProcessBlock({
            id: INPUTS_BLOCK,
            position: {
                x: 10,
                y: 10
            },
            size: {
                width: 1,
                height: 30*portList.length
            },
            attrs: {
                '.label': {
                    text: "Inputs"
                },
                '.body': {
                    'rx': 5,
                    'ry': 5
                }
            }
        });        
        
        for (var i=0; i < portList.length; i++) {
            var port = portList[i];
            if (!port.path.includes("/"))
                shape.addInPort(port.path); 
        }
        
        shape.name = shape.id;
        graph.addCell(shape);
    };
    
    var drawOutputPorts = function(portList) {
        
        if (portList.length == 0)
            return;
        
        var shape = new ProcessBlock({
            id: OUTPUTS_BLOCK,
            position: {
                x: paper.width-10,
                y: 10
            },
            size: {
                width: 10,
                height: 30*portList.length
            },
            attrs: {
                '.label': {
                    text: "Outputs"
                },
                '.body': {
                    'rx': 5,
                    'ry': 5
                }
            }
        });        
        
        for (var i=0; i < portList.length; i++) {
            var port = portList[i];
            if (!port.path.includes("/"))
                shape.addOutPort(port.path); 
        }
        
        shape.name = shape.id;
        graph.addCell(shape);
    };
    
    var drawParamPorts = function(portList) {        
        if (portList.length == 0)
            return;
    };
    
    var drawConnections = function(connList) {
        
        for (var i=0; i < connList.length; i++) {
            var c = connList[i];
            
            var srcPath = c.src.split();
            var srcBlock, srcPort;
            if (srcPath[0] == COMPONENTS_PATH) {
                srcBlock = getProcessBlockByName(srcPath[1]);
                srcPort = srcPath[3];
            } else {
                srcBlock = getProcessBlockByName("__" + srcPath[0]);
                srcPort = srcPath[1];
            }
            
            var destPath = c.dest.split();
            var destBlock, destPort;
            if (destPath[0] == COMPONENTS_PATH) {
                destBlock = getProcessBlockByName(destPath[1]);
                destPort = destPath[3];
            } else {
                destBlock = getProcessBlockByName("__" + destPath[0]);
                destPort = destPath[1];
            }
                
            var link = new joint.shapes.devs.Link({
                source: {
                  id: srcBlock.id,
                  port: srcPort
                },
                target: {
                  id: destBlock.id,
                  port: destPort
                }
            });
             
            graph.addCell(link)
        }
    };
    
    // called if server modified state object
    this.onStateChange = function() {
        paper.setDimensions(1400, 800);
        drawInputPorts(this.getState().inputs);
        drawParamPorts(this.getState().params);
        drawOutputPorts(this.getState().outputs);
        drawBlocks(this.getState().dataSources);
        drawBlocks(this.getState().processBlocks);
        drawConnections(this.getState().connections);
    };
    
    
}