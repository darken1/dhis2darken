Ext.onReady( function() {
	var createViewport,
		initialize,
		gis;

	// set app config
	(function() {

		// ext configuration
		Ext.QuickTips.init();

		Ext.override(Ext.LoadMask, {
			onHide: function() {
				this.callParent();
			}
		});

		// right click handler
		document.body.oncontextmenu = function() {
			return false;
		};
	}());

	GIS.app = {};

	GIS.app.extendInstance = function(gis) {
		var conf = gis.conf,
			util = gis.util,
			init = gis.init,
			store = gis.store,
			layer;

		// util
		(function() {
			util.map.getFeaturesByLayers = function(layers) {
				var a = [];
				for (var i = 0; i < layers.length; i++) {
					a = a.concat(layers[i].features);
				}
				return a;
			};

			util.map.getPointsByFeatures = function(features) {
				var a = [];
				for (var i = 0; i < features.length; i++) {
					if (features[i].geometry.CLASS_NAME === gis.conf.finals.openLayers.point_classname) {
						a.push(features[i]);
					}
				}
				return a;
			};

			util.map.getLonLatsByPoints = function(points) {
				var lonLat,
					point,
					a = [];
				for (var i = 0; i < points.length; i++) {
					point = points[i];
					lonLat = new OpenLayers.LonLat(point.geometry.x, point.geometry.y);
					a.push(lonLat);
				}
				return a;
			};

			util.map.hasVisibleFeatures = function() {
				var layers = util.map.getVisibleVectorLayers(),
					layer;

				if (layers.length) {
					for (var i = 0; i < layers.length; i++) {
						layer = layers[i];
						if (layer.features.length) {
							return true;
						}
					}
				}

				return false;
			};

			util.map.getLayersByType = function(layerType) {
				var layers = [];
				for (var i = 0; i < gis.olmap.layers.length; i++) {
					var layer = gis.olmap.layers[i];
					if (layer.layerType === layerType) {
						layers.push(layer);
					}
				}
				return layers;
			};

			util.map.addMapControl = function(name, fn) {
				var panel = GIS.app.MapControlPanel(name, fn);
				gis.olmap.addControl(panel);
				panel.div.className += ' ' + name;
				panel.div.childNodes[0].className += ' ' + name + 'Button';
			};

			util.map.getTransformedPointByXY = function(x, y) {
				var p = new OpenLayers.Geometry.Point(parseFloat(x), parseFloat(y));
				return p.transform(new OpenLayers.Projection('EPSG:4326'), new OpenLayers.Projection('EPSG:900913'));
			};

			util.map.getLonLatByXY = function(x, y) {
				var point = util.map.getTransformedPointByXY(x, y);
				return new OpenLayers.LonLat(point.x, point.y);
			};

			util.map.map2plugin = function(map) {
				map.url = init.contextPath;

				if (map.id) {
					return {id: map.id};
				}

				delete map.access;
				delete map.created;
				delete lastUpdated;
				delete name;

				for (var i = 0, dimensions, layout; i < map.mapViews.length; i++) {
					layout = map.mapViews[i];

					dimensions = Ext.Array.clean([].concat(layout.columns || [], layout.rows || [], layout.filters || []));dimension = dimensions[i];

					for (var j = 0, dimension; j < dimensions.length; j++) {
						dimension = dimensions[j];

						delete dimension.id;
						delete dimension.ids;
						delete dimension.type;
						delete dimension.dimensionName;
						delete dimension.objectName;

						for (var k = 0, item; k < dimension.items.length; k++) {
							item = dimension.items[k];

							delete item.name;
							delete item.code;
							delete item.created;
							delete item.lastUpdated;
						}
					}

					if (layout.legendSet) {
						delete layout.method;
						delete layout.classes;
						delete layout.colorLow;
						delete layout.colorHigh;
						delete layout.radiusLow;
						delete layout.radiusHigh;
					}
					else {
						if (layout.method === 2) {
							delete layout.method;
						}

						if (layout.classes === 5) {
							delete layout.classes;
						}

						if (layout.colorLow === "ff0000") {
							delete layout.colorLow;
						}

						if (layout.colorHigh === "00ff00") {
							delete layout.colorHigh;
						}

						if (layout.radiusLow === 5) {
							delete layout.radiusLow;
						}

						if (layout.radiusHigh === 15) {
							delete layout.radiusHigh;
						}
					}

					if (layout.opacity === gis.conf.layout.layer.opacity) {
						delete layout.opacity;
					}

					if (!layout.userOrganisationUnit) {
						delete layout.userOrganisationUnit;
					}

					if (!layout.userOrganisationUnitChildren) {
						delete layout.userOrganisationUnitChildren;
					}

					if (!layout.userOrganisationUnitGrandChildren) {
						delete layout.userOrganisationUnitGrandChildren;
					}

					if (!layout.organisationUnitGroupSet) {
						delete layout.organisationUnitGroupSet;
					}

					delete layout.parentGraphMap;
				}

				return map;
			};

			util.url = util.url || {};

			util.url.getUrlParam = function(s) {
				var output = '';
				var href = window.location.href;
				if (href.indexOf('?') > -1 ) {
					var query = href.substr(href.indexOf('?') + 1);
					var query = query.split('&');
					for (var i = 0; i < query.length; i++) {
						if (query[i].indexOf('=') > -1) {
							var a = query[i].split('=');
							if (a[0].toLowerCase() === s) {
								output = a[1];
								break;
							}
						}
					}
				}
				return unescape(output);
			};

			util.svg = util.svg || {};

			util.svg.merge = function(str, strArray) {
				if (strArray.length) {
					str = str || '<svg></svg>';
					for (var i = 0; i < strArray.length; i++) {
						str = str.replace('</svg>', '');
						strArray[i] = strArray[i].substring(strArray[i].indexOf('>') + 1);
						str += strArray[i];
					}
				}
				return str;
			};

			util.svg.getString = function(title, layers) {
				var svgArray = [],
					svg = '',
					namespace,
					title = Ext.htmlEncode(title),
					titleSVG,
					legendSVG = '',
					scalelineSVG,
					x = 20,
					y = 35,
					center = gis.viewport.centerRegion,
					scalelineEl = Ext.get(Ext.query('.olControlScaleLineTop')[0]);

				if (!layers.length) {
					return false;
				}

				layers = layers.reverse();

				namespace = 'xmlns="http://www.w3.org/2000/svg"';

				svg = '<svg ' + namespace + ' width="' + center.getWidth() + '" height="' + center.getHeight() + '"></svg>';

				titleSVG = '<g id="title" style="display: block; visibility: visible;">' +
						   '<text id="title" x="' + x + '" y="' + y + '" font-size="18" font-weight="bold">' +
						   '<tspan>' + title + '</tspan></text></g>';

				y += 35;

				for (var i = layers.length - 1; i > 0; i--) {
					if (layers[i].id === gis.layer.facility.id) {
						layers.splice(i, 1);
						console.log(GIS.i18n.facility_layer_export_currently_not_supported);
					}
				}

				for (var i = 0; i < layers.length; i++) {
					var layer = layers[i],
						id = layer.id,
						imageLegendConfig = layer.core.getImageLegendConfig(),
						what,
						when,
						where,
						legend;

					// SVG
					svgArray.push(layer.div.innerHTML);

					// Legend
					if (id !== gis.layer.boundary.id && id !== gis.layer.facility.id && id !== gis.layer.event.id) {
						what = '<g id="indicator" style="display: block; visibility: visible;">' +
							   '<text id="indicator" x="' + x + '" y="' + y + '" font-size="12">' +
							   '<tspan>' + Ext.htmlEncode(layer.core.view.columns[0].items[0].name) + '</tspan></text></g>';

						y += 15;

						when = '<g id="period" style="display: block; visibility: visible;">' +
							   '<text id="period" x="' + x + '" y="' + y + '" font-size="12">' +
							   '<tspan>' + Ext.htmlEncode(layer.core.view.filters[0].items[0].name) + '</tspan></text></g>';

						y += 8;

						legend = '<g>';

						for (var j = 0; j < imageLegendConfig.length; j++) {
							if (j !== 0) {
								y += 15;
							}

							legend += '<rect x="' + x + '" y="' + y + '" height="15" width="30" ' +
									  'fill="' + Ext.htmlEncode(imageLegendConfig[j].color) + '" stroke="#000000" stroke-width="1"/>';

							legend += '<text id="label" x="' + (x + 40) + '" y="' + (y + 12) + '" font-size="12">' +
									  '<tspan>' + Ext.htmlEncode(imageLegendConfig[j].label) + '</tspan></text>';
						}

						legend += '</g>';

						legendSVG += (what + when + where + legend);

						y += 50;
					}
				}

				// Scale line
				scalelineSVG = '<text x="' + (x + 3) + '" y="' + y + '" fill="#000">' + scalelineEl.dom.innerHTML + '</text>';

				y += 3;
				scalelineSVG += '<line x1="' + x + '" y1="' + y + '" x2="' + x + '" y2="' + (y + 3) + '" style="stroke:#000;stroke-width:1" />';
				scalelineSVG += '<line x1="' + (x + scalelineEl.getWidth()) + '" y1="' + y + '" x2="' + (x + scalelineEl.getWidth()) + '" y2="' + (y + 3) + '" style="stroke:#000;stroke-width:1" />';

				y += 3;
				scalelineSVG += '<line x1="' + x + '" y1="' + y + '" x2="' + (x + scalelineEl.getWidth()) + '" y2="' + y + '" style="stroke:#000;stroke-width:1" />';

				// Map
				if (svgArray.length) {
					svg = util.svg.merge(svg, svgArray);
				}

				svg = svg.replace('</svg>', (titleSVG + legendSVG + scalelineSVG) + '</svg>');

				return svg;
			};

			util.json = util.json || {};

			util.json.encodeString = function(str) {
				return Ext.isString(str) ? str.replace(/[^a-zA-Z 0-9(){}<>_!+;:?*&%#-]+/g,'') : str;
			};

			util.json.decodeAggregatedValues = function(responseText) {
				responseText = Ext.decode(responseText);
				var values = [];

				for (var i = 0; i < responseText.length; i++) {
					values.push({
						oi: responseText[i][0],
						v: responseText[i][1]
					});
				}
				return values;
			};

			util.gui = util.gui || {};
			util.gui.window = util.gui.window || {};

			util.gui.window.setPositionTopRight = function(window) {
				window.setPosition(gis.viewport.centerRegion.getWidth() - (window.getWidth() + 3), gis.viewport.centerRegion.y + 58);
			};

			util.gui.window.setPositionTopLeft = function(window) {
				window.setPosition(2,33);
			};

			util.gui.window.addHideOnBlurHandler = function(w) {
				var el = Ext.get(Ext.query('.x-mask')[0]);

				el.on('click', function() {
					if (w.hideOnBlur) {
						w.hide();
					}
				});

				w.hasHideOnBlurHandler = true;
			};

			util.gui.window.addDestroyOnBlurHandler = function(w) {
				var el = Ext.get(Ext.query('.x-mask')[0]);

				el.on('click', function() {
					if (w.destroyOnBlur) {
						w.destroy();
					}
				});

				w.hasDestroyOnBlurHandler = true;
			};

			util.gui.window.setAnchorPosition = function(w, target) {
				var vpw = gis.viewport.getWidth(),
					targetx = target ? target.getPosition()[0] : 4,
					winw = w.getWidth(),
					y = target ? target.getPosition()[1] + target.getHeight() + 4 : 33;

				if ((targetx + winw) > vpw) {
					w.setPosition((vpw - winw - 2), y);
				}
				else {
					w.setPosition(targetx, y);
				}
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
		}());

		// init
		(function() {

			// root nodes
			for (var i = 0; i < init.rootNodes.length; i++) {
				init.rootNodes[i].path = '/root/' + init.rootNodes[i].id;
			}

			// sort organisation unit levels
			gis.util.array.sort(init.organisationUnitLevels, 'ASC', 'level');

			// sort indicator groups
			gis.util.array.sort(init.indicatorGroups);

			// sort data element groups
			gis.util.array.sort(init.dataElementGroups);
		}());

		// store
		(function() {
			store.periodTypes = Ext.create('Ext.data.Store', {
				fields: ['id', 'name'],
				data: gis.conf.period.periodTypes
			});

			store.groupSets = Ext.create('Ext.data.Store', {
				fields: ['id', 'name'],
				proxy: {
					type: 'ajax',
					url: gis.init.contextPath + '/api/organisationUnitGroupSets.json?fields=id,name&paging=false',
					reader: {
						type: 'json',
						root: 'organisationUnitGroupSets'
					}
				},
				isLoaded: false,
				loadFn: function(fn) {
					if (this.isLoaded) {
						fn.call();
					}
					else {
						this.load(fn);
					}
				},
				listeners: {
					load: function() {
						if (!this.isLoaded) {
							this.isLoaded = true;
						}
						this.sort('name', 'ASC');
					}
				}
			});

			store.groupsByGroupSet = Ext.create('Ext.data.Store', {
				fields: ['id', 'name', 'symbol'],
			});

			store.organisationUnitGroup = Ext.create('Ext.data.Store', {
				fields: ['id', 'name'],
				proxy: {
					type: 'ajax',
					url: init.contextPath + '/api/organisationUnitGroups.json?fields=id,name&paging=false',
					reader: {
						type: 'json',
						root: 'organisationUnitGroups'
					}
				}
			});

			store.legendSets = Ext.create('Ext.data.Store', {
				fields: ['id', 'name'],
				proxy: {
					type: 'ajax',
					url: gis.init.contextPath + '/api/mapLegendSets.json?fields=id,name&paging=false',
					reader: {
						type: 'json',
						root: 'mapLegendSets'
					}
				},
				isLoaded: false,
				loadFn: function(fn) {
					if (this.isLoaded) {
						fn.call();
					}
					else {
						this.load(fn);
					}
				},
				listeners: {
					load: function() {
						if (!this.isLoaded) {
							this.isLoaded = true;
						}
						this.sort('name', 'ASC');
					}
				}
			});

			store.maps = Ext.create('Ext.data.Store', {
				fields: ['id', 'name', 'access'],
				proxy: {
					type: 'ajax',
					reader: {
						type: 'json',
						root: 'maps'
					}
				},
				isLoaded: false,
				pageSize: 10,
				page: 1,
				defaultUrl: gis.init.contextPath + gis.conf.finals.url.path_api + 'maps.json?fields=id,name,access',
				loadStore: function(url) {
					this.proxy.url = url || this.defaultUrl;

					this.load({
						params: {
							pageSize: this.pageSize,
							page: this.page
						}
					});
				},
				loadFn: function(fn) {
					if (this.isLoaded) {
						fn.call();
					}
					else {
						this.load(fn);
					}
				},
				listeners: {
					load: function() {
						if (!this.isLoaded) {
							this.isLoaded = true;
						}

						this.sort('name', 'ASC');
					}
				}
			});
		}());

		// layer
		(function() {
			layer = gis.layer.event;
			layer.menu = GIS.app.LayerMenu(layer, 'gis-toolbar-btn-menu-first');
			layer.widget = GIS.app.LayerWidgetEvent(layer);
			layer.window = GIS.app.WidgetWindow(layer, gis.conf.layout.widget.window_width + 150, 1);
			layer.window.widget = layer.widget;
			GIS.core.createSelectHandlers(gis, layer);

			layer = gis.layer.facility;
			layer.menu = GIS.app.LayerMenu(layer);
			layer.widget = GIS.app.LayerWidgetFacility(layer);
			layer.window = GIS.app.WidgetWindow(layer);
			layer.window.widget = layer.widget;
			GIS.core.createSelectHandlers(gis, layer);

			layer = gis.layer.boundary;
			layer.menu = GIS.app.LayerMenu(layer);
			layer.widget = GIS.app.LayerWidgetBoundary(layer);
			layer.window = GIS.app.WidgetWindow(layer);
			layer.window.widget = layer.widget;
			GIS.core.createSelectHandlers(gis, layer);

			layer = gis.layer.thematic1;
			layer.menu = GIS.app.LayerMenu(layer);
			layer.widget = GIS.app.LayerWidgetThematic(layer);
			layer.window = GIS.app.WidgetWindow(layer);
			layer.window.widget = layer.widget;
			GIS.core.createSelectHandlers(gis, layer);

			layer = gis.layer.thematic2;
			layer.menu = GIS.app.LayerMenu(layer);
			layer.widget = GIS.app.LayerWidgetThematic(layer);
			layer.window = GIS.app.WidgetWindow(layer);
			layer.window.widget = layer.widget;
			GIS.core.createSelectHandlers(gis, layer);

			layer = gis.layer.thematic3;
			layer.menu = GIS.app.LayerMenu(layer);
			layer.widget = GIS.app.LayerWidgetThematic(layer);
			layer.window = GIS.app.WidgetWindow(layer);
			GIS.core.createSelectHandlers(gis, layer);

			layer = gis.layer.thematic4;
			layer.menu = GIS.app.LayerMenu(layer);
			layer.widget = GIS.app.LayerWidgetThematic(layer);
			layer.window = GIS.app.WidgetWindow(layer);
			layer.window.widget = layer.widget;
			GIS.core.createSelectHandlers(gis, layer);
		}());
	};

	GIS.app.createExtensions = function() {

		Ext.define('Ext.ux.panel.LayerItemPanel', {
			extend: 'Ext.panel.Panel',
			alias: 'widget.layeritempanel',
			layout: 'column',
			layer: null,
			checkbox: null,
			numberField: null,
			imageUrl: null,
			text: null,
			height: 22,
			value: false,
			opacity: gis.conf.layout.layer.opacity,
			getValue: function() {
				return this.checkbox.getValue();
			},
			setValue: function(value, opacity) {
				this.checkbox.setValue(value);
				this.numberField.setDisabled(!value);
				this.layer.setVisibility(value);

				if (value) {
					opacity = Ext.isNumber(parseFloat(opacity)) ? parseFloat(opacity) : this.opacity;

					if (opacity === 0) {
						this.numberField.setValue(0);
						this.setOpacity(0.01);
					}
					else {
						this.numberField.setValue(opacity * 100);
						this.setOpacity(opacity);
					}
				}
			},
			getOpacity: function() {
				return this.opacity;
			},
			setOpacity: function(opacity) {
				this.opacity = opacity === 0 ? 0.01 : opacity;
				this.layer.setLayerOpacity(this.opacity);

				if (this.layer.circleLayer) {
					this.layer.circleLayer.setOpacity(this.opacity);
				}
			},
			disableItem: function() {
				this.checkbox.setValue(false);
				this.numberField.disable();
				this.layer.setVisibility(false);
			},
			enableItem: function() {
				this.checkbox.setValue(true);
				this.numberField.enable();
				this.layer.setVisibility(true);
			},
			updateItem: function(value) {
				this.numberField.setDisabled(!value);
				this.layer.setVisibility(value);

				if (value && this.layer.layerType === gis.conf.finals.layer.type_base) {
					gis.olmap.setBaseLayer(this.layer);
				}

				if (this.layer.circleLayer) {
					this.layer.circleLayer.setVisibility(value);
				}
			},
			initComponent: function() {
				var that = this,
					image;

				this.checkbox = Ext.create('Ext.form.field.Checkbox', {
					width: 14,
					checked: this.value,
					listeners: {
						change: function(chb, value) {
							if (value && that.layer.layerType === gis.conf.finals.layer.type_base) {
								var layers = gis.util.map.getLayersByType(gis.conf.finals.layer.type_base);

								for (var i = 0; i < layers.length; i++) {
									if (layers[i] !== that.layer) {
										layers[i].item.checkbox.suppressChange = true;
										layers[i].item.disableItem();
									}
								}
							}
							that.updateItem(value);

							if (gis.viewport) {
								gis.viewport.downloadButton.xable();
							}
						}
					}
				});

				image = Ext.create('Ext.Img', {
					width: 14,
					height: 14,
					src: this.imageUrl
				});

				this.numberField = Ext.create('Ext.form.field.Number', {
					cls: 'gis-numberfield',
					width: 47,
					height: 18,
					minValue: 0,
					maxValue: 100,
					value: this.opacity * 100,
					allowBlank: false,
					disabled: this.numberFieldDisabled,
					listeners: {
						change: function() {
							var value = this.getValue(),
								opacity = value === 0 ? 0.01 : value/100;

							that.setOpacity(opacity);
						}
					}
				});

				this.items = [
					{
						width: this.checkbox.width + 4,
						items: this.checkbox
					},
					{
						width: image.width + 5,
						items: image,
						bodyStyle: 'padding-top: 4px'
					},
					{
						width: 106,
						html: this.text,
						bodyStyle: 'padding-top: 4px'
					},
					{
						width: this.numberField.width,
						items: this.numberField
					}
				];

				this.layer.setOpacity(this.opacity);

				this.callParent();
			}
		});

		Ext.define('Ext.ux.panel.CheckTextNumber', {
			extend: 'Ext.panel.Panel',
			alias: 'widget.checktextnumber',
			layout: 'column',
			layer: null,
			checkbox: null,
			text: null,
			numberField: null,
			width: 184,
			height: 22,
			value: false,
			number: 5000,
			getValue: function() {
				return this.checkbox.getValue();
			},
			getNumber: function() {
				return this.numberField.getValue();
			},
			setValue: function(value, number) {
				if (value) {
					this.checkbox.setValue(value);
				}
				if (number) {
					this.numberField.setValue(number);
				}
			},
			enable: function() {
				this.numberField.enable();
			},
			disable: function() {
				this.numberField.disable();
			},
			reset: function() {
				this.checkbox.setValue(false);
				this.numberField.setValue(this.number);
				this.numberField.disable();
			},
			initComponent: function() {
				var that = this,
					padding = 6;

				this.numberField = Ext.create('Ext.form.field.Number', {
					cls: 'gis-numberfield',
					fieldStyle: 'border-top-left-radius: 1px; border-bottom-left-radius: 1px',
					style: 'padding-bottom: 3px',
					width: 70,
					height: 21,
					minValue: 0,
					maxValue: 9999999,
					value: this.number,
					allowBlank: false,
					disabled: true
				});

				this.checkbox = Ext.create('Ext.form.field.Checkbox', {
					width: this.width - this.numberField.width - padding,
					boxLabel: this.text,
					checked: this.value,
					disabled: this.disabled,
					boxLabelCls: 'x-form-cb-label-alt1',
					listeners: {
						change: function(chb, value) {
							if (value) {
								that.enable();
							}
							else {
								that.disable();
							}
						}
					}
				});

				this.items = [
					{
						width: this.checkbox.width + padding,
						items: this.checkbox
					},
					{
						width: this.numberField.width,
						items: this.numberField
					}
				];

				this.callParent();
			}
		});

        var operatorCmpWidth = 70,
            valueCmpWidth = 306,
            buttonCmpWidth = 20,
            nameCmpWidth = 400,
            namePadding = '2px 3px',
            margin = '3px 0 1px';

        Ext.define('Ext.ux.panel.DataElementIntegerContainer', {
			extend: 'Ext.container.Container',
			alias: 'widget.dataelementintegerpanel',
			layout: 'column',
            bodyStyle: 'border:0 none',
            style: 'margin: ' + margin,
            getRecord: function() {
                var record = {};

                record.dimension = this.dataElement.id;
                record.name = this.dataElement.name;

                if (this.valueCmp.getValue()) {
					record.filter = this.operatorCmp.getValue() + ':' + this.valueCmp.getValue();
				}

				return record;
            },
            setRecord: function(record) {
				if (record.filter) {
					var a = record.filter.split(':');

					this.operatorCmp.setValue(a[0]);
					this.valueCmp.setValue(a[1]);
				}
			},
            initComponent: function() {
                var container = this;

                this.nameCmp = Ext.create('Ext.form.Label', {
                    text: this.dataElement.name,
                    width: nameCmpWidth,
                    style: 'padding:' + namePadding
                });

                this.operatorCmp = Ext.create('Ext.form.field.ComboBox', {
                    valueField: 'id',
                    displayField: 'name',
                    queryMode: 'local',
                    editable: false,
                    width: operatorCmpWidth,
					style: 'margin-bottom:0',
                    value: 'EQ',
                    store: {
                        fields: ['id', 'name'],
                        data: [
                            {id: 'EQ', name: '='},
                            {id: 'GT', name: '>'},
                            {id: 'GE', name: '>='},
                            {id: 'LT', name: '<'},
                            {id: 'LE', name: '<='},
                            {id: 'NE', name: '!='}
                        ]
                    }
                });

                this.valueCmp = Ext.create('Ext.form.field.Number', {
                    width: valueCmpWidth,
					style: 'margin-bottom:0'
                });

                this.addCmp = Ext.create('Ext.button.Button', {
                    text: '+',
                    width: buttonCmpWidth,
                    handler: function() {
						container.duplicateDataElement();
					}
                });

                this.removeCmp = Ext.create('Ext.button.Button', {
                    text: 'x',
                    width: buttonCmpWidth,
                    handler: function() {
                        container.removeDataElement();
                    }
                });

                this.items = [
                    this.nameCmp,
                    this.operatorCmp,
                    this.valueCmp,
                    this.addCmp,
                    this.removeCmp
                ];

                this.callParent();
            }
        });

        Ext.define('Ext.ux.panel.DataElementStringContainer', {
			extend: 'Ext.container.Container',
			alias: 'widget.dataelementstringpanel',
			layout: 'column',
            bodyStyle: 'border:0 none',
            style: 'margin: ' + margin,
            getRecord: function() {
                var record = {};

                record.dimension = this.dataElement.id;
                record.name = this.dataElement.name;

                if (this.valueCmp.getValue()) {
					record.filter = this.operatorCmp.getValue() + ':' + this.valueCmp.getValue();
				}

				return record;
            },
            setRecord: function(record) {
                this.operatorCmp.setValue(record.operator);
                this.valueCmp.setValue(record.filter);
            },
            initComponent: function() {
                var container = this;

                this.nameCmp = Ext.create('Ext.form.Label', {
                    text: this.dataElement.name,
                    width: nameCmpWidth,
                    style: 'padding:' + namePadding
                });

                this.operatorCmp = Ext.create('Ext.form.field.ComboBox', {
                    valueField: 'id',
                    displayField: 'name',
                    queryMode: 'local',
                    editable: false,
                    width: operatorCmpWidth,
					style: 'margin-bottom:0',
                    value: 'LIKE',
                    store: {
                        fields: ['id', 'name'],
                        data: [
                            {id: 'LIKE', name: 'Contains'},
                            {id: 'EQ', name: 'Is exact'}
                        ]
                    }
                });

                this.valueCmp = Ext.create('Ext.form.field.Text', {
                    width: valueCmpWidth,
					style: 'margin-bottom:0'
                });

                this.addCmp = Ext.create('Ext.button.Button', {
                    text: '+',
                    width: buttonCmpWidth,
                    handler: function() {
						container.duplicateDataElement();
					}
                });

                this.removeCmp = Ext.create('Ext.button.Button', {
                    text: 'x',
                    width: buttonCmpWidth,
                    handler: function() {
                        container.removeDataElement();
                    }
                });

                this.items = [
                    this.nameCmp,
                    this.operatorCmp,
                    this.valueCmp,
                    this.addCmp,
                    this.removeCmp
                ];

                this.callParent();
            }
        });

        Ext.define('Ext.ux.panel.DataElementDateContainer', {
			extend: 'Ext.container.Container',
			alias: 'widget.dataelementdatepanel',
			layout: 'column',
            bodyStyle: 'border:0 none',
            style: 'margin: ' + margin,
            getRecord: function() {
                var record = {};

                record.dimension = this.dataElement.id;
                record.name = this.dataElement.name;

                if (this.valueCmp.getValue()) {
					record.filter = this.operatorCmp.getValue() + ':' + this.valueCmp.getSubmitValue();
				}

				return record;
            },
            setRecord: function(record) {
				if (record.filter && Ext.isString(record.filter)) {
					var a = record.filter.split(':');

					this.operatorCmp.setValue(a[0]);
					this.valueCmp.setValue(a[1]);
				}
            },
            initComponent: function() {
                var container = this;

                this.nameCmp = Ext.create('Ext.form.Label', {
                    text: this.dataElement.name,
                    width: nameCmpWidth,
                    style: 'padding:' + namePadding
                });

                this.operatorCmp = Ext.create('Ext.form.field.ComboBox', {
                    valueField: 'id',
                    displayField: 'name',
                    queryMode: 'local',
                    editable: false,
                    width: operatorCmpWidth,
                    style: 'margin-bottom:0',
                    value: 'EQ',
                    store: {
                        fields: ['id', 'name'],
                        data: [
                            {id: 'EQ', name: '='},
                            {id: 'GT', name: '>'},
                            {id: 'GE', name: '>='},
                            {id: 'LT', name: '<'},
                            {id: 'LE', name: '<='},
                            {id: 'NE', name: '!='}
                        ]
                    }
                });

                this.valueCmp = Ext.create('Ext.form.field.Date', {
					width: valueCmpWidth,
					style: 'margin-bottom:0',
					format: 'Y-m-d'
				});

                this.addCmp = Ext.create('Ext.button.Button', {
                    text: '+',
                    width: buttonCmpWidth,
                    handler: function() {
						container.duplicateDataElement();
					}
                });

                this.removeCmp = Ext.create('Ext.button.Button', {
                    text: 'x',
                    width: buttonCmpWidth,
                    handler: function() {
                        container.removeDataElement();
                    }
                });

                this.items = [
                    this.nameCmp,
                    this.operatorCmp,
                    this.valueCmp,
                    this.addCmp,
                    this.removeCmp
                ];

                this.callParent();
            }
        });

        Ext.define('Ext.ux.panel.DataElementBooleanContainer', {
			extend: 'Ext.container.Container',
			alias: 'widget.dataelementbooleanpanel',
			layout: 'column',
            bodyStyle: 'border:0 none',
            style: 'margin: ' + margin,
            getRecord: function() {
                var record = {};

                record.dimension = this.dataElement.id;
                record.name = this.dataElement.name;

                if (this.valueCmp.getValue()) {
					record.filter = 'EQ:' + this.valueCmp.getValue();
				}

				return record;
            },
            setRecord: function(record) {
                this.valueCmp.setValue(record.filter);
            },
            initComponent: function() {
                var container = this;

                this.nameCmp = Ext.create('Ext.form.Label', {
                    text: this.dataElement.name,
                    width: nameCmpWidth,
                    style: 'padding:' + namePadding
                });

                this.valueCmp = Ext.create('Ext.form.field.ComboBox', {
                    valueField: 'id',
                    displayField: 'name',
                    queryMode: 'local',
                    editable: false,
                    width: operatorCmpWidth + valueCmpWidth,
                    style: 'margin-bottom:0',
                    value: 'false',
                    store: {
                        fields: ['id', 'name'],
                        data: [
                            {id: 'true', name: 'Yes'},
                            {id: 'false', name: 'No'}
                        ]
                    }
                });

                this.addCmp = Ext.create('Ext.button.Button', {
                    text: '+',
                    width: buttonCmpWidth,
                    handler: function() {
						container.duplicateDataElement();
					}
                });

                this.removeCmp = Ext.create('Ext.button.Button', {
                    text: 'x',
                    width: buttonCmpWidth,
                    handler: function() {
                        container.removeDataElement();
                    }
                });

                this.items = [
                    this.nameCmp,
                    this.valueCmp,
                    this.addCmp,
                    this.removeCmp
                ];

                this.callParent();
            }
        });

		Ext.define('Ext.ux.panel.DataElementOptionContainer', {
			extend: 'Ext.container.Container',
			alias: 'widget.dataelementoptionpanel',
			layout: 'column',
            bodyStyle: 'border:0 none',
            style: 'margin: ' + margin,
            getRecord: function() {
				var valueArray = this.valueCmp.getValue().split(';'),
					record = {};

				for (var i = 0; i < valueArray.length; i++) {
					valueArray[i] = Ext.String.trim(valueArray[i]);
				}

				record.dimension = this.dataElement.id;
				record.name = this.dataElement.name;

				if (Ext.Array.clean(valueArray).length) {
					record.filter = this.operatorCmp.getValue() + ':' + valueArray.join(';');
				}

				return record;
            },
            setRecord: function(record) {
				if (Ext.isString(record.filter) && record.filter) {
					var a = record.filter.split(':');
					this.valueCmp.setOptionValues(a[1].split(';'));
				}
            },
            initComponent: function() {
                var container = this;

                this.nameCmp = Ext.create('Ext.form.Label', {
                    text: this.dataElement.name,
                    width: nameCmpWidth,
                    style: 'padding:' + namePadding
                });

                this.operatorCmp = Ext.create('Ext.form.field.ComboBox', {
                    valueField: 'id',
                    displayField: 'name',
                    queryMode: 'local',
                    editable: false,
                    style: 'margin-bottom:0',
                    width: operatorCmpWidth,
                    value: 'IN',
                    store: {
                        fields: ['id', 'name'],
                        data: [
                            {id: 'IN', name: 'One of'}
                        ]
                    }
                });

                this.valueStore = Ext.create('Ext.data.Store', {
					fields: ['id', 'name'],
					data: [],
					loadOptionSet: function(optionSetId, key, pageSize) {
						var store = this,
							params = {};

						params['max'] = pageSize || 15;

						if (key) {
							params['key'] = key;
						}

						Ext.Ajax.request({
							url: gis.init.contextPath + '/api/optionSets/' + optionSetId + '/options.json',
							params: params,
							disableCaching: false,
							success: function(r) {
								var options = Ext.decode(r.responseText).options,
									data = [];

								Ext.each(options, function(option) {
									data.push({
										id: option,
										name: option
									});
								});

								store.removeAll();
								store.add(data);
							}
						});
					},
                    listeners: {
						datachanged: function(s) {
							if (container.searchCmp && s.getRange().length) {
								container.searchCmp.expand();
							}
						}
					}
				});

                this.searchCmp = Ext.create('Ext.form.field.ComboBox', {
                    width: 62,
                    style: 'margin-bottom:0',
                    emptyText: 'Search..',
                    valueField: 'id',
                    displayField: 'name',
                    hideTrigger: true,
                    delimiter: '; ',
                    enableKeyEvents: true,
                    queryMode: 'local',
                    listConfig: {
                        minWidth: 304
                    },
                    store: this.valueStore,
                    listeners: {
						keyup: {
							fn: function(cb) {
								var value = cb.getValue(),
									optionSetId = container.dataElement.optionSet.id;

								// search
								container.valueStore.loadOptionSet(optionSetId, value);

                                // trigger
                                if (!value || (Ext.isString(value) && value.length === 1)) {
									container.triggerCmp.setDisabled(!!value);
								}
							}
						},
						select: function(cb) {

                            // value
							container.valueCmp.addOptionValue(cb.getValue());

                            // search
							cb.clearValue();

                            // trigger
                            container.triggerCmp.enable();
						}
					}
                });

                this.triggerCmp = Ext.create('Ext.button.Button', {
                    cls: 'gis-button-combotrigger',
                    disabledCls: 'gis-button-combotrigger-disabled',
                    width: 18,
                    height: 22,
                    storage: [],
                    handler: function(b) {
                        if (b.storage.length) {
							container.valueStore.removeAll();
                            container.valueStore.add(Ext.clone(b.storage));
                        }
                        else {
                            Ext.Ajax.request({
                                url: gis.init.contextPath + '/api/optionSets/' + container.dataElement.optionSet.id + '/options.json',
                                params: {
                                    'max': 14
                                },
                                success: function(r) {
                                    var options = Ext.decode(r.responseText).options,
                                        data = [];

                                    Ext.each(options, function(option) {
                                        data.push({
                                            id: option,
                                            name: option
                                        });
                                    });

                                    b.storage = Ext.clone(data);
									container.valueStore.removeAll();
                                    container.valueStore.add(data);
                                }
                            });
                        }
                    }
                });

                this.valueCmp = Ext.create('Ext.form.field.Text', {
					width: 226,
                    style: 'margin-bottom:0',
					addOptionValue: function(option) {
						var value = this.getValue();

						if (value) {
							var a = value.split(';');

							for (var i = 0; i < a.length; i++) {
								a[i] = Ext.String.trim(a[i]);
							};

							a = Ext.Array.clean(a);

							value = a.join('; ');
							value += '; ';
						}

						this.setValue(value += option);
					},
                    setOptionValues: function(optionArray) {
                        var value = '';

                        for (var i = 0; i < optionArray.length; i++) {
                            value += optionArray[i] + (i < (optionArray.length - 1) ? '; ' : '');
                        }

                        this.setValue(value);
                    }
				});

                this.addCmp = Ext.create('Ext.button.Button', {
                    text: '+',
                    width: buttonCmpWidth,
                    style: 'font-weight:bold',
                    handler: function() {
						container.duplicateDataElement();
					}
                });

                this.removeCmp = Ext.create('Ext.button.Button', {
                    text: 'x',
                    width: buttonCmpWidth,
                    handler: function() {
                        container.removeDataElement();
                    }
                });

                this.items = [
                    this.nameCmp,
                    this.operatorCmp,
                    this.searchCmp,
                    this.triggerCmp,
                    this.valueCmp,
                    this.addCmp,
                    this.removeCmp
                ];

                this.callParent();
            }
        });

    };

    // Objects

    GIS.app.LayerMenu = function(layer, cls) {
		var items = [],
			item;

		item = {
			text: GIS.i18n.edit_layer,
			iconCls: 'gis-menu-item-icon-edit',
			cls: 'gis-menu-item-first',
			alwaysEnabled: true,
			handler: function() {
				layer.window.show();
			}
		};
		items.push(item);

		items.push({
			xtype: 'menuseparator',
			alwaysEnabled: true
		});

		item = {
			text: GIS.i18n.labels,
			iconCls: 'gis-menu-item-icon-labels',
			handler: function() {
                var window = layer.labelWindow || (layer.labelWidow = GIS.app.LabelWindow(layer));

                if (layer.id === 'boundary') {
                    window.updateLabels();
                }
                else {
                    window.show();
                }
			}
		};
		items.push(item);

		if (!(layer.id === gis.layer.boundary.id || layer.id === gis.layer.facility.id || layer.id === gis.layer.event.id)) {
			item = {
				text: GIS.i18n.filter + '..',
				iconCls: 'gis-menu-item-icon-filter',
				handler: function() {
					if (layer.filterWindow) {
						if (layer.filterWindow.isVisible()) {
							return;
						}
						else {
							layer.filterWindow.destroy();
						}
					}

					layer.filterWindow = layer.id === gis.layer.facility.id ?
						GIS.app.FilterWindowFacility(layer) : GIS.app.FilterWindow(layer);
					layer.filterWindow.show();
				}
			};
			items.push(item);
		}

        if (!(layer.id === gis.layer.event.id)) {
            item = {
                text: GIS.i18n.search,
                iconCls: 'gis-menu-item-icon-search',
                handler: function() {
                    if (layer.searchWindow) {
                        if (layer.searchWindow.isVisible()) {
                            return;
                        }
                        else {
                            layer.searchWindow.destroy();
                        }
                    }

                    layer.searchWindow = GIS.app.SearchWindow(layer);
                    layer.searchWindow.show();
                }
            };
            items.push(item);
        }

		items.push({
			xtype: 'menuseparator',
			alwaysEnabled: true
		});

		item = {
			text: GIS.i18n.clear,
			iconCls: 'gis-menu-item-icon-clear',
			handler: function() {
				layer.core.reset();
			}
		};
		items.push(item);

		return Ext.create('Ext.menu.Menu', {
			shadow: false,
			showSeparator: false,
			enableItems: function() {
				Ext.each(this.items.items, function(item) {
					item.enable();
				});
			},
			disableItems: function() {
				Ext.Array.each(this.items.items, function(item) {
					if (!item.alwaysEnabled) {
						item.disable();
					}
				});
			},
			items: items,
			listeners: {
				afterrender: function() {
					this.getEl().addCls('gis-toolbar-btn-menu');
					if (cls) {
						this.getEl().addCls(cls);
					}
				},
				show: function() {
					if (layer.features.length) {
						this.enableItems();
					}
					else {
						this.disableItems();
					}

					this.doLayout(); // show menu bug workaround
				}
			}
		});
	};

	GIS.app.LayersPanel = function() {
		var layers = gis.layer,
			layer,
			items = [],
			item,
			panel,
			visibleLayer = window.google ? layers.googleStreets : layers.openStreetMap,
			orderedLayers = gis.olmap.layers.reverse();

		for (var i = 0; i < orderedLayers.length; i++) {
			layer = orderedLayers[i];

			item = Ext.create('Ext.ux.panel.LayerItemPanel', {
				cls: 'gis-container-inner',
				height: 23,
				layer: layer,
				text: layer.name,
				imageUrl: 'images/' + layer.id + '_14.png',
				value: layer.id === visibleLayer.id && window.google ? true : false,
				opacity: layer.layerOpacity,
				defaultOpacity: layer.layerOpacity,
				numberFieldDisabled: layer.id !== visibleLayer.id
			});

			layer.item = item;
			items.push(layer.item);
		}

		visibleLayer.item.setValue(!!window.google);

        panel = Ext.create('Ext.panel.Panel', {
			renderTo: 'layerItems',
			layout: 'fit',
			cls: 'gis-container-inner',
			layerItems: items,
			items: {
				cls: 'gis-container-inner',
				items: items
			}
		});

		return panel;
	};

	GIS.app.WidgetWindow = function(layer, width, padding) {
		width = width || gis.conf.layout.widget.window_width;
		padding = padding || 5;

		return Ext.create('Ext.window.Window', {
			//autoShow: true,
			title: layer.name,
			layout: 'fit',
			iconCls: 'gis-window-title-icon-' + layer.id,
            bodyStyle: 'padding:' + padding + 'px',
			cls: 'gis-container-default',
			closeAction: 'hide',
			width: width,
			resizable: false,
			isRendered: false,
			items: layer.widget,
			bbar: [
				'->',
				{
					text: GIS.i18n.update,
					handler: function() {
						var view = layer.widget.getView();

						if (view) {
							var loader = layer.core.getLoader();
							loader.compare = (layer.id !== gis.layer.facility.id),
							loader.zoomToVisibleExtent = true;
							loader.hideMask = true;
							loader.load(view);
						}
					}
				}
			],
			listeners: {
				show: function() {
					if (!this.isRendered) {
						this.isRendered = true;

						if (layer.core.view) {
							this.widget.setGui(layer.core.view);
						}
					}

					gis.util.gui.window.setPositionTopLeft(this);
				}
			}
		});
	};

	GIS.app.SearchWindow = function(layer) {
		var data = [],
			store = layer.core.featureStore,
			button,
			window;

		for (var i = 0; i < layer.features.length; i++) {
			data.push([layer.features[i].data.id, layer.features[i].data.name]);
		}

		if (!data.length) {
			GIS.logg.push([data, layer.id + '.search.data: feature ids/names']);
			alert(GIS.i18n.layer + ' ' + GIS.i18n.has_no_orgunits);
			return;
		}

		button = Ext.create('Ext.ux.button.ColorButton', {
			width: gis.conf.layout.tool.item_width - gis.conf.layout.tool.itemlabel_width,
			value: '0000ff'
		});

		window = Ext.create('Ext.window.Window', {
			title: GIS.i18n.organisationunit_search,
			layout: 'fit',
			iconCls: 'gis-window-title-icon-search',
			cls: 'gis-container-default',
			width: gis.conf.layout.tool.window_width,
			resizable: false,
			height: 400,
			items: [
				{
					cls: 'gis-container-inner',
					items: [
						{
							layout: 'column',
							cls: 'gis-container-inner',
							items: [
								{
									cls: 'gis-panel-html-label',
									html: GIS.i18n.highlight_color + ':',
									width: gis.conf.layout.tool.itemlabel_width
								},
								button
							]
						},
						{
							cls: 'gis-panel-html-separator'
						},
						{
							layout: 'column',
							cls: 'gis-container-inner',
							items: [
								{
									cls: 'gis-panel-html-label',
									html: GIS.i18n.text_filter + ':',
									width: gis.conf.layout.tool.itemlabel_width
								},
								{
									xtype: 'textfield',
									cls: 'gis-textfield',
									width: gis.conf.layout.tool.item_width - gis.conf.layout.tool.itemlabel_width,
									enableKeyEvents: true,
									listeners: {
										keyup: function() {
											store.clearFilter();
											if (this.getValue()) {
												store.filter('name', this.getValue());
											}
											store.sortStore();
										}
									}
								}
							]
						},
						{
							xtype: 'grid',
							cls: 'gis-grid',
							height: 290,
							width: gis.conf.layout.tool.item_width,
							scroll: 'vertical',
							hideHeaders: true,
							columns: [{
								id: 'name',
								text: 'Organisation units',
								dataIndex: 'name',
								sortable: false,
								width: gis.conf.layout.tool.item_width
							}],
							store: layer.core.featureStore,
							listeners: {
								select: function(grid, record) {
									var feature = layer.getFeaturesByAttribute('id', record.data.id)[0],
										color = button.getValue(),
										symbolizer;

									layer.redraw();

									if (feature.geometry.CLASS_NAME === gis.conf.finals.openLayers.point_classname) {
										symbolizer = new OpenLayers.Symbolizer.Point({
											pointRadius: 6,
											fillColor: '#' + color,
											strokeWidth: 1
										});
									}
									else {
										symbolizer = new OpenLayers.Symbolizer.Polygon({
											strokeColor: '#' + color,
											fillColor: '#' + color
										});
									}

									layer.drawFeature(feature, symbolizer);
								}
							}
						}
					]
				}
			],
			listeners: {
				render: function() {
					gis.util.gui.window.setPositionTopLeft(this);
					store.sortStore();
				},
				destroy: function() {
					layer.redraw();
				}
			}
		});

		return window;
	};

	GIS.app.FilterWindow = function(layer) {
		var lowerNumberField,
			greaterNumberField,
			lt,
			gt,
			filter,
			window;

		greaterNumberField = Ext.create('Ext.form.field.Number', {
			width: gis.conf.layout.tool.itemlabel_width,
			value: parseInt(layer.core.minVal),
			listeners: {
				change: function() {
					gt = this.getValue();
				}
			}
		});

		lowerNumberField = Ext.create('Ext.form.field.Number', {
			width: gis.conf.layout.tool.itemlabel_width,
			value: parseInt(layer.core.maxVal) + 1,
			listeners: {
				change: function() {
					lt = this.getValue();
				}
			}
		});

        filter = function() {
			var cache = layer.core.featureStore.features.slice(0),
				features = [];

            if (!gt && !lt) {
                features = cache;
            }
            else if (gt && lt) {
                for (var i = 0; i < cache.length; i++) {
                    if (gt < lt && (cache[i].attributes.value > gt && cache[i].attributes.value < lt)) {
                        features.push(cache[i]);
                    }
                    else if (gt > lt && (cache[i].attributes.value > gt || cache[i].attributes.value < lt)) {
                        features.push(cache[i]);
                    }
                    else if (gt === lt && cache[i].attributes.value === gt) {
                        features.push(cache[i]);
                    }
                }
            }
            else if (gt && !lt) {
                for (var i = 0; i < cache.length; i++) {
                    if (cache[i].attributes.value > gt) {
                        features.push(cache[i]);
                    }
                }
            }
            else if (!gt && lt) {
                for (var i = 0; i < cache.length; i++) {
                    if (cache[i].attributes.value < lt) {
                        features.push(cache[i]);
                    }
                }
            }

            layer.removeAllFeatures();
            layer.addFeatures(features);
        };

		window = Ext.create('Ext.window.Window', {
			title: 'Filter by value',
			iconCls: 'gis-window-title-icon-filter',
			cls: 'gis-container-default',
			width: gis.conf.layout.tool.window_width,
			resizable: false,
			filter: filter,
			items: {
				layout: 'fit',
				cls: 'gis-container-inner',
				items: [
					{
						cls: 'gis-container-inner',
						html: '<b>Show</b> organisation units with values..'
					},
					{
						cls: 'gis-panel-html-separator'
					},
					{
						cls: 'gis-panel-html-separator'
					},
					{
						layout: 'column',
						height: 22,
						cls: 'gis-container-inner',
						items: [
							{
								cls: 'gis-panel-html-label',
								html: 'Greater than:',
								width: gis.conf.layout.tool.item_width - gis.conf.layout.tool.itemlabel_width
							},
							greaterNumberField
						]
					},
					{
						cls: 'gis-panel-html-separator'
					},
					{
						layout: 'column',
						height: 22,
						cls: 'gis-container-inner',
						items: [
							{
								cls: 'gis-panel-html-label',
								html: 'And/or lower than:',
								width: gis.conf.layout.tool.item_width - gis.conf.layout.tool.itemlabel_width
							},
							lowerNumberField
						]
					}
				]
			},
			bbar: [
				'->',
				{
					xtype: 'button',
					text: GIS.i18n.update,
					handler: function() {
						filter();
					}
				}
			],
			listeners: {
				render: function() {
					gis.util.gui.window.setPositionTopLeft(this);
				},
				destroy: function() {
					layer.removeAllFeatures();
					layer.addFeatures(layer.core.featureStore.features);
				}
			}
		});

		return window;
	};

	GIS.app.FilterWindowFacility = function(layer) {
		var window,
			multiSelect,
			button,
			filter,
			selection,
			features = [],
			coreFeatures = layer.core.featureStore.features.slice(0),
			groupSetName = layer.core.view.organisationUnitGroupSet.name,
			store = gis.store.groupsByGroupSet;

		filter = function() {
			features = [];

			if (!selection.length || !selection[0]) {
				features = coreFeatures;
			}
			else {
				for (var i = 0; i < coreFeatures.length; i++) {
					for (var j = 0; j < selection.length; j++) {
						if (coreFeatures[i].attributes[groupSetName] === selection[j]) {
							features.push(coreFeatures[i]);
						}
					}
				}
			}

			layer.removeAllFeatures();
			layer.addFeatures(features);
		};

		multiSelect = Ext.create('Ext.ux.form.MultiSelect', {
			hideLabel: true,
			dataFields: ['id', 'name'],
			valueField: 'name',
			displayField: 'name',
			width: 200,
			height: 300,
			store: store
		});

		button = Ext.create('Ext.button.Button', {
			text: 'Filter',
			handler: function() {
				selection = multiSelect.getValue();
				filter();
			}
		});

		window = Ext.create('Ext.window.Window', {
			title: 'Filter by value',
			iconCls: 'gis-window-title-icon-filter',
			cls: 'gis-container-default',
			resizable: false,
			filter: filter,
			items: multiSelect,
			bbar: [
				'->',
				button
			],
			listeners: {
				render: function() {
					gis.util.gui.window.setPositionTopLeft(this);
				},
				destroy: function() {
					layer.removeAllFeatures();
					layer.addFeatures(coreFeatures);
				}
			}
		});

		return window;
	};

	GIS.app.LabelWindow = function(layer) {
		var fontSize,
			strong,
			italic,
			color,
			getValues,
			updateLabels,
			window;

		fontSize = Ext.create('Ext.form.field.Number', {
			width: gis.conf.layout.tool.item_width - gis.conf.layout.tool.itemlabel_width,
			allowDecimals: false,
			minValue: 8,
			value: 13,
			emptyText: 13,
			listeners: {
				change: function() {
					updateLabels();
				}
			}
		});

		strong = Ext.create('Ext.form.field.Checkbox', {
			listeners: {
				change: function() {
					updateLabels();
				}
			}
		});

		italic = Ext.create('Ext.form.field.Checkbox', {
			listeners: {
				change: function() {
					updateLabels();
				}
			}
		});

		button = Ext.create('Ext.ux.button.ColorButton', {
			width: gis.conf.layout.tool.item_width - gis.conf.layout.tool.itemlabel_width,
			value: '0000ff'
		});

		color = Ext.create('Ext.ux.button.ColorButton', {
			width: gis.conf.layout.tool.item_width - gis.conf.layout.tool.itemlabel_width,
			value: '000000',
			menuHandler: function() {
				updateLabels();
			}
		});

		getLabelConfig = function(isLabel) {
			var style = {
				fontSize: fontSize.getValue(),
				strong: strong.getValue(),
				italic: italic.getValue(),
				color: color.getValue()
            };

            if (isLabel) {
                style.label = '\${label}';
                style.fontFamily = 'arial,sans-serif,ubuntu,consolas';
			}

            return style;
		};

		updateLabels = function() {
            var loader = layer.core.getLoader();
            loader.hideMask = true;

            if (layer.hasLabels) {
                layer.hasLabels = false;

                if (layer.id === 'boundary') {
                    layer.core.setFeatureLabelStyle(false);
                }
                else {
                    layer.styleMap = GIS.core.StyleMap(layer.id);
                    loader.loadLegend();
                }
            }
            else {
                layer.hasLabels = true;

                if (layer.id === 'boundary') {
                    layer.core.setFeatureLabelStyle(true);
                }
                else {
                    layer.styleMap = GIS.core.StyleMap(layer.id, getLabelConfig(true));
                    loader.loadLegend();
                }
            }
		};

		window = Ext.create('Ext.window.Window', {
			title: GIS.i18n.labels,
			iconCls: 'gis-window-title-icon-labels',
			cls: 'gis-container-default',
			width: gis.conf.layout.tool.window_width,
			resizable: false,
			closeAction: 'hide',
            updateLabels: updateLabels,
			items: {
				layout: 'fit',
				cls: 'gis-container-inner',
				items: [
					//{
						//layout: 'column',
						//cls: 'gis-container-inner',
						//items: [
							//{
								//cls: 'gis-panel-html-label',
								//html: GIS.i18n.font_size,
								//width: gis.conf.layout.tool.itemlabel_width
							//},
							//fontSize
						//]
					//},
					{
						layout: 'column',
						cls: 'gis-container-inner',
						items: [
							{
								cls: 'gis-panel-html-label',
								html: '<b>' + GIS.i18n.bold_ + '</b>:',
								width: gis.conf.layout.tool.itemlabel_width
							},
							strong
						]
					},
					{
						layout: 'column',
						cls: 'gis-container-inner',
						items: [
							{
								cls: 'gis-panel-html-label',
								html: '<i>' + GIS.i18n.italic + '</i>:',
								width: gis.conf.layout.tool.itemlabel_width
							},
							italic
						]
					},
					{
						layout: 'column',
						cls: 'gis-container-inner',
						items: [
							{
								cls: 'gis-panel-html-label',
								html: GIS.i18n.color + ':',
								width: gis.conf.layout.tool.itemlabel_width
							},
							color
						]
					}
				]
			},
			bbar: [
				'->',
				{
					xtype: 'button',
					text: GIS.i18n.showhide,
					handler: function() {
                        updateLabels();
					}
				}
			],
			listeners: {
				render: function() {
					gis.util.gui.window.setPositionTopLeft(this);
				}
			}
		});

		return window;
	};

	GIS.app.SharingWindow = function(sharing) {

		// Objects
		var UserGroupRow,

		// Functions
			getBody,

		// Components
			userGroupStore,
			userGroupField,
			userGroupButton,
			userGroupRowContainer,
			externalAccess,
			publicGroup,
			window;

		UserGroupRow = function(obj, isPublicAccess, disallowPublicAccess) {
			var getData,
				store,
				getItems,
				combo,
				getAccess,
				panel;

			getData = function() {
				var data = [
					{id: 'r-------', name: GIS.i18n.can_view},
					{id: 'rw------', name: GIS.i18n.can_edit_and_view}
				];

				if (isPublicAccess) {
					data.unshift({id: '-------', name: GIS.i18n.none});
				}

				return data;
			}

			store = Ext.create('Ext.data.Store', {
				fields: ['id', 'name'],
				data: getData()
			});

			getItems = function() {
				var items = [];

				combo = Ext.create('Ext.form.field.ComboBox', {
					fieldLabel: isPublicAccess ? GIS.i18n.public_access : obj.name,
					labelStyle: 'color:#333',
					cls: 'gis-combo',
					width: 380,
					labelWidth: 250,
					queryMode: 'local',
					valueField: 'id',
					displayField: 'name',
					labelSeparator: null,
					editable: false,
					disabled: !!disallowPublicAccess,
					value: obj.access || 'rw------',
					store: store
				});

				items.push(combo);

				if (!isPublicAccess) {
					items.push(Ext.create('Ext.Img', {
						src: 'images/grid-delete_16.png',
						style: 'margin-top:2px; margin-left:7px',
						overCls: 'pointer',
						width: 16,
						height: 16,
						listeners: {
							render: function(i) {
								i.getEl().on('click', function(e) {
									i.up('panel').destroy();
									window.doLayout();
								});
							}
						}
					}));
				}

				return items;
			};

			getAccess = function() {
				return {
					id: obj.id,
					name: obj.name,
					access: combo.getValue()
				};
			};

			panel = Ext.create('Ext.panel.Panel', {
				layout: 'column',
				bodyStyle: 'border:0 none',
				getAccess: getAccess,
				items: getItems()
			});

			return panel;
		};

		getBody = function() {
			var body = {
				object: {
					id: sharing.object.id,
					name: sharing.object.name,
					publicAccess: publicGroup.down('combobox').getValue(),
					externalAccess: externalAccess ? externalAccess.getValue() : false,
					user: {
						id: gis.init.user.id,
						name: gis.init.user.name
					}
				}
			};

			if (userGroupRowContainer.items.items.length > 1) {
				body.object.userGroupAccesses = [];
				for (var i = 1, item; i < userGroupRowContainer.items.items.length; i++) {
					item = userGroupRowContainer.items.items[i];
					body.object.userGroupAccesses.push(item.getAccess());
				}
			}

			return body;
		};

		// Initialize
		userGroupStore = Ext.create('Ext.data.Store', {
			fields: ['id', 'name'],
			proxy: {
				type: 'ajax',
				url: gis.init.contextPath + gis.conf.finals.url.path_api + 'sharing/search',
				reader: {
					type: 'json',
					root: 'userGroups'
				}
			}
		});

		userGroupField = Ext.create('Ext.form.field.ComboBox', {
			valueField: 'id',
			displayField: 'name',
			emptyText: GIS.i18n.search_for_user_groups,
			queryParam: 'key',
			queryDelay: 200,
			minChars: 1,
			hideTrigger: true,
			fieldStyle: 'height:26px; padding-left:6px; border-radius:1px; font-size:11px',
			style: 'margin-bottom:5px',
			width: 380,
			store: userGroupStore,
			listeners: {
				beforeselect: function(cb) { // beforeselect instead of select, fires regardless of currently selected item
					userGroupButton.enable();
				},
				afterrender: function(cb) {
					cb.inputEl.on('keyup', function() {
						userGroupButton.disable();
					});
				}
			}
		});

		userGroupButton = Ext.create('Ext.button.Button', {
			text: '+',
			style: 'margin-left:2px; padding-right:4px; padding-left:4px; border-radius:1px',
			disabled: true,
			height: 26,
			handler: function(b) {
				userGroupRowContainer.add(UserGroupRow({
					id: userGroupField.getValue(),
					name: userGroupField.getRawValue(),
					access: 'r-------'
				}));

				userGroupField.clearValue();
				b.disable();
			}
		});

		userGroupRowContainer = Ext.create('Ext.container.Container', {
			bodyStyle: 'border:0 none'
		});

		if (sharing.meta.allowExternalAccess) {
			externalAccess = userGroupRowContainer.add({
				xtype: 'checkbox',
				fieldLabel: GIS.i18n.allow_external_access,
				labelSeparator: '',
				labelWidth: 250,
				checked: !!sharing.object.externalAccess
			});
		}

		publicGroup = userGroupRowContainer.add(UserGroupRow({
			id: sharing.object.id,
			name: sharing.object.name,
			access: sharing.object.publicAccess
		}, true, !sharing.meta.allowPublicAccess));

		if (Ext.isArray(sharing.object.userGroupAccesses)) {
			for (var i = 0, userGroupRow; i < sharing.object.userGroupAccesses.length; i++) {
				userGroupRow = UserGroupRow(sharing.object.userGroupAccesses[i]);
				userGroupRowContainer.add(userGroupRow);
			}
		}

		window = Ext.create('Ext.window.Window', {
			title: 'Sharing settings',
			bodyStyle: 'padding:6px 6px 0; background-color:#fff',
			resizable: false,
			modal: true,
			destroyOnBlur: true,
			items: [
				{
					html: sharing.object.name,
					bodyStyle: 'border:0 none; font-weight:bold; color:#333',
					style: 'margin-bottom:8px'
				},
				{
					xtype: 'container',
					layout: 'column',
					bodyStyle: 'border:0 none',
					items: [
						userGroupField,
						userGroupButton
					]
				},
				userGroupRowContainer
			],
			bbar: [
				'->',
				{
					text: 'Save',
					handler: function() {
						Ext.Ajax.request({
							url: gis.init.contextPath + gis.conf.finals.url.path_api + 'sharing?type=map&id=' + sharing.object.id,
							method: 'POST',
							headers: {
								'Content-Type': 'application/json'
							},
							params: Ext.encode(getBody())
						});

						window.destroy();
					}
				}
			],
			listeners: {
				show: function(w) {
					var pos = gis.viewport.favoriteWindow.getPosition();
					w.setPosition(pos[0] + 5, pos[1] + 5);
				}
			}
		});

		return window;
	};

    GIS.app.MapControlPanel = function(name, fn) {
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

		return panel;
	};

	GIS.app.FavoriteWindow = function() {

		// Objects
		var NameWindow,

		// Instances
			nameWindow,

		// Components
			addButton,
			searchTextfield,
			grid,
			prevButton,
			nextButton,
			tbar,
			bbar,
			info,
			nameTextfield,
			createButton,
			updateButton,
			cancelButton,
			favoriteWindow,

		// Vars
			windowWidth = 500,
			windowCmpWidth = windowWidth - 14,

			dimConf = gis.conf.finals.dimension;

		gis.store.maps.on('load', function(store, records) {
			var pager = store.proxy.reader.jsonData.pager;

			info.setText('Page ' + pager.page + ' of ' + pager.pageCount);

			prevButton.enable();
			nextButton.enable();

			if (pager.page === 1) {
				prevButton.disable();
			}

			if (pager.page === pager.pageCount) {
				nextButton.disable();
			}
		});

		NameWindow = function(id) {
			var window,
				record = gis.store.maps.getById(id);

			nameTextfield = Ext.create('Ext.form.field.Text', {
				height: 26,
				width: 371,
				fieldStyle: 'padding-left: 5px; border-radius: 1px; border-color: #bbb; font-size:11px',
				style: 'margin-bottom:0',
				emptyText: 'Favorite name',
				value: id ? record.data.name : '',
				listeners: {
					afterrender: function() {
						this.focus();
					}
				}
			});

			createButton = Ext.create('Ext.button.Button', {
				text: GIS.i18n.create,
				handler: function() {
					var name = nameTextfield.getValue(),
						layers = gis.util.map.getRenderedVectorLayers(),
						layer,
						lonlat = gis.olmap.getCenter(),
						views = [],
						view,
						map;

					if (!layers.length) {
						alert('Please create a map first');
						return;
					}

					if (!name) {
						alert('Please enter a name');
						return;
					}

					for (var i = 0; i < layers.length; i++) {
						layer = layers[i];

						view = Ext.clone(layer.core.view);

                        view.hidden = !layer.visibility;

						// Operand
						if (Ext.isArray(view.columns) && view.columns.length) {
							for (var j = 0; j < view.columns.length; j++) {
								for (var k = 0, item; k < view.columns[j].items.length; k++) {
									item = view.columns[j].items[k];

									if (item.id.indexOf('#') !== -1) {
										item.id = item.id.replace('#', '.');
									}
								}
							}
						}

						// add
						view.layer = layer.id;

						views.push(view);
					}

					map = {
						name: name,
						longitude: lonlat.lon,
						latitude: lonlat.lat,
						zoom: gis.olmap.getZoom(),
						mapViews: views,
						user: {
							id: 'currentUser'
						}
					};

					Ext.Ajax.request({
						url: gis.init.contextPath + gis.conf.finals.url.path_api + 'maps/',
						method: 'POST',
						headers: {'Content-Type': 'application/json'},
						params: Ext.encode(map),
						success: function(r) {
							var id = r.getAllResponseHeaders().location.split('/').pop();

                            gis.map = {
                                id: id,
                                name: name
                            };

							gis.store.maps.loadStore();

							window.destroy();
						}
					});
				}
			});

			updateButton = Ext.create('Ext.button.Button', {
				text: GIS.i18n.update,
				handler: function() {
					var name = nameTextfield.getValue(),
                        map;

                    Ext.Ajax.request({
                        url: gis.init.contextPath + '/api/maps/' + id + '.json?fields=' + gis.conf.url.mapFields.join(','),
                        success: function(r) {
                            map = Ext.decode(r.responseText);

                            map.name = name;

                            Ext.Ajax.request({
                                url: gis.init.contextPath + '/api/maps/' + id,
                                method: 'PUT',
                                headers: {'Content-Type': 'application/json'},
                                params: Ext.encode(map),
                                success: function() {
                                    gis.store.maps.loadStore();

                                    window.destroy();
                                }
                            });
                        }
                    });
				}
			});

			cancelButton = Ext.create('Ext.button.Button', {
				text: GIS.i18n.cancel,
				handler: function() {
					window.destroy();
				}
			});

			window = Ext.create('Ext.window.Window', {
				title: id ? 'Rename favorite' : 'Create new favorite',
				iconCls: 'gis-window-title-icon-favorite',
				bodyStyle: 'padding:1px; background:#fff',
				resizable: false,
				modal: true,
				items: nameTextfield,
				bbar: [
					cancelButton,
					'->',
					id ? updateButton : createButton
				],
				listeners: {
					show: function() {
						this.setPosition(favoriteWindow.x + 14, favoriteWindow.y + 67);

						nameTextfield.focus(false, 500);
					}
				}
			});

			return window;
		};

		addButton = Ext.create('Ext.button.Button', {
			text: GIS.i18n.add_new,
			width: 67,
			height: 26,
			style: 'border-radius: 1px;',
			menu: {},
			handler: function() {
				nameWindow = new NameWindow(null, 'create');
				nameWindow.show();
			}
		});

		searchTextfield = Ext.create('Ext.form.field.Text', {
			width: windowCmpWidth - addButton.width - 3,
			height: 26,
			fieldStyle: 'padding-right: 0; padding-left: 4px; border-radius: 1px; border-color: #bbb; font-size:11px',
			emptyText: GIS.i18n.search_for_favorites,
			enableKeyEvents: true,
			currentValue: '',
			listeners: {
				keyup: {
					fn: function() {
						if (this.getValue() !== this.currentValue) {
							this.currentValue = this.getValue();

							var value = this.getValue(),
								url = value ? gis.init.contextPath + '/api/maps.json?fields=id,name,access' + (value ? '&filter=name:like:' + value : '') : null;
								store = gis.store.maps;

							store.page = 1;
							store.loadStore(url);
						}
					},
					buffer: 100
				}
			}
		});

		prevButton = Ext.create('Ext.button.Button', {
			text: GIS.i18n.prev,
			handler: function() {
				var value = searchTextfield.getValue(),
					url = value ? gis.init.contextPath + '/api/maps.json?fields=id,name,access' + (value ? '&filter=name:like:' + value : '') : null;
					store = gis.store.maps;

				store.page = store.page <= 1 ? 1 : store.page - 1;
				store.loadStore(url);
			}
		});

		nextButton = Ext.create('Ext.button.Button', {
			text: GIS.i18n.next,
			handler: function() {
				var value = searchTextfield.getValue(),
					url = value ? gis.init.contextPath + '/api/maps.json?fields=id,name,access' + (value ? '&filter=name:like:' + value : '') : null;
					store = gis.store.maps;

				store.page = store.page + 1;
				store.loadStore(url);
			}
		});

		info = Ext.create('Ext.form.Label', {
			cls: 'gis-label-info',
			width: 300,
			height: 22
		});

		grid = Ext.create('Ext.grid.Panel', {
			cls: 'gis-grid',
			scroll: false,
			hideHeaders: true,
			columns: [
				{
					dataIndex: 'name',
					sortable: false,
					width: windowCmpWidth - 88,
					renderer: function(value, metaData, record) {
						var fn = function() {
							var element = Ext.get(record.data.id);

							if (element) {
								element = element.parent('td');
								element.addClsOnOver('link');
								element.load = function() {
									favoriteWindow.hide();
									gis.map = {id: record.data.id};
									GIS.core.MapLoader(gis).load();
								};
								element.dom.setAttribute('onclick', 'Ext.get(this).load();');
							}
						};

						Ext.defer(fn, 100);

						return '<div id="' + record.data.id + '" class="el-fontsize-10">' + value + '</div>';
					}
				},
				{
					xtype: 'actioncolumn',
					sortable: false,
					width: 80,
					items: [
						{
							iconCls: 'gis-grid-row-icon-edit',
							getClass: function(value, metaData, record) {
								return 'tooltip-favorite-edit' + (!record.data.access.update ? ' disabled' : '');
							},
							handler: function(grid, rowIndex, colIndex, col, event) {
								var record = this.up('grid').store.getAt(rowIndex);

								if (record.data.access.update) {
									nameWindow = new NameWindow(record.data.id);
									nameWindow.show();
								}
							}
						},
						{
							iconCls: 'gis-grid-row-icon-overwrite',
							getClass: function(value, metaData, record) {
								return 'tooltip-favorite-overwrite' + (!record.data.access.update ? ' disabled' : '');
							},
							handler: function(grid, rowIndex, colIndex, col, event) {
								var record = this.up('grid').store.getAt(rowIndex),
									layers,
									layer,
									lonlat,
									views,
									view,
									map,
									message;

								if (record.data.access.update) {
									layers = gis.util.map.getRenderedVectorLayers();
									message = 'Overwrite favorite?\n\n' + record.data.name;

									if (layers.length) {
										if (confirm(message)) {
											lonlat = gis.olmap.getCenter();
											views = [];

											for (var i = 0; i < layers.length; i++) {
												layer = layers[i];
												view = layer.core.view;

												// add
												view.layer = layer.id;
                                                view.hidden = !layer.visibility;

												// remove
												delete view.periodType;

												views.push(view);
											}

											map = {
												longitude: lonlat.lon,
												latitude: lonlat.lat,
												zoom: gis.olmap.getZoom(),
												mapViews: views
											};

											Ext.Ajax.request({
												url: gis.init.contextPath + gis.conf.finals.url.path_api + 'maps/' + record.data.id,
												method: 'PUT',
												headers: {'Content-Type': 'application/json'},
												params: Ext.encode(map),
												success: function() {
													gis.map = map;
													gis.store.maps.loadStore();
												}
											});
										}
									}
									else {
										alert(GIS.i18n.no_map_to_save);
									}
								}
							}
						},
						{
							iconCls: 'gis-grid-row-icon-sharing',
							getClass: function(value, metaData, record) {
								return 'tooltip-favorite-sharing' + (!record.data.access.manage ? ' disabled' : '');
							},
							handler: function(grid, rowIndex) {
								var record = this.up('grid').store.getAt(rowIndex);

								if (record.data.access.manage) {
									Ext.Ajax.request({
										url: gis.init.contextPath + '/api/sharing?type=map&id=' + record.data.id,
										method: 'GET',
										failure: function(r) {
                                            gis.olmap.mask.hide();
											alert(r.responseText);
										},
										success: function(r) {
											var sharing = Ext.decode(r.responseText),
												window = GIS.app.SharingWindow(sharing);
											window.show();
										}
									});
								}
							}
						},
						{
							iconCls: 'gis-grid-row-icon-delete',
							getClass: function(value, metaData, record) {
								return 'tooltip-favorite-delete' + (!record.data.access['delete'] ? ' disabled' : '');
							},
							handler: function(grid, rowIndex, colIndex, col, event) {
								var record = this.up('grid').store.getAt(rowIndex),
									message;

								if (record.data.access['delete']) {
									message = 'Delete favorite?\n\n' + record.data.name;

									if (confirm(message)) {
										Ext.Ajax.request({
											url: gis.init.contextPath + '/api/maps/' + record.data.id,
											method: 'DELETE',
											success: function() {
												gis.store.maps.loadStore();
											}
										});
									}
								}
							}
						}
					]
				},
				{
					sortable: false,
					width: 6
				}
			],
			store: gis.store.maps,
			bbar: [
				info,
				'->',
				prevButton,
				nextButton
			],
			listeners: {
				added: function() {
					gis.viewport.mapGrid = this;
				},
				render: function() {
					var size = Math.floor((gis.viewport.centerRegion.getHeight() - 155) / gis.conf.layout.grid.row_height);
					this.store.pageSize = size;
					this.store.page = 1;
					this.store.loadStore();

					gis.store.maps.on('load', function() {
						if (this.isVisible()) {
							this.fireEvent('afterrender');
						}
					}, this);
				},
				afterrender: function() {
					var fn = function() {
						var editArray = Ext.query('.tooltip-favorite-edit'),
							overwriteArray = Ext.query('.tooltip-favorite-overwrite'),
							sharingArray = Ext.query('.tooltip-favorite-sharing'),
							dashboardArray = Ext.query('.tooltip-favorite-dashboard'),
							deleteArray = Ext.query('.tooltip-favorite-delete'),
							el;

						for (var i = 0; i < editArray.length; i++) {
							var el = editArray[i];
							Ext.create('Ext.tip.ToolTip', {
								target: el,
								html: GIS.i18n.rename,
								'anchor': 'bottom',
								anchorOffset: -14,
								showDelay: 1000
							});
						}

						for (var i = 0; i < overwriteArray.length; i++) {
							el = overwriteArray[i];
							Ext.create('Ext.tip.ToolTip', {
								target: el,
								html: GIS.i18n.overwrite,
								'anchor': 'bottom',
								anchorOffset: -14,
								showDelay: 1000
							});
						}

						for (var i = 0; i < sharingArray.length; i++) {
							el = sharingArray[i];
							Ext.create('Ext.tip.ToolTip', {
								target: el,
								html: GIS.i18n.share_with_other_people,
								'anchor': 'bottom',
								anchorOffset: -14,
								showDelay: 1000
							});
						}

						for (var i = 0; i < dashboardArray.length; i++) {
							el = dashboardArray[i];
							Ext.create('Ext.tip.ToolTip', {
								target: el,
								html: GIS.i18n.add_to_dashboard,
								'anchor': 'bottom',
								anchorOffset: -14,
								showDelay: 1000
							});
						}

						for (var i = 0; i < deleteArray.length; i++) {
							el = deleteArray[i];
							Ext.create('Ext.tip.ToolTip', {
								target: el,
								html: GIS.i18n.delete_,
								'anchor': 'bottom',
								anchorOffset: -14,
								showDelay: 1000
							});
						}
					};

					Ext.defer(fn, 100);
				},
				itemmouseenter: function(grid, record, item) {
					this.currentItem = Ext.get(item);
					this.currentItem.removeCls('x-grid-row-over');
				},
				select: function() {
					this.currentItem.removeCls('x-grid-row-selected');
				},
				selectionchange: function() {
					this.currentItem.removeCls('x-grid-row-focused');
				}
			}
		});

		favoriteWindow = Ext.create('Ext.window.Window', {
			title: 'Manage favorites',
			iconCls: 'gis-window-title-icon-favorite',
			cls: 'gis-container-default',
			bodyStyle: 'padding:1px',
			resizable: false,
			modal: true,
			width: windowWidth,
			items: [
				{
					xtype: 'panel',
					layout: 'hbox',
					cls: 'gis-container-inner',
					height: 27,
					items: [
						addButton,
						{
							height: 26,
							width: 1,
							style: 'width:1px; margin-left:1px; margin-right:1px',
							bodyStyle: 'border-left: 1px solid #aaa'
						},
						searchTextfield
					]
				},
				grid
			],
			listeners: {
				show: function() {
					this.setPosition(199, 33);

					searchTextfield.focus(false, 500);
				}
			}
		});

		return favoriteWindow;
	};

	GIS.app.LegendSetWindow = function() {

		// Stores
		var legendSetStore,
			legendStore,
			tmpLegendStore,

		// Objects
			LegendSetPanel,
			LegendPanel,

		// Instances
			legendSetPanel,
			legendPanel,

		// Components
			window,
			legendSetName,
			legendName,
			startValue,
			endValue,
			color,
			legendGrid,
			create,
			update,
			cancel,
			info,

		// Functions
			showUpdateLegendSet,
			deleteLegendSet,
			deleteLegend,
			getRequestBody,
			reset,
			validateLegends;

		legendSetStore = Ext.create('Ext.data.Store', {
			fields: ['id', 'name'],
			proxy: {
				type: 'ajax',
				url: gis.init.contextPath + gis.conf.finals.url.path_api + 'mapLegendSets.json?fields=id,name&paging=false',
				reader: {
					type: 'json',
					root: 'mapLegendSets'
				},
				pageParam: false,
				startParam: false,
				limitParam: false
			},
			listeners: {
				load: function(store, records) {
					this.sort('name', 'ASC');

					info.setText(records.length + ' legend set' + (records.length !== 1 ? 's' : '') + ' available');
				}
			}
		});

		legendStore = Ext.create('Ext.data.Store', {
			fields: ['id', 'name', 'startValue', 'endValue', 'color'],
			proxy: {
				type: 'ajax',
				url: '',
				reader: {
					type: 'json',
					root: 'mapLegends'
				}
			},
			deleteLegend: deleteLegend,
			listeners: {
				load: function(store, records) {
					var data = [],
						record;

					for (var i = 0; i < records.length; i++) {
						data.push(records[i].data);
					}

					Ext.Array.sort(data, function (a, b) {
						return a.startValue - b.startValue;
					});

					tmpLegendStore.add(data);

					info.setText(records.length + ' legend sets available');
				}
			}
		});

		LegendSetPanel = function() {
			var items,
				addButton;

			addButton = Ext.create('Ext.button.Button', {
				text: GIS.i18n.add_new,
				height: 26,
				style: 'border-radius: 1px',
				menu: {},
				handler: function() {
					showUpdateLegendSet();
				}
			});

			legendSetGrid = Ext.create('Ext.grid.Panel', {
				cls: 'gis-grid',
				scroll: 'vertical',
				height: true,
				hideHeaders: true,
				currentItem: null,
				columns: [
					{
						dataIndex: 'name',
						sortable: false,
						width: 369
					},
					{
						xtype: 'actioncolumn',
						sortable: false,
						width: 40,
						items: [
							{
								iconCls: 'gis-grid-row-icon-edit',
								getClass: function() {
									return 'tooltip-legendset-edit';
								},
								handler: function(grid, rowIndex, colIndex, col, event) {
									var id = this.up('grid').store.getAt(rowIndex).data.id;
									showUpdateLegendSet(id);
								}
							},
							{
								iconCls: 'gis-grid-row-icon-delete',
								getClass: function() {
									return 'tooltip-legendset-delete';
								},
								handler: function(grid, rowIndex, colIndex, col, event) {
									var record = this.up('grid').store.getAt(rowIndex),
										id = record.data.id,
										name = record.data.name,
										message = 'Delete legend set?\n\n' + name;

									if (confirm(message)) {
										deleteLegendSet(id);
									}
								}
							}
						]
					},
					{
						sortable: false,
						width: 17
					}
				],
				store: legendSetStore,
				listeners: {
					render: function() {
						var that = this,
							maxHeight = gis.viewport.centerRegion.getHeight() - 155,
							height;

						this.store.on('load', function() {
							if (Ext.isDefined(that.setHeight)) {
								height = 1 + that.store.getCount() * gis.conf.layout.grid.row_height;
								that.setHeight(height > maxHeight ? maxHeight : height);
								window.doLayout();
							}
						});

						this.store.load();
					},
					afterrender: function() {
						var fn = function() {
							var editArray = document.getElementsByClassName('tooltip-legendset-edit'),
								deleteArray = document.getElementsByClassName('tooltip-legendset-delete'),
								len = editArray.length,
								el;

							for (var i = 0; i < len; i++) {
								el = editArray[i];
								Ext.create('Ext.tip.ToolTip', {
									target: el,
									html: 'Edit',
									'anchor': 'bottom',
									anchorOffset: -14,
									showDelay: 1000
								});

								el = deleteArray[i];
								Ext.create('Ext.tip.ToolTip', {
									target: el,
									html: 'Delete',
									'anchor': 'bottom',
									anchorOffset: -14,
									showDelay: 1000
								});
							}
						};

						Ext.defer(fn, 100);
					},
					itemmouseenter: function(grid, record, item) {
						this.currentItem = Ext.get(item);
						this.currentItem.removeCls('x-grid-row-over');
					},
					select: function() {
						this.currentItem.removeCls('x-grid-row-selected');
					},
					selectionchange: function() {
						this.currentItem.removeCls('x-grid-row-focused');
					}
				}
			});

			items = [
				{
					xtype: 'panel',
					layout: 'hbox',
					cls: 'gis-container-inner',
					style: 'margin-bottom: 1px',
					items: [
						addButton
					]
				},
				legendSetGrid
			];

			return items;
		};

		LegendPanel = function(id) {
			var panel,
				addLegend,
				reset,
				data = [];

			tmpLegendStore = Ext.create('Ext.data.ArrayStore', {
				fields: ['id', 'name', 'startValue', 'endValue', 'color']
			});

			legendSetName = Ext.create('Ext.form.field.Text', {
				cls: 'gis-textfield',
				width: 428,
				height: 25,
				fieldStyle: 'padding-left: 6px; border-color: #bbb',
				fieldLabel: GIS.i18n.legend_set_name
			});

			legendName = Ext.create('Ext.form.field.Text', {
				cls: 'gis-textfield',
				fieldStyle: 'padding-left: 6px',
				width: 415,
				height: 23,
				fieldLabel: GIS.i18n.legend_name
			});

			startValue = Ext.create('Ext.form.field.Number', {
				width: 153,
				height: 23,
				allowDecimals: true,
				fieldStyle: 'padding-left: 6px; border-radius: 1px',
				value: 0
			});

			endValue = Ext.create('Ext.form.field.Number', {
				width: 154,
				height: 23,
				allowDecimals: true,
				fieldStyle: 'padding-left: 6px; border-radius: 1px',
				value: 0,
				style: 'padding-left: 3px'
			});

			color = Ext.create('Ext.ux.button.ColorButton', {
				width: 310,
				height: 23,
				fieldLabel: GIS.i18n.legend_symbolizer,
				style: 'border-radius: 1px',
				value: 'e1e1e1'
			});

			addLegend = Ext.create('Ext.button.Button', {
				text: GIS.i18n.add_legend,
				height: 26,
				style: 'border-radius: 1px',
				handler: function() {
					var date = new Date(),
						id = date.toISOString(),
						ln = legendName.getValue(),
						sv = startValue.getValue(),
						ev = endValue.getValue(),
						co = color.getValue().toUpperCase(),
						items = tmpLegendStore.data.items,
						data = [];

					if (ln && (ev > sv)) {
						for (var i = 0; i < items.length; i++) {
							data.push(items[i].data);
						}

						data.push({
							id: id,
							name: ln,
							startValue: sv,
							endValue: ev,
							color: '#' + co
						});

						Ext.Array.sort(data, function (a, b) {
							return a.startValue - b.startValue;
						});

						tmpLegendStore.removeAll();
						tmpLegendStore.add(data);

						legendName.reset();
						startValue.reset();
						endValue.reset();
						color.reset();
					}
				}
			});

			legendGrid = Ext.create('Ext.grid.Panel', {
				cls: 'gis-grid',
				bodyStyle: 'border-top: 0 none',
				width: 428,
				height: 235,
				scroll: 'vertical',
				hideHeaders: true,
				currentItem: null,
				columns: [
					{
						dataIndex: 'name',
						sortable: false,
						width: 256
					},
					{
						sortable: false,
						width: 45,
						renderer: function(value, metaData, record) {
							return '<span style="color:' + record.data.color + '">Color</span>';
						}
					},
					{
						dataIndex: 'startValue',
						sortable: false,
						width: 45
					},
					{
						dataIndex: 'endValue',
						sortable: false,
						width: 45
					},
					{
						xtype: 'actioncolumn',
						sortable: false,
						width: 20,
						items: [
							{
								iconCls: 'gis-grid-row-icon-delete',
								getClass: function() {
									return 'tooltip-legend-delete';
								},
								handler: function(grid, rowIndex, colIndex, col, event) {
									var id = this.up('grid').store.getAt(rowIndex).data.id;
									deleteLegend(id);
								}
							}
						]
					},
					{
						sortable: false,
						width: 17
					}
				],
				store: tmpLegendStore,
				listeners: {
					itemmouseenter: function(grid, record, item) {
						this.currentItem = Ext.get(item);
						this.currentItem.removeCls('x-grid-row-over');
					},
					select: function() {
						this.currentItem.removeCls('x-grid-row-selected');
					},
					selectionchange: function() {
						this.currentItem.removeCls('x-grid-row-focused');
					},
					afterrender: function() {
						var fn = function() {
							var deleteArray = document.getElementsByClassName('tooltip-legend-delete'),
								len = deleteArray.length,
								el;

							for (var i = 0; i < len; i++) {
								el = deleteArray[i];
								Ext.create('Ext.tip.ToolTip', {
									target: el,
									html: 'Delete',
									'anchor': 'bottom',
									anchorOffset: -14,
									showDelay: 1000
								});
							}
						};

						Ext.defer(fn, 100);
					}
				}
			});

			panel = Ext.create('Ext.panel.Panel', {
				cls: 'gis-container-inner',
				legendSetId: id,
				bodyStyle: 'padding:3px',
				items: [
					legendSetName,
					{
						cls: 'gis-panel-html-separator'
					},
					{
						html: GIS.i18n.add_legend,
						cls: 'gis-panel-html-title'
					},
					{
						cls: 'gis-panel-html-separator'
					},
					{
						bodyStyle: 'background-color:#f1f1f1; border:1px solid #ccc; border-radius:1px; padding:5px',
						items: [
							legendName,
							{
								layout: 'hbox',
								bodyStyle: 'background: transparent',
								items: [
									{
										html: GIS.i18n.start_end_value + ':',
										width: 105,
										bodyStyle: 'background:transparent; padding-top:3px'
									},
									startValue,
									endValue
								]
							},
							{
								layout: 'column',
								cls: 'gis-container-inner',
								bodyStyle: 'background: transparent',
								items: [
									{
										cls: 'gis-panel-html-label',
										html: GIS.i18n.legend_symbolizer,
										bodyStyle: 'background: transparent',
										width: gis.conf.layout.widget.itemlabel_width + 10
									},
									color
								]
							},
						]
					},
					{
						cls: 'gis-panel-html-separator'
					},
					{
						cls: 'gis-container-inner',
						bodyStyle: 'text-align: right',
						width: 428,
						items: addLegend
					},
					{
						html: GIS.i18n.current_legends,
						cls: 'gis-panel-html-title'
					},
					{
						cls: 'gis-panel-html-separator'
					},
					legendGrid
				]
			});

			if (id) {
				legendStore.proxy.url = gis.init.contextPath + gis.conf.finals.url.path_api +  'mapLegendSets/' + id + '.json?links=false&paging=false';
				legendStore.load();

				legendSetName.setValue(legendSetStore.getById(id).data.name);
			}

			return panel;
		};

		showUpdateLegendSet = function(id) {
			legendPanel = new LegendPanel(id);
			window.removeAll();
			window.add(legendPanel);
			info.hide();
			cancel.show();

			if (id) {
				update.show();
			}
			else {
				create.show();
			}
		};

		deleteLegendSet = function(id) {
			if (id) {
				Ext.Ajax.request({
					url: gis.init.contextPath + gis.conf.finals.url.path_api + 'mapLegendSets/' + id,
					method: 'DELETE',
					success: function() {
						legendSetStore.load();
						gis.store.legendSets.load();
					}
				});
			}
		};

		deleteLegend = function(id) {
			tmpLegendStore.remove(tmpLegendStore.getById(id));
		};

		getRequestBody = function() {
			var items = tmpLegendStore.data.items,
				body;

			body = {
				name: legendSetName.getValue(),
				symbolizer: gis.conf.finals.widget.symbolizer_color,
				mapLegends: []
			};

			for (var i = 0; i < items.length; i++) {
				var item = items[i];
				body.mapLegends.push({
					name: item.data.name,
					startValue: item.data.startValue,
					endValue: item.data.endValue,
					color: item.data.color
				});
			}

			return body;
		};

		reset = function() {
			legendPanel.destroy();
			legendSetPanel = new LegendSetPanel();
			window.removeAll();
			window.add(legendSetPanel);

			info.show();
			cancel.hide();
			create.hide();
			update.hide();
		};

		validateLegends = function() {
			var items = tmpLegendStore.data.items,
				item,
				prevItem;

			if (items.length === 0) {
				alert('No legend set name');
				return false;
			}

			for (var i = 1; i < items.length; i++) {
				item = items[i].data;
				prevItem = items[i - 1].data;

				if (item.startValue < prevItem.endValue) {
					var msg = 'Overlapping legends not allowed!\n\n' +
							  prevItem.name + ' (' + prevItem.startValue + ' - ' + prevItem.endValue + ')\n' +
							  item.name + ' (' + item.startValue + ' - ' + item.endValue + ')';
					alert(msg);
					return false;
				}

				if (prevItem.endValue < item.startValue) {
					var msg = 'Legend gaps detected!\n\n' +
							  prevItem.name + ' (' + prevItem.startValue + ' - ' + prevItem.endValue + ')\n' +
							  item.name + ' (' + item.startValue + ' - ' + item.endValue + ')\n\n' +
							  'Proceed anyway?';

					if (!confirm(msg)) {
						return false;
					}
				}
			}

			return true;
		};

		create = Ext.create('Ext.button.Button', {
			text: GIS.i18n.create,
			hidden: true,
			handler: function() {
				if (legendSetName.getValue() && validateLegends()) {
					if (legendSetStore.findExact('name', legendSetName.getValue()) !== -1) {
						alert('Name already in use');
						return;
					}

					var body = Ext.encode(getRequestBody());

					Ext.Ajax.request({
						url: gis.init.contextPath + gis.conf.finals.url.path_api + 'mapLegendSets/',
						method: 'POST',
						headers: {'Content-Type': 'application/json'},
						params: body,
						success: function() {
							gis.store.legendSets.load();
							reset();
						}
					});
				}
			}
		});

		update = Ext.create('Ext.button.Button', {
			text: GIS.i18n.update,
			hidden: true,
			handler: function() {
				if (legendSetName.getValue() && validateLegends()) {
					var body = getRequestBody(),
						id = legendPanel.legendSetId;
					body.id = id;
					body = Ext.encode(getRequestBody());

					Ext.Ajax.request({
						url: gis.init.contextPath + gis.conf.finals.url.path_api + 'mapLegendSets/' + id,
						method: 'PUT',
						headers: {'Content-Type': 'application/json'},
						params: body,
						success: function() {
							reset();
						}
					});
				}
			}
		});

		cancel = Ext.create('Ext.button.Button', {
			text: GIS.i18n.cancel,
			hidden: true,
			handler: function() {
				reset();
			}
		});

		info = Ext.create('Ext.form.Label', {
			cls: 'gis-label-info',
			width: 400,
			height: 22
		});

		window = Ext.create('Ext.window.Window', {
			title: GIS.i18n.legendsets,
			iconCls: 'gis-window-title-icon-legendset', //todo
            bodyStyle: 'padding:1px; background-color:#fff',
			resizable: false,
			width: 450,
			modal: true,
			items: new LegendSetPanel(),
			bbar: {
				height: 27,
				items: [
					info,
					cancel,
					'->',
					create,
					update
				]
			},
			listeners: {
				show: function() {
					this.setPosition(269, 33);
				}
			}
		});

		return window;
	};

	GIS.app.DownloadWindow = function() {
		var window,
			format,
			name,
			button;

		format = Ext.create('Ext.form.field.ComboBox', {
			cls: 'gis-combo',
			width: 60,
			style: 'margin-bottom:0; margin-left:1px',
			valueField: 'id',
			displayField: 'text',
			editable: false,
			queryMode: 'local',
			forceSelection: true,
			value: 'png',
			store: Ext.create('Ext.data.ArrayStore', {
				fields: ['id', 'text'],
				data: [
					['png', 'PNG'],
					['pdf', 'PDF']
				]
			})
		});

		name = Ext.create('Ext.form.field.Text', {
			cls: 'gis-textfield',
			//height: 23,
			width: 230,
			fieldStyle: 'padding-left:4px',
			style: 'margin-bottom:0',
			emptyText: GIS.i18n.please_enter_map_title
		});

		button = Ext.create('Ext.button.Button', {
			text: GIS.i18n.download,
			handler: function() {
				var type = format.getValue(),
					title = name.getValue(),
					svg = gis.util.svg.getString(title, gis.util.map.getVisibleVectorLayers()),
					exportForm = document.getElementById('exportForm');

				if (!svg) {
					alert(GIS.i18n.please_create_map_first);
					return;
				}

				document.getElementById('filenameField').value = title;
				document.getElementById('svgField').value = svg;
				exportForm.action = gis.init.contextPath + '/api/svg.' + type;
				exportForm.submit();

				window.destroy();
			}
		});

		window = Ext.create('Ext.window.Window', {
			title: GIS.i18n.download_map_as_png,
			layout: 'column',
			iconCls: 'gis-window-title-icon-download',
			cls: 'gis-container-default',
            bodyStyle: 'padding:1px',
			resizable: true,
			modal: true,
			items: [
				name,
				format
			],
			bbar: [
				'->',
				button
			],
			listeners: {
				show: function() {
					this.setPosition(253, 33);
				}
			}
		});

		return window;
	};

	GIS.app.InterpretationWindow = function() {
		var window,
			textarea,
			panel,
			button;

		textarea = Ext.create('Ext.form.field.TextArea', {
			cls: 'gis-textarea',
			height: 130,
			fieldStyle: 'padding-left: 3px; padding-top: 3px',
			emptyText: GIS.i18n.write_your_interpretation + '..'
		});

		panel = Ext.create('Ext.panel.Panel', {
			cls: 'gis-container-inner',
			html: function() {
				var moduleUrl = gis.init.contextPath + '/dhis-web-mapping/app/index.html?id=' + gis.map.id,
					apiUrl = gis.init.contextPath + '/api/maps/' + gis.map.id + '/data',
					html = '';

				html += '<div><b>Map link: </b><span class="user-select"><a href="' + moduleUrl + '" target="_blank">' + moduleUrl + '</a></span></div>';
				html += '<div style="padding-top:3px"><b>API link: </b><span class="user-select"><a href="' + apiUrl + '" target="_blank">' + apiUrl + '</a></span></div>';

				return html;
			}(),
			style: 'padding:3px',
		});

		button = Ext.create('Ext.button.Button', {
			text: GIS.i18n.share,
			handler: function() {
				if (textarea.getValue() && gis.map && gis.map.id) {
					Ext.Ajax.request({
						url: gis.init.contextPath + gis.conf.finals.url.path_api + 'interpretations/map/' + gis.map.id,
						method: 'POST',
						params: textarea.getValue(),
						headers: {'Content-Type': 'text/html'},
						success: function() {
							window.destroy();
						}
					});
				}
			}
		});

		window = Ext.create('Ext.window.Window', {
			title: gis.map.name,
			layout: 'fit',
			iconCls: 'gis-window-title-icon-interpretation',
			cls: 'gis-container-default',
            bodyStyle: 'padding: 1px',
			width: 500,
			resizable: true,
			modal: true,
			items: [
				textarea,
				panel
			],
			bbar: [
				'->',
				button
			],
			listeners: {
				show: function() {
					this.setPosition(325, 33);
				},
				destroy: function() {
					document.body.oncontextmenu = function(){
						return false;
					};
				}

			}
		});

		document.body.oncontextmenu = true; // right click to copy url

		return window;
	};

	GIS.app.CircleLayer = function(features, radius) {
		var points = gis.util.map.getPointsByFeatures(features),
			lonLats = gis.util.map.getLonLatsByPoints(points),
			controls = [],
			control,
			layer = new OpenLayers.Layer.Vector(),
			deactivateControls,
			createCircles,
			params = {};

		radius = radius && Ext.isNumber(parseInt(radius)) ? parseInt(radius) : 5;

		deactivateControls = function() {
			for (var i = 0; i < controls.length; i++) {
				controls[i].deactivate();
			}
		};

		createCircles = function() {
			if (lonLats.length) {
				for (var i = 0; i < lonLats.length; i++) {
					control = new OpenLayers.Control.Circle({
						layer: layer
					});
					control.lonLat = lonLats[i];
					controls.push(control);
				}

				gis.olmap.addControls(controls);

				for (var i = 0; i < controls.length; i++) {
					control = controls[i];
					control.activate();
					control.updateCircle(control.lonLat, radius);
				}
			}
		}();

		layer.deactivateControls = deactivateControls;

		return layer;
	};

	GIS.app.LayerWidgetEvent = function(layer) {

		// stores
		var programStore,
			stagesByProgramStore,
            //dataElementsByStageStore,

        // cache
            stageStorage = {},
            attributeStorage = {},
            dataElementStorage = {},

		// components
			program,
            onProgramSelect,
			stage,
            onStageSelect,
            loadDataElements,
            dataElementAvailable,
            dataElementSelected,
            addUxFromDataElement,
            selectDataElements,
            dataElement,

            periodMode,
            onPeriodModeSelect,
            getDateLink,
			startDate,
			endDate,
            startEndDate,

            onPeriodChange,
            onCheckboxAdd,
            intervalListeners,
            relativePeriodCmpMap = {},
            weeks,
            months,
            biMonths,
            quarters,
            sixMonths,
            financialYears,
            years,
            relativePeriod,
            checkboxes = [],

            fixedPeriodAvailable,
            fixedPeriodSelected,
            onPeriodTypeSelect,
            periodType,
            prevYear,
            nextYear,
            fixedPeriodSettings,
            fixedPeriodAvailableSelected,
            periods,
			period,

			treePanel,
			userOrganisationUnit,
			userOrganisationUnitChildren,
			userOrganisationUnitGrandChildren,
			organisationUnitLevel,
			organisationUnitGroup,
            organisationUnitPanel,
			toolMenu,
			tool,
			toolPanel,
            organisationUnit,

			panel,

		// functions
			reset,
			setGui,
			getView,
			validateView,

        // constants
            baseWidth = 444,
            toolWidth = 36,

            accBaseWidth = baseWidth - 2;

		// stores

		programStore = Ext.create('Ext.data.Store', {
			fields: ['id', 'name'],
			proxy: {
				type: 'ajax',
				url: gis.init.contextPath + '/api/programs.json?fields=id,name&paging=false',
				reader: {
					type: 'json',
					root: 'programs'
				},
				pageParam: false,
				startParam: false,
				limitParam: false
			},
			sortInfo: {field: 'name', direction: 'ASC'},
			isLoaded: false,
			listeners: {
				load: function() {
					if (!this.isLoaded) {
						this.isLoaded = true;
					}
				}
			}
		});

		stagesByProgramStore = Ext.create('Ext.data.Store', {
			fields: ['id', 'name'],
			proxy: {
				type: 'ajax',
				url: '',
				reader: {
					type: 'json',
					root: 'programStages'
				}
			},
			isLoaded: false,
			loadFn: function(fn) {
				if (Ext.isFunction(fn)) {
					if (this.isLoaded) {
						fn.call();
					}
					else {
						this.load({
							callback: fn
						});
					}
				}
			},
			listeners: {
				load: function() {
					if (!this.isLoaded) {
						this.isLoaded = true;
					}
					this.sort('name', 'ASC');
				}
			}
		});

		dataElementsByStageStore = Ext.create('Ext.data.Store', {
			fields: [''],
			data: [],
			sorters: [{
				property: 'name',
				direction: 'ASC'
			}]
		});

		// components

            // data element
		program = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: GIS.i18n.programs,
			editable: false,
			valueField: 'id',
			displayField: 'name',
			fieldLabel: 'Program',
			labelAlign: 'top',
			labelCls: 'gis-form-item-label-top',
			labelSeparator: '',
			emptyText: 'Select program',
			forceSelection: true,
			queryMode: 'remote',
			columnWidth: 0.5,
			style: 'margin:1px 1px 1px 0',
			storage: {},
			store: programStore,
            getRecord: function() {
                return this.getValue ? {
                    id: this.getValue(),
                    name: this.getRawValue()
                } : null;
            },
			listeners: {
				select: function(cb) {
					onProgramSelect(cb.getValue());
				}
			}
		});

		onProgramSelect = function(programId, layout) {
            var load;

            programId = layout ? layout.program.id : programId;
			stage.clearValue();

			dataElementsByStageStore.removeAll();
			dataElementSelected.removeAll();

            load = function(stages) {
                stage.enable();
                stage.clearValue();

                stagesByProgramStore.removeAll();
                stagesByProgramStore.loadData(stages);

                //ns.app.aggregateLayoutWindow.resetData();
				//ns.app.queryLayoutWindow.resetData();

                stageId = (layout ? layout.programStage.id : null) || (stages.length === 1 ? stages[0].id : null);

                if (stageId) {
                    stage.setValue(stageId);
                    onStageSelect(stageId, layout);
                }
            };

            if (stageStorage.hasOwnProperty(programId)) {
                load(stageStorage[programId]);
            }
            else {
                Ext.Ajax.request({
                    url: gis.init.contextPath + '/api/programs.json?filter=id:eq:' + programId + '&fields=programStages[id,name],programTrackedEntityAttributes[attribute[id,name,valueType,optionSet[id,name]]]&paging=false',
                    success: function(r) {
                        var program = Ext.decode(r.responseText).programs[0],
                            stages,
                            attributes,
                            stageId;

                        if (!program) {
                            return;
                        }

                        stages = program.programStages;
                        attributes = Ext.Array.pluck(program.programTrackedEntityAttributes, 'attribute');

                        // attributes cache
                        if (Ext.isArray(attributes) && attributes.length) {
                            attributeStorage[programId] = attributes;
                        }

                        if (Ext.isArray(stages) && stages.length) {

                            // stages cache
                            stageStorage[programId] = stages;

                            load(stages);
                        }
                    }
                });
            }
		};

		stage = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: GIS.i18n.indicator,
			editable: false,
			valueField: 'id',
			displayField: 'name',
			fieldLabel: 'Stage',
			labelAlign: 'top',
			labelCls: 'gis-form-item-label-top',
			labelSeparator: '',
			emptyText: 'Select stage',
			queryMode: 'local',
			forceSelection: true,
			columnWidth: 0.5,
			style: 'margin:1px 0 1px 0',
			disabled: true,
			listConfig: {loadMask: false},
			store: stagesByProgramStore,
            getRecord: function() {
                return this.getValue() ? {
                    id: this.getValue(),
                    name: this.getRawValue()
                } : null;
            },
			listeners: {
				select: function(cb) {
					onStageSelect(cb.getValue());
				}
			}
		});

		onStageSelect = function(stageId, layout) {
            if (!layout) {
				dataElementSelected.removeAll();
			}

			loadDataElements(stageId, layout);
		};

		loadDataElements = function(stageId, layout) {
			var programId = layout ? layout.program.id : (program.getValue() || null),
                load;

            stageId = stageId || layout.programStage.id;

			load = function(dataElements) {
                var attributes = attributeStorage[programId],
                    data = Ext.Array.clean([].concat(attributes || [], dataElements || []));

				dataElementsByStageStore.loadData(dataElements);

                if (layout) {
                    var dataDimensions = gis.util.layout.getDataDimensionsFromLayout(layout),
                        records = [];

                    for (var i = 0, dim, row; i < dataDimensions.length; i++) {
                        dim = dataDimensions[i];
                        row = dataElementsByStageStore.getById(dim.dimension);

                        if (row) {
                            records.push(Ext.applyIf(dim, row.data));
                        }
                    }

                    selectDataElements(records, layout);
                }
			};

            // data elements
            if (dataElementStorage.hasOwnProperty(stageId)) {
                load(dataElementStorage[stageId]);
            }
            else {
                Ext.Ajax.request({
                    url: gis.init.contextPath + '/api/programStages.json?filter=id:eq:' + stageId + '&fields=programStageDataElements[dataElement[id,name,type,optionSet[id,name]]]',
                    success: function(r) {
                        var objects = Ext.decode(r.responseText).programStages,
                            dataElements;

                        if (!objects.length) {
                            load();
                            return;
                        }

                        dataElements = Ext.Array.pluck(objects[0].programStageDataElements, 'dataElement');

                        // data elements cache
                        dataElementStorage[stageId] = dataElements;

                        load(dataElements);
                    }
                });
            }
		};

		dataElementAvailable = Ext.create('Ext.ux.form.MultiSelect', {
			cls: 'ns-toolbar-multiselect-left',
			width: accBaseWidth,
            height: 118,
			valueField: 'id',
			displayField: 'name',
            style: 'margin-bottom:1px',
			store: dataElementsByStageStore,
			tbar: [
				{
					xtype: 'label',
                    text: 'Available data items',
					cls: 'ns-toolbar-multiselect-left-label'
				},
				'->',
				{
					xtype: 'button',
					icon: 'images/arrowdown.png',
					width: 22,
					height: 22,
					handler: function() {
                        if (dataElementAvailable.getValue().length) {
                            selectDataElements(dataElementAvailable.getValue());
                        }
					}
				},
				{
					xtype: 'button',
					icon: 'images/arrowdowndouble.png',
					width: 22,
					height: 22,
					handler: function() {
                        if (dataElementsByStageStore.getRange().length) {
                            selectDataElements(dataElementsByStageStore.getRange());
                        }
					}
				}
			],
			listeners: {
				afterrender: function(ms) {
					this.boundList.on('itemdblclick', function() {
                        if (ms.getValue().length) {
                            selectDataElements(ms.getValue());
                        }
					});
				}
			}
		});

        dataElementSelected = Ext.create('Ext.panel.Panel', {
			width: accBaseWidth,
            height: 204,
            bodyStyle: 'padding:2px 0 1px 3px; overflow-y: scroll',
            tbar: {
                height: 27,
                items: [
					{
						xtype: 'label',
						text: 'Selected data items',
						style: 'padding-left:6px; color:#222',
						cls: 'ns-toolbar-multiselect-left-label'
					},
					'->',
					{
						xtype: 'button',
						icon: 'images/arrowupdouble.png',
						width: 22,
						height: 22,
						handler: function() {
							dataElementSelected.removeAllDataElements();
						}
					}
				]
            },
            getChildIndex: function(child) {
				var items = this.items.items;

				for (var i = 0; i < items.length; i++) {
					if (items[i].id === child.id) {
						return i;
					}
				}

				return items.length;
			},
			hasDataElement: function(dataElementId) {
				var hasDataElement = false;

				this.items.each(function(item) {
					if (item.dataElement.id === dataElementId) {
						hasDataElement = true;
					}
				});

				return hasDataElement;
			},
			removeAllDataElements: function() {
				var items = this.items.items,
					len = items.length;

				for (var i = 0; i < len; i++) {
					items[0].removeDataElement();
				}
			}
        });

        addUxFromDataElement = function(element, index) {
			var getUxType,
				ux;

            element.type = element.type || element.valueType;

			index = index || dataElementSelected.items.items.length;

			getUxType = function(element) {
				if (Ext.isObject(element.optionSet) && Ext.isString(element.optionSet.id)) {
					return 'Ext.ux.panel.DataElementOptionContainer';
				}

				if (element.type === 'int' || element.type === 'number') {
					return 'Ext.ux.panel.DataElementIntegerContainer';
				}

				if (element.type === 'string') {
					return 'Ext.ux.panel.DataElementStringContainer';
				}

				if (element.type === 'date') {
					return 'Ext.ux.panel.DataElementDateContainer';
				}

				return 'Ext.ux.panel.DataElementIntegerContainer';
			};

			// add
			ux = dataElementSelected.insert(index, Ext.create(getUxType(element), {
				dataElement: element
			}));

			ux.removeDataElement = function() {
				dataElementSelected.remove(ux);

				if (!dataElementSelected.hasDataElement(element.id)) {
					dataElementsByStageStore.add(element);
					dataElementsByStageStore.sort();

                    //ns.app.aggregateLayoutWindow.removeDimension(element.id);
                    //ns.app.queryLayoutWindow.removeDimension(element.id);
				}
			};

			ux.duplicateDataElement = function() {
				var index = dataElementSelected.getChildIndex(ux) + 1;
				addUxFromDataElement(element, index);
			};

			dataElementsByStageStore.removeAt(dataElementsByStageStore.findExact('id', element.id));

            return ux;
		};

        selectDataElements = function(items) {
            var dataElements = [];

			// data element objects
            for (var i = 0, item; i < items.length; i++) {
				item = items[i];

                if (Ext.isString(item)) {
                    dataElements.push(dataElementsByStageStore.getAt(dataElementsByStageStore.findExact('id', item)).data);
                }
                else if (Ext.isObject(item)) {
                    if (item.data) {
                        dataElements.push(item.data);
                    }
                    else {
                        dataElements.push(item);
                    }
                }
            }

			// panel, store
            for (var i = 0, element, ux; i < dataElements.length; i++) {
				element = dataElements[i];

				addUxFromDataElement(element);
			}
        };

        dataElement = Ext.create('Ext.panel.Panel', {
            title: '<div class="gis-panel-title-data">Data</div>',
            bodyStyle: 'padding:1px',
            hideCollapseTool: true,
            items: [
                {
					layout: 'column',
                    bodyStyle: 'border:0 none',
					style: 'margin-top:2px',
					items: [
						program,
						stage
					]
				},
                dataElementAvailable,
                dataElementSelected
            ]
        });

            // date
		startDate = Ext.create('Ext.form.field.Date', {
			fieldLabel: 'Start date',
			labelAlign: 'top',
			labelCls: 'gis-form-item-label-top',
            //labelStyle: 'font-weight: bold',
			labelSeparator: '',
			columnWidth: 0.5,
			style: 'margin-right: 1px',
			format: 'Y-m-d',
			value: new Date( (new Date()).setMonth( (new Date()).getMonth() - 3))
		});

		endDate = Ext.create('Ext.form.field.Date', {
			fieldLabel: 'End date',
			labelAlign: 'top',
			labelCls: 'gis-form-item-label-top',
            //labelStyle: 'font-weight: bold',
			labelSeparator: '',
			columnWidth: 0.5,
			style: 'margin-left: 1px',
			format: 'Y-m-d',
			value: new Date()
		});

        period = Ext.create('Ext.panel.Panel', {
            title: '<div class="gis-panel-title-period">Periods</div>',
            bodyStyle: 'padding:4px 2px 2px',
            hideCollapseTool: true,
            layout: 'column',
            width: accBaseWidth,
            items: [
                startDate,
                endDate
            ]
        });

            // organisation unit
		treePanel = Ext.create('Ext.tree.Panel', {
			cls: 'gis-tree',
			height: 333,
            bodyStyle: 'border:0 none',
			style: 'border-top: 1px solid #ddd; padding-top: 1px',
			displayField: 'name',
			rootVisible: false,
			autoScroll: true,
			multiSelect: true,
			rendered: false,
			reset: function() {
				var rootNode = this.getRootNode().findChild('id', gis.init.rootNodes[0].id);
				this.collapseAll();
				this.expandPath(rootNode.getPath());
				this.getSelectionModel().select(rootNode);
			},
			selectRootIf: function() {
				if (this.getSelectionModel().getSelection().length < 1) {
					var node = this.getRootNode().findChild('id', gis.init.rootNodes[0].id);
					if (this.rendered) {
						this.getSelectionModel().select(node);
					}
					return node;
				}
			},
			isPending: false,
			recordsToSelect: [],
			recordsToRestore: [],
			multipleSelectIf: function(map, doUpdate) {
				if (this.recordsToSelect.length === gis.util.object.getLength(map)) {
					this.getSelectionModel().select(this.recordsToSelect);
					this.recordsToSelect = [];
					this.isPending = false;

					if (doUpdate) {
						update();
					}
				}
			},
			multipleExpand: function(id, map, doUpdate) {
				var that = this,
					rootId = gis.conf.finals.root.id,
					path = map[id];

				if (path.substr(0, rootId.length + 1) !== ('/' + rootId)) {
					path = '/' + rootId + path;
				}

				that.expandPath(path, 'id', '/', function() {
					record = Ext.clone(that.getRootNode().findChild('id', id, true));
					that.recordsToSelect.push(record);
					that.multipleSelectIf(map, doUpdate);
				});
			},
            select: function(url, params) {
                if (!params) {
                    params = {};
                }
                Ext.Ajax.request({
                    url: url,
                    method: 'GET',
                    params: params,
                    scope: this,
                    success: function(r) {
                        var a = Ext.decode(r.responseText).organisationUnits;
                        this.numberOfRecords = a.length;
                        for (var i = 0; i < a.length; i++) {
                            this.multipleExpand(a[i].id, a[i].path);
                        }
                    }
                });
            },
			getParentGraphMap: function() {
				var selection = this.getSelectionModel().getSelection(),
					map = {};

				if (Ext.isArray(selection) && selection.length) {
					for (var i = 0, pathArray, key; i < selection.length; i++) {
						pathArray = selection[i].getPath().split('/');
						map[pathArray.pop()] = pathArray.join('/');
					}
				}

				return map;
			},
			selectGraphMap: function(map, update) {
				if (!gis.util.object.getLength(map)) {
					return;
				}

				this.isPending = true;

				for (var key in map) {
					if (map.hasOwnProperty(key)) {
						treePanel.multipleExpand(key, map, update);
					}
				}
			},
            store: Ext.create('Ext.data.TreeStore', {
				fields: ['id', 'name', 'hasChildren'],
				proxy: {
					type: 'rest',
					format: 'json',
					noCache: false,
					extraParams: {
						fields: 'children[id,name,children::isNotEmpty|rename(hasChildren)&paging=false'
					},
					url: gis.init.contextPath + '/api/organisationUnits',
					reader: {
						type: 'json',
						root: 'children'
					},
					sortParam: false
				},
				sorters: [{
					property: 'name',
					direction: 'ASC'
				}],
				root: {
					id: gis.conf.finals.root.id,
					expanded: true,
					children: gis.init.rootNodes
				},
				listeners: {
					load: function(store, node, records) {
						Ext.Array.each(records, function(record) {
                            if (Ext.isBoolean(record.data.hasChildren)) {
                                record.set('leaf', !record.data.hasChildren);
                            }
                        });
					}
				}
			}),
			xable: function(values) {
				for (var i = 0; i < values.length; i++) {
					if (!!values[i]) {
						this.disable();
						return;
					}
				}

				this.enable();
			},
			getDimension: function() {
				var r = treePanel.getSelectionModel().getSelection(),
					config = {
						dimension: gis.conf.finals.dimension.organisationUnit.objectName,
						items: []
					};

				if (toolMenu.menuValue === 'orgunit') {
					if (userOrganisationUnit.getValue() || userOrganisationUnitChildren.getValue() || userOrganisationUnitGrandChildren.getValue()) {
						if (userOrganisationUnit.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT',
								name: ''
							});
						}
						if (userOrganisationUnitChildren.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT_CHILDREN',
								name: ''
							});
						}
						if (userOrganisationUnitGrandChildren.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT_GRANDCHILDREN',
								name: ''
							});
						}
					}
					else {
						for (var i = 0; i < r.length; i++) {
							config.items.push({id: r[i].data.id});
						}
					}
				}
				else if (toolMenu.menuValue === 'level') {
					var levels = organisationUnitLevel.getValue();

					for (var i = 0; i < levels.length; i++) {
						config.items.push({
							id: 'LEVEL-' + levels[i],
							name: ''
						});
					}

					for (var i = 0; i < r.length; i++) {
						config.items.push({
							id: r[i].data.id,
							name: ''
						});
					}
				}
				else if (toolMenu.menuValue === 'group') {
					var groupIds = organisationUnitGroup.getValue();

					for (var i = 0; i < groupIds.length; i++) {
						config.items.push({
							id: 'OU_GROUP-' + groupIds[i],
							name: ''
						});
					}

					for (var i = 0; i < r.length; i++) {
						config.items.push({
							id: r[i].data.id,
							name: ''
						});
					}
				}

				return config.items.length ? config : null;
			},
			listeners: {
				beforeitemexpand: function() {
					var rts = treePanel.recordsToSelect;

					if (!treePanel.isPending) {
						treePanel.recordsToRestore = treePanel.getSelectionModel().getSelection();
					}
				},
				itemexpand: function() {
					if (!treePanel.isPending && treePanel.recordsToRestore.length) {
						treePanel.getSelectionModel().select(treePanel.recordsToRestore);
						treePanel.recordsToRestore = [];
					}
				},
				render: function() {
					this.rendered = true;
				},
				afterrender: function() {
					this.getSelectionModel().select(0);
				},
				itemcontextmenu: function(v, r, h, i, e) {
					v.getSelectionModel().select(r, false);

					if (v.menu) {
						v.menu.destroy();
					}
					v.menu = Ext.create('Ext.menu.Menu', {
						id: 'treepanel-contextmenu',
						showSeparator: false,
						shadow: false
					});
					if (!r.data.leaf) {
						v.menu.add({
							id: 'treepanel-contextmenu-item',
							text: gis.i18n.select_all_children,
							icon: 'images/node-select-child.png',
							handler: function() {
								r.expand(false, function() {
									v.getSelectionModel().select(r.childNodes, true);
									v.getSelectionModel().deselect(r);
								});
							}
						});
					}
					else {
						return;
					}

					v.menu.showAt(e.xy);
				}
			}
		});

		userOrganisationUnit = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.28,
			style: 'padding-top:2px; padding-left:3px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'User org unit',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnitChildren.getValue(), userOrganisationUnitGrandChildren.getValue()]);
			}
		});

		userOrganisationUnitChildren = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.34,
			style: 'padding-top:2px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'User OU children',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnit.getValue(), userOrganisationUnitGrandChildren.getValue()]);
			}
		});

		userOrganisationUnitGrandChildren = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.38,
			style: 'padding-top:2px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'User OU grand children',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnit.getValue(), userOrganisationUnitChildren.getValue()]);
			}
		});

		organisationUnitLevel = Ext.create('Ext.form.field.ComboBox', {
			cls: 'gis-combo',
			multiSelect: true,
			style: 'margin-bottom:0',
			width: accBaseWidth - toolWidth - 2,
			valueField: 'level',
			displayField: 'name',
			emptyText: GIS.i18n.select_organisation_unit_levels,
			editable: false,
			hidden: true,
			store: {
				fields: ['id', 'name', 'level'],
				data: gis.init.organisationUnitLevels
			}
		});

		organisationUnitGroup = Ext.create('Ext.form.field.ComboBox', {
			cls: 'gis-combo',
			multiSelect: true,
			style: 'margin-bottom:0',
			width: accBaseWidth - toolWidth - 2,
			valueField: 'id',
			displayField: 'name',
			emptyText: GIS.i18n.select_organisation_unit_groups,
			editable: false,
			hidden: true,
			store: gis.store.organisationUnitGroup
		});

        organisationUnitPanel = Ext.create('Ext.panel.Panel', {
			width: accBaseWidth - toolWidth - 2,
            layout: 'column',
            bodyStyle: 'border:0 none',
            items: [
                userOrganisationUnit,
                userOrganisationUnitChildren,
                userOrganisationUnitGrandChildren,
                organisationUnitLevel,
                organisationUnitGroup
            ]
        });

		toolMenu = Ext.create('Ext.menu.Menu', {
			shadow: false,
			showSeparator: false,
			menuValue: 'orgunit',
			clickHandler: function(param) {
				if (!param) {
					return;
				}

				var items = this.items.items;
				this.menuValue = param;

				// Menu item icon cls
				for (var i = 0; i < items.length; i++) {
					if (items[i].setIconCls) {
						if (items[i].param === param) {
							items[i].setIconCls('gis-menu-item-selected');
						}
						else {
							items[i].setIconCls('gis-menu-item-unselected');
						}
					}
				}

				// Gui
				if (param === 'orgunit') {
					userOrganisationUnit.show();
					userOrganisationUnitChildren.show();
					userOrganisationUnitGrandChildren.show();
					organisationUnitLevel.hide();
					organisationUnitGroup.hide();

					if (userOrganisationUnit.getValue() || userOrganisationUnitChildren.getValue()) {
						treePanel.disable();
					}
				}
				else if (param === 'level') {
					userOrganisationUnit.hide();
					userOrganisationUnitChildren.hide();
					userOrganisationUnitGrandChildren.hide();
					organisationUnitLevel.show();
					organisationUnitGroup.hide();
					treePanel.enable();
				}
				else if (param === 'group') {
					userOrganisationUnit.hide();
					userOrganisationUnitChildren.hide();
					userOrganisationUnitGrandChildren.hide();
					organisationUnitLevel.hide();
					organisationUnitGroup.show();
					treePanel.enable();
				}
			},
			items: [
				{
					xtype: 'label',
					text: 'Selection mode',
					style: 'padding:7px 5px 5px 7px; font-weight:bold; border:0 none'
				},
				{
					text: GIS.i18n.select_organisation_units + '&nbsp;&nbsp;',
					param: 'orgunit',
					iconCls: 'gis-menu-item-selected'
				},
				{
					text: 'Select levels' + '&nbsp;&nbsp;',
					param: 'level',
					iconCls: 'gis-menu-item-unselected'
				},
				{
					text: 'Select groups' + '&nbsp;&nbsp;',
					param: 'group',
					iconCls: 'gis-menu-item-unselected'
				}
			],
			listeners: {
				afterrender: function() {
					this.getEl().addCls('gis-btn-menu');
				},
				click: function(menu, item) {
					this.clickHandler(item.param);
				}
			}
		});

		tool = Ext.create('Ext.button.Button', {
			cls: 'gis-button-organisationunitselection',
			iconCls: 'gis-button-icon-gear',
			width: toolWidth,
			height: 24,
			menu: toolMenu
		});

		toolPanel = Ext.create('Ext.panel.Panel', {
			width: toolWidth,
			bodyStyle: 'border:0 none; text-align:right',
			style: 'margin-right:2px',
			items: tool
		});

        organisationUnit = Ext.create('Ext.panel.Panel', {
            title: '<div class="gis-panel-title-organisationunit">' + GIS.i18n.organisation_units + '</div>',
            cls: 'gis-accordion-last',
            bodyStyle: 'padding:2px',
            hideCollapseTool: true,
            items: [
                {
                    layout: 'column',
                    width: accBaseWidth,
                    bodyStyle: 'border:0 none',
                    style: 'padding-bottom:2px',
                    items: [
                        toolPanel,
                        organisationUnitPanel
                    ]
                },
                treePanel
            ]
        });

            // accordion
        accordionBody = Ext.create('Ext.panel.Panel', {
			layout: 'accordion',
			activeOnTop: true,
			cls: 'gis-accordion',
			bodyStyle: 'border:0 none',
			height: 450,
			items: [
                dataElement,
                period,
                organisationUnit
            ],
            listeners: {
                afterrender: function() { // nasty workaround
                    organisationUnit.expand();
                    period.expand();
                    dataElement.expand();
                }
            }
		});

		// functions

		reset = function(skipTree) {

			// Item
			layer.item.setValue(false);

			if (!layer.window.isRendered) {
				layer.core.view = null;
				return;
			}

			// Components
            program.clearValue();
            stage.clearValue();

            dataElementsByStageStore.removeAll();
            dataElementSelected.removeAll();

            startDate.reset();
            endDate.reset();

			toolMenu.clickHandler(toolMenu.menuValue);

			if (!skipTree) {
				treePanel.reset();
			}

			userOrganisationUnit.setValue(false);
			userOrganisationUnitChildren.setValue(false);
			userOrganisationUnitGrandChildren.setValue(false);

			organisationUnitLevel.clearValue();
			organisationUnitGroup.clearValue();

			// Layer options
			//if (layer.labelWindow) {
				//layer.labelWindow.destroy();
				//layer.labelWindow = null;
			//}
		};

		setGui = function(view) { //todo
			var ouDim = view.rows[0],
				isOu = false,
				isOuc = false,
				isOugc = false,
				levels = [],
				groups = [],
				setWidgetGui,
				setLayerGui;

			setWidgetGui = function() {

				// Components
				if (!layer.window.isRendered) {
					return;
				}

				reset(true);

				// Organisation units
				for (var i = 0, item; i < ouDim.items.length; i++) {
					item = ouDim.items[i];

					if (item.id === 'USER_ORGUNIT') {
						isOu = true;
					}
					else if (item.id === 'USER_ORGUNIT_CHILDREN') {
						isOuc = true;
					}
					else if (item.id === 'USER_ORGUNIT_GRANDCHILDREN') {
						isOugc = true;
					}
					else if (item.id.substr(0,5) === 'LEVEL') {
						levels.push(parseInt(item.id.split('-')[1]));
					}
					else if (item.id.substr(0,8) === 'OU_GROUP') {
						groups.push(parseInt(item.id.split('-')[1]));
					}
				}

				if (levels.length) {
					toolMenu.clickHandler('level');
					organisationUnitLevel.setValue(levels);
				}
				else if (groups.length) {
					toolMenu.clickHandler('group');
					organisationUnitGroup.setValue(groups);
				}
				else {
					toolMenu.clickHandler('orgunit');
					userOrganisationUnit.setValue(isOu);
					userOrganisationUnitChildren.setValue(isOuc);
					userOrganisationUnitGrandChildren.setValue(isOugc);
				}

				treePanel.selectGraphMap(view.parentGraphMap);
			}();

			setLayerGui = function() {

				// Layer item
				layer.item.setValue(!view.hidden, view.opacity);

				// Layer menu
				layer.menu.enableItems();
			}();
		};

		getView = function(config) {
			var view = {};

            view.program = program.getRecord();
            view.stage = stage.getRecord();

            view.startDate = startDate.getSubmitValue();
            view.endDate = endDate.getSubmitValue();

            view.dataElements = [];

            for (var i = 0, panel; i < dataElementSelected.items.items.length; i++) {
                panel = dataElementSelected.items.items[i];

                view.dataElements.push(panel.getRecord());
            }

            view.organisationUnits = treePanel.getDimension().items;

			return view;
		};

		validateView = function(view) {
			if (!(Ext.isArray(view.rows) && view.rows.length && Ext.isString(view.rows[0].dimension) && Ext.isArray(view.rows[0].items) && view.rows[0].items.length)) {
				GIS.logg.push([view.rows, layer.id + '.rows: dimension array']);
				alert('No organisation units selected');
				return false;
			}

			return view;
		};

		panel = Ext.create('Ext.panel.Panel', {
			map: layer.map,
			layer: layer,
			menu: layer.menu,

			reset: reset,
			setGui: setGui,
			getView: getView,
			getParentGraphMap: function() {
				return treePanel.getParentGraphMap();
			},

			cls: 'gis-form-widget',
			border: false,
			items: [
                accordionBody
			]
		});

		return panel;
	};

	GIS.app.LayerWidgetFacility = function(layer) {

		// Stores
		var infrastructuralDataElementValuesStore,

		// Components
			groupSet,

			treePanel,
			userOrganisationUnit,
			userOrganisationUnitChildren,
			userOrganisationUnitGrandChildren,
			organisationUnitLevel,
			organisationUnitGroup,
			toolMenu,
			tool,
			toolPanel,

			areaRadius,

		// Functions

			//createSelectHandlers,
			reset,
			setGui,
			getView,
			validateView,

			panel;

		// Stores

		infrastructuralDataElementValuesStore = Ext.create('Ext.data.Store', {
			fields: ['name', 'value']
		});

		// Components

		groupSet = Ext.create('Ext.form.field.ComboBox', {
			cls: 'gis-combo',
            editable: false,
            valueField: 'id',
            displayField: 'name',
            emptyText: GIS.i18n.select_groupset,
            mode: 'remote',
            forceSelection: true,
            width: gis.conf.layout.widget.item_width,
            labelWidth: gis.conf.layout.widget.itemlabel_width,
            currentValue: false,
            store: gis.store.groupSets
        });

		treePanel = Ext.create('Ext.tree.Panel', {
			cls: 'gis-tree',
			height: 200,
			style: 'border-top: 1px solid #ddd; padding-top: 1px',
			displayField: 'name',
			width: gis.conf.layout.widget.item_width,
			rootVisible: false,
			autoScroll: true,
			multiSelect: true,
			rendered: false,
			reset: function() {
				var rootNode = this.getRootNode().findChild('id', gis.init.rootNodes[0].id);
				this.collapseAll();
				this.expandPath(rootNode.getPath());
				this.getSelectionModel().select(rootNode);
			},
			selectRootIf: function() {
				if (this.getSelectionModel().getSelection().length < 1) {
					var node = this.getRootNode().findChild('id', gis.init.rootNodes[0].id);
					if (this.rendered) {
						this.getSelectionModel().select(node);
					}
					return node;
				}
			},
			isPending: false,
			recordsToSelect: [],
			recordsToRestore: [],
			multipleSelectIf: function(map, doUpdate) {
				if (this.recordsToSelect.length === gis.util.object.getLength(map)) {
					this.getSelectionModel().select(this.recordsToSelect);
					this.recordsToSelect = [];
					this.isPending = false;

					if (doUpdate) {
						update();
					}
				}
			},
			multipleExpand: function(id, map, doUpdate) {
				var that = this,
					rootId = gis.conf.finals.root.id,
					path = map[id];

				if (path.substr(0, rootId.length + 1) !== ('/' + rootId)) {
					path = '/' + rootId + path;
				}

				that.expandPath(path, 'id', '/', function() {
					record = Ext.clone(that.getRootNode().findChild('id', id, true));
					that.recordsToSelect.push(record);
					that.multipleSelectIf(map, doUpdate);
				});
			},
            select: function(url, params) {
                if (!params) {
                    params = {};
                }
                Ext.Ajax.request({
                    url: url,
                    method: 'GET',
                    params: params,
                    scope: this,
                    success: function(r) {
                        var a = Ext.decode(r.responseText).organisationUnits;
                        this.numberOfRecords = a.length;
                        for (var i = 0; i < a.length; i++) {
                            this.multipleExpand(a[i].id, a[i].path);
                        }
                    }
                });
            },
			getParentGraphMap: function() {
				var selection = this.getSelectionModel().getSelection(),
					map = {};

				if (Ext.isArray(selection) && selection.length) {
					for (var i = 0, pathArray, key; i < selection.length; i++) {
						pathArray = selection[i].getPath().split('/');
						map[pathArray.pop()] = pathArray.join('/');
					}
				}

				return map;
			},
			selectGraphMap: function(map, update) {
				if (!gis.util.object.getLength(map)) {
					return;
				}

				this.isPending = true;

				for (var key in map) {
					if (map.hasOwnProperty(key)) {
						treePanel.multipleExpand(key, map, update);
					}
				}
			},
            store: Ext.create('Ext.data.TreeStore', {
				fields: ['id', 'name', 'hasChildren'],
				proxy: {
					type: 'rest',
					format: 'json',
					noCache: false,
					extraParams: {
						fields: 'children[id,name,children::isNotEmpty|rename(hasChildren)&paging=false'
					},
					url: gis.init.contextPath + '/api/organisationUnits',
					reader: {
						type: 'json',
						root: 'children'
					},
					sortParam: false
				},
				sorters: [{
					property: 'name',
					direction: 'ASC'
				}],
				root: {
					id: gis.conf.finals.root.id,
					expanded: true,
					children: gis.init.rootNodes
				},
				listeners: {
					load: function(store, node, records) {
						Ext.Array.each(records, function(record) {
                            if (Ext.isBoolean(record.data.hasChildren)) {
                                record.set('leaf', !record.data.hasChildren);
                            }
                        });
					}
				}
			}),
			xable: function(values) {
				for (var i = 0; i < values.length; i++) {
					if (!!values[i]) {
						this.disable();
						return;
					}
				}

				this.enable();
			},
			getDimension: function() {
				var r = treePanel.getSelectionModel().getSelection(),
					config = {
						dimension: gis.conf.finals.dimension.organisationUnit.objectName,
						items: []
					};

				if (toolMenu.menuValue === 'orgunit') {
					if (userOrganisationUnit.getValue() || userOrganisationUnitChildren.getValue() || userOrganisationUnitGrandChildren.getValue()) {
						if (userOrganisationUnit.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT',
								name: ''
							});
						}
						if (userOrganisationUnitChildren.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT_CHILDREN',
								name: ''
							});
						}
						if (userOrganisationUnitGrandChildren.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT_GRANDCHILDREN',
								name: ''
							});
						}
					}
					else {
						for (var i = 0; i < r.length; i++) {
							config.items.push({id: r[i].data.id});
						}
					}
				}
				else if (toolMenu.menuValue === 'level') {
					var levels = organisationUnitLevel.getValue();

					for (var i = 0; i < levels.length; i++) {
						config.items.push({
							id: 'LEVEL-' + levels[i],
							name: ''
						});
					}

					for (var i = 0; i < r.length; i++) {
						config.items.push({
							id: r[i].data.id,
							name: ''
						});
					}
				}
				else if (toolMenu.menuValue === 'group') {
					var groupIds = organisationUnitGroup.getValue();

					for (var i = 0; i < groupIds.length; i++) {
						config.items.push({
							id: 'OU_GROUP-' + groupIds[i],
							name: ''
						});
					}

					for (var i = 0; i < r.length; i++) {
						config.items.push({
							id: r[i].data.id,
							name: ''
						});
					}
				}

				return config.items.length ? config : null;
			},
			listeners: {
				beforeitemexpand: function() {
					var rts = treePanel.recordsToSelect;

					if (!treePanel.isPending) {
						treePanel.recordsToRestore = treePanel.getSelectionModel().getSelection();
					}
				},
				itemexpand: function() {
					if (!treePanel.isPending && treePanel.recordsToRestore.length) {
						treePanel.getSelectionModel().select(treePanel.recordsToRestore);
						treePanel.recordsToRestore = [];
					}
				},
				render: function() {
					this.rendered = true;
				},
				afterrender: function() {
					this.getSelectionModel().select(0);
				},
				itemcontextmenu: function(v, r, h, i, e) {
					v.getSelectionModel().select(r, false);

					if (v.menu) {
						v.menu.destroy();
					}
					v.menu = Ext.create('Ext.menu.Menu', {
						id: 'treepanel-contextmenu',
						showSeparator: false,
						shadow: false
					});
					if (!r.data.leaf) {
						v.menu.add({
							id: 'treepanel-contextmenu-item',
							text: gis.i18n.select_all_children,
							icon: 'images/node-select-child.png',
							handler: function() {
								r.expand(false, function() {
									v.getSelectionModel().select(r.childNodes, true);
									v.getSelectionModel().deselect(r);
								});
							}
						});
					}
					else {
						return;
					}

					v.menu.showAt(e.xy);
				}
			}
		});

		userOrganisationUnit = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.30,
			style: 'padding-top:2px; padding-left:3px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'User OU',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnitChildren.getValue(), userOrganisationUnitGrandChildren.getValue()]);
			}
		});

		userOrganisationUnitChildren = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.30,
			style: 'padding-top:2px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'Children',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnit.getValue(), userOrganisationUnitGrandChildren.getValue()]);
			}
		});

		userOrganisationUnitGrandChildren = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.40,
			style: 'padding-top:2px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'Grand children',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnit.getValue(), userOrganisationUnitChildren.getValue()]);
			}
		});

		organisationUnitLevel = Ext.create('Ext.form.field.ComboBox', {
			cls: 'gis-combo',
			multiSelect: true,
			style: 'margin-bottom:0',
			width: gis.conf.layout.widget.item_width - 38,
			valueField: 'level',
			displayField: 'name',
			emptyText: GIS.i18n.select_organisation_unit_levels,
			editable: false,
			hidden: true,
			store: {
				fields: ['id', 'name', 'level'],
				data: gis.init.organisationUnitLevels
			}
		});

		organisationUnitGroup = Ext.create('Ext.form.field.ComboBox', {
			cls: 'gis-combo',
			multiSelect: true,
			style: 'margin-bottom:0',
			width: gis.conf.layout.widget.item_width - 38,
			valueField: 'id',
			displayField: 'name',
			emptyText: GIS.i18n.select_organisation_unit_groups,
			editable: false,
			hidden: true,
			store: gis.store.organisationUnitGroup
		});

		toolMenu = Ext.create('Ext.menu.Menu', {
			shadow: false,
			showSeparator: false,
			menuValue: 'level',
			clickHandler: function(param) {
				if (!param) {
					return;
				}

				var items = this.items.items;
				this.menuValue = param;

				// Menu item icon cls
				for (var i = 0; i < items.length; i++) {
					if (items[i].setIconCls) {
						if (items[i].param === param) {
							items[i].setIconCls('gis-menu-item-selected');
						}
						else {
							items[i].setIconCls('gis-menu-item-unselected');
						}
					}
				}

				// Gui
				if (param === 'orgunit') {
					userOrganisationUnit.show();
					userOrganisationUnitChildren.show();
					userOrganisationUnitGrandChildren.show();
					organisationUnitLevel.hide();
					organisationUnitGroup.hide();

					if (userOrganisationUnit.getValue() || userOrganisationUnitChildren.getValue()) {
						treePanel.disable();
					}
				}
				else if (param === 'level') {
					userOrganisationUnit.hide();
					userOrganisationUnitChildren.hide();
					userOrganisationUnitGrandChildren.hide();
					organisationUnitLevel.show();
					organisationUnitGroup.hide();
					treePanel.enable();
				}
				else if (param === 'group') {
					userOrganisationUnit.hide();
					userOrganisationUnitChildren.hide();
					userOrganisationUnitGrandChildren.hide();
					organisationUnitLevel.hide();
					organisationUnitGroup.show();
					treePanel.enable();
				}
			},
			items: [
				{
					xtype: 'label',
					text: 'Selection mode',
					style: 'padding:7px 5px 5px 7px; font-weight:bold; border:0 none'
				},
				{
					text: GIS.i18n.select_organisation_units + '&nbsp;&nbsp;',
					param: 'orgunit',
					iconCls: 'gis-menu-item-selected'
				},
				{
					text: 'Select levels' + '&nbsp;&nbsp;',
					param: 'level',
					iconCls: 'gis-menu-item-unselected'
				},
				{
					text: 'Select groups' + '&nbsp;&nbsp;',
					param: 'group',
					iconCls: 'gis-menu-item-unselected'
				}
			],
			listeners: {
				afterrender: function() {
					this.getEl().addCls('gis-btn-menu');
				},
				click: function(menu, item) {
					this.clickHandler(item.param);
				}
			}
		});

		tool = Ext.create('Ext.button.Button', {
			cls: 'gis-button-organisationunitselection',
			iconCls: 'gis-button-icon-gear',
			width: 36,
			height: 24,
			menu: toolMenu
		});

		toolPanel = Ext.create('Ext.panel.Panel', {
			width: 36,
			bodyStyle: 'border:0 none; text-align:right',
			style: 'margin-right:2px',
			items: tool
		});

		areaRadius = Ext.create('Ext.ux.panel.CheckTextNumber', {
			width: gis.conf.layout.widget.item_width,
			text: GIS.i18n.show_circular_area + ':'
		});

		// Functions

		reset = function(skipTree) {

			// Item
			layer.item.setValue(false, layer.item.defaultOpacity);

			// Layer
			if (layer.searchWindow) {
				layer.searchWindow.destroy();
				layer.searchWindow = null;
			}
			if (layer.filterWindow) {
				layer.filterWindow.destroy();
				layer.filterWindow = null;
			}
			if (layer.labelWindow) {
				layer.labelWindow.destroy();
				layer.labelWindow = null;
			}

			if (layer.circleLayer & !skipTree) {
				layer.circleLayer.deactivateControls();
				layer.circleLayer = null;
			}

			// Components
			if (!layer.window.isRendered) {
				layer.core.view = null;
				return;
			}

			groupSet.clearValue();

			toolMenu.clickHandler(toolMenu.menuValue);

			if (!skipTree) {
				treePanel.reset();
			}

			userOrganisationUnit.setValue(false);
			userOrganisationUnitChildren.setValue(false);
			userOrganisationUnitGrandChildren.setValue(false);

			organisationUnitLevel.clearValue();
			organisationUnitGroup.clearValue();

			areaRadius.reset();
		};

		setGui = function(view) {
			var ouDim = view.rows[0],
				isOu = false,
				isOuc = false,
				isOugc = false,
				levels = [],
				groups = [],
				setWidgetGui,
				setLayerGui;

			setWidgetGui = function() {

				// Components
				if (!layer.window.isRendered) {
					return;
				}

				reset(true);

				// Group set
				groupSet.store.removeAll();
				groupSet.store.add(view.organisationUnitGroupSet);
				groupSet.setValue(view.organisationUnitGroupSet.id);

				// Organisation units
				for (var i = 0, item; i < ouDim.items.length; i++) {
					item = ouDim.items[i];

					if (item.id === 'USER_ORGUNIT') {
						isOu = true;
					}
					else if (item.id === 'USER_ORGUNIT_CHILDREN') {
						isOuc = true;
					}
					else if (item.id === 'USER_ORGUNIT_GRANDCHILDREN') {
						isOugc = true;
					}
					else if (item.id.substr(0,5) === 'LEVEL') {
						levels.push(parseInt(item.id.split('-')[1]));
					}
					else if (item.id.substr(0,8) === 'OU_GROUP') {
						groups.push(parseInt(item.id.split('-')[1]));
					}
				}

				if (levels.length) {
					toolMenu.clickHandler('level');
					organisationUnitLevel.setValue(levels);
				}
				else if (groups.length) {
					toolMenu.clickHandler('group');
					organisationUnitGroup.setValue(groups);
				}
				else {
					toolMenu.clickHandler('orgunit');
					userOrganisationUnit.setValue(isOu);
					userOrganisationUnitChildren.setValue(isOuc);
					userOrganisationUnitGrandChildren.setValue(isOugc);
				}

				treePanel.selectGraphMap(view.parentGraphMap);

				// Area radius
				areaRadius.setValue(!!view.areaRadius, !!view.areaRadius ? view.areaRadius : null);
			}();

			setLayerGui = function() {

				// Layer item
				layer.item.setValue(!view.hidden, view.opacity);

				// Layer menu
				layer.menu.enableItems();

				// Update filter window
				if (layer.filterWindow && layer.filterWindow.isVisible()) {
					layer.filterWindow.filter();
				}
			}();
		};

		getView = function(config) {
			var view = {};

			view.layer = layer.id;

			view.rows = [treePanel.getDimension()];

			view.organisationUnitGroupSet = {
				id: groupSet.getValue()
			};

			view.areaRadius = areaRadius.getValue() ? areaRadius.getNumber() : null;

			view.opacity = layer.item.getOpacity();

			return validateView(view);
		};

		validateView = function(view) {
			if (!(Ext.isObject(view.organisationUnitGroupSet) && Ext.isString(view.organisationUnitGroupSet.id))) {
				GIS.logg.push([view.organisationUnitGroupSet.id, layer.id + '.organisationUnitGroupSet.id: string']);
				alert(GIS.i18n.no_groupset_selected);
				return false;
			}

			if (!(Ext.isArray(view.rows) && view.rows.length && Ext.isString(view.rows[0].dimension) && Ext.isArray(view.rows[0].items) && view.rows[0].items.length)) {
				GIS.logg.push([view.rows, layer.id + '.rows: dimension array']);
				alert('No organisation units selected');
				return false;
			}

			return view;
		};

		panel = Ext.create('Ext.panel.Panel', {
			map: layer.map,
			layer: layer,
			menu: layer.menu,

			reset: reset,
			setGui: setGui,
			getView: getView,
			getParentGraphMap: function() {
				return treePanel.getParentGraphMap();
			},

			infrastructuralDataElementValuesStore: infrastructuralDataElementValuesStore,

			cls: 'gis-form-widget el-border-0',
			border: false,
			items: [
				{
					xtype: 'form',
					cls: 'el-border-0',
					items: [
						{
							html: GIS.i18n.organisationunit_groupset,
							cls: 'gis-form-subtitle-first'
						},
						groupSet,
						{
							html: GIS.i18n.organisation_units,
							cls: 'gis-form-subtitle'
						},
						{
							layout: 'column',
							bodyStyle: 'border:0 none',
							style: 'padding-bottom:2px',
							items: [
								toolPanel,
								{
									width: gis.conf.layout.widget.item_width - 38,
									layout: 'column',
									bodyStyle: 'border:0 none',
									items: [
										userOrganisationUnit,
										userOrganisationUnitChildren,
										userOrganisationUnitGrandChildren,
										organisationUnitLevel,
										organisationUnitGroup
									]
								}
							]
						},
						treePanel,
						{
							html: GIS.i18n.surrounding_areas,
							cls: 'gis-form-subtitle'
						},
						areaRadius
					]
				}
			],
			listeners: {
				render: function() {
					toolMenu.clickHandler('level');
				}
			}
		});

		//createSelectHandlers();

		return panel;
	};

	GIS.app.LayerWidgetBoundary = function(layer) {

		// Stores
		var infrastructuralDataElementValuesStore,

		// Components

			treePanel,
			userOrganisationUnit,
			userOrganisationUnitChildren,
			userOrganisationUnitGrandChildren,
			organisationUnitLevel,
			organisationUnitGroup,
			toolMenu,
			tool,
			toolPanel,

		// Functions
			reset,
			setGui,
			getView,
			validateView,

			panel;

		// Stores

		infrastructuralDataElementValuesStore = Ext.create('Ext.data.Store', {
			fields: ['name', 'value']
		});

		// Components

		treePanel = Ext.create('Ext.tree.Panel', {
			cls: 'gis-tree',
			height: 200,
			style: 'border-top: 1px solid #ddd; padding-top: 1px',
			displayField: 'name',
			width: gis.conf.layout.widget.item_width,
			rootVisible: false,
			autoScroll: true,
			multiSelect: true,
			rendered: false,
			reset: function() {
				var rootNode = this.getRootNode().findChild('id', gis.init.rootNodes[0].id);
				this.collapseAll();
				this.expandPath(rootNode.getPath());
				this.getSelectionModel().select(rootNode);
			},
			selectRootIf: function() {
				if (this.getSelectionModel().getSelection().length < 1) {
					var node = this.getRootNode().findChild('id', gis.init.rootNodes[0].id);
					if (this.rendered) {
						this.getSelectionModel().select(node);
					}
					return node;
				}
			},
			isPending: false,
			recordsToSelect: [],
			recordsToRestore: [],
			multipleSelectIf: function(map, doUpdate) {
				if (this.recordsToSelect.length === gis.util.object.getLength(map)) {
					this.getSelectionModel().select(this.recordsToSelect);
					this.recordsToSelect = [];
					this.isPending = false;

					if (doUpdate) {
						update();
					}
				}
			},
			multipleExpand: function(id, map, doUpdate) {
				var that = this,
					rootId = gis.conf.finals.root.id,
					path = map[id];

				if (path.substr(0, rootId.length + 1) !== ('/' + rootId)) {
					path = '/' + rootId + path;
				}

				that.expandPath(path, 'id', '/', function() {
					record = Ext.clone(that.getRootNode().findChild('id', id, true));
					that.recordsToSelect.push(record);
					that.multipleSelectIf(map, doUpdate);
				});
			},
            select: function(url, params) {
                if (!params) {
                    params = {};
                }
                Ext.Ajax.request({
                    url: url,
                    method: 'GET',
                    params: params,
                    scope: this,
                    success: function(r) {
                        var a = Ext.decode(r.responseText).organisationUnits;
                        this.numberOfRecords = a.length;
                        for (var i = 0; i < a.length; i++) {
                            this.multipleExpand(a[i].id, a[i].path);
                        }
                    }
                });
            },
			getParentGraphMap: function() {
				var selection = this.getSelectionModel().getSelection(),
					map = {};

				if (Ext.isArray(selection) && selection.length) {
					for (var i = 0, pathArray, key; i < selection.length; i++) {
						pathArray = selection[i].getPath().split('/');
						map[pathArray.pop()] = pathArray.join('/');
					}
				}

				return map;
			},
			selectGraphMap: function(map, update) {
				if (!gis.util.object.getLength(map)) {
					return;
				}

				this.isPending = true;

				for (var key in map) {
					if (map.hasOwnProperty(key)) {
						treePanel.multipleExpand(key, map, update);
					}
				}
			},
            store: Ext.create('Ext.data.TreeStore', {
				fields: ['id', 'name', 'hasChildren'],
				proxy: {
					type: 'rest',
					format: 'json',
					noCache: false,
					extraParams: {
						fields: 'children[id,name,children::isNotEmpty|rename(hasChildren)&paging=false'
					},
					url: gis.init.contextPath + '/api/organisationUnits',
					reader: {
						type: 'json',
						root: 'children'
					},
					sortParam: false
				},
				sorters: [{
					property: 'name',
					direction: 'ASC'
				}],
				root: {
					id: gis.conf.finals.root.id,
					expanded: true,
					children: gis.init.rootNodes
				},
				listeners: {
					load: function(store, node, records) {
						Ext.Array.each(records, function(record) {
                            if (Ext.isBoolean(record.data.hasChildren)) {
                                record.set('leaf', !record.data.hasChildren);
                            }
                        });
					}
				}
			}),
			xable: function(values) {
				for (var i = 0; i < values.length; i++) {
					if (!!values[i]) {
						this.disable();
						return;
					}
				}

				this.enable();
			},
			getDimension: function() {
				var r = treePanel.getSelectionModel().getSelection(),
					config = {
						dimension: gis.conf.finals.dimension.organisationUnit.objectName,
						items: []
					};

				if (toolMenu.menuValue === 'orgunit') {
					if (userOrganisationUnit.getValue() || userOrganisationUnitChildren.getValue() || userOrganisationUnitGrandChildren.getValue()) {
						if (userOrganisationUnit.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT',
								name: ''
							});
						}
						if (userOrganisationUnitChildren.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT_CHILDREN',
								name: ''
							});
						}
						if (userOrganisationUnitGrandChildren.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT_GRANDCHILDREN',
								name: ''
							});
						}
					}
					else {
						for (var i = 0; i < r.length; i++) {
							config.items.push({id: r[i].data.id});
						}
					}
				}
				else if (toolMenu.menuValue === 'level') {
					var levels = organisationUnitLevel.getValue();

					for (var i = 0; i < levels.length; i++) {
						config.items.push({
							id: 'LEVEL-' + levels[i],
							name: ''
						});
					}

					for (var i = 0; i < r.length; i++) {
						config.items.push({
							id: r[i].data.id,
							name: ''
						});
					}
				}
				else if (toolMenu.menuValue === 'group') {
					var groupIds = organisationUnitGroup.getValue();

					for (var i = 0; i < groupIds.length; i++) {
						config.items.push({
							id: 'OU_GROUP-' + groupIds[i],
							name: ''
						});
					}

					for (var i = 0; i < r.length; i++) {
						config.items.push({
							id: r[i].data.id,
							name: ''
						});
					}
				}

				return config.items.length ? config : null;
			},
			listeners: {
				beforeitemexpand: function() {
					var rts = treePanel.recordsToSelect;

					if (!treePanel.isPending) {
						treePanel.recordsToRestore = treePanel.getSelectionModel().getSelection();
					}
				},
				itemexpand: function() {
					if (!treePanel.isPending && treePanel.recordsToRestore.length) {
						treePanel.getSelectionModel().select(treePanel.recordsToRestore);
						treePanel.recordsToRestore = [];
					}
				},
				render: function() {
					this.rendered = true;
				},
				afterrender: function() {
					this.getSelectionModel().select(0);
				},
				itemcontextmenu: function(v, r, h, i, e) {
					v.getSelectionModel().select(r, false);

					if (v.menu) {
						v.menu.destroy();
					}
					v.menu = Ext.create('Ext.menu.Menu', {
						id: 'treepanel-contextmenu',
						showSeparator: false,
						shadow: false
					});
					if (!r.data.leaf) {
						v.menu.add({
							id: 'treepanel-contextmenu-item',
							text: gis.i18n.select_all_children,
							icon: 'images/node-select-child.png',
							handler: function() {
								r.expand(false, function() {
									v.getSelectionModel().select(r.childNodes, true);
									v.getSelectionModel().deselect(r);
								});
							}
						});
					}
					else {
						return;
					}

					v.menu.showAt(e.xy);
				}
			}
		});

		userOrganisationUnit = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.30,
			style: 'padding-top:2px; padding-left:3px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'User OU',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnitChildren.getValue(), userOrganisationUnitGrandChildren.getValue()]);
			}
		});

		userOrganisationUnitChildren = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.30,
			style: 'padding-top:2px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'Children',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnit.getValue(), userOrganisationUnitGrandChildren.getValue()]);
			}
		});

		userOrganisationUnitGrandChildren = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.40,
			style: 'padding-top:2px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'Grand children',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnit.getValue(), userOrganisationUnitChildren.getValue()]);
			}
		});

		organisationUnitLevel = Ext.create('Ext.form.field.ComboBox', {
			cls: 'gis-combo',
			multiSelect: true,
			style: 'margin-bottom:0',
			width: gis.conf.layout.widget.item_width - 38,
			valueField: 'level',
			displayField: 'name',
			emptyText: GIS.i18n.select_organisation_unit_levels,
			editable: false,
			hidden: true,
			store: {
				fields: ['id', 'name', 'level'],
				data: gis.init.organisationUnitLevels
			}
		});

		organisationUnitGroup = Ext.create('Ext.form.field.ComboBox', {
			cls: 'gis-combo',
			multiSelect: true,
			style: 'margin-bottom:0',
			width: gis.conf.layout.widget.item_width - 38,
			valueField: 'id',
			displayField: 'name',
			emptyText: GIS.i18n.select_organisation_unit_groups,
			editable: false,
			hidden: true,
			store: gis.store.organisationUnitGroup
		});

		toolMenu = Ext.create('Ext.menu.Menu', {
			shadow: false,
			showSeparator: false,
			menuValue: 'level',
			clickHandler: function(param) {
				if (!param) {
					return;
				}

				var items = this.items.items;
				this.menuValue = param;

				// Menu item icon cls
				for (var i = 0; i < items.length; i++) {
					if (items[i].setIconCls) {
						if (items[i].param === param) {
							items[i].setIconCls('gis-menu-item-selected');
						}
						else {
							items[i].setIconCls('gis-menu-item-unselected');
						}
					}
				}

				// Gui
				if (param === 'orgunit') {
					userOrganisationUnit.show();
					userOrganisationUnitChildren.show();
					userOrganisationUnitGrandChildren.show();
					organisationUnitLevel.hide();
					organisationUnitGroup.hide();

					if (userOrganisationUnit.getValue() || userOrganisationUnitChildren.getValue()) {
						treePanel.disable();
					}
				}
				else if (param === 'level') {
					userOrganisationUnit.hide();
					userOrganisationUnitChildren.hide();
					userOrganisationUnitGrandChildren.hide();
					organisationUnitLevel.show();
					organisationUnitGroup.hide();
					treePanel.enable();
				}
				else if (param === 'group') {
					userOrganisationUnit.hide();
					userOrganisationUnitChildren.hide();
					userOrganisationUnitGrandChildren.hide();
					organisationUnitLevel.hide();
					organisationUnitGroup.show();
					treePanel.enable();
				}
			},
			items: [
				{
					xtype: 'label',
					text: 'Selection mode',
					style: 'padding:7px 5px 5px 7px; font-weight:bold; border:0 none'
				},
				{
					text: GIS.i18n.select_organisation_units + '&nbsp;&nbsp;',
					param: 'orgunit',
					iconCls: 'gis-menu-item-selected'
				},
				{
					text: 'Select levels' + '&nbsp;&nbsp;',
					param: 'level',
					iconCls: 'gis-menu-item-unselected'
				},
				{
					text: 'Select groups' + '&nbsp;&nbsp;',
					param: 'group',
					iconCls: 'gis-menu-item-unselected'
				}
			],
			listeners: {
				afterrender: function() {
					this.getEl().addCls('gis-btn-menu');
				},
				click: function(menu, item) {
					this.clickHandler(item.param);
				}
			}
		});

		tool = Ext.create('Ext.button.Button', {
			cls: 'gis-button-organisationunitselection',
			iconCls: 'gis-button-icon-gear',
			width: 36,
			height: 24,
			menu: toolMenu
		});

		toolPanel = Ext.create('Ext.panel.Panel', {
			width: 36,
			bodyStyle: 'border:0 none; text-align:right',
			style: 'margin-right:2px',
			items: tool
		});

		// Functions

		reset = function(skipTree) {

			// Item
			layer.item.setValue(false);

			if (!layer.window.isRendered) {
				layer.core.view = null;
				return;
			}

			// Components
			toolMenu.clickHandler(toolMenu.menuValue);

			if (!skipTree) {
				treePanel.reset();
			}

			userOrganisationUnit.setValue(false);
			userOrganisationUnitChildren.setValue(false);
			userOrganisationUnitGrandChildren.setValue(false);

			organisationUnitLevel.clearValue();
			organisationUnitGroup.clearValue();

			// Layer options
			if (layer.searchWindow) {
				layer.searchWindow.destroy();
				layer.searchWindow = null;
			}
			if (layer.labelWindow) {
				layer.labelWindow.destroy();
				layer.labelWindow = null;
			}
		};

		setGui = function(view) {
			var ouDim = view.rows[0],
				isOu = false,
				isOuc = false,
				isOugc = false,
				levels = [],
				groups = [],
				setWidgetGui,
				setLayerGui;

			setWidgetGui = function() {

				// Components
				if (!layer.window.isRendered) {
					return;
				}

				reset(true);

				// Organisation units
				for (var i = 0, item; i < ouDim.items.length; i++) {
					item = ouDim.items[i];

					if (item.id === 'USER_ORGUNIT') {
						isOu = true;
					}
					else if (item.id === 'USER_ORGUNIT_CHILDREN') {
						isOuc = true;
					}
					else if (item.id === 'USER_ORGUNIT_GRANDCHILDREN') {
						isOugc = true;
					}
					else if (item.id.substr(0,5) === 'LEVEL') {
						levels.push(parseInt(item.id.split('-')[1]));
					}
					else if (item.id.substr(0,8) === 'OU_GROUP') {
						groups.push(parseInt(item.id.split('-')[1]));
					}
				}

				if (levels.length) {
					toolMenu.clickHandler('level');
					organisationUnitLevel.setValue(levels);
				}
				else if (groups.length) {
					toolMenu.clickHandler('group');
					organisationUnitGroup.setValue(groups);
				}
				else {
					toolMenu.clickHandler('orgunit');
					userOrganisationUnit.setValue(isOu);
					userOrganisationUnitChildren.setValue(isOuc);
					userOrganisationUnitGrandChildren.setValue(isOugc);
				}

				treePanel.selectGraphMap(view.parentGraphMap);
			}();

			setLayerGui = function() {

				// Layer item
				layer.item.setValue(!view.hidden, view.opacity);

				// Layer menu
				layer.menu.enableItems();
			}();
		};

		getView = function(config) {
			var view = {};

			view.rows = [treePanel.getDimension()];

			return validateView(view);
		};

		validateView = function(view) {
			if (!(Ext.isArray(view.rows) && view.rows.length && Ext.isString(view.rows[0].dimension) && Ext.isArray(view.rows[0].items) && view.rows[0].items.length)) {
				GIS.logg.push([view.rows, layer.id + '.rows: dimension array']);
				alert('No organisation units selected');
				return false;
			}

			return view;
		};

		panel = Ext.create('Ext.panel.Panel', {
			map: layer.map,
			layer: layer,
			menu: layer.menu,

			reset: reset,
			setGui: setGui,
			getView: getView,
			getParentGraphMap: function() {
				return treePanel.getParentGraphMap();
			},

			infrastructuralDataElementValuesStore: infrastructuralDataElementValuesStore,

			cls: 'gis-form-widget el-border-0',
			border: false,
			items: [
				{
					xtype: 'form',
					cls: 'el-border-0',
					//width: 270,
					items: [
						{
							html: GIS.i18n.organisation_units,
							cls: 'gis-form-subtitle-first'
						},
						{
							layout: 'column',
							bodyStyle: 'border:0 none',
							style: 'padding-bottom:2px',
							items: [
								toolPanel,
								{
									width: gis.conf.layout.widget.item_width - 38,
									layout: 'column',
									bodyStyle: 'border:0 none',
									items: [
										userOrganisationUnit,
										userOrganisationUnitChildren,
										userOrganisationUnitGrandChildren,
										organisationUnitLevel,
										organisationUnitGroup
									]
								}
							]
						},
						treePanel
					]
				}
			],
			listeners: {
				render: function() {
					toolMenu.clickHandler('level');
				}
			}
		});

		//createSelectHandlers();

		return panel;
	};

	GIS.app.LayerWidgetThematic = function(layer) {

		// Stores
		var indicatorsByGroupStore,
			dataElementsByGroupStore,
			periodsByTypeStore,
			infrastructuralDataElementValuesStore,
			legendsByLegendSetStore,

		// Togglers
			valueTypeToggler,
			legendTypeToggler,

		// Components
			valueType,
			indicatorGroup,
			indicator,
			dataElementGroup,
			dataElement,
			dataElementDetailLevel,
			dataElementPanel,
			dataSet,
			periodType,
			period,
			periodPrev,
			periodNext,
			legendType,
			legendSet,
			classes,
			method,
			colorLow,
			colorHigh,
			radiusLow,
			radiusHigh,

			treePanel,
			userOrganisationUnit,
			userOrganisationUnitChildren,
			userOrganisationUnitGrandChildren,
			organisationUnitLevel,
			organisationUnitGroup,
			toolMenu,
			tool,
			toolPanel,

			periodTypePanel,
			methodPanel,
			lowPanel,
			highPanel,

		// Functions
			//createSelectHandlers,
			reset,
			setGui,
			getView,

		// Convenience
			dimConf = gis.conf.finals.dimension,

			panel;

		// Stores

		indicatorsByGroupStore = Ext.create('Ext.data.Store', {
			fields: ['id', 'name', 'legendSet'],
			proxy: {
				type: 'ajax',
				url: '',
				reader: {
					type: 'json',
					root: 'indicators'
				}
			},
			isLoaded: false,
			loadFn: function(fn) {
				if (Ext.isFunction(fn)) {
					if (this.isLoaded) {
						fn.call();
					}
					else {
						this.load({
							callback: fn
						});
					}
				}
			},
			listeners: {
				load: function() {
					if (!this.isLoaded) {
						this.isLoaded = true;
					}
					this.sort('name', 'ASC');
				}
			}
		});

		dataElementsByGroupStore = Ext.create('Ext.data.Store', {
			fields: ['id', 'name'],
			proxy: {
				type: 'ajax',
				url: '',
				reader: {
					type: 'json',
					root: 'dataElements'
				}
			},
			isLoaded: false,
			loadFn: function(fn) {
				if (this.isLoaded) {
					fn.call();
				}
				else {
					this.load(fn);
				}
			},
			sortStore: function() {
				this.sort('name', 'ASC');
			},
			setTotalsProxy: function(uid, preventLoad, callbackFn) {
				var path;

				if (Ext.isString(uid)) {
                    path = '/dataElements.json?fields=id,name&domainType=aggregate&paging=false&filter=dataElement.dataElementGroups.id:eq:' + uid;
				}
				else if (uid === 0) {
					path = '/dataElements.json?fields=id,name&domainType=aggregate&paging=false';
				}

				if (!path) {
					alert('Invalid parameter');
					return;
				}

				this.setProxy({
					type: 'ajax',
					url: gis.init.contextPath + '/api' + path,
					reader: {
						type: 'json',
						root: 'dataElements'
					}
				});

				if (!preventLoad) {
					this.load({
						scope: this,
						callback: function() {
							this.sortStore();

							if (Ext.isFunction(callbackFn)) {
								callbackFn();
							}
						}
					});
				}
			},
			setDetailsProxy: function(uid, preventLoad, callbackFn) {
				if (Ext.isString(uid)) {
					this.setProxy({
						type: 'ajax',
						url: gis.init.contextPath + '/api/dataElementOperands.json?fields=id,name&paging=false&filter=dataElement.dataElementGroups.id:eq:' + uid,
						reader: {
							type: 'json',
							root: 'dataElementOperands'
						}
					});

					if (!preventLoad) {
						this.load({
							scope: this,
							callback: function() {
								this.each(function(r) {
                                    r.set('id', r.data.id.split('#').join('.'));
								});

								this.sortStore();

								if (Ext.isFunction(callbackFn)) {
									callbackFn();
								}
							}
						});
					}
				}
				else {
					alert('Invalid parameter');
				}
			},
			listeners: {
				load: function() {
					if (!this.isLoaded) {
						this.isLoaded = true;
					}
					this.sort('name', 'ASC');
				}
			}
		});

        dataSetStore = Ext.create('Ext.data.Store', {
            fields: ['id', 'name'],
            proxy: {
                type: 'ajax',
                url: gis.init.contextPath + '/api/dataSets.json?fields=id,name&paging=false',
                reader: {
                    type: 'json',
                    root: 'dataSets'
                }
            },
            sortStore: function() {
                this.sort('name', 'ASC');
            },
            isLoaded: false,
            listeners: {
                load: function(s) {
                    this.isLoaded = true;
                }
            }
        });

		periodsByTypeStore = Ext.create('Ext.data.Store', {
			fields: ['id', 'name', 'index'],
			data: [],
			setIndex: function(periods) {
				for (var i = 0; i < periods.length; i++) {
					periods[i].index = i;
				}
			},
			sortStore: function() {
				this.sort('index', 'ASC');
			}
		});

		infrastructuralDataElementValuesStore = Ext.create('Ext.data.Store', {
			fields: ['name', 'value']
		});

		legendsByLegendSetStore = Ext.create('Ext.data.Store', {
			fields: ['id', 'name', 'startValue', 'endValue', 'color'],
			proxy: {
				type: 'ajax',
				url: '',
				reader: {
					type: 'json',
					root: 'mapLegends'
				}
			},
			isLoaded: false,
			loadFn: function(fn) {
				if (this.isLoaded) {
					fn.call();
				}
				else {
					this.load(fn);
				}
			},
			listeners: {
				load: function() {
					if (!this.isLoaded) {
						this.isLoaded = true;
					}
					this.sort('name', 'ASC');
				}
			}
		});

		// Togglers

		valueTypeToggler = function(valueType) {
			if (valueType === dimConf.indicator.objectName) {
				indicatorGroup.show();
				indicator.show();
				dataElementGroup.hide();
				dataElementPanel.hide();
				dataSet.hide();
			}
			else if (valueType === dimConf.dataElement.objectName || valueType === dimConf.operand.objectName) {
				indicatorGroup.hide();
				indicator.hide();
				dataElementGroup.show();
				dataElementPanel.show();
				dataSet.hide();
			}
			else if (valueType === dimConf.dataSet.objectName) {
				indicatorGroup.hide();
				indicator.hide();
				dataElementGroup.hide();
				dataElementPanel.hide();
				dataSet.show();
			}
		};

		legendTypeToggler = function(legendType) {
			if (legendType === 'automatic') {
				methodPanel.show();
				lowPanel.show();
				highPanel.show();
				legendSet.hide();
			}
			else if (legendType === 'predefined') {
				methodPanel.hide();
				lowPanel.hide();
				highPanel.hide();
				legendSet.show();
			}
		};

		// Components

		valueType = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: GIS.i18n.value_type,
			editable: false,
			valueField: 'id',
			displayField: 'name',
			queryMode: 'local',
			forceSelection: true,
			width: gis.conf.layout.widget.item_width,
			labelWidth: gis.conf.layout.widget.itemlabel_width,
			value: dimConf.indicator.objectName,
			store: Ext.create('Ext.data.ArrayStore', {
				fields: ['id', 'name'],
				data: [
					[dimConf.indicator.objectName, GIS.i18n.indicator],
					[dimConf.dataElement.objectName, GIS.i18n.dataelement],
					[dimConf.dataSet.objectName, GIS.i18n.reporting_rates]
				]
			}),
			listeners: {
				select: function() {
					valueTypeToggler(this.getValue());
				}
			}
		});

		indicatorGroup = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: GIS.i18n.indicator_group,
			editable: false,
			valueField: 'id',
			displayField: 'name',
			forceSelection: true,
			queryMode: 'local',
			width: gis.conf.layout.widget.item_width,
			labelWidth: gis.conf.layout.widget.itemlabel_width,
			store: {
				fields: ['id', 'name'],
				data: gis.init.indicatorGroups
			},
			listeners: {
				select: function() {
					indicator.clearValue();

					indicator.store.proxy.url = gis.init.contextPath + '/api/indicators.json?fields=id,name&paging=false&filter=indicatorGroups.id:eq:' + this.getValue();
					indicator.store.load();
				}
			}
		});

		indicator = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: GIS.i18n.indicator,
			editable: false,
			valueField: 'id',
			displayField: 'name',
			queryMode: 'local',
			forceSelection: true,
			width: gis.conf.layout.widget.item_width,
			labelWidth: gis.conf.layout.widget.itemlabel_width,
			listConfig: {loadMask: false},
			store: indicatorsByGroupStore,
			listeners: {
				select: function(cb) {
					Ext.Ajax.request({
						url: gis.init.contextPath + '/api/indicators.json?fields=legendSet[id]&paging=false&filter=id:eq:' + this.getValue(),
						success: function(r) {
							r = Ext.decode(r.responseText);

							if (Ext.isObject(r.mapLegendSet) && r.mapLegendSet.id) {
								legendType.setValue(gis.conf.finals.widget.legendtype_predefined);
								legendTypeToggler(gis.conf.finals.widget.legendtype_predefined);

								if (gis.store.legendSets.isLoaded) {
									legendSet.setValue(r.mapLegendSet.id);
								}
								else {
									gis.store.legendSets.loadFn( function() {
										legendSet.setValue(r.mapLegendSet.id);
									});
								}
							}
							else {
								legendType.setValue(gis.conf.finals.widget.legendtype_automatic);
								legendTypeToggler(gis.conf.finals.widget.legendtype_automatic);
							}
						}
					});
				}
			}
		});

		dataElementGroup = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: GIS.i18n.dataelement_group,
			editable: false,
			valueField: 'id',
			displayField: 'name',
			forceSelection: true,
			hidden: true,
			width: gis.conf.layout.widget.item_width,
			labelWidth: gis.conf.layout.widget.itemlabel_width,
			store: {
				fields: ['id', 'name'],
				data: gis.init.dataElementGroups
			},
			loadAvailable: function(preventLoad) {
				var store = dataElementsByGroupStore,
					detailLevel = dataElementDetailLevel.getValue(),
					value = this.getValue();

				if (value) {
					if (detailLevel === gis.conf.finals.dimension.dataElement.objectName) {
						store.setTotalsProxy(value, preventLoad);
					}
					else {
						store.setDetailsProxy(value, preventLoad);
					}
				}
			},
			listeners: {
				select: function(cb) {
					dataElement.clearValue();
					cb.loadAvailable();
				}
			}
		});

		dataElement = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: GIS.i18n.dataelement,
			editable: false,
			valueField: 'id',
			displayField: 'name',
			queryMode: 'local',
			forceSelection: true,
			width: gis.conf.layout.widget.item_width - 65,
			labelWidth: gis.conf.layout.widget.itemlabel_width,
			listConfig: {
				loadMask: false,
				minWidth: 188
			},
			store: dataElementsByGroupStore,
			listeners: {
				select: function() {
					var id = this.getValue(),
						index = id.indexOf('#');

					if (index !== -1) {
						id = id.substr(0, index);
					}

					Ext.Ajax.request({
						url: gis.init.contextPath + '/api/dataElements.json?fields=legendSet[id]&paging=false&filter=id:eq:' + this.getValue(),
						success: function(r) {
							r = Ext.decode(r.responseText);

							if (Ext.isObject(r.mapLegendSet) && r.mapLegendSet.id) {
								legendType.setValue(gis.conf.finals.widget.legendtype_predefined);
								legendTypeToggler(gis.conf.finals.widget.legendtype_predefined);

								if (gis.store.legendSets.isLoaded) {
									legendSet.setValue(r.mapLegendSet.id);
								}
								else {
									gis.store.legendSets.loadFn( function() {
										legendSet.setValue(r.mapLegendSet.id);
									});
								}
							}
							else {
								legendType.setValue(gis.conf.finals.widget.legendtype_automatic);
								legendTypeToggler(gis.conf.finals.widget.legendtype_automatic);
							}
						}
					});
				}
			}
		});

		dataElementDetailLevel = Ext.create('Ext.form.field.ComboBox', {
			style: 'margin-left:2px',
			queryMode: 'local',
			editable: false,
			valueField: 'id',
			displayField: 'text',
			width: 65 - 2,
			value: dimConf.dataElement.objectName,
			onSelect: function() {
				dataElementGroup.loadAvailable();
				dataElement.clearValue();
			},
			store: {
				fields: ['id', 'text'],
				data: [
					{id: dimConf.dataElement.objectName, text: GIS.i18n.totals},
					{id: dimConf.operand.objectName, text: GIS.i18n.details}
				]
			},
			listeners: {
				select: function(cb) {
					cb.onSelect();
				}
			}
		});

		dataElementPanel = Ext.create('Ext.container.Container', {
			layout: 'column',
			bodyStyle: 'border:0 none',
			hidden: true,
			items: [
				dataElement,
				dataElementDetailLevel
			]
		});

		dataSet = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: GIS.i18n.dataset,
			editable: false,
			valueField: 'id',
			displayField: 'name',
			forceSelection: true,
			hidden: true,
			width: gis.conf.layout.widget.item_width,
			labelWidth: gis.conf.layout.widget.itemlabel_width,
			listConfig: {loadMask: false},
			store: dataSetStore
		});

		periodType = Ext.create('Ext.form.field.ComboBox', {
			editable: false,
			valueField: 'id',
			displayField: 'name',
			forceSelection: true,
			queryMode: 'local',
			width: 142,
			store: gis.store.periodTypes,
			periodOffset: 0,
			selectHandler: function() {
				var type = this.getValue(),
					pType,
					offset,
					periods;

				if (type === 'relativePeriods') {
					periodsByTypeStore.loadData(gis.conf.period.relativePeriods);

					periodPrev.disable();
					periodNext.disable();
				}
				else {
					pType = new PeriodType();
					offset = this.periodOffset;
					periods = pType.get(type).generatePeriods({
						offset: offset,
						filterFuturePeriods: true,
						reversePeriods: true
					});

					periodsByTypeStore.setIndex(periods);
					periodsByTypeStore.loadData(periods);

					periodPrev.enable();
					periodNext.enable();
				}

				period.selectFirst();
			},
			listeners: {
				select: function() {
					this.selectHandler();
				}
			}
		});

		period = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: GIS.i18n.period,
			editable: false,
			valueField: 'id',
			displayField: 'name',
			queryMode: 'local',
			forceSelection: true,
			width: gis.conf.layout.widget.item_width,
			labelWidth: gis.conf.layout.widget.itemlabel_width,
			store: periodsByTypeStore,
			selectFirst: function() {
				this.setValue(this.store.getAt(0).data.id);
			}
		});

		periodPrev = Ext.create('Ext.button.Button', {
			xtype: 'button',
			text: '<',
			width: 20,
			style: 'margin-left: 3px',
			handler: function() {
				if (periodType.getValue()) {
					periodType.periodOffset--;
					periodType.fireEvent('select');
				}
			}
		});

		periodNext = Ext.create('Ext.button.Button', {
			xtype: 'button',
			text: '>',
			width: 20,
			style: 'margin-left: 3px',
			scope: this,
			handler: function() {
				if (periodType.getValue() && periodType.periodOffset < 0) {
					periodType.periodOffset++;
					periodType.fireEvent('select');
				}
			}
		});

		legendType = Ext.create('Ext.form.field.ComboBox', {
			editable: false,
			valueField: 'id',
			displayField: 'name',
			fieldLabel: GIS.i18n.legend_type,
			value: gis.conf.finals.widget.legendtype_automatic,
			queryMode: 'local',
			width: gis.conf.layout.widget.item_width,
			labelWidth: gis.conf.layout.widget.itemlabel_width,
			store: Ext.create('Ext.data.ArrayStore', {
				fields: ['id', 'name'],
				data: [
					[gis.conf.finals.widget.legendtype_automatic, GIS.i18n.automatic],
					[gis.conf.finals.widget.legendtype_predefined, GIS.i18n.predefined]
				]
			}),
			listeners: {
				select: function() {
					legendTypeToggler(this.getValue());
				}
			}
		});

		legendSet = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: GIS.i18n.legendset,
			editable: false,
			valueField: 'id',
			displayField: 'name',
			width: gis.conf.layout.widget.item_width,
			labelWidth: gis.conf.layout.widget.itemlabel_width,
			hidden: true,
			store: gis.store.legendSets
		});

		classes = Ext.create('Ext.form.field.Number', {
			editable: false,
			valueField: 'id',
			displayField: 'id',
			queryMode: 'local',
			value: 5,
			minValue: 1,
			maxValue: 7,
			width: 50,
			style: 'margin-right: 3px',
			store: Ext.create('Ext.data.ArrayStore', {
				fields: ['id'],
				data: [[1], [2], [3], [4], [5], [6], [7]]
			})
		});

		method = Ext.create('Ext.form.field.ComboBox', {
			editable: false,
			valueField: 'id',
			displayField: 'name',
			queryMode: 'local',
			value: 3,
			width: 135,
			store: Ext.create('Ext.data.ArrayStore', {
				fields: ['id', 'name'],
				data: [
					[2, GIS.i18n.equal_intervals],
					[3, GIS.i18n.equal_counts]
				]
			})
		});

		colorLow = Ext.create('Ext.ux.button.ColorButton', {
			style: 'margin-right: 3px',
			width: 135,
			value: 'ff0000',
			scope: this
		});

		colorHigh = Ext.create('Ext.ux.button.ColorButton', {
			style: 'margin-right: 3px',
			width: 135,
			value: '00ff00',
			scope: this
		});

		radiusLow = Ext.create('Ext.form.field.Number', {
			width: 50,
			allowDecimals: false,
			minValue: 1,
			value: 5
		});

		radiusHigh = Ext.create('Ext.form.field.Number', {
			width: 50,
			allowDecimals: false,
			minValue: 1,
			value: 15
		});

		treePanel = Ext.create('Ext.tree.Panel', {
			cls: 'gis-tree',
			height: 200,
			style: 'border-top: 1px solid #ddd; padding-top: 1px',
			displayField: 'name',
			width: gis.conf.layout.widget.item_width,
			rootVisible: false,
			autoScroll: true,
			multiSelect: true,
			rendered: false,
			reset: function() {
				var rootNode = this.getRootNode().findChild('id', gis.init.rootNodes[0].id);
				this.collapseAll();
				this.expandPath(rootNode.getPath());
				this.getSelectionModel().select(rootNode);
			},
			selectRootIf: function() {
				if (this.getSelectionModel().getSelection().length < 1) {
					var node = this.getRootNode().findChild('id', gis.init.rootNodes[0].id);
					if (this.rendered) {
						this.getSelectionModel().select(node);
					}
					return node;
				}
			},
			isPending: false,
			recordsToSelect: [],
			recordsToRestore: [],
			multipleSelectIf: function(map, doUpdate) {
				if (this.recordsToSelect.length === gis.util.object.getLength(map)) {
					this.getSelectionModel().select(this.recordsToSelect);
					this.recordsToSelect = [];
					this.isPending = false;

					if (doUpdate) {
						update();
					}
				}
			},
			multipleExpand: function(id, map, doUpdate) {
				var that = this,
					rootId = gis.conf.finals.root.id,
					path = map[id];

				if (path.substr(0, rootId.length + 1) !== ('/' + rootId)) {
					path = '/' + rootId + path;
				}

				that.expandPath(path, 'id', '/', function() {
					record = Ext.clone(that.getRootNode().findChild('id', id, true));
					that.recordsToSelect.push(record);
					that.multipleSelectIf(map, doUpdate);
				});
			},
            select: function(url, params) {
                if (!params) {
                    params = {};
                }
                Ext.Ajax.request({
                    url: url,
                    method: 'GET',
                    params: params,
                    scope: this,
                    success: function(r) {
                        var a = Ext.decode(r.responseText).organisationUnits;
                        this.numberOfRecords = a.length;
                        for (var i = 0; i < a.length; i++) {
                            this.multipleExpand(a[i].id, a[i].path);
                        }
                    }
                });
            },
			getParentGraphMap: function() {
				var selection = this.getSelectionModel().getSelection(),
					map = {};

				if (Ext.isArray(selection) && selection.length) {
					for (var i = 0, pathArray, key; i < selection.length; i++) {
						pathArray = selection[i].getPath().split('/');
						map[pathArray.pop()] = pathArray.join('/');
					}
				}

				return map;
			},
			selectGraphMap: function(map, update) {
				if (!gis.util.object.getLength(map)) {
					return;
				}

				this.isPending = true;

				for (var key in map) {
					if (map.hasOwnProperty(key)) {
						treePanel.multipleExpand(key, map, update);
					}
				}
			},
            store: Ext.create('Ext.data.TreeStore', {
				fields: ['id', 'name', 'hasChildren'],
				proxy: {
					type: 'rest',
					format: 'json',
					noCache: false,
					extraParams: {
						fields: 'children[id,name,children::isNotEmpty|rename(hasChildren)&paging=false'
					},
					url: gis.init.contextPath + '/api/organisationUnits',
					reader: {
						type: 'json',
						root: 'children'
					},
					sortParam: false
				},
				sorters: [{
					property: 'name',
					direction: 'ASC'
				}],
				root: {
					id: gis.conf.finals.root.id,
					expanded: true,
					children: gis.init.rootNodes
				},
				listeners: {
					load: function(store, node, records) {
						Ext.Array.each(records, function(record) {
                            if (Ext.isBoolean(record.data.hasChildren)) {
                                record.set('leaf', !record.data.hasChildren);
                            }
                        });
					}
				}
			}),
			xable: function(values) {
				for (var i = 0; i < values.length; i++) {
					if (!!values[i]) {
						this.disable();
						return;
					}
				}

				this.enable();
			},
			getDimension: function() {
				var r = treePanel.getSelectionModel().getSelection(),
					config = {
						dimension: gis.conf.finals.dimension.organisationUnit.objectName,
						items: []
					};

				if (toolMenu.menuValue === 'orgunit') {
					if (userOrganisationUnit.getValue() || userOrganisationUnitChildren.getValue() || userOrganisationUnitGrandChildren.getValue()) {
						if (userOrganisationUnit.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT',
								name: ''
							});
						}
						if (userOrganisationUnitChildren.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT_CHILDREN',
								name: ''
							});
						}
						if (userOrganisationUnitGrandChildren.getValue()) {
							config.items.push({
								id: 'USER_ORGUNIT_GRANDCHILDREN',
								name: ''
							});
						}
					}
					else {
						for (var i = 0; i < r.length; i++) {
							config.items.push({id: r[i].data.id});
						}
					}
				}
				else if (toolMenu.menuValue === 'level') {
					var levels = organisationUnitLevel.getValue();

					for (var i = 0; i < levels.length; i++) {
						config.items.push({
							id: 'LEVEL-' + levels[i],
							name: ''
						});
					}

					for (var i = 0; i < r.length; i++) {
						config.items.push({
							id: r[i].data.id,
							name: ''
						});
					}
				}
				else if (toolMenu.menuValue === 'group') {
					var groupIds = organisationUnitGroup.getValue();

					for (var i = 0; i < groupIds.length; i++) {
						config.items.push({
							id: 'OU_GROUP-' + groupIds[i],
							name: ''
						});
					}

					for (var i = 0; i < r.length; i++) {
						config.items.push({
							id: r[i].data.id,
							name: ''
						});
					}
				}

				return config.items.length ? config : null;
			},
			listeners: {
				beforeitemexpand: function() {
					var rts = treePanel.recordsToSelect;

					if (!treePanel.isPending) {
						treePanel.recordsToRestore = treePanel.getSelectionModel().getSelection();
					}
				},
				itemexpand: function() {
					if (!treePanel.isPending && treePanel.recordsToRestore.length) {
						treePanel.getSelectionModel().select(treePanel.recordsToRestore);
						treePanel.recordsToRestore = [];
					}
				},
				render: function() {
					this.rendered = true;
				},
				afterrender: function() {
					this.getSelectionModel().select(0);
				},
				itemcontextmenu: function(v, r, h, i, e) {
					v.getSelectionModel().select(r, false);

					if (v.menu) {
						v.menu.destroy();
					}
					v.menu = Ext.create('Ext.menu.Menu', {
						id: 'treepanel-contextmenu',
						showSeparator: false,
						shadow: false
					});
					if (!r.data.leaf) {
						v.menu.add({
							id: 'treepanel-contextmenu-item',
							text: gis.i18n.select_all_children,
							icon: 'images/node-select-child.png',
							handler: function() {
								r.expand(false, function() {
									v.getSelectionModel().select(r.childNodes, true);
									v.getSelectionModel().deselect(r);
								});
							}
						});
					}
					else {
						return;
					}

					v.menu.showAt(e.xy);
				}
			}
		});

		userOrganisationUnit = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.30,
			style: 'padding-top:2px; padding-left:3px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'User OU',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnitChildren.getValue(), userOrganisationUnitGrandChildren.getValue()]);
			}
		});

		userOrganisationUnitChildren = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.30,
			style: 'padding-top:2px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'Children',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnit.getValue(), userOrganisationUnitGrandChildren.getValue()]);
			}
		});

		userOrganisationUnitGrandChildren = Ext.create('Ext.form.field.Checkbox', {
			columnWidth: 0.40,
			style: 'padding-top:2px; margin-bottom:0',
			boxLabelCls: 'x-form-cb-label-alt1',
			boxLabel: 'Grand children',
			labelWidth: gis.conf.layout.form_label_width,
			handler: function(chb, checked) {
				treePanel.xable([checked, userOrganisationUnit.getValue(), userOrganisationUnitChildren.getValue()]);
			}
		});

		organisationUnitLevel = Ext.create('Ext.form.field.ComboBox', {
			cls: 'gis-combo',
			multiSelect: true,
			style: 'margin-bottom:0',
			width: gis.conf.layout.widget.item_width - 38,
			valueField: 'level',
			displayField: 'name',
			emptyText: GIS.i18n.select_organisation_unit_levels,
			editable: false,
			store: {
				fields: ['id', 'name', 'level'],
				data: gis.init.organisationUnitLevels
			}
		});

		organisationUnitGroup = Ext.create('Ext.form.field.ComboBox', {
			cls: 'gis-combo',
			multiSelect: true,
			style: 'margin-bottom:0',
			width: gis.conf.layout.widget.item_width - 38,
			valueField: 'id',
			displayField: 'name',
			emptyText: GIS.i18n.select_organisation_unit_groups,
			editable: false,
			store: gis.store.organisationUnitGroup
		});

		toolMenu = Ext.create('Ext.menu.Menu', {
			shadow: false,
			showSeparator: false,
			menuValue: 'level',
			clickHandler: function(param) {
				if (!param) {
					return;
				}

				var items = this.items.items;
				this.menuValue = param;

				// Menu item icon cls
				for (var i = 0; i < items.length; i++) {
					if (items[i].setIconCls) {
						if (items[i].param === param) {
							items[i].setIconCls('gis-menu-item-selected');
						}
						else {
							items[i].setIconCls('gis-menu-item-unselected');
						}
					}
				}

				// Gui
				if (param === 'orgunit') {
					userOrganisationUnit.show();
					userOrganisationUnitChildren.show();
					userOrganisationUnitGrandChildren.show();
					organisationUnitLevel.hide();
					organisationUnitGroup.hide();

					if (userOrganisationUnit.getValue() || userOrganisationUnitChildren.getValue()) {
						treePanel.disable();
					}
				}
				else if (param === 'level') {
					userOrganisationUnit.hide();
					userOrganisationUnitChildren.hide();
					userOrganisationUnitGrandChildren.hide();
					organisationUnitLevel.show();
					organisationUnitGroup.hide();
					treePanel.enable();
				}
				else if (param === 'group') {
					userOrganisationUnit.hide();
					userOrganisationUnitChildren.hide();
					userOrganisationUnitGrandChildren.hide();
					organisationUnitLevel.hide();
					organisationUnitGroup.show();
					treePanel.enable();
				}
			},
			items: [
				{
					xtype: 'label',
					text: 'Selection mode',
					style: 'padding:7px 5px 5px 7px; font-weight:bold; border:0 none'
				},
				{
					text: GIS.i18n.select_organisation_units + '&nbsp;&nbsp;',
					param: 'orgunit',
					iconCls: 'gis-menu-item-selected'
				},
				{
					text: 'Select levels' + '&nbsp;&nbsp;',
					param: 'level',
					iconCls: 'gis-menu-item-unselected'
				},
				{
					text: 'Select groups' + '&nbsp;&nbsp;',
					param: 'group',
					iconCls: 'gis-menu-item-unselected'
				}
			],
			listeners: {
				afterrender: function() {
					this.getEl().addCls('gis-btn-menu');
				},
				click: function(menu, item) {
					this.clickHandler(item.param);
				}
			}
		});

		tool = Ext.create('Ext.button.Button', {
			cls: 'gis-button-organisationunitselection',
			iconCls: 'gis-button-icon-gear',
			width: 36,
			height: 24,
			menu: toolMenu
		});

		toolPanel = Ext.create('Ext.panel.Panel', {
			width: 36,
			bodyStyle: 'border:0 none; text-align:right',
			style: 'margin-right:2px',
			items: tool
		});

		periodTypePanel = Ext.create('Ext.panel.Panel', {
			layout: 'hbox',
			items: [
				{
					html: GIS.i18n.period_type + ':',
					width: 100,
					bodyStyle: 'color: #444',
					style: 'padding: 3px 0 0 4px'
				},
				periodType,
				periodPrev,
				periodNext
			]
		});

		methodPanel = Ext.create('Ext.panel.Panel', {
			layout: 'hbox',
			items: [
				{
					html: GIS.i18n.classes_method,
					width: 100,
					bodyStyle: 'color: #444',
					style: 'padding: 3px 0 0 4px'
				},
				classes,
				method
			]
		});

		lowPanel = Ext.create('Ext.panel.Panel', {
			layout: 'hbox',
			items: [
				{
					html: GIS.i18n.low_color_size,
					width: 100,
					bodyStyle: 'color: #444',
					style: 'padding: 3px 0 0 4px'
				},
				colorLow,
				radiusLow
			]
		});

		highPanel = Ext.create('Ext.panel.Panel', {
			layout: 'hbox',
			items: [
				{
					html: GIS.i18n.high_color_size,
					width: 100,
					bodyStyle: 'color: #444',
					style: 'padding: 3px 0 0 4px'
				},
				colorHigh,
				radiusHigh
			]
		});

		// Functions

		reset = function(skipTree) {

			// Item
			layer.item.setValue(false);

			// Layer options
			if (layer.searchWindow) {
				layer.searchWindow.destroy();
				layer.searchWindow = null;
			}
			if (layer.filterWindow) {
				layer.filterWindow.destroy();
				layer.filterWindow = null;
			}
			if (layer.labelWindow) {
				layer.labelWindow.destroy();
				layer.labelWindow = null;
			}

			// Components
			if (!layer.window.isRendered) {
				layer.core.view = null;
				return;
			}

			valueType.reset();
			valueTypeToggler(dimConf.indicator.objectName);

			indicatorGroup.clearValue();
			indicator.clearValue();
			indicator.store.removeAll();

			dataElementGroup.clearValue();
			dataElement.clearValue();
			dataElement.store.removeAll();

			dataSet.clearValue();
			dataSet.store.removeAll();

			periodType.clearValue();
			period.clearValue();
			period.store.removeAll();

			legendType.reset();
			legendTypeToggler(gis.conf.finals.widget.legendtype_automatic);
			legendSet.clearValue();
			legendSet.store.removeAll();

			classes.reset();
			method.reset();
			colorLow.reset();
			colorHigh.reset();
			radiusLow.reset();
			radiusHigh.reset();

			toolMenu.clickHandler(toolMenu.menuValue);

			if (!skipTree) {
				treePanel.reset();
			}

			userOrganisationUnit.setValue(false);
			userOrganisationUnitChildren.setValue(false);
			userOrganisationUnitGrandChildren.setValue(false);

			organisationUnitLevel.clearValue();
			organisationUnitGroup.clearValue();
		};

		setGui = function(view) {
			var dxDim = view.columns[0],
				peDim = view.filters[0],
				ouDim = view.rows[0],
				vType = dxDim.dimension === dimConf.operand.objectName ? dimConf.dataElement.objectName : dxDim.dimension,
				lType = Ext.isObject(view.legendSet) && Ext.isString(view.legendSet.id) ? gis.conf.finals.widget.legendtype_predefined : gis.conf.finals.widget.legendtype_automatic,
				objectNameCmpMap = {},
				isOu = false,
				isOuc = false,
				isOugc = false,
				levels = [],
				groups = [],
				setLayerGui,
				setWidgetGui;

			objectNameCmpMap[dimConf.indicator.objectName] = indicator;
			objectNameCmpMap[dimConf.dataElement.objectName] = dataElement;
			objectNameCmpMap[dimConf.operand.objectName] = dataElement;
			objectNameCmpMap[dimConf.dataSet.objectName] = dataSet;

			setWidgetGui = function() {

				// Components
				if (!layer.window.isRendered) {
					return;
				}

				// Reset
				reset(true);

				// Value type
				valueType.setValue(vType);
				valueTypeToggler(vType);

				if (vType === dimConf.dataElement.objectName) {
					dataElementDetailLevel.setValue(dxDim.dimension);
				}

				// Data
				objectNameCmpMap[dxDim.dimension].store.add(dxDim.items[0]);
				objectNameCmpMap[dxDim.dimension].setValue(dxDim.items[0].id);

				// Period
				period.store.add(gis.conf.period.relativePeriodsMap[peDim.items[0].id] ? gis.conf.period.relativePeriodsMap[peDim.items[0].id] : peDim.items[0]);
				period.setValue(peDim.items[0].id);

				// Legend
				legendType.setValue(lType);
				legendTypeToggler(lType);

				if (lType === gis.conf.finals.widget.legendtype_automatic) {
					classes.setValue(view.classes);
					method.setValue(view.method);
					colorLow.setValue(view.colorLow);
					colorHigh.setValue(view.colorHigh);
					radiusLow.setValue(view.radiusLow);
					radiusHigh.setValue(view.radiusHigh);
				}
				else if (lType === gis.conf.finals.widget.legendtype_predefined) {
					legendSet.store.add(view.legendSet);
					legendSet.setValue(view.legendSet.id);
				}

				// Organisation units
				for (var i = 0, item; i < ouDim.items.length; i++) {
					item = ouDim.items[i];

					if (item.id === 'USER_ORGUNIT') {
						isOu = true;
					}
					else if (item.id === 'USER_ORGUNIT_CHILDREN') {
						isOuc = true;
					}
					else if (item.id === 'USER_ORGUNIT_GRANDCHILDREN') {
						isOugc = true;
					}
					else if (item.id.substr(0,5) === 'LEVEL') {
						levels.push(parseInt(item.id.split('-')[1]));
					}
					else if (item.id.substr(0,8) === 'OU_GROUP') {
						groups.push(parseInt(item.id.split('-')[1]));
					}
				}

				if (levels.length) {
					toolMenu.clickHandler('level');
					organisationUnitLevel.setValue(levels);
				}
				else if (groups.length) {
					toolMenu.clickHandler('group');
					organisationUnitGroup.setValue(groups);
				}
				else {
					toolMenu.clickHandler('orgunit');
					userOrganisationUnit.setValue(isOu);
					userOrganisationUnitChildren.setValue(isOuc);
					userOrganisationUnitGrandChildren.setValue(isOugc);
				}

				treePanel.selectGraphMap(view.parentGraphMap);
			}();

			setLayerGui = function() {

				// Layer item
				layer.item.setValue(!view.hidden, view.opacity);

				// Layer menu
				layer.menu.enableItems();

				// Filter
				if (layer.filterWindow && layer.filterWindow.isVisible()) {
					layer.filterWindow.filter();
				}
			}();
		};

		getView = function(config) {
			var vType = valueType.getValue() === dimConf.dataElement.objectName ? dataElementDetailLevel.getValue() : valueType.getValue(),
				objectNameCmpMap = {},
				view = {};

            view.layer = layer.id;

			objectNameCmpMap[dimConf.indicator.objectName] = indicator;
			objectNameCmpMap[dimConf.dataElement.objectName] = dataElement;
			objectNameCmpMap[dimConf.operand.objectName] = dataElement;
			objectNameCmpMap[dimConf.dataSet.objectName] = dataSet;

            if (objectNameCmpMap[vType].getValue()) {
                view.columns = [{
                    dimension: vType,
                    items: [{
                        id: objectNameCmpMap[vType].getValue()
                    }]
                }];
            }

            if (treePanel.getDimension()) {
                view.rows = [treePanel.getDimension()];
            }

            if (period.getValue()) {
                view.filters = [{
                    dimension: dimConf.period.objectName,
                    items: [{
                        id: period.getValue()
                    }]
                }];
            }

			view.classes = parseInt(classes.getValue());
			view.method = parseInt(method.getValue());
			view.colorLow = colorLow.getValue();
			view.colorHigh = colorHigh.getValue();
			view.radiusLow = parseInt(radiusLow.getValue());
			view.radiusHigh = parseInt(radiusHigh.getValue());
			view.opacity = layer.item.getOpacity();

			if (legendType.getValue() === gis.conf.finals.widget.legendtype_predefined && legendSet.getValue()) {
				view.legendSet = {
					id: legendSet.getValue()
				};
			}

			return gis.api.layout.Layout(view);
		};

		panel = Ext.create('Ext.panel.Panel', {
			map: layer.map,
			layer: layer,
			menu: layer.menu,

			reset: reset,
			setGui: setGui,
			getView: getView,
			getParentGraphMap: function() {
				return treePanel.getParentGraphMap();
			},

			infrastructuralDataElementValuesStore: infrastructuralDataElementValuesStore,

			cls: 'gis-form-widget el-border-0',
			border: false,
			items: [
				{
					xtype: 'form',
					cls: 'el-border-0',
					items: [
						{
							html: GIS.i18n.data_options,
							cls: 'gis-form-subtitle-first'
						},
						valueType,
						indicatorGroup,
						indicator,
						dataElementGroup,
						dataElementPanel,
						dataSet,
						periodTypePanel,
						period,
						{
							html: GIS.i18n.legend_options,
							cls: 'gis-form-subtitle'
						},
						legendType,
						legendSet,
						methodPanel,
						lowPanel,
						highPanel,
						{
							html: GIS.i18n.organisation_units,
							cls: 'gis-form-subtitle'
						},
						{
							layout: 'column',
							bodyStyle: 'border:0 none',
							style: 'padding-bottom:2px',
							items: [
								toolPanel,
								{
									width: gis.conf.layout.widget.item_width - 38,
									layout: 'column',
									bodyStyle: 'border:0 none',
									items: [
										userOrganisationUnit,
										userOrganisationUnitChildren,
										userOrganisationUnitGrandChildren,
										organisationUnitLevel,
										organisationUnitGroup
									]
								}
							]
						},
						treePanel
					]
				}
			],
			listeners: {
				render: function() {
					toolMenu.clickHandler('level');
				}
			}
		});

		//createSelectHandlers();

		return panel;
	};

	createViewport = function() {
		var centerRegion,
			eastRegion,
			downloadButton,
			shareButton,
			defaultButton,
			layersPanel,
			resizeButton,
			viewport,
			onRender,
			afterRender;

		resizeButton = Ext.create('Ext.button.Button', {
			text: '>>>',
			handler: function() {
				eastRegion.toggleCollapse();
			}
		});

		defaultButton = Ext.create('Ext.button.Button', {
			text: GIS.i18n.map,
			iconCls: 'gis-button-icon-map',
			toggleGroup: 'module',
			pressed: true,
			handler: function() {
				if (!this.pressed) {
					this.toggle();
				}
			}
		});

		interpretationItem = Ext.create('Ext.menu.Item', {
			text: 'Write interpretation' + '&nbsp;&nbsp;',
			iconCls: 'gis-menu-item-tablelayout',
			disabled: true,
			xable: function() {
				if (gis.map) {
					this.enable();
				}
				else {
					this.disable();
				}
			},
			handler: function() {
				if (viewport.interpretationWindow) {
					viewport.interpretationWindow.destroy();
					viewport.interpretationWindow = null;
				}

				viewport.interpretationWindow = GIS.app.InterpretationWindow();
				viewport.interpretationWindow.show();
			}
		});

		pluginItem = Ext.create('Ext.menu.Item', {
			text: 'Embed as plugin' + '&nbsp;&nbsp;',
			iconCls: 'gis-menu-item-datasource',
			disabled: true,
			xable: function() {
				if (gis.util.map.hasVisibleFeatures()) {
					this.enable();
				}
				else {
					this.disable();
				}
			},
			handler: function() {
				var textArea,
					window,
					text = '',
                    el = 'table1',
                    layout = gis.util.map.map2plugin(gis.util.layout.getPluginConfig());

                layout.el = el;

                if (layout.mapViews) {
                    for (var i = 0, view; i < layout.mapViews.length; i++) {
                        view = layout.mapViews[i];

                        if (view.legendSet) {
                            delete view.legendSet.bounds;
                            delete view.legendSet.colors;
                            delete view.legendSet.names;
                        }
                    }
                }

				text += '<html>\n<head>\n';
				text += '<link rel="stylesheet" href="http://dhis2-cdn.org/v215/ext/resources/css/ext-plugin-gray.css" />\n';
				text += '<script src="http://dhis2-cdn.org/v215/ext/ext-all.js"></script>\n';
				text += '<script src="http://dhis2-cdn.org/v215/plugin/table.js"></script>\n';
				text += '</head>\n\n<body>\n';
				text += '<div id="' + el + '"></div>\n\n';
				text += '<script>\n\n';
				text += 'Ext.onReady(function() {\n\n';
				text += 'DHIS.getMap(' + JSON.stringify(layout, null, 2) + ');\n\n';
				text += '});\n\n';
				text += '</script>\n\n';
				text += '</body>\n</html>';

				textArea = Ext.create('Ext.form.field.TextArea', {
					width: 700,
					height: 400,
					readOnly: true,
					cls: 'ns-textarea monospaced',
					value: text
				});

				window = Ext.create('Ext.window.Window', {
					title: 'Plugin configuration',
					layout: 'fit',
					modal: true,
					resizable: false,
					items: textArea,
					destroyOnBlur: true,
					bbar: [
						'->',
						{
							text: 'Select',
							handler: function() {
								textArea.selectText();
							}
						}
					],
					listeners: {
						show: function(w) {
							this.setPosition(215, 33);
						}
					}
				});

				window.show();
			}
		});

		shareButton = Ext.create('Ext.button.Button', {
			text: GIS.i18n.share,
            disabled: true,
			xableItems: function() {
				interpretationItem.xable();
				pluginItem.xable();
			},
			menu: {
				cls: 'gis-menu',
				shadow: false,
				showSeparator: false,
				items: [
					interpretationItem,
					pluginItem
				],
				listeners: {
					afterrender: function() {
						this.getEl().addCls('gis-toolbar-btn-menu');
					},
					show: function() {
						shareButton.xableItems();
					}
				}
			}
		});

		centerRegion = new GeoExt.panel.Map({
			region: 'center',
			map: gis.olmap,
			cmp: {
				tbar: {}
			},
			tbar: {
				defaults: {
					height: 26
				},
				items: function() {
					var a = [];
					a.push({
						iconCls: 'gis-btn-icon-' + gis.layer.event.id,
						menu: gis.layer.event.menu,
						tooltip: GIS.i18n.event_layer,
						width: 26
					});
					a.push({
						iconCls: 'gis-btn-icon-' + gis.layer.facility.id,
						menu: gis.layer.facility.menu,
						tooltip: GIS.i18n.symbol_layer,
						width: 26
					});
					a.push({
						iconCls: 'gis-btn-icon-' + gis.layer.boundary.id,
						menu: gis.layer.boundary.menu,
						tooltip: GIS.i18n.boundary_layer,
						width: 26
					});
					a.push({
						iconCls: 'gis-btn-icon-' + gis.layer.thematic1.id,
						menu: gis.layer.thematic1.menu,
						tooltip: GIS.i18n.thematic_layer + ' 1',
						width: 26
					});
					a.push({
						iconCls: 'gis-btn-icon-' + gis.layer.thematic2.id,
						menu: gis.layer.thematic2.menu,
						tooltip: GIS.i18n.thematic_layer + ' 2',
						width: 26
					});
					a.push({
						iconCls: 'gis-btn-icon-' + gis.layer.thematic3.id,
						menu: gis.layer.thematic3.menu,
						tooltip: GIS.i18n.thematic_layer + ' 3',
						width: 26
					});
					a.push({
						iconCls: 'gis-btn-icon-' + gis.layer.thematic4.id,
						menu: gis.layer.thematic4.menu,
						tooltip: GIS.i18n.thematic_layer + ' 4',
						width: 26
					});
					a.push({
						text: GIS.i18n.favorites,
						menu: {},
						handler: function() {
							if (viewport.favoriteWindow && viewport.favoriteWindow.destroy) {
								viewport.favoriteWindow.destroy();
							}

							viewport.favoriteWindow = GIS.app.FavoriteWindow();
							viewport.favoriteWindow.show();
						}
					});

					if (gis.init.user.isAdmin) {
						a.push({
							text: GIS.i18n.legend,
							menu: {},
							handler: function() {
								if (viewport.legendSetWindow && viewport.legendSetWindow.destroy) {
									viewport.legendSetWindow.destroy();
								}

								viewport.legendSetWindow = GIS.app.LegendSetWindow();
								viewport.legendSetWindow.show();
							}
						});
					}
					a.push({
						xtype: 'tbseparator',
						height: 18,
						style: 'border-color: transparent #d1d1d1 transparent transparent; margin-right: 4px',
					});
					a.push({
						text: GIS.i18n.download,
						menu: {},
						disabled: true,
						handler: function() {
							if (viewport.downloadWindow && viewport.downloadWindow.destroy) {
								viewport.downloadWindow.destroy();
							}

							viewport.downloadWindow = GIS.app.DownloadWindow();
							viewport.downloadWindow.show();
						},
						xable: function() {
							if (gis.util.map.hasVisibleFeatures()) {
								this.enable();
							}
							else {
								this.disable();
							}
						},
						listeners: {
							added: function() {
								downloadButton = this;
							}
						}
					});
					a.push(shareButton);
					a.push('->');

					a.push({
						text: GIS.i18n.table,
						iconCls: 'gis-button-icon-table',
						toggleGroup: 'module',
						menu: {},
						handler: function(b) {
							b.menu = Ext.create('Ext.menu.Menu', {
								closeAction: 'destroy',
								shadow: false,
								showSeparator: false,
								items: [
									{
										text: GIS.i18n.go_to_pivot_tables + '&nbsp;&nbsp;', //i18n
										cls: 'gis-menu-item-noicon',
										handler: function() {
											window.location.href = gis.init.contextPath + '/dhis-web-pivot/app/index.html';
										}
									},
									'-',
									{
										text: GIS.i18n.open_this_map_as_table + '&nbsp;&nbsp;', //i18n
										cls: 'gis-menu-item-noicon',
										disabled: !(GIS.isSessionStorage && gis.util.layout.getAnalytical()),
										handler: function() {
											if (GIS.isSessionStorage) {
												gis.util.layout.setSessionStorage('analytical', gis.util.layout.getAnalytical(), gis.init.contextPath + '/dhis-web-pivot/app/index.html?s=analytical');
											}
										}
									},
									{
										text: GIS.i18n.open_last_table + '&nbsp;&nbsp;', //i18n
										cls: 'gis-menu-item-noicon',
										disabled: !(GIS.isSessionStorage && JSON.parse(sessionStorage.getItem('dhis2')) && JSON.parse(sessionStorage.getItem('dhis2'))['table']),
										handler: function() {
											window.location.href = gis.init.contextPath + '/dhis-web-pivot/app/index.html?s=table';
										}
									}
								],
								listeners: {
									show: function() {
										gis.util.gui.window.setAnchorPosition(b.menu, b);
									},
									hide: function() {
										b.menu.destroy();
										defaultButton.toggle();
									},
									destroy: function(m) {
										b.menu = null;
									}
								}
							});

							b.menu.show();
						}
					});

					a.push({
						text: GIS.i18n.chart,
						iconCls: 'gis-button-icon-chart',
						toggleGroup: 'module',
						menu: {},
						handler: function(b) {
							b.menu = Ext.create('Ext.menu.Menu', {
								closeAction: 'destroy',
								shadow: false,
								showSeparator: false,
								items: [
									{
										text: GIS.i18n.go_to_charts + '&nbsp;&nbsp;', //i18n
										cls: 'gis-menu-item-noicon',
										handler: function() {
											window.location.href = gis.init.contextPath + '/dhis-web-visualizer/app/index.html';
										}
									},
									'-',
									{
										text: GIS.i18n.open_this_map_as_chart + '&nbsp;&nbsp;', //i18n
										cls: 'gis-menu-item-noicon',
										disabled: !GIS.isSessionStorage || !gis.util.layout.getAnalytical(),
										handler: function() {
											if (GIS.isSessionStorage) {
												gis.util.layout.setSessionStorage('analytical', gis.util.layout.getAnalytical(), gis.init.contextPath + '/dhis-web-visualizer/app/index.html?s=analytical');
											}
										}
									},
									{
										text: GIS.i18n.open_last_chart + '&nbsp;&nbsp;', //i18n
										cls: 'gis-menu-item-noicon',
										disabled: !(GIS.isSessionStorage && JSON.parse(sessionStorage.getItem('dhis2')) && JSON.parse(sessionStorage.getItem('dhis2'))['chart']),
										handler: function() {
											window.location.href = gis.init.contextPath + '/dhis-web-visualizer/app/index.html?s=chart';
										}
									}
								],
								listeners: {
									show: function() {
										gis.util.gui.window.setAnchorPosition(b.menu, b);
									},
									hide: function() {
										b.menu.destroy();
										defaultButton.toggle();
									},
									destroy: function(m) {
										b.menu = null;
									}
								}
							});

							b.menu.show();
						}
					});

					a.push(defaultButton);

					a.push({
						xtype: 'tbseparator',
						height: 18,
						style: 'border-color: transparent #d1d1d1 transparent transparent; margin-right: 6px; margin-left: 3px',
					});

					a.push({
						xtype: 'button',
						text: GIS.i18n.home,
						handler: function() {
							window.location.href = '../../dhis-web-commons-about/redirect.action';
						}
					});

					a.push(resizeButton);

					return a;
				}()
			}
		});

		eastRegion = Ext.create('Ext.panel.Panel', {
			region: 'east',
			layout: 'anchor',
			width: 200,
			preventHeader: true,
			collapsible: true,
			collapseMode: 'mini',
			items: function() {
				var a = [];

				layersPanel = GIS.app.LayersPanel();

				a.push({
					title: GIS.i18n.layer_stack_transparency,
					bodyStyle: 'padding: 3px 2px 2px 5px; border:0 none; border-bottom: 1px solid #d0d0d0; border-top: 1px solid #d0d0d0',
					style: 'border:0 none',
					items: layersPanel,
					collapsible: true,
					animCollapse: false
				});

				a.push({
					title: GIS.i18n.facility_layer_legend,
					bodyStyle: 'padding: 5px 6px 3px; border: 0 none; border-bottom: 1px solid #d0d0d0; border-top: 1px solid #d0d0d0',
					collapsible: true,
					collapsed: true,
					animCollapse: false,
					listeners: {
						added: function() {
							gis.layer.facility.legendPanel = this;
						}
					}
				});

				a.push({
					title: GIS.i18n.thematic_layer_1_legend,
					bodyStyle: 'padding: 4px 6px 6px; border: 0 none; border-bottom: 1px solid #d0d0d0; border-top: 1px solid #d0d0d0',
					collapsible: true,
					collapsed: true,
					animCollapse: false,
					listeners: {
						added: function() {
							gis.layer.thematic1.legendPanel = this;
						}
					}
				});

				a.push({
					title: GIS.i18n.thematic_layer_2_legend,
					bodyStyle: 'padding: 4px 6px 6px; border: 0 none; border-bottom: 1px solid #d0d0d0; border-top: 1px solid #d0d0d0',
					collapsible: true,
					collapsed: true,
					animCollapse: false,
					listeners: {
						added: function() {
							gis.layer.thematic2.legendPanel = this;
						}
					}
				});

				a.push({
					title: GIS.i18n.thematic_layer_3_legend,
					bodyStyle: 'padding: 4px 6px 6px; border: 0 none; border-bottom: 1px solid #d0d0d0',
					collapsible: true,
					collapsed: true,
					animCollapse: false,
					listeners: {
						added: function() {
							gis.layer.thematic3.legendPanel = this;
						}
					}
				});

				a.push({
					title: GIS.i18n.thematic_layer_4_legend,
					bodyStyle: 'padding: 4px 6px 6px; border: 0 none',
					collapsible: true,
					collapsed: true,
					animCollapse: false,
					listeners: {
						added: function() {
							gis.layer.thematic4.legendPanel = this;
						}
					}
				});

				return a;
			}(),
			listeners: {
				collapse: function() {
					resizeButton.setText('<<<');
				},
				expand: function() {
					resizeButton.setText('>>>');
				}
			}
		});

		onRender = function(vp) {
			gis.olmap.mask = Ext.create('Ext.LoadMask', vp.getEl(), {
				msg: 'Loading'
			});
		};

		afterRender = function() {

			// Map tools
			Ext.query('.zoomInButton')[0].innerHTML = '<img src="images/zoomin_24.png" />';
			Ext.query('.zoomOutButton')[0].innerHTML = '<img src="images/zoomout_24.png" />';
			Ext.query('.zoomVisibleButton')[0].innerHTML = '<img src="images/zoomvisible_24.png" />';
			Ext.query('.measureButton')[0].innerHTML = '<img src="images/measure_24.png" />';

			gis.olmap.events.register('click', null, function(e) {
				if (gis.olmap.relocate.active) {
					var el = Ext.query('#mouseposition')[0],
                        id = gis.olmap.relocate.feature.attributes.id,
						coordinates = '[' + el.childNodes[1].data + ',' + el.childNodes[3].data + ']',
						center = gis.viewport.centerRegion;

                    Ext.Ajax.request({
                        url: gis.init.contextPath + '/api/organisationUnits/' + id + '.json?links=false',
                        success: function(r) {
                            var orgUnit = Ext.decode(r.responseText);

                            orgUnit.coordinates = coordinates;

                            Ext.Ajax.request({
                                url: gis.init.contextPath + '/api/metaData?preheatCache=false',
                                method: 'POST',
                                headers: {'Content-Type': 'application/json'},
                                params: Ext.encode({organisationUnits: [orgUnit]}),
                                success: function(r) {
                                    gis.olmap.relocate.active = false;
                                    gis.olmap.relocate.window.destroy();
                                    gis.olmap.relocate.feature.move({x: parseFloat(e.clientX - center.x), y: parseFloat(e.clientY - 28)});
                                    gis.olmap.getViewport().style.cursor = 'auto';

                                    console.log(gis.olmap.relocate.feature.attributes.name + ' relocated to ' + coordinates);
                                }
                            });
                        }
                    });
				}
			});

			// Favorite
			var id = gis.util.url.getUrlParam('id'),
				session = gis.util.url.getUrlParam('s'),
				base = gis.util.url.getUrlParam('base'),
				layout;

			if (id) {
				gis.map = {
					id: id
				};
				GIS.core.MapLoader(gis).load();
			}
			else if (Ext.isString(session) && GIS.isSessionStorage && Ext.isObject(JSON.parse(sessionStorage.getItem('dhis2'))) && session in JSON.parse(sessionStorage.getItem('dhis2'))) {
				layout = gis.api.layout.Layout(JSON.parse(sessionStorage.getItem('dhis2'))[session]);

				if (layout) {
					GIS.core.MapLoader(gis).load([layout]);
				}
			}

			if (base.length) {

				// hide base layer
				if (Ext.Array.contains(['false', 'none', 'no', 'off'], base)) {
					for (var i = 0, item; i < layersPanel.layerItems.length; i++) {
						item = layersPanel.layerItems[i];

						if (item.layer.layerType === gis.conf.finals.layer.type_base && item.layer.visibility) {
							item.disableItem();
						}
					}
				}
				else {
					var isEnabled = false;

					for (var i = 0, item; i < layersPanel.layerItems.length; i++) {
						item = layersPanel.layerItems[i];

						if (item.layer.layerType === gis.conf.finals.layer.type_base) {
							if (base === item.layer.id) {
								item.enableItem();
								isEnabled = true;
							}
							else {
								item.disableItem();
							}
						}
					}

					if (!isEnabled) {
						layersPanel.layerItems[layersPanel.layerItems.length - 1].enableItem();
					}
				}
			}
		};

		viewport = Ext.create('Ext.container.Viewport', {
			id: 'viewport',
			layout: 'border',
			eastRegion: eastRegion,
			centerRegion: centerRegion,
			downloadButton: downloadButton,
			shareButton: shareButton,
			layersPanel: layersPanel,
			items: [
				centerRegion,
				eastRegion
			],
			listeners: {
				render: function() {
					onRender(this);
				},
				afterrender: function() {
					afterRender();
				}
			}
		});

		return viewport;
	};

	initialize = function() {
		var requests = [],
			callbacks = 0,
			init = {
				user: {},
				systemSettings: {},
				extensions: {}
			},
			fn;

		fn = function() {
			if (++callbacks === requests.length) {
				gis = GIS.core.getInstance(init);

				GIS.app.createExtensions();

				GIS.app.extendInstance(gis);

				gis.viewport = createViewport();
			}
		};

        Ext.Ajax.request({
            url: 'manifest.webapp',
			success: function(r) {
				init.contextPath = Ext.decode(r.responseText).activities.dhis.href;

				Ext.Ajax.request({
					url: 'i18n.json',
					success: function(r) {
						var i18nArray = Ext.decode(r.responseText);

						Ext.Ajax.request({
							url: init.contextPath + '/api/system/info.json',
							success: function(r) {
								init.contextPath = Ext.decode(r.responseText).contextPath || init.contextPath;

								// i18n
								requests.push({
									url: init.contextPath + '/api/i18n?package=org.hisp.dhis.mapping',
									method: 'POST',
									headers: {
										'Content-Type': 'application/json',
										'Accepts': 'application/json'
									},
									params: Ext.encode(i18nArray),
									success: function(r) {
										GIS.i18n = Ext.decode(r.responseText);
										fn();
									}
								});

								// root nodes
								requests.push({
									url: init.contextPath + '/api/organisationUnits.json?userDataViewFallback=true&paging=false&fields=id,name,children[id,name]',
									success: function(r) {
										init.rootNodes = Ext.decode(r.responseText).organisationUnits || [];
										fn();
									}
								});

								// organisation unit levels
								requests.push({
									url: init.contextPath + '/api/organisationUnitLevels.json?fields=id,name,level&paging=false',
									success: function(r) {
										init.organisationUnitLevels = Ext.decode(r.responseText).organisationUnitLevels || [];

										if (!init.organisationUnitLevels.length) {
											alert('No organisation unit levels');
										}

										fn();
									}
								});

								// user orgunits and children
								requests.push({
									url: init.contextPath + '/api/organisationUnits.json?userOnly=true&fields=id,name,children[id,name]&paging=false',
									success: function(r) {
										var organisationUnits = Ext.decode(r.responseText).organisationUnits || [],
											ou = [],
											ouc = [];

										if (organisationUnits.length) {
											for (var i = 0, org; i < organisationUnits.length; i++) {
												org = organisationUnits[i];

												ou.push(org.id);

                                                if (org.children) {
                                                    ouc = Ext.Array.clean(ouc.concat(Ext.Array.pluck(org.children, 'id') || []));
                                                }
											}

											init.user = init.user || {};
											init.user.ou = ou;
                                            init.user.ouc = ouc;
										}
										else {
											alert('User is not assigned to any organisation units');
										}

										fn();
									}
								});

								// admin
								requests.push({
									url: init.contextPath + '/api/me/authorization/F_GIS_ADMIN',
									success: function(r) {
										init.user.isAdmin = (r.responseText === 'true');
										fn();
									}
								});

								// indicator groups
								requests.push({
									url: init.contextPath + '/api/indicatorGroups.json?fields=id,name&paging=false',
									success: function(r) {
										init.indicatorGroups = Ext.decode(r.responseText).indicatorGroups || [];
										fn();
									}
								});

								// data element groups
								requests.push({
									url: init.contextPath + '/api/dataElementGroups.json?fields=id,name&paging=false',
									success: function(r) {
										init.dataElementGroups = Ext.decode(r.responseText).dataElementGroups || [];
										fn();
									}
								});

                                // infrastructural data element group
								requests.push({
									url: init.contextPath + '/api/configuration/infrastructuralDataElements.json',
									success: function(r) {
										var obj = Ext.decode(r.responseText);

										init.systemSettings.infrastructuralDataElementGroup = Ext.isObject(obj) ? obj : null;
										fn();
									}
								});

                                // infrastructural period type
								requests.push({
									url: init.contextPath + '/api/configuration/infrastructuralPeriodType.json',
									success: function(r) {
										var obj = Ext.decode(r.responseText);

										init.systemSettings.infrastructuralPeriodType = Ext.isObject(obj) ? obj : null;
										fn();
									}
								});

								for (var i = 0; i < requests.length; i++) {
									Ext.Ajax.request(requests[i]);
								}
                            }
                        });
                    }
                });
            }
        });
	}();
});
