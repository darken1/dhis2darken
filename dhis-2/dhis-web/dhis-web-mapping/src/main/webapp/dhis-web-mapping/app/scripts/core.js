Ext.onReady( function() {

	// ext config
	Ext.Ajax.method = 'GET';

	// gis
	GIS = {
		core: {
			instances: []
		},
		i18n: {},
		isDebug: false,
		isSessionStorage: 'sessionStorage' in window && window['sessionStorage'] !== null,
		logg: []
	};

	GIS.core.getOLMap = function(gis) {
		var olmap,
			addControl;

		addControl = function(name, fn) {
			var button,
				panel;

			button = new OpenLayers.Control.Button({
				displayClass: 'olControlButton',
				trigger: function() {
					fn.call(gis.olmap);
				}
			});

			panel = new OpenLayers.Control.Panel({
				defaultControl: button
			});

			panel.addControls([button]);

			olmap.addControl(panel);

			panel.div.className += ' ' + name;
			panel.div.childNodes[0].className += ' ' + name + 'Button';
		};

		olmap = new OpenLayers.Map({
			controls: [
				new OpenLayers.Control.Navigation({
					zoomWheelEnabled: true,
					documentDrag: true
				}),
				new OpenLayers.Control.MousePosition({
					prefix: '<span id="mouseposition" class="el-fontsize-10"><span class="text-mouseposition-lonlat">LON </span>',
					separator: '<span class="text-mouseposition-lonlat">,&nbsp;LAT </span>',
					suffix: '<div id="google-logo" name="http://www.google.com/intl/en-US_US/help/terms_maps.html" onclick="window.open(Ext.get(this).dom.attributes.name.nodeValue);"></div></span>'
				}),
				new OpenLayers.Control.Permalink(),
				new OpenLayers.Control.ScaleLine({
					geodesic: true,
					maxWidth: 170,
					minWidth: 100
				})
			],
			displayProjection: new OpenLayers.Projection('EPSG:4326'),
			maxExtent: new OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508),
			mouseMove: {}, // Track all mouse moves
			relocate: {} // Relocate organisation units
		});

		// Map events
		olmap.events.register('mousemove', null, function(e) {
			gis.olmap.mouseMove.x = e.clientX;
			gis.olmap.mouseMove.y = e.clientY;
		});

		olmap.zoomToVisibleExtent = function() {
			gis.util.map.zoomToVisibleExtent(this);
		};

		olmap.closeAllLayers = function() {
			gis.layer.event.core.reset();
			gis.layer.facility.core.reset();
			gis.layer.boundary.core.reset();
			gis.layer.thematic1.core.reset();
			gis.layer.thematic2.core.reset();
			gis.layer.thematic3.core.reset();
			gis.layer.thematic4.core.reset();
		};

		addControl('zoomIn', olmap.zoomIn);
		addControl('zoomOut', olmap.zoomOut);
		addControl('zoomVisible', olmap.zoomToVisibleExtent);
		addControl('measure', function() {
			GIS.core.MeasureWindow(gis).show();
		});

		return olmap;
	};

	GIS.core.getLayers = function(gis) {
		var layers = {},
			createSelectionHandlers,
			layerNumbers = ['1', '2', '3', '4'];

		if (window.google) {
			layers.googleStreets = new OpenLayers.Layer.Google('Google Streets', {
				numZoomLevels: 20,
				animationEnabled: true,
				layerType: gis.conf.finals.layer.type_base,
				layerOpacity: 1,
				setLayerOpacity: function(number) {
					if (number) {
						this.layerOpacity = parseFloat(number);
					}
					this.setOpacity(this.layerOpacity);
				}
			});
			layers.googleStreets.id = 'googleStreets';

			layers.googleHybrid = new OpenLayers.Layer.Google('Google Hybrid', {
				type: google.maps.MapTypeId.HYBRID,
				numZoomLevels: 20,
				animationEnabled: true,
				layerType: gis.conf.finals.layer.type_base,
				layerOpacity: 1,
				setLayerOpacity: function(number) {
					if (number) {
						this.layerOpacity = parseFloat(number);
					}
					this.setOpacity(this.layerOpacity);
				}
			});
			layers.googleHybrid.id = 'googleHybrid';
		}

		layers.openStreetMap = new OpenLayers.Layer.OSM.Mapnik('OpenStreetMap', {
			layerType: gis.conf.finals.layer.type_base,
			layerOpacity: 1,
			setLayerOpacity: function(number) {
				if (number) {
					this.layerOpacity = parseFloat(number);
				}
				this.setOpacity(this.layerOpacity);
			}
		});
		layers.openStreetMap.id = 'openStreetMap';

		layers.event = GIS.core.VectorLayer(gis, 'event', GIS.i18n.event_layer, {opacity: gis.conf.layout.layer.opacity});
		layers.event.core = new mapfish.GeoStat.Event(gis.olmap, {
			layer: layers.event,
			gis: gis
		});

		layers.facility = GIS.core.VectorLayer(gis, 'facility', GIS.i18n.facility_layer, {opacity: 1});
		layers.facility.core = new mapfish.GeoStat.Facility(gis.olmap, {
			layer: layers.facility,
			gis: gis
		});

		layers.boundary = GIS.core.VectorLayer(gis, 'boundary', GIS.i18n.boundary_layer, {opacity: gis.conf.layout.layer.opacity});
		layers.boundary.core = new mapfish.GeoStat.Boundary(gis.olmap, {
			layer: layers.boundary,
			gis: gis
		});

		for (var i = 0, number; i < layerNumbers.length; i++) {
			number = layerNumbers[i];

			layers['thematic' + number] = GIS.core.VectorLayer(gis, 'thematic' + number, GIS.i18n.thematic_layer + ' ' + number, {opacity: gis.conf.layout.layer.opacity});
			layers['thematic' + number].layerCategory = gis.conf.finals.layer.category_thematic,
			layers['thematic' + number].core = new mapfish.GeoStat['Thematic' + number](gis.olmap, {
				layer: layers['thematic' + number],
				gis: gis
			});
		}

		return layers;
	};

	GIS.core.createSelectHandlers = function(gis, layer) {
		var isRelocate = !!GIS.app ? !!gis.init.user.isAdmin : false,
			options = {},
			infrastructuralPeriod,
			defaultHoverSelect,
			defaultHoverUnselect,
			defaultClickSelect,
            selectHandlers,
			dimConf = gis.conf.finals.dimension,
            defaultHoverWindow,
            eventWindow,
            isBoundary = layer.id === 'boundary',
            isEvent = layer.id === 'event';

		defaultHoverSelect = function fn(feature) {
            if (isBoundary) {
                var style = layer.core.getDefaultFeatureStyle();

                style.fillOpacity = 0.15;
                style.strokeColor = feature.style.strokeColor;
                style.strokeWidth = feature.style.strokeWidth;
                style.label = feature.style.label;
                style.fontFamily = feature.style.fontFamily;
                style.fontWeight = feature.style.strokeWidth > 1 ? 'bold' : 'normal';
                style.labelAlign = feature.style.labelAlign;
                style.labelYOffset = feature.style.labelYOffset;

                layer.drawFeature(feature, style);
            }

			if (defaultHoverWindow) {
				defaultHoverWindow.destroy();
			}
			defaultHoverWindow = Ext.create('Ext.window.Window', {
				cls: 'gis-window-widget-feature gis-plugin',
				preventHeader: true,
				shadow: false,
				resizable: false,
				items: {
					html: feature.attributes.label
				}
			});

			defaultHoverWindow.show();

			var eastX = gis.viewport.eastRegion.getPosition()[0],
				centerX = gis.viewport.centerRegion.getPosition()[0],
				centerRegionCenterX = centerX + ((eastX - centerX) / 2),
				centerRegionY = gis.viewport.centerRegion.getPosition()[1] + (GIS.app ? 32 : 0);

			defaultHoverWindow.setPosition(centerRegionCenterX - (defaultHoverWindow.getWidth() / 2), centerRegionY);
		};

		defaultHoverUnselect = function fn(feature) {
			defaultHoverWindow.destroy();
		};

		defaultClickSelect = function fn(feature) {
			var showInfo,
				showRelocate,
				drill,
				menu,
				selectHandlers,
				isPoint = feature.geometry.CLASS_NAME === gis.conf.finals.openLayers.point_classname,
				att = feature.attributes;

			// Relocate
			showRelocate = function() {
				if (gis.olmap.relocate.window) {
					gis.olmap.relocate.window.destroy();
				}

				gis.olmap.relocate.window = Ext.create('Ext.window.Window', {
					title: 'Relocate facility',
					layout: 'fit',
					iconCls: 'gis-window-title-icon-relocate',
					cls: 'gis-container-default',
					setMinWidth: function(minWidth) {
						this.setWidth(this.getWidth() < minWidth ? minWidth : this.getWidth());
					},
					items: {
						html: att.name,
						cls: 'gis-container-inner'
					},
					bbar: [
						'->',
						{
							xtype: 'button',
							hideLabel: true,
							text: GIS.i18n.cancel,
							handler: function() {
								gis.olmap.relocate.active = false;
								gis.olmap.relocate.window.destroy();
								gis.olmap.getViewport().style.cursor = 'auto';
							}
						}
					],
					listeners: {
						close: function() {
							gis.olmap.relocate.active = false;
							gis.olmap.getViewport().style.cursor = 'auto';
						}
					}
				});

				gis.olmap.relocate.window.show();
				gis.olmap.relocate.window.setMinWidth(220);

				gis.util.gui.window.setPositionTopRight(gis.olmap.relocate.window);
			};

			// Infrastructural data
			showInfo = function() {
				Ext.Ajax.request({
					url: gis.init.contextPath + '/api/organisationUnits/' + att.id + '.json?links=false',
					success: function(r) {
						var ou = Ext.decode(r.responseText);

						if (layer.infrastructuralWindow) {
							layer.infrastructuralWindow.destroy();
						}

						layer.infrastructuralWindow = Ext.create('Ext.window.Window', {
							title: GIS.i18n.information,
							layout: 'column',
							iconCls: 'gis-window-title-icon-information',
							cls: 'gis-container-default',
							width: 460,
							height: 400, //todo
							period: null,
							items: [
								{
									cls: 'gis-container-inner',
									columnWidth: 0.4,
									bodyStyle: 'padding-right:4px',
									items: function() {
										var a = [];

										if (att.name) {
											a.push({html: GIS.i18n.name, cls: 'gis-panel-html-title'}, {html: att.name, cls: 'gis-panel-html'}, {cls: 'gis-panel-html-separator'});
										}

										if (ou.parent) {
											a.push({html: GIS.i18n.parent_unit, cls: 'gis-panel-html-title'}, {html: ou.parent.name, cls: 'gis-panel-html'}, {cls: 'gis-panel-html-separator'});
										}

										if (ou.code) {
											a.push({html: GIS.i18n.code, cls: 'gis-panel-html-title'}, {html: ou.code, cls: 'gis-panel-html'}, {cls: 'gis-panel-html-separator'});
										}

										if (ou.address) {
											a.push({html: GIS.i18n.address, cls: 'gis-panel-html-title'}, {html: ou.address, cls: 'gis-panel-html'}, {cls: 'gis-panel-html-separator'});
										}

										if (ou.email) {
											a.push({html: GIS.i18n.email, cls: 'gis-panel-html-title'}, {html: ou.email, cls: 'gis-panel-html'}, {cls: 'gis-panel-html-separator'});
										}

										if (ou.phoneNumber) {
											a.push({html: GIS.i18n.phone_number, cls: 'gis-panel-html-title'}, {html: ou.phoneNumber, cls: 'gis-panel-html'}, {cls: 'gis-panel-html-separator'});
										}

                                        if (Ext.isString(ou.coordinates)) {
                                            var co = ou.coordinates.replace("[","").replace("]","").replace(",",", ");
											a.push({html: GIS.i18n.coordinate, cls: 'gis-panel-html-title'}, {html: co, cls: 'gis-panel-html'}, {cls: 'gis-panel-html-separator'});
                                        }

                                        if (Ext.isArray(ou.organisationUnitGroups) && ou.organisationUnitGroups.length) {
                                            var html = '';

                                            for (var i = 0; i < ou.organisationUnitGroups.length; i++) {
                                                html += ou.organisationUnitGroups[i].name;
                                                html += i < ou.organisationUnitGroups.length - 1 ? '<br/>' : '';
                                            }

											a.push({html: GIS.i18n.groups, cls: 'gis-panel-html-title'}, {html: html, cls: 'gis-panel-html'}, {cls: 'gis-panel-html-separator'});
                                        }

										return a;
									}()
								},
								{
									xtype: 'form',
									cls: 'gis-container-inner gis-form-widget',
									columnWidth: 0.6,
									bodyStyle: 'padding-left:4px',
									items: [
										{
											html: GIS.i18n.infrastructural_data,
											cls: 'gis-panel-html-title'
										},
										{
											cls: 'gis-panel-html-separator'
										},
										{
											xtype: 'combo',
											fieldLabel: GIS.i18n.period,
											editable: false,
											valueField: 'id',
											displayField: 'name',
											emptyText: 'Select period',
											forceSelection: true,
											width: 258, //todo
											labelWidth: 70,
											store: {
												fields: ['id', 'name'],
												data: function() {
													var pt = new PeriodType(),
														periodType = gis.init.systemSettings.infrastructuralPeriodType.id,
														data;

													data = pt.get(periodType).generatePeriods({
														offset: 0,
														filterFuturePeriods: true,
														reversePeriods: true
													});

													if (Ext.isArray(data) && data.length) {
														data = data.slice(0,5);
													}

													return data;
												}()
											},
											lockPosition: false,
											listeners: {
												select: function(cmp) {
													var period = cmp.getValue(),
														url = gis.init.contextPath + '/api/analytics.json?',
														group = gis.init.systemSettings.infrastructuralDataElementGroup;

													if (group && group.dataElements) {
														url += 'dimension=dx:';

														for (var i = 0; i < group.dataElements.length; i++) {
															url += group.dataElements[i].id;
															url += i < group.dataElements.length - 1 ? ';' : '';
														}
													}

													url += '&filter=pe:' + period;
													url += '&filter=ou:' + att.id;

													Ext.Ajax.request({
														url: url,
														success: function(r) {
															var response = Ext.decode(r.responseText),
																data = [];

															if (Ext.isArray(response.rows)) {
																for (var i = 0; i < response.rows.length; i++) {
																	data.push({
																		name: response.metaData.names[response.rows[i][0]],
																		value: response.rows[i][1]
																	});
																}
															}

															layer.widget.infrastructuralDataElementValuesStore.loadData(data);
														}
													});

													//layer.widget.infrastructuralDataElementValuesStore.load({
														//params: {
															//periodId: infrastructuralPeriod,
															//organisationUnitId: att.internalId
														//}
													//});
												}
											}
										},
										{
											xtype: 'grid',
											cls: 'gis-grid',
											height: 300, //todo
											width: 258,
											scroll: 'vertical',
											columns: [
												{
													id: 'name',
													text: 'Data element',
													dataIndex: 'name',
													sortable: true,
													width: 195
												},
												{
													id: 'value',
													header: 'Value',
													dataIndex: 'value',
													sortable: true,
													width: 63
												}
											],
											disableSelection: true,
											store: layer.widget.infrastructuralDataElementValuesStore
										}
									]
								}
							],
							listeners: {
								show: function() {
									if (infrastructuralPeriod) {
										this.down('combo').setValue(infrastructuralPeriod);
										infrastructuralDataElementValuesStore.load({
											params: {
												periodId: infrastructuralPeriod,
												organisationUnitId: att.internalId
											}
										});
									}
								}
							}
						});

						layer.infrastructuralWindow.show();
						gis.util.gui.window.setPositionTopRight(layer.infrastructuralWindow);
					}
				});
			};

			// Drill or float
			drill = function(parentId, parentGraph, level) {
				var view = Ext.clone(layer.core.view),
					loader;

				// parent graph map
				view.parentGraphMap = {};
				view.parentGraphMap[parentId] = parentGraph;

				// dimension
				view.rows = [{
					dimension: dimConf.organisationUnit.objectName,
					items: [
						{id: parentId},
						{id: 'LEVEL-' + level}
					]
				}];

				if (view) {
					loader = layer.core.getLoader();
					loader.updateGui = true;
					loader.zoomToVisibleExtent = true;
					loader.hideMask = true;
					loader.load(view);
				}
			};

			// Menu
			var menuItems = [];

            if (layer.id !== 'facility') {
				menuItems.push(Ext.create('Ext.menu.Item', {
					text: 'Float up',
					iconCls: 'gis-menu-item-icon-float',
					cls: 'gis-plugin',
					disabled: !att.hasCoordinatesUp,
					handler: function() {
						drill(att.grandParentId, att.grandParentParentGraph, parseInt(att.level) - 1);
					}
				}));

                menuItems.push(Ext.create('Ext.menu.Item', {
					text: 'Drill down',
					iconCls: 'gis-menu-item-icon-drill',
					cls: 'gis-menu-item-first gis-plugin',
					disabled: !att.hasCoordinatesDown,
					handler: function() {
						drill(att.id, att.parentGraph, parseInt(att.level) + 1);
					}
				}));
			}

			if (isRelocate && isPoint) {

                if (layer.id !== 'facility') {
                    menuItems.push({
                        xtype: 'menuseparator'
                    });
                }

				menuItems.push( Ext.create('Ext.menu.Item', {
					text: GIS.i18n.relocate,
					iconCls: 'gis-menu-item-icon-relocate',
					disabled: !gis.init.user.isAdmin,
					handler: function(item) {
						gis.olmap.relocate.active = true;
						gis.olmap.relocate.feature = feature;
						gis.olmap.getViewport().style.cursor = 'crosshair';
						showRelocate();
					}
				}));

				menuItems.push( Ext.create('Ext.menu.Item', {
                    text: 'Swap lon/lat',
					iconCls: 'gis-menu-item-icon-relocate',
					disabled: !gis.init.user.isAdmin,
					handler: function(item) {
                        var id = feature.attributes.id,
                            geo = Ext.clone(feature.geometry).transform('EPSG:900913', 'EPSG:4326');

                        if (Ext.isNumber(geo.x) && Ext.isNumber(geo.y) && gis.init.user.isAdmin) {
                            Ext.Ajax.request({
                                url: gis.init.contextPath + '/api/organisationUnits/' + id + '.json?links=false',
                                success: function(r) {
                                    var orgUnit = Ext.decode(r.responseText);

                                    orgUnit.coordinates = '[' + geo.y.toFixed(5) + ',' + geo.x.toFixed(5) + ']';

                                    Ext.Ajax.request({
                                        url: gis.init.contextPath + '/api/metaData?preheatCache=false',
                                        method: 'POST',
                                        headers: {'Content-Type': 'application/json'},
                                        params: Ext.encode({organisationUnits: [orgUnit]}),
                                        success: function(r) {
                                            var x = feature.geometry.x,
                                                y = feature.geometry.y;

                                            delete feature.geometry.bounds;
                                            feature.geometry.x = y;
                                            feature.geometry.y = x;

                                            layer.redraw();

                                            console.log(feature.attributes.name + ' relocated to ' + orgUnit.coordinates);
                                        }
                                    });
                                }
                            });
                        }
					}
				}));

				menuItems.push( Ext.create('Ext.menu.Item', {
					text: GIS.i18n.show_information_sheet,
					iconCls: 'gis-menu-item-icon-information',
					handler: function(item) {
                        showInfo();
                    }
                }));
			}

			if (menuItems.length) {
                menuItems[menuItems.length - 1].addCls('gis-menu-item-last');
            }

			menu = new Ext.menu.Menu({
				baseCls: 'gis-plugin gis-popupmenu',
				shadow: false,
				showSeparator: false,
				defaults: {
					bodyStyle: 'padding-right:6px'
				},
				items: menuItems
			});

			menu.showAt([gis.olmap.mouseMove.x, gis.olmap.mouseMove.y]);
		};

		options = {
            onHoverSelect: defaultHoverSelect,
            onHoverUnselect: defaultHoverUnselect,
            onClickSelect: defaultClickSelect
        };

		if (isEvent) {
			options.onClickSelect = function fn(feature) {
                var ignoreKeys = ['label', 'value', 'nameColumnMap', 'psi', 'ps', 'longitude', 'latitude', 'eventdate', 'ou', 'oucode', 'ouname'],
                    attributes = feature.attributes,
                    map = attributes.nameColumnMap,
                    html = '<table class="padding1">',
                    titleStyle = ' style="font-weight:bold; padding-right:10px; vertical-align:top"',
                    valueStyle = ' style="max-width:170px"',
                    windowPosition;

                // default properties
                html += '<tr><td' + titleStyle + '>' + map['ou'] + '</td><td' + valueStyle + '>' + attributes['ouname'] + '</td></tr>';
                html += '<tr><td' + titleStyle + '>' + map['eventdate'] + '</td><td' + valueStyle + '>' + attributes['eventdate'] + '</td></tr>';
                html += '<tr><td' + titleStyle + '>' + map['longitude'] + '</td><td' + valueStyle + '>' + attributes['longitude'] + '</td></tr>';
                html += '<tr><td' + titleStyle + '>' + map['latitude'] + '</td><td' + valueStyle + '>' + attributes['latitude'] + '</td></tr>';

                for (var key in attributes) {
                    if (attributes.hasOwnProperty(key) && !Ext.Array.contains(ignoreKeys, key)) {
                        html += '<tr><td' + titleStyle + '>' + map[key] + '</td><td>' + attributes[key] + '</td></tr>';
                    }
                }

                html += '</table>';

                if (Ext.isObject(eventWindow) && eventWindow.destroy) {
                    windowPosition = eventWindow.getPosition();
                    eventWindow.destroy();
                    eventWindow = null;
                }

                eventWindow = Ext.create('Ext.window.Window', {
                    title: 'Event',
                    layout: 'fit',
                    resizable: false,
                    bodyStyle: 'background-color:#fff; padding:5px',
                    html: html,
                    autoShow: true,
                    listeners: {
                        show: function(w) {
                            if (windowPosition) {
                                w.setPosition(windowPosition);
                            }
                            else {
                                gis.util.gui.window.setPositionTopRight(w);
                            }
                        },
                        destroy: function() {
                            eventWindow = null;
                        }
                    }
                });
            };
		}

		selectHandlers = new OpenLayers.Control.newSelectFeature(layer, options);

		gis.olmap.addControl(selectHandlers);
		selectHandlers.activate();
	};

	GIS.core.OrganisationUnitLevelStore = function(gis) {
        var isPlugin = GIS.plugin && !GIS.app;

		return Ext.create('Ext.data.Store', {
			fields: ['id', 'name', 'level'],
			proxy: {
				type: isPlugin ? 'jsonp' : 'ajax',
				url: gis.init.contextPath + gis.conf.finals.url.path_api + 'organisationUnitLevels.' + (isPlugin ? 'jsonp' : 'json') + '?fields=id,name,level&paging=false',
				reader: {
					type: 'json',
					root: 'organisationUnitLevels'
				}
			},
			autoLoad: true,
			cmp: [],
			isLoaded: false,
			loadFn: function(fn) {
				if (this.isLoaded) {
					fn.call();
				}
				else {
					this.load(fn);
				}
			},
			getRecordByLevel: function(level) {
				return this.getAt(this.findExact('level', level));
			},
			listeners: {
				load: function() {
					if (!this.isLoaded) {
						this.isLoaded = true;
						gis.util.gui.combo.setQueryMode(this.cmp, 'local');
					}
					this.sort('level', 'ASC');
				}
			}
		});
	};

	GIS.core.StyleMap = function(id, labelConfig) {
		var defaults = {
				fillOpacity: 1,
				strokeColor: '#fff',
				strokeWidth: 1,
                pointRadius: 5,
                labelAlign: 'cr',
                labelYOffset: 13
			},
			select = {
				fillOpacity: 0.9,
				strokeColor: '#fff',
				strokeWidth: 1,
                pointRadius: 5,
				cursor: 'pointer',
                labelAlign: 'cr',
                labelYOffset: 13
			};

		if (labelConfig) {
            defaults.label = labelConfig.label;
            defaults.fontFamily = labelConfig.fontFamily;
			defaults.fontSize = (labelConfig.fontSize || 13) + 'px';
			defaults.fontWeight = labelConfig.strong ? 'bold' : 'normal';
			defaults.fontStyle = labelConfig.italic ? 'italic' : 'normal';
			defaults.fontColor = labelConfig.color ? (labelConfig.color.split('').shift() !== '#' ? '#' + labelConfig.color : labelConfig.color) : '#000000';
		}

		return new OpenLayers.StyleMap({
			'default': defaults,
			select: select
		});
	};

	GIS.core.VectorLayer = function(gis, id, name, config) {
		var layer = new OpenLayers.Layer.Vector(name, {
			strategies: [
				new OpenLayers.Strategy.Refresh({
					force:true
				})
			],
			styleMap: GIS.core.StyleMap(id),
			visibility: false,
			displayInLayerSwitcher: false,
			layerType: gis.conf.finals.layer.type_vector,
			layerOpacity: config ? config.opacity || 1 : 1,
			setLayerOpacity: function(number) {
				if (number) {
					this.layerOpacity = parseFloat(number);
				}
				this.setOpacity(this.layerOpacity);
			},
			hasLabels: false
		});

		layer.id = id;

		return layer;
	};

	GIS.core.MeasureWindow = function(gis) {
		var window,
			label,
			handleMeasurements,
			control,
			styleMap;

		styleMap = new OpenLayers.StyleMap({
			'default': new OpenLayers.Style()
		});

		control = new OpenLayers.Control.Measure(OpenLayers.Handler.Path, {
			persist: true,
			immediate: true,
			handlerOption: {
				layerOptions: {
					styleMap: styleMap
				}
			}
		});

		handleMeasurements = function(e) {
			if (e.measure) {
				label.setText(e.measure.toFixed(2) + ' ' + e.units);
			}
		};

		gis.olmap.addControl(control);

		control.events.on({
			measurepartial: handleMeasurements,
			measure: handleMeasurements
		});

		control.geodesic = true;
		control.activate();

		label = Ext.create('Ext.form.Label', {
			style: 'height: 20px',
			text: '0 km'
		});

		window = Ext.create('Ext.window.Window', {
			title: GIS.i18n.measure_distance,
			layout: 'fit',
			cls: 'gis-container-default gis-plugin',
			bodyStyle: 'text-align: center',
			width: 130,
			minWidth: 130,
			resizable: false,
			items: label,
			listeners: {
				show: function() {
					var x = gis.viewport.eastRegion.getPosition()[0] - this.getWidth() - 3,
						y = gis.viewport.centerRegion.getPosition()[1] + 26;
					this.setPosition(x, y);
				},
				destroy: function() {
					control.deactivate();
					gis.olmap.removeControl(control);
				}
			}
		});

		return window;
	};

	GIS.core.MapLoader = function(gis) {
		var getMap,
			setMap,
			afterLoad,
			callBack,
			register = [],
			loader;

		getMap = function() {
            var isPlugin = GIS.plugin && !GIS.app,
                type = isPlugin ? 'jsonp' : 'json',
                url = gis.init.contextPath + '/api/maps/' + gis.map.id + '.' + type + '?fields=' + gis.conf.url.mapFields.join(','),
                success,
                failure;

            success = function(r) {

                // operand
                if (Ext.isArray(r.mapViews)) {
                    for (var i = 0, view; i < r.mapViews.length; i++) {
                        view = r.mapViews[i];

                        if (view) {
                            if (Ext.isArray(view.columns) && view.columns.length) {
                                for (var j = 0, dim; j < view.columns.length; j++) {
                                    dim = view.columns[j];

                                    if (Ext.isArray(dim.items) && dim.items.length) {
                                        for (var k = 0, item; k < dim.items.length; k++) {
                                            item = dim.items[k];

                                            item.id = item.id.replace('#', '.');
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                gis.map = r;
                setMap();
            };

            failure = function() {
                gis.olmap.mask.hide();
                alert('Map id not recognized' + (gis.el ? ' (' + gis.el + ')' : ''));
                return;
            };

            if (isPlugin) {
                Ext.data.JsonP.request({
                    url: url,
                    success: function(r) {
                        success(r);
                    }
                });
            }
            else {
                Ext.Ajax.request({
                    url: url,
                    success: function(r) {
                        success(Ext.decode(r.responseText));
                    }
                });
            }
		};

		setMap = function() {
			var views = gis.map.mapViews,
				loader;

			if (!(Ext.isArray(views) && views.length)) {
				gis.olmap.mask.hide();
				alert(GIS.i18n.favorite_outdated_create_new);
				return;
			}

			for (var i = 0; i < views.length; i++) {
				views[i] = gis.api.layout.Layout(views[i]);
			}

			views = Ext.Array.clean(views);

			if (!views.length) {
				return;
			}

			if (gis.viewport && gis.viewport.favoriteWindow && gis.viewport.favoriteWindow.isVisible()) {
				gis.viewport.favoriteWindow.destroy();
			}

			gis.olmap.closeAllLayers();

			for (var i = 0, layout; i < views.length; i++) {
				layout = views[i];

				loader = gis.layer[layout.layer].core.getLoader();
				loader.updateGui = !gis.el;
				loader.callBack = callBack;
				loader.load(layout);
			}
		};

		callBack = function(layer) {
			register.push(layer);

			if (register.length === gis.map.mapViews.length) {
				afterLoad();
			}
		};

		afterLoad = function() {
			register = [];

			if (gis.el) {
				gis.olmap.zoomToVisibleExtent();
			}
			else {
				if (gis.map.longitude && gis.map.latitude && gis.map.zoom) {
					gis.olmap.setCenter(new OpenLayers.LonLat(gis.map.longitude, gis.map.latitude), gis.map.zoom);
				}
				else {
					gis.olmap.zoomToVisibleExtent();
				}
			}

			// interpretation button
			if (gis.viewport.shareButton) {
				gis.viewport.shareButton.enable();
			}

			// session storage
			if (GIS.isSessionStorage) {
				gis.util.layout.setSessionStorage('map', gis.util.layout.getAnalytical());
			}

			gis.olmap.mask.hide();
		};

		loader = {
			load: function(views) {
				gis.olmap.mask.show();

				if (gis.map && gis.map.id) {
					getMap();
				}
				else {
					if (views) {
						gis.map = {
							mapViews: views
						};
					}

					setMap();
				}
			}
		};

		return loader;
	};

	GIS.core.LayerLoaderEvent = function(gis, layer) {
		var olmap = layer.map,
			compareView,
			loadOrganisationUnits,
			loadData,
			loadLegend,
			afterLoad,
			loader,
			dimConf = gis.conf.finals.dimension;

		loadOrganisationUnits = function(view) {
            loadData(view);
		};

		loadData = function(view) {
			view = view || layer.core.view;

            var paramString = '?',
                features = [];

            // stage
            paramString += 'stage=' + view.stage.id;

            // dates
            paramString += '&startDate=' + view.startDate;
            paramString += '&endDate=' + view.endDate;

            // ou
            if (Ext.isArray(view.organisationUnits)) {
                paramString += '&dimension=ou:';

				for (var i = 0; i < view.organisationUnits.length; i++) {
					paramString += view.organisationUnits[i].id;
                    paramString += i < view.organisationUnits.length - 1 ? ';' : '';
				}
			}

            // de
            for (var i = 0, element; i < view.dataElements.length; i++) {
                element = view.dataElements[i];

                paramString += '&dimension=' + element.dimension + (element.filter ? ':' + element.filter : '');

                //if (element.filter) {
					//if (element.operator) {
						//paramString += ':' + element.operator;
					//}

					//paramString += ':' + element.value;
                //}
            }

			Ext.data.JsonP.request({
				url: gis.init.contextPath + '/api/analytics/events/query/' + view.program.id + '.jsonp' + paramString,
				disableCaching: false,
				scope: this,
				success: function(r) {
                    var events = [],
                        features = [],
                        rows = [],
                        lonIndex,
                        latIndex,
                        map = Ext.clone(r.metaData.names);

                    // name-column map, lonIndex, latIndex
                    for (var i = 0; i < r.headers.length; i++) {
                        map[r.headers[i].name] = r.headers[i].column;

                        if (r.headers[i].name === 'longitude') {
							lonIndex = i;
						}

						if (r.headers[i].name === 'latitude') {
							latIndex = i;
						}
                    }

					// get events with coordinates
                    if (Ext.isArray(r.rows) && r.rows.length) {
						for (var i = 0, row; i < r.rows.length; i++) {
							row = r.rows[i];

							if (row[lonIndex] && row[latIndex]) {
								rows.push(row);
							}
						}
					}

                    if (!rows.length) {
                        alert('No coordinates found');
                        olmap.mask.hide();
                        return;
                    }

                    // name-column map
                    map = r.metaData.names;

                    for (var i = 0; i < r.headers.length; i++) {
                        map[r.headers[i].name] = r.headers[i].column;
                    }

                    // events
                    for (var i = 0, row, obj; i < rows.length; i++) {
                        row = rows[i];
                        obj = {};

                        for (var j = 0; j < row.length; j++) {
                            obj[r.headers[j].name] = row[j];
                        }

                        obj[gis.conf.finals.widget.value] = 0;
                        obj.label = obj.ouname;
                        obj.nameColumnMap = map;

                        events.push(obj);
                    }

                    // features
                    for (var i = 0, event, point; i < events.length; i++) {
                        event = events[i];

                        point = gis.util.map.getTransformedPointByXY(event.longitude, event.latitude);

                        features.push(new OpenLayers.Feature.Vector(point, event));
                    }

                    layer.removeFeatures(layer.features);
                    layer.addFeatures(features);

                    loadLegend(view);
                }
            });
		};

		loadLegend = function(view) {
			view = view || layer.core.view;

            // classification optionsvar options = {
            var options = {
            	indicator: gis.conf.finals.widget.value,
				method: 2,
				numClasses: 5,
				colors: layer.core.getColors('000000', '222222'),
				minSize: 5,
				maxSize: 5
			};

            layer.core.view = view;

            layer.core.applyClassification(options);

            afterLoad(view);
		};

		afterLoad = function(view) {

			// Layer
			if (layer.item) {
				layer.item.setValue(true, view.opacity);
			}
			else {
				layer.setLayerOpacity(view.opacity);
			}

			// Gui
			if (loader.updateGui && Ext.isObject(layer.widget)) {
				layer.widget.setGui(view);
			}

			// Zoom
			if (loader.zoomToVisibleExtent) {
				olmap.zoomToVisibleExtent();
			}

			// Mask
			if (loader.hideMask) {
				olmap.mask.hide();
			}

			// Map callback
			if (loader.callBack) {
				loader.callBack(layer);
			}
			else {
				gis.map = null;
			}

			// session storage
			//if (GIS.isSessionStorage) {
				//gis.util.layout.setSessionStorage('map', gis.util.layout.getAnalytical());
			//}
		};

		loader = {
			compare: false,
			updateGui: false,
			zoomToVisibleExtent: false,
			hideMask: false,
			callBack: null,
			load: function(view) {
				gis.olmap.mask.show();

                loadOrganisationUnits(view);
			},
			loadData: loadData,
			loadLegend: loadLegend
		};

		return loader;
	};

	GIS.core.LayerLoaderFacility = function(gis, layer) {
		var olmap = layer.map,
			compareView,
			loadOrganisationUnits,
			loadData,
			loadLegend,
			addCircles,
			afterLoad,
			loader;

		compareView = function(view, doExecute) {
			var src = layer.core.view,
				viewIds,
				viewDim,
				srcIds,
				srcDim;

			loader.zoomToVisibleExtent = true;

			if (!src) {
				if (doExecute) {
					loadOrganisationUnits(view);
				}
				return gis.conf.finals.widget.loadtype_organisationunit;
			}

			// organisation units
			viewIds = [];
			viewDim = view.rows[0];
			srcIds = [];
			srcDim = src.rows[0];

			if (viewDim.items.length === srcDim.items.length) {
				for (var i = 0; i < viewDim.items.length; i++) {
					viewIds.push(viewDim.items[i].id);
				}

				for (var i = 0; i < srcDim.items.length; i++) {
					srcIds.push(srcDim.items[i].id);
				}

				if (Ext.Array.difference(viewIds, srcIds).length !== 0) {
					if (doExecute) {
						loadOrganisationUnits(view);
					}
					return gis.conf.finals.widget.loadtype_organisationunit;
				}
			}
			else {
				if (doExecute) {
					loadOrganisationUnits(view);
				}
				return gis.conf.finals.widget.loadtype_organisationunit;
			}

			// Group set
			loader.zoomToVisibleExtent = false;

			if (view.organisationUnitGroupSet.id !== src.organisationUnitGroupSet.id) {
				if (doExecute) {
					loadOrganisationUnits(view);
				}
				return gis.conf.finals.widget.loadtype_organisationunit;
			}

			//if (view.areaRadius !== src.areaRadius) {
				//if (doExecute) {
					//loadLegend(view);
				//}
				//return gis.conf.finals.widget.loadtype_legend;
			//}

			// always reload legend
			if (doExecute) {
				loadLegend(view);
				return gis.conf.finals.widget.loadtype_legend;
			}

			//gis.olmap.mask.hide();
		};

		loadOrganisationUnits = function(view) {
            var items = view.rows[0].items,
                url = function() {
                    var params = '';
                    for (var i = 0; i < items.length; i++) {
                        params += 'ids=' + items[i].id;
                        params += i !== items.length - 1 ? '&' : '';
                    }
                    return gis.init.contextPath + gis.conf.finals.url.path_module + 'getGeoJsonFacilities.action?' + params;
                }(),
                success,
                failure;

            success = function(r) {
                var geojson = layer.core.decode(r),
                    format = new OpenLayers.Format.GeoJSON(),
                    features = gis.util.map.getTransformedFeatureArray(format.read(geojson));

                if (!Ext.isArray(features)) {
                    olmap.mask.hide();
                    alert(GIS.i18n.invalid_coordinates);
                    return;
                }

                if (!features.length) {
                    olmap.mask.hide();
                    alert(GIS.i18n.no_valid_coordinates_found);
                    return;
                }

                layer.core.featureStore.loadFeatures(features.slice(0));

                loadData(view, features);
            };

            failure = function() {
                olmap.mask.hide();
                alert(GIS.i18n.coordinates_could_not_be_loaded);
            };

            if (GIS.plugin && !GIS.app) {
                Ext.data.JsonP.request({
                    url: url,
                    disableCaching: false,
                    success: function(r) {
                        success(r);
                    }
                });
            }
            else {
                Ext.Ajax.request({
                    url: url,
                    disableCaching: false,
                    success: function(r) {
                        success(Ext.decode(r.responseText));
                    }
                });
            }
        };

		loadData = function(view, features) {
			view = view || layer.core.view;
			features = features || layer.core.featureStore.features;

			for (var i = 0; i < features.length; i++) {
				features[i].attributes.label = features[i].attributes.name;
			}

			layer.removeFeatures(layer.features);
			layer.addFeatures(features);

			loadLegend(view);
		};

		loadLegend = function(view) {
            var isPlugin = GIS.plugin && !GIS.app,
                type = isPlugin ? 'jsonp' : 'json',
                url = gis.init.contextPath + '/api/organisationUnitGroupSets/' + view.organisationUnitGroupSet.id + '.' + type + '?fields=organisationUnitGroups[id,name]',
                success;

			view = view || layer.core.view;

            success = function(r) {
                var data = r.organisationUnitGroups,
                    options = {
                        indicator: view.organisationUnitGroupSet.id
                    };

                gis.store.groupsByGroupSet.loadData(data);

                layer.core.view = view;

                layer.core.applyClassification({
                    indicator: view.organisationUnitGroupSet.id
                });

                addCircles(view);

                afterLoad(view);
            };

            if (isPlugin) {
                Ext.data.JsonP.request({
                    url: url,
                    success: function(r) {
                        success(r);
                    }
                });
            }
            else {
                Ext.Ajax.request({
                    url: url,
                    success: function(r) {
                        success(Ext.decode(r.responseText));
                    }
                });
            }
		};

		addCircles = function(view) {
			var radius = view.areaRadius;

			if (layer.circleLayer) {
				layer.circleLayer.deactivateControls();
				layer.circleLayer = null;
			}
			if (Ext.isDefined(radius) && radius) {
				layer.circleLayer = GIS.app.CircleLayer(layer.features, radius);
				nissa = layer.circleLayer;
			}
		};

		afterLoad = function(view) {

			// Legend
			gis.viewport.eastRegion.doLayout();
			layer.legendPanel.expand();

			// Layer
			if (layer.item) {
				layer.item.setValue(true, view.opacity);
			}
			else {
				layer.setLayerOpacity(view.opacity);
			}

			// Gui
			if (loader.updateGui && Ext.isObject(layer.widget)) {
				layer.widget.setGui(view);
			}

			// Zoom
			if (loader.zoomToVisibleExtent) {
				olmap.zoomToVisibleExtent();
			}

			// Mask
			if (loader.hideMask) {
				olmap.mask.hide();
			}

			// Map callback
			if (loader.callBack) {
				loader.callBack(layer);
			}
			else {
				gis.map = null;

				if (gis.viewport.shareButton) {
                    gis.viewport.shareButton.enable();
				}
			}
		};

		loader = {
			compare: false,
			updateGui: false,
			zoomToVisibleExtent: false,
			hideMask: false,
			callBack: null,
			load: function(view) {
				gis.olmap.mask.show();

				if (this.compare) {
					compareView(view, true);
				}
				else {
					loadOrganisationUnits(view);
				}
			},
			loadData: loadData,
			loadLegend: loadLegend
		};

		return loader;
	};

	GIS.core.LayerLoaderBoundary = function(gis, layer) {
		var olmap = layer.map,
			compareView,
			loadOrganisationUnits,
			loadData,
			loadLegend,
			afterLoad,
			loader;

		compareView = function(view, doExecute) {
			var src = layer.core.view,
				viewIds,
				viewDim,
				srcIds,
				srcDim;

			if (!src) {
				if (doExecute) {
					loadOrganisationUnits(view);
				}
				return gis.conf.finals.widget.loadtype_organisationunit;
			}

			viewIds = [];
			viewDim = view.rows[0];
			srcIds = [];
			srcDim = src.rows[0];

			// organisation units
			if (viewDim.items.length === srcDim.items.length) {
				for (var i = 0; i < viewDim.items.length; i++) {
					viewIds.push(viewDim.items[i].id);
				}

				for (var i = 0; i < srcDim.items.length; i++) {
					srcIds.push(srcDim.items[i].id);
				}

				if (Ext.Array.difference(viewIds, srcIds).length !== 0) {
					if (doExecute) {
						loadOrganisationUnits(view);
					}
					return gis.conf.finals.widget.loadtype_organisationunit;
				}
			}
			else {
				if (doExecute) {
					loadOrganisationUnits(view);
				}
				return gis.conf.finals.widget.loadtype_organisationunit;
			}

			gis.olmap.mask.hide();
		};

		loadOrganisationUnits = function(view) {
			var items = view.rows[0].items,
				idParamString = '';

			for (var i = 0; i < items.length; i++) {
				idParamString += 'ids=' + items[i].id;
				idParamString += i !== items.length - 1 ? '&' : '';
			}

			Ext.data.JsonP.request({
				url: gis.init.contextPath + gis.conf.finals.url.path_module + 'getGeoJson.action?' + idParamString,
				scope: this,
				disableCaching: false,
				success: function(r) {
					var geojson = gis.util.geojson.decode(r, 'DESC'),
						format = new OpenLayers.Format.GeoJSON(),
						features = gis.util.map.getTransformedFeatureArray(format.read(geojson)),
                        colors = ['black', 'blue', 'red', 'green', 'yellow'],
                        levels = [],
                        levelObjectMap = {};

					if (!Ext.isArray(features)) {
						olmap.mask.hide();
						alert(GIS.i18n.invalid_coordinates);
						return;
					}

					if (!features.length) {
						olmap.mask.hide();
						alert(GIS.i18n.no_valid_coordinates_found);
						return;
					}

                    // get levels, colors, map
                    for (var i = 0; i < features.length; i++) {
                        levels.push(parseFloat(features[i].attributes.level));
                    }

                    levels = Ext.Array.unique(levels).sort();

                    for (var i = 0; i < levels.length; i++) {
                        levelObjectMap[levels[i]] = {
                            strokeColor: colors[i]
                        };
                    }

                    // style
                    for (var i = 0, feature, obj, strokeWidth; i < features.length; i++) {
                        feature = features[i];
                        obj = levelObjectMap[feature.attributes.level];
                        strokeWidth = levels.length === 1 ? 1 : feature.attributes.level == 2 ? 2 : 1;

                        feature.style = {
                            strokeColor: obj.strokeColor || 'black',
                            strokeWidth: strokeWidth,
                            fillOpacity: 0,
                            pointRadius: 5,
                            labelAlign: 'cr',
                            labelYOffset: 13
                        };
                    }

					layer.core.featureStore.loadFeatures(features.slice(0));

					loadData(view, features);
				},
				failure: function(r) {
					olmap.mask.hide();
					alert(GIS.i18n.coordinates_could_not_be_loaded);
				}
			});
		};

		loadData = function(view, features) {
			view = view || layer.core.view;
			features = features || layer.core.featureStore.features;

			for (var i = 0; i < features.length; i++) {
				features[i].attributes.label = features[i].attributes.name;
				features[i].attributes.value = 0;
			}

			layer.removeFeatures(layer.features);
			layer.addFeatures(features);

            // labels
            if (layer.hasLabels) {
                layer.core.setFeatureLabelStyle(true, true);
            }

			loadLegend(view);
		};

		loadLegend = function(view) {
			view = view || layer.core.view;

			var options = {
				indicator: gis.conf.finals.widget.value,
				method: 2,
				numClasses: 5,
				colors: layer.core.getColors('000000', '000000'),
				minSize: 6,
				maxSize: 6
			};

			layer.core.view = view;

			layer.core.applyClassification(options);

			afterLoad(view);
		};

		afterLoad = function(view) {

			// Layer
			if (layer.item) {
				layer.item.setValue(true, view.opacity);
			}
			else {
				layer.setLayerOpacity(view.opacity);
			}

			// Gui
			if (loader.updateGui && Ext.isObject(layer.widget)) {
				layer.widget.setGui(view);
			}

			// Zoom
			if (loader.zoomToVisibleExtent) {
				olmap.zoomToVisibleExtent();
			}

			// Mask
			if (loader.hideMask) {
				olmap.mask.hide();
			}

			// Map callback
			if (loader.callBack) {
				loader.callBack(layer);
			}
			else {
				gis.map = null;

				if (gis.viewport.shareButton) {
                    gis.viewport.shareButton.enable();
				}
			}
		};

		loader = {
			compare: false,
			updateGui: false,
			zoomToVisibleExtent: false,
			hideMask: false,
			callBack: null,
			load: function(view) {
				gis.olmap.mask.show();

				if (this.compare) {
					compareView(view, true);
				}
				else {
					loadOrganisationUnits(view);
				}
			},
			loadData: loadData,
			loadLegend: loadLegend
		};

		return loader;
	};

	GIS.core.LayerLoaderThematic = function(gis, layer) {
		var olmap = layer.map,
			compareView,
			loadOrganisationUnits,
			loadData,
			loadLegend,
			afterLoad,
			loader,
			dimConf = gis.conf.finals.dimension;

		compareView = function(view, doExecute) {
			var src = layer.core.view,
				viewIds,
				viewDim,
				srcIds,
				srcDim;

			loader.zoomToVisibleExtent = true;

			if (!src) {
				if (doExecute) {
					loadOrganisationUnits(view);
				}
				return gis.conf.finals.widget.loadtype_organisationunit;
			}

			// organisation units
			viewIds = [];
			viewDim = view.rows[0];
			srcIds = [];
			srcDim = src.rows[0];

			if (viewDim.items.length === srcDim.items.length) {
				for (var i = 0; i < viewDim.items.length; i++) {
					viewIds.push(viewDim.items[i].id);
				}

				for (var i = 0; i < srcDim.items.length; i++) {
					srcIds.push(srcDim.items[i].id);
				}

				if (Ext.Array.difference(viewIds, srcIds).length !== 0) {
					if (doExecute) {
						loadOrganisationUnits(view);
					}
					return gis.conf.finals.widget.loadtype_organisationunit;
				}
			}
			else {
				if (doExecute) {
					loadOrganisationUnits(view);
				}
				return gis.conf.finals.widget.loadtype_organisationunit;
			}

			// data
			loader.zoomToVisibleExtent = false;

			viewIds = [];
			viewDim = view.columns[0];
			srcIds = [];
			srcDim = src.columns[0];

			if (viewDim.items.length === srcDim.items.length) {
				for (var i = 0; i < viewDim.items.length; i++) {
					viewIds.push(viewDim.items[i].id);
				}

				for (var i = 0; i < srcDim.items.length; i++) {
					srcIds.push(srcDim.items[i].id);
				}

				if (Ext.Array.difference(viewIds, srcIds).length !== 0) {
					if (doExecute) {
						loadData(view);
					}
					return gis.conf.finals.widget.loadtype_organisationunit;
				}
			}
			else {
				if (doExecute) {
					loadData(view);
				}
				return gis.conf.finals.widget.loadtype_organisationunit;
			}

			// period
			viewIds = [];
			viewDim = view.filters[0];
			srcIds = [];
			srcDim = src.filters[0];

			if (viewDim.items.length === srcDim.items.length) {
				for (var i = 0; i < viewDim.items.length; i++) {
					viewIds.push(viewDim.items[i].id);
				}

				for (var i = 0; i < srcDim.items.length; i++) {
					srcIds.push(srcDim.items[i].id);
				}

				if (Ext.Array.difference(viewIds, srcIds).length !== 0) {
					if (doExecute) {
						loadData(view);
					}
					return gis.conf.finals.widget.loadtype_organisationunit;
				}
			}
			else {
				if (doExecute) {
					loadData(view);
				}
				return gis.conf.finals.widget.loadtype_organisationunit;
			}

			// legend
			//if (typeof view.legendSet !== typeof src.legendSet) {
				//if (doExecute) {
					//loadLegend(view);
				//}
				//return gis.conf.finals.widget.loadtype_legend;
			//}
			//else if (view.classes !== src.classes ||
				//view.method !== src.method ||
				//view.colorLow !== src.colorLow ||
				//view.radiusLow !== src.radiusLow ||
				//view.colorHigh !== src.colorHigh ||
				//view.radiusHigh !== src.radiusHigh) {
					//if (doExecute) {
						//loadLegend(view);
					//}
					//return gis.conf.finals.widget.loadtype_legend;
			//}

			// if no changes - reload legend but do not zoom
			if (doExecute) {
				loader.zoomToVisibleExtent = false;
				loadLegend(view);
				return gis.conf.finals.widget.loadtype_legend;
			}

			//gis.olmap.mask.hide();
		};

		loadOrganisationUnits = function(view) {
			var items = view.rows[0].items,
				url = function() {
                    var params = '';
                    for (var i = 0; i < items.length; i++) {
                        params += 'ids=' + items[i].id;
                        params += i !== items.length - 1 ? '&' : '';
                    }
                    return gis.init.contextPath + gis.conf.finals.url.path_module + 'getGeoJson.action?' + params;
                }(),
                success,
                failure;

            success = function(r) {
                var geojson = gis.util.geojson.decode(r),
                    format = new OpenLayers.Format.GeoJSON(),
                    features = gis.util.map.getTransformedFeatureArray(format.read(geojson));

                if (!Ext.isArray(features)) {
                    olmap.mask.hide();
                    alert(GIS.i18n.invalid_coordinates);
                    return;
                }

                if (!features.length) {
                    olmap.mask.hide();
                    alert(GIS.i18n.no_valid_coordinates_found);
                    return;
                }

                layer.core.featureStore.loadFeatures(features.slice(0));

                loadData(view, features);
            };

            failure = function() {
                olmap.mask.hide();
                alert(GIS.i18n.coordinates_could_not_be_loaded);
            };

            if (GIS.plugin && !GIS.app) {
                Ext.data.JsonP.request({
                    url: url,
                    disableCaching: false,
                    success: function(r) {
                        success(r);
                    }
                });
            }
            else {
                Ext.Ajax.request({
                    url: url,
                    disableCaching: false,
                    success: function(r) {
                        success(Ext.decode(r.responseText));
                    }
                });
            }
		};

		loadData = function(view, features) {
			var success;

			view = view || layer.core.view;
			features = features || layer.core.featureStore.features;

			var dimConf = gis.conf.finals.dimension,
				paramString = '?',
				dxItems = view.columns[0].items,
				isOperand = view.columns[0].dimension === dimConf.operand.objectName,
				peItems = view.filters[0].items,
				ouItems = view.rows[0].items;

			// ou
			paramString += 'dimension=ou:';

			for (var i = 0; i < ouItems.length; i++) {
				paramString += ouItems[i].id;
				paramString += i < ouItems.length - 1 ? ';' : '';
			}

			// dx
			paramString += '&dimension=dx:';

			for (var i = 0; i < dxItems.length; i++) {
				paramString += isOperand ? dxItems[i].id.split('.')[0] : dxItems[i].id;
				paramString += i < dxItems.length - 1 ? ';' : '';
			}

			paramString += isOperand ? '&dimension=co' : '';

			// pe
			paramString += '&filter=pe:';

			for (var i = 0; i < peItems.length; i++) {
				paramString += peItems[i].id;
				paramString += i < peItems.length - 1 ? ';' : '';
			}

			success = function(json) {
				var response = gis.api.response.Response(json),
					featureMap = {},
					valueMap = {},
					ouIndex,
					dxIndex,
					valueIndex,
					newFeatures = [],
					dimensions,
					items = [];

				if (!response) {
					olmap.mask.hide();
					return;
				}

				// ou index, value index
				for (var i = 0; i < response.headers.length; i++) {
					if (response.headers[i].name === dimConf.organisationUnit.dimensionName) {
						ouIndex = i;
					}
					else if (response.headers[i].name === dimConf.value.dimensionName) {
						valueIndex = i;
					}
				}

				// Feature map
				for (var i = 0, id; i < features.length; i++) {
					var id = features[i].attributes.id;

					featureMap[id] = true;
				}

				// Value map
				for (var i = 0; i < response.rows.length; i++) {
					var id = response.rows[i][ouIndex],
						value = parseFloat(response.rows[i][valueIndex]);

					valueMap[id] = value;
				}

				for (var i = 0; i < features.length; i++) {
					var feature = features[i],
						id = feature.attributes.id;

					if (featureMap.hasOwnProperty(id) && valueMap.hasOwnProperty(id)) {
						feature.attributes.value = valueMap[id];
						feature.attributes.label = feature.attributes.name + ' (' + feature.attributes.value + ')';
						newFeatures.push(feature);
					}
				}

				layer.removeFeatures(layer.features);
				layer.addFeatures(newFeatures);

				gis.response = response;

				loadLegend(view);
			};

			if (Ext.isObject(GIS.app)) {
				Ext.Ajax.request({
					url: gis.init.contextPath + '/api/analytics.json' + paramString,
					disableCaching: false,
					failure: function(r) {
						alert(r.responseText);
					},
					success: function(r) {
						success(Ext.decode(r.responseText));
					}
				});
			}
			else if (Ext.isObject(GIS.plugin)) {
				Ext.data.JsonP.request({
					url: gis.init.contextPath + '/api/analytics.jsonp' + paramString,
					disableCaching: false,
					scope: this,
					success: function(r) {
						success(r);
					}
				});
			}
		};

		loadLegend = function(view) {
			var bounds,
				addNames,
				fn;

			view = view || layer.core.view;

			addNames = function(response) {

				// All dimensions
				var dimensions = Ext.Array.clean([].concat(view.columns || [], view.rows || [], view.filters || [])),
					metaData = response.metaData,
					peIds = metaData[dimConf.period.objectName];

				for (var i = 0, dimension; i < dimensions.length; i++) {
					dimension = dimensions[i];

					for (var j = 0, item; j < dimension.items.length; j++) {
						item = dimension.items[j];

						if (item.id.indexOf('.') !== -1) {
							var ids = item.id.split('.');
							item.name = metaData.names[ids[0]] + ' ' + metaData.names[ids[1]];
						}
						else {
							item.name = metaData.names[item.id];
						}
					}
				}

				// Period name without changing the id
				view.filters[0].items[0].name = metaData.names[peIds[peIds.length - 1]];
			};

			fn = function() {
				addNames(gis.response);

				// Classification options
				var options = {
					indicator: gis.conf.finals.widget.value,
					method: view.legendSet ? mapfish.GeoStat.Distribution.CLASSIFY_WITH_BOUNDS : view.method,
					numClasses: view.classes,
					bounds: bounds,
					colors: layer.core.getColors(view.colorLow, view.colorHigh),
					minSize: view.radiusLow,
					maxSize: view.radiusHigh
				};

				layer.core.view = view;
				layer.core.colorInterpolation = colors;
				layer.core.applyClassification(options);

				afterLoad(view);
			};

			if (view.legendSet) {
				var bounds = [],
					colors = [],
					names = [],
					legends = [];

				Ext.Ajax.request({
					url: gis.init.contextPath + gis.conf.finals.url.path_api + 'mapLegendSets/' + view.legendSet.id + '.json?fields=' + gis.conf.url.mapLegendSetFields.join(','),
					scope: this,
					success: function(r) {
						legends = Ext.decode(r.responseText).mapLegends;

						Ext.Array.sort(legends, function (a, b) {
							return a.startValue - b.startValue;
						});

						for (var i = 0; i < legends.length; i++) {
							if (bounds[bounds.length - 1] !== legends[i].startValue) {
								if (bounds.length !== 0) {
									colors.push(new mapfish.ColorRgb(240,240,240));
									names.push('');
								}
								bounds.push(legends[i].startValue);
							}
							colors.push(new mapfish.ColorRgb());
							colors[colors.length - 1].setFromHex(legends[i].color);
							names.push(legends[i].name);
							bounds.push(legends[i].endValue);
						}

						view.legendSet.names = names;
						view.legendSet.bounds = bounds;
						view.legendSet.colors = colors;

						fn();
					}
				});
			}
			else {
				fn();
			}
		};

		afterLoad = function(view) {

			// Legend
			gis.viewport.eastRegion.doLayout();
			layer.legendPanel.expand();

			// Layer
			layer.setLayerOpacity(view.opacity);

			if (layer.item) {
				layer.item.setValue(true);
			}

			// Filter
			if (layer.filterWindow && layer.filterWindow.isVisible()) {
				layer.filterWindow.filter();
			}

			// Gui
			if (loader.updateGui && Ext.isObject(layer.widget)) {
				layer.widget.setGui(view);
			}

			// Zoom
			if (loader.zoomToVisibleExtent) {
				olmap.zoomToVisibleExtent();
			}

			// Mask
			if (loader.hideMask) {
				olmap.mask.hide();
			}

			// Map callback
			if (loader.callBack) {
				loader.callBack(layer);
			}
			else {
				gis.map = null;
				if (gis.viewport.shareButton) {
                    gis.viewport.shareButton.enable();
				}
			}

			// session storage
			if (GIS.isSessionStorage) {
				gis.util.layout.setSessionStorage('map', gis.util.layout.getAnalytical());
			}
		};

		loader = {
			compare: false,
			updateGui: false,
			zoomToVisibleExtent: false,
			hideMask: false,
			callBack: null,
			load: function(view) {
				gis.olmap.mask.show();

				if (this.compare) {
					compareView(view, true);
				}
				else {
					loadOrganisationUnits(view);
				}
			},
			loadData: loadData,
			loadLegend: loadLegend
		};

		return loader;
	};

	GIS.core.getInstance = function(init) {
		var conf = {},
			util = {},
			api = {},
			store = {},
			layers = [],
			gis = {};

		// conf
		(function() {
			conf.finals = {
				url: {
					path_api: '/api/',
					path_module: '/dhis-web-mapping/',
					path_commons: '/dhis-web-commons-ajax-json/'
				},
				layer: {
					type_base: 'base',
					type_vector: 'vector',
					category_thematic: 'thematic'
				},
				dimension: {
					data: {
						id: 'data',
						value: 'data',
						param: 'dx',
						dimensionName: 'dx',
						objectName: 'dx'
					},
					category: {
						name: GIS.i18n.categories,
						dimensionName: 'co',
						objectName: 'co',
					},
					indicator: {
						id: 'indicator',
						value: 'indicators',
						param: 'in',
						dimensionName: 'dx',
						objectName: 'in'
					},
					dataElement: {
						id: 'dataElement',
						value: 'dataElement',
						param: 'de',
						dimensionName: 'dx',
						objectName: 'de'
					},
					operand: {
						id: 'operand',
						value: 'operand',
						param: 'dc',
						dimensionName: 'dx',
						objectName: 'dc'
					},
					dataSet: {
						value: 'dataSets',
						dimensionName: 'dx',
						objectName: 'ds'
					},
					period: {
						id: 'period',
						value: 'period',
						param: 'pe',
						dimensionName: 'pe',
						objectName: 'pe'
					},
					organisationUnit: {
						id: 'organisationUnit',
						value: 'organisationUnit',
						param: 'ou',
						dimensionName: 'ou',
						objectName: 'ou'
					},
					value: {
						id: 'value',
						value: 'value',
						param: 'value',
						dimensionName: 'value',
						objectName: 'value'
					}
				},
				widget: {
					value: 'value',
					legendtype_automatic: 'automatic',
					legendtype_predefined: 'predefined',
					symbolizer_color: 'color',
					symbolizer_image: 'image',
					loadtype_organisationunit: 'organisationUnit',
					loadtype_data: 'data',
					loadtype_legend: 'legend'
				},
				openLayers: {
					point_classname: 'OpenLayers.Geometry.Point'
				},
				mapfish: {
					classify_with_bounds: 1,
					classify_by_equal_intervals: 2,
					classify_by_quantils: 3
				},
				root: {
					id: 'root'
				}
			};

			conf.layout = {
				widget: {
					item_width: 288,
					itemlabel_width: 95,
					window_width: 310
				},
				tool: {
					item_width: 228,
					itemlabel_width: 95,
					window_width: 250
				},
				grid: {
					row_height: 27
				},
				layer: {
					opacity: 0.8
				}
			};

			conf.period = {
				periodTypes: [
					{id: 'relativePeriods', name: GIS.i18n.relative},
					{id: 'Daily', name: GIS.i18n.daily},
					{id: 'Weekly', name: GIS.i18n.weekly},
					{id: 'Monthly', name: GIS.i18n.monthly},
					{id: 'BiMonthly', name: GIS.i18n.bimonthly},
					{id: 'Quarterly', name: GIS.i18n.quarterly},
					{id: 'SixMonthly', name: GIS.i18n.sixmonthly},
                    {id: 'SixMonthlyApril', name: GIS.i18n.sixmonthly_april},
					{id: 'Yearly', name: GIS.i18n.yearly},
					{id: 'FinancialOct', name: GIS.i18n.financial_oct},
					{id: 'FinancialJuly', name: GIS.i18n.financial_july},
					{id: 'FinancialApril', name: GIS.i18n.financial_april}
				],
				relativePeriods: [
					{id: 'LAST_WEEK', name: GIS.i18n.last_week},
					{id: 'LAST_MONTH', name: GIS.i18n.last_month},
					{id: 'LAST_BIMONTH', name: GIS.i18n.last_bimonth},
					{id: 'LAST_QUARTER', name: GIS.i18n.last_quarter},
					{id: 'LAST_SIX_MONTH', name: GIS.i18n.last_sixmonth},
					{id: 'LAST_FINANCIAL_YEAR', name: GIS.i18n.last_financial_year},
					{id: 'THIS_YEAR', name: GIS.i18n.this_year},
					{id: 'LAST_YEAR', name: GIS.i18n.last_year}
				],
				relativePeriodsMap: {
					'LAST_WEEK': {id: 'LAST_WEEK', name: GIS.i18n.last_week},
					'LAST_MONTH': {id: 'LAST_MONTH', name: GIS.i18n.last_month},
					'LAST_BIMONTH': {id: 'LAST_BIMONTH', name: GIS.i18n.last_bimonth},
					'LAST_QUARTER': {id: 'LAST_QUARTER', name: GIS.i18n.last_quarter},
					'LAST_SIX_MONTH': {id: 'LAST_SIX_MONTH', name: GIS.i18n.last_sixmonth},
					'LAST_FINANCIAL_YEAR': {id: 'LAST_FINANCIAL_YEAR', name: GIS.i18n.last_financial_year},
					'THIS_YEAR': {id: 'THIS_YEAR', name: GIS.i18n.this_year},
					'LAST_YEAR': {id: 'LAST_YEAR', name: GIS.i18n.last_year}
				},
				integratedRelativePeriodsMap: {
					'LAST_WEEK': 'LAST_WEEK',
					'LAST_4_WEEKS': 'LAST_WEEK',
					'LAST_12_WEEKS': 'LAST_WEEK',
					'LAST_MONTH': 'LAST_MONTH',
					'LAST_3_MONTHS': 'LAST_MONTH',
					'LAST_12_MONTHS': 'LAST_MONTH',
					'LAST_BIMONTH': 'LAST_BIMONTH',
					'LAST_6_BIMONTHS': 'LAST_BIMONTH',
					'LAST_QUARTER': 'LAST_QUARTER',
					'LAST_4_QUARTERS': 'LAST_QUARTER',
					'LAST_SIX_MONTH': 'LAST_SIX_MONTH',
					'LAST_2_SIXMONTHS': 'LAST_SIX_MONTH',
					'LAST_FINANCIAL_YEAR': 'LAST_FINANCIAL_YEAR',
					'LAST_5_FINANCIAL_YEARS': 'LAST_FINANCIAL_YEAR',
					'THIS_YEAR': 'THIS_YEAR',
					'LAST_YEAR': 'LAST_YEAR',
					'LAST_5_YEARS': 'LAST_YEAR'
				}
			};

            conf.url = {};

            conf.url.analysisFields = [
                '*',
                'columns[dimension,filter,items[id,name]]',
                'rows[dimension,filter,items[id,name]]',
                'filters[dimension,filter,items[id,name]]',
                '!lastUpdated',
                '!href',
                '!created',
                '!publicAccess',
                '!rewindRelativePeriods',
                '!userOrganisationUnit',
                '!userOrganisationUnitChildren',
                '!userOrganisationUnitGrandChildren',
                '!externalAccess',
                '!access',
                '!relativePeriods',
                '!columnDimensions',
                '!rowDimensions',
                '!filterDimensions',
                '!user',
                '!organisationUnitGroups',
                '!itemOrganisationUnitGroups',
                '!userGroupAccesses',
                '!indicators',
                '!dataElements',
                '!dataElementOperands',
                '!dataElementGroups',
                '!dataSets',
                '!periods',
                '!organisationUnitLevels',
                '!organisationUnits',

                '!sortOrder',
                '!topLimit'
            ];

            conf.url.mapFields = [
                conf.url.analysisFields.join(','),
                'mapViews[' + conf.url.analysisFields.join(',') + ']'
            ];

            conf.url.mapLegendFields = [
                '*',
                '!created',
                '!lastUpdated',
                '!displayName',
                '!externalAccess',
                '!access',
                '!userGroupAccesses'
            ];

            conf.url.mapLegendSetFields = [
                'id,name,mapLegends[' + conf.url.mapLegendFields.join(',') + ']'
            ];
        }());

		// util
		(function() {
			util.map = {};

			util.map.getVisibleVectorLayers = function() {
				var layers = [];

				for (var i = 0, layer; i < gis.olmap.layers.length; i++) {
					layer = gis.olmap.layers[i];
					if (layer.layerType === conf.finals.layer.type_vector && layer.visibility && layer.features.length) {
						layers.push(layer);
					}
				}
				return layers;
			};

			util.map.getRenderedVectorLayers = function() {
				var layers = [];

				for (var i = 0, layer; i < gis.olmap.layers.length; i++) {
					layer = gis.olmap.layers[i];
					if (layer.layerType === conf.finals.layer.type_vector && layer.features.length) {
						layers.push(layer);
					}
				}
				return layers;
			};

			util.map.getExtendedBounds = function(layers) {
				var bounds = null;
				if (layers.length) {
					bounds = layers[0].getDataExtent();
					if (layers.length > 1) {
						for (var i = 1; i < layers.length; i++) {
							bounds.extend(layers[i].getDataExtent());
						}
					}
				}
				return bounds;
			};

			util.map.zoomToVisibleExtent = function(olmap) {
				var bounds = util.map.getExtendedBounds(util.map.getVisibleVectorLayers(olmap));
				if (bounds) {
					olmap.zoomToExtent(bounds);
				}
			};

			util.map.getTransformedFeatureArray = function(features) {
				var sourceProjection = new OpenLayers.Projection("EPSG:4326"),
					destinationProjection = new OpenLayers.Projection("EPSG:900913");
				for (var i = 0; i < features.length; i++) {
					features[i].geometry.transform(sourceProjection, destinationProjection);
				}
				return features;
			};

			util.geojson = {};

			util.geojson.decode = function(doc, levelOrder) {
				var geojson = {};
				geojson.type = 'FeatureCollection';
				geojson.crs = {
					type: 'EPSG',
					properties: {
						code: '4326'
					}
				};
				geojson.features = [];

                levelOrder = levelOrder || 'ASC';

                // sort
                doc.geojson = util.array.sort(doc.geojson, levelOrder, 'le');

				for (var i = 0; i < doc.geojson.length; i++) {
					geojson.features.push({
						geometry: {
							type: parseInt(doc.geojson[i].ty) === 1 ? 'MultiPolygon' : 'Point',
							coordinates: doc.geojson[i].co
						},
						properties: {
							id: doc.geojson[i].uid,
							internalId: doc.geojson[i].iid,
							name: doc.geojson[i].na,
							hasCoordinatesDown: doc.geojson[i].hcd,
							hasCoordinatesUp: doc.geojson[i].hcu,
							level: doc.geojson[i].le,
							grandParentParentGraph: doc.geojson[i].gppg,
							grandParentId: doc.geojson[i].gpuid,
							parentGraph: doc.geojson[i].parentGraph,
							parentId: doc.geojson[i].pi,
							parentName: doc.geojson[i].pn
						}
					});
				}

				return geojson;
			};

			util.gui = {};
			util.gui.combo = {};

			util.gui.combo.setQueryMode = function(cmpArray, mode) {
				for (var i = 0; i < cmpArray.length; i++) {
					cmpArray[i].queryMode = mode;
				}
			};

			util.object = {};

			util.object.getLength = function(object) {
				var size = 0;

				for (var key in object) {
					if (object.hasOwnProperty(key)) {
						size++;
					}
				}

				return size;
			};

			util.array = {};

			util.array.sort = function(array, direction, key) {
				// accepts [number], [string], [{prop: number}], [{prop: string}]

				if (!util.object.getLength(array)) {
					return;
				}

				key = key || 'name';

				array.sort( function(a, b) {

					// if object, get the property values
					if (Ext.isObject(a) && Ext.isObject(b) && key) {
						a = a[key];
						b = b[key];
					}

					// string
					if (Ext.isString(a) && Ext.isString(b)) {
						a = a.toLowerCase();
						b = b.toLowerCase();

						if (direction === 'DESC') {
							return a < b ? 1 : (a > b ? -1 : 0);
						}
						else {
							return a < b ? -1 : (a > b ? 1 : 0);
						}
					}

					// number
					else if (Ext.isNumber(a) && Ext.isNumber(b)) {
						return direction === 'DESC' ? b - a : a - b;
					}

					return 0;
				});

				return array;
			};

            util.layout = {};

			util.layout.getAnalytical = function(map) {
				var layout,
					layer;

				if (Ext.isObject(map) && Ext.isArray(map.mapViews) && map.mapViews.length) {
					for (var i = 0, view, id; i < map.mapViews.length; i++) {
						view = map.mapViews[i];
						id = view.layer;

						if (gis.layer.hasOwnProperty(id) && gis.layer[id].layerCategory === gis.conf.finals.layer.category_thematic) {
							layout = gis.api.layout.Layout(view);

							if (layout) {
								return layout;
							}
						}
					}
				}
				else {
					for (var key in gis.layer) {
						if (gis.layer.hasOwnProperty(key) && gis.layer[key].layerCategory === gis.conf.finals.layer.category_thematic && gis.layer[key].core.view) {
							layer = gis.layer[key];
							layout = gis.api.layout.Layout(layer.core.view);

							if (layout) {
								if (!layout.parentGraphMap && layer.widget) {
									layout.parentGraphMap = layer.widget.getParentGraphMap();
								}

								return layout;
							}
						}
					}
				}

				return;
			};

			util.layout.getPluginConfig = function() {
				var layers = gis.util.map.getVisibleVectorLayers(),
					map = {};

				if (gis.map) {
					return gis.map;
				}

				map.mapViews = [];

				for (var i = 0, layer; i < layers.length; i++) {
					layer = layers[i];

					if (layer.core.view) {
						layer.core.view.layer = layer.id;

						map.mapViews.push(layer.core.view);
					}
				}

				return map;
			};

			util.layout.setSessionStorage = function(session, obj, url) {
				if (GIS.isSessionStorage) {
					var dhis2 = JSON.parse(sessionStorage.getItem('dhis2')) || {};
					dhis2[session] = obj;
					sessionStorage.setItem('dhis2', JSON.stringify(dhis2));

					if (Ext.isString(url)) {
						window.location.href = url;
					}
				}
			};

            util.layout.getDataDimensionsFromLayout = function(layout) {
                var dimensions = Ext.Array.clean([].concat(layout.columns || [], layout.rows || [], layout.filters || [])),
                    ignoreKeys = ['pe', 'ou'],
                    dataDimensions = [];

                for (var i = 0; i < dimensions.length; i++) {
                    if (!Ext.Array.contains(ignoreKeys, dimensions[i].dimension)) {
                        dataDimensions.push(dimensions[i]);
                    }
                }

                return dataDimensions;
            };

		}());

		gis.init = init;
		gis.conf = conf;
		gis.util = util;

		// api
		(function() {
			var dimConf = gis.conf.finals.dimension;

			api.layout = {};
			api.response = {};

			api.layout.Record = function(config) {
				var record = {};

				// id: string

				return function() {
					if (!Ext.isObject(config)) {
						console.log('Record config is not an object', config);
						return;
					}

					if (!Ext.isString(config.id)) {
						console.log('Record id is not text', config);
						return;
					}

					record.id = config.id.replace('#', '.');

					if (Ext.isString(config.name)) {
						record.name = config.name;
					}

					return Ext.clone(record);
				}();
			};

			api.layout.Dimension = function(config) {
				var dimension = {};

				// dimension: string

				// items: [Record]

				return function() {
					if (!Ext.isObject(config)) {
						//console.log('Dimension config is not an object: ' + config);
						return;
					}

					if (!Ext.isString(config.dimension)) {
						console.log('Dimension name is not text', config);
						return;
					}

					if (config.dimension !== conf.finals.dimension.category.objectName) {
						var records = [];

						if (!Ext.isArray(config.items)) {
							console.log('Dimension items is not an array', config);
							return;
						}

						for (var i = 0; i < config.items.length; i++) {
							record = api.layout.Record(config.items[i]);

							if (record) {
								records.push(record);
							}
						}

						config.items = records;

						if (!config.items.length) {
							console.log('Dimension has no valid items', config);
							return;
						}
					}

					dimension.dimension = config.dimension;
					dimension.items = config.items;

					return Ext.clone(dimension);
				}();
			};

			api.layout.Layout = function(config) {
				var config = Ext.clone(config),
					layout = {},
					getValidatedDimensionArray,
					validateSpecialCases;

				// layer: string

				// columns: [Dimension]

				// rows: [Dimension]

				// filters: [Dimension]

				// classes: integer (5) - 1-7

				// method: integer (2) - 2, 3 // 2=equal intervals, 3=equal counts

				// colorLow: string ('ff0000')

				// colorHigh: string ('00ff00')

				// radiusLow: integer (5)

				// radiusHigh: integer (15)

				// opacity: integer (0.8) - 0-1

				// legendSet: object

                // areaRadius: integer

                // hidden: boolean (false)

				getValidatedDimensionArray = function(dimensionArray) {
					var dimensions = [];

					if (!(dimensionArray && Ext.isArray(dimensionArray) && dimensionArray.length)) {
						return;
					}

					for (var i = 0, dimension; i < dimensionArray.length; i++) {
						dimension = api.layout.Dimension(dimensionArray[i]);

						if (dimension) {
							dimensions.push(dimension);
						}
					}

					dimensionArray = dimensions;

					return dimensionArray.length ? dimensionArray : null;
				};

				validateSpecialCases = function(config) {
					var dimensions = Ext.Array.clean([].concat(config.columns || [], config.rows || [], config.filters || [])),
						map = conf.period.integratedRelativePeriodsMap,
						dxDim,
						peDim,
						ouDim;

					for (var i = 0, dim; i < dimensions.length; i++) {
						dim = dimensions[i];

						if (dim.dimension === dimConf.indicator.objectName ||
							dim.dimension === dimConf.dataElement.objectName ||
							dim.dimension === dimConf.operand.objectName ||
							dim.dimension === dimConf.dataSet.objectName) {
							dxDim = dim;
						}
						else if (dim.dimension === dimConf.period.objectName) {
							peDim = dim;
						}
						else if (dim.dimension === dimConf.organisationUnit.objectName) {
							ouDim = dim;
						}
					}

					if (!ouDim) {
						alert('No organisation units specified');
						return;
					}

					if (dxDim) {
						dxDim.items = [dxDim.items[0]];
					}

					if (peDim) {
						peDim.items = [peDim.items[0]];
						peDim.items[0].id = map[peDim.items[0].id] ? map[peDim.items[0].id] : peDim.items[0].id;
					}

					config.columns = [dxDim];
					config.rows = [ouDim];
					config.filters = [peDim];

					return config;
				};

				return function() {
					var a = [],
						objectNames = [],
						dimConf = conf.finals.dimension,
                        layerConf =
						isOu = false,
						isOuc = false,
						isOugc = false;

					config = validateSpecialCases(config);

					if (!config) {
						return;
					}

					config.columns = getValidatedDimensionArray(config.columns);
					config.rows = getValidatedDimensionArray(config.rows);
					config.filters = getValidatedDimensionArray(config.filters);

					if (!config.rows) {
						console.log('Organisation unit dimension is invalid', config.rows);
						return;
					}

                    if (Ext.Array.contains([gis.layer.thematic1.id, gis.layer.thematic2.id, gis.layer.thematic3.id, gis.layer.thematic4.id], config.layer)) {
                        if (!config.columns) {
                            console.log('Data dimension is invalid', config.columns);
                            return;
                        }
                    }

					// Collect object names and user orgunits
					for (var i = 0, dim, dims = Ext.Array.clean([].concat(config.columns, config.rows, config.filters)); i < dims.length; i++) {
						dim = dims[i];

						if (dim) {

							// Object names
							if (Ext.isString(dim.dimension)) {
								objectNames.push(dim.dimension);
							}

							// user orgunits
							if (dim.dimension === dimConf.organisationUnit.objectName && Ext.isArray(dim.items)) {
								for (var j = 0; j < dim.items.length; j++) {
									if (dim.items[j].id === 'USER_ORGUNIT') {
										isOu = true;
									}
									else if (dim.items[j].id === 'USER_ORGUNIT_CHILDREN') {
										isOuc = true;
									}
									else if (dim.items[j].id === 'USER_ORGUNIT_GRANDCHILDREN') {
										isOugc = true;
									}
								}
							}
						}
					}

					// Layout
					layout.columns = config.columns;
					layout.rows = config.rows;
					layout.filters = config.filters;

					// Properties
					layout.layer = Ext.isString(config.layer) && !Ext.isEmpty(config.layer) ? config.layer : 'thematic1';
					layout.classes = Ext.isNumber(config.classes) && !Ext.isEmpty(config.classes) ? config.classes : 5;
					layout.method = Ext.isNumber(config.method) && !Ext.isEmpty(config.method) ? config.method : 2;
					layout.colorLow = Ext.isString(config.colorLow) && !Ext.isEmpty(config.colorLow) ? config.colorLow : 'ff0000';
					layout.colorHigh = Ext.isString(config.colorHigh) && !Ext.isEmpty(config.colorHigh) ? config.colorHigh : '00ff00';
					layout.radiusLow = Ext.isNumber(config.radiusLow) && !Ext.isEmpty(config.radiusLow) ? config.radiusLow : 5;
					layout.radiusHigh = Ext.isNumber(config.radiusHigh) && !Ext.isEmpty(config.radiusHigh) ? config.radiusHigh : 15;
					layout.opacity = Ext.isNumber(config.opacity) && !Ext.isEmpty(config.opacity) ? config.opacity : gis.conf.layout.layer.opacity;
					layout.areaRadius = config.areaRadius;
                    layout.hidden = !!config.hidden;

					layout.userOrganisationUnit = isOu;
					layout.userOrganisationUnitChildren = isOuc;
					layout.userOrganisationUnitGrandChildren = isOugc;

					layout.parentGraphMap = Ext.isObject(config.parentGraphMap) ? config.parentGraphMap : null;

					layout.legendSet = config.legendSet;

					layout.organisationUnitGroupSet = config.organisationUnitGroupSet;

					return layout;
				}();
			};

			api.response.Header = function(config) {
				var header = {};

				// name: string

				// meta: boolean

				return function() {
					if (!Ext.isObject(config)) {
						console.log('Header is not an object', config);
						return;
					}

					if (!Ext.isString(config.name)) {
						console.log('Header name is not text', config);
						return;
					}

					if (!Ext.isBoolean(config.meta)) {
						console.log('Header meta is not boolean', config);
						return;
					}

					header.name = config.name;
					header.meta = config.meta;

					return Ext.clone(header);
				}();
			};

			api.response.Response = function(config) {
				var response = {};

				// headers: [Header]

				return function() {
					var headers = [];

					if (!(config && Ext.isObject(config))) {
						alert('Data response invalid', config);
						return false;
					}

					if (!(config.headers && Ext.isArray(config.headers))) {
						alert('Data response invalid', config);
						return false;
					}

					for (var i = 0, header; i < config.headers.length; i++) {
						header = api.response.Header(config.headers[i]);

						if (header) {
							headers.push(header);
						}
					}

					config.headers = headers;

					if (!config.headers.length) {
						alert('No valid response headers', config);
						return;
					}

					if (!(Ext.isArray(config.rows) && config.rows.length > 0)) {
						alert('No values found', config);
						return false;
					}

					if (config.headers.length !== config.rows[0].length) {
						alert('Data invalid', config);
						return false;
					}

					response.headers = config.headers;
					response.metaData = config.metaData;
					response.width = config.width;
					response.height = config.height;
					response.rows = config.rows;

					return response;
				}();
			};
		}());

		// store
		(function() {
			store.organisationUnitLevels = GIS.core.OrganisationUnitLevelStore(gis);
		}());

		gis.api = api;
		gis.store = store;

		gis.olmap = GIS.core.getOLMap(gis);
		gis.layer = GIS.core.getLayers(gis);
		gis.thematicLayers = [gis.layer.thematic1, gis.layer.thematic2, gis.layer.thematic3, gis.layer.thematic4];

		if (window.google) {
			layers.push(gis.layer.googleStreets, gis.layer.googleHybrid);
		}

		layers.push(
			gis.layer.openStreetMap,
			gis.layer.thematic4,
			gis.layer.thematic3,
			gis.layer.thematic2,
			gis.layer.thematic1,
			gis.layer.boundary,
			gis.layer.facility,
			gis.layer.event
		);

		gis.olmap.addLayers(layers);

		GIS.core.instances.push(gis);

		return gis;
	};

});
