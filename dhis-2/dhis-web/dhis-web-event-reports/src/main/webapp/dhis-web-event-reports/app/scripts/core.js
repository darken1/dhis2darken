Ext.onReady( function() {

	// ext config
	Ext.Ajax.method = 'GET';

	// namespace
	ER = {};
	var NS = ER;

	NS.instances = [];
	NS.i18n = {};
	NS.isDebug = false;
	NS.isSessionStorage = ('sessionStorage' in window && window['sessionStorage'] !== null);

	NS.getCore = function(init) {
        var conf = {},
            api = {},
            support = {},
            service = {},
            web = {},
            dimConf;

		// conf
		(function() {
			conf.finals = {
				dimension: {
					data: {
						value: 'data',
						name: NS.i18n.data,
						dimensionName: 'dx',
						objectName: 'dx',
						warning: {
							filter: '...'//NS.i18n.wm_multiple_filter_ind_de
						}
					},
					category: {
						name: NS.i18n.categories,
						dimensionName: 'co',
						objectName: 'co',
					},
					indicator: {
						value: 'indicators',
						name: NS.i18n.indicators,
						dimensionName: 'dx',
						objectName: 'in'
					},
					dataElement: {
						value: 'dataElements',
						name: NS.i18n.data_elements,
						dimensionName: 'dx',
						objectName: 'de'
					},
					operand: {
						value: 'operand',
						name: 'Operand',
						dimensionName: 'dx',
						objectName: 'dc'
					},
					dataSet: {
						value: 'dataSets',
						name: NS.i18n.data_sets,
						dimensionName: 'dx',
						objectName: 'ds'
					},
					period: {
						value: 'period',
						name: NS.i18n.periods,
						dimensionName: 'pe',
						objectName: 'pe'
					},
					fixedPeriod: {
						value: 'periods'
					},
					relativePeriod: {
						value: 'relativePeriods',
						name: NS.i18n.relative_periods
					},
                    startEndDate: {
                        value: 'dates',
                        name: NS.i18n.start_end_dates
                    },
					organisationUnit: {
						value: 'organisationUnits',
						name: NS.i18n.organisation_units,
						dimensionName: 'ou',
						objectName: 'ou'
					},
					dimension: {
						value: 'dimension'
						//objectName: 'di'
					},
					value: {
						value: 'value'
					}
				},
				root: {
					id: 'root'
				}
			};

			dimConf = conf.finals.dimension;

			dimConf.objectNameMap = {};
			dimConf.objectNameMap[dimConf.data.objectName] = dimConf.data;
			dimConf.objectNameMap[dimConf.indicator.objectName] = dimConf.indicator;
			dimConf.objectNameMap[dimConf.dataElement.objectName] = dimConf.dataElement;
			dimConf.objectNameMap[dimConf.operand.objectName] = dimConf.operand;
			dimConf.objectNameMap[dimConf.dataSet.objectName] = dimConf.dataSet;
			dimConf.objectNameMap[dimConf.category.objectName] = dimConf.category;
			dimConf.objectNameMap[dimConf.period.objectName] = dimConf.period;
			dimConf.objectNameMap[dimConf.organisationUnit.objectName] = dimConf.organisationUnit;
			dimConf.objectNameMap[dimConf.dimension.objectName] = dimConf.dimension;

			conf.period = {
				periodTypes: [
					{id: 'Daily', name: NS.i18n.daily},
					{id: 'Weekly', name: NS.i18n.weekly},
					{id: 'Monthly', name: NS.i18n.monthly},
					{id: 'BiMonthly', name: NS.i18n.bimonthly},
					{id: 'Quarterly', name: NS.i18n.quarterly},
					{id: 'SixMonthly', name: NS.i18n.sixmonthly},
					{id: 'Yearly', name: NS.i18n.yearly},
					{id: 'FinancialOct', name: NS.i18n.financial_oct},
					{id: 'FinancialJuly', name: NS.i18n.financial_july},
					{id: 'FinancialApril', name: NS.i18n.financial_april}
				]
			};

			conf.layout = {
				west_width: 452,
				west_fill: 2,
				west_fill_accordion_indicator: 59,
				west_fill_accordion_dataelement: 59,
				west_fill_accordion_dataset: 33,
				west_fill_accordion_period: 296,
				west_fill_accordion_organisationunit: 62,
				west_maxheight_accordion_indicator: 400,
				west_maxheight_accordion_dataelement: 400,
				west_maxheight_accordion_dataset: 400,
				west_maxheight_accordion_period: 513,
				west_maxheight_accordion_organisationunit: 900,
				west_maxheight_accordion_group: 340,
				west_maxheight_accordion_options: 449,
				west_scrollbarheight_accordion_indicator: 300,
				west_scrollbarheight_accordion_dataelement: 300,
				west_scrollbarheight_accordion_dataset: 300,
				west_scrollbarheight_accordion_period: 450,
				west_scrollbarheight_accordion_organisationunit: 450,
				west_scrollbarheight_accordion_group: 300,
				east_tbar_height: 31,
				east_gridcolumn_height: 30,
				form_label_width: 55,
				window_favorite_ypos: 100,
				window_confirm_width: 250,
				window_share_width: 500,
				grid_favorite_width: 420,
				grid_row_height: 27,
				treepanel_minheight: 135,
				treepanel_maxheight: 400,
				treepanel_fill_default: 310,
				treepanel_toolbar_menu_width_group: 140,
				treepanel_toolbar_menu_width_level: 120,
				multiselect_minheight: 100,
				multiselect_maxheight: 250,
				multiselect_fill_default: 345,
				multiselect_fill_reportingrates: 315
			};

			conf.report = {
				digitGroupSeparator: {
					'comma': ',',
					'space': ' '
				},
				displayDensity: {
					'compact': '3px',
					'normal': '5px',
					'comfortable': '10px',
				},
				fontSize: {
					'small': '10px',
					'normal': '11px',
					'large': '13px'
				}
			};

            conf.url = {
                analysisFields: [
                    '*',
                    'program[id,name]',
                    'programStage[id,name]',
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
                    '!organisationUnits'
                ]
            };
		}());

		// api
		(function() {
			api.layout = {};

			api.layout.Record = function(config) {
				var config = Ext.clone(config);

				// id: string

				return function() {
					if (!Ext.isObject(config)) {
						console.log('Record: config is not an object: ' + config);
						return;
					}

					if (!Ext.isString(config.id)) {
						alert('Record: id is not text: ' + config);
						return;
					}

					//config.id = config.id.replace('.', '-');

					return config;
				}();
			};

			api.layout.Dimension = function(config) {
				var config = Ext.clone(config);

				// dimension: string

				// items: [Record]

				return function() {
					if (!Ext.isObject(config)) {
						console.log('Dimension: config is not an object: ' + config);
						return;
					}

					if (!Ext.isString(config.dimension)) {
						console.log('Dimension: name is not a string: ' + config);
						return;
					}

					if (config.dimension !== conf.finals.dimension.category.objectName) {
						var records = [];

						//if (!Ext.isArray(config.items)) {
							//console.log('Dimension: items is not an array: ' + config);
							//return;
						//}

						//for (var i = 0; i < config.items.length; i++) {
							//records.push(api.layout.Record(config.items[i]));
						//}

						//config.items = Ext.Array.clean(records);

						//if (!config.items.length) {
							//console.log('Dimension: has no valid items: ' + config);
							//return;
						//}
					}

					return config;
				}();
			};

			api.layout.Layout = function(config) {
				var config = Ext.clone(config),
					layout = {},
					getValidatedDimensionArray,
					validateSpecialCases;

				// columns: [Dimension]

				// rows: [Dimension]

				// filters: [Dimension]

				// showTotals: boolean (true)

				// showSubTotals: boolean (true)

				// hideEmptyRows: boolean (false)

                // countType: string ('events') - 'events', 'tracked_entity_instance'

                // aggregationType: string ('default') - 'default', 'count', 'sum'

				// showHierarchy: boolean (false)

				// displayDensity: string ('normal') - 'compact', 'normal', 'comfortable'

				// fontSize: string ('normal') - 'small', 'normal', 'large'

				// digitGroupSeparator: string ('space') - 'none', 'comma', 'space'

				// legendSet: object

				// parentGraphMap: object

				// sorting: transient object

				// reportingPeriod: boolean (false) //report tables only

				// organisationUnit: boolean (false) //report tables only

				// parentOrganisationUnit: boolean (false) //report tables only

				// regression: boolean (false)

				// cumulative: boolean (false)

				// sortOrder: integer (0) //-1, 0, 1

				// topLimit: integer (100) //5, 10, 20, 50, 100

				getValidatedDimensionArray = function(dimensionArray) {
					var dimensionArray = Ext.clone(dimensionArray);

					if (!(dimensionArray && Ext.isArray(dimensionArray) && dimensionArray.length)) {
						return;
					}

					for (var i = 0; i < dimensionArray.length; i++) {
						dimensionArray[i] = api.layout.Dimension(dimensionArray[i]);
					}

					dimensionArray = Ext.Array.clean(dimensionArray);

					return dimensionArray.length ? dimensionArray : null;
				};

				validateSpecialCases = function() {
					var dimConf = conf.finals.dimension,
						dimensions,
						objectNameDimensionMap = {};

					if (!layout) {
						return;
					}

					dimensions = Ext.Array.clean([].concat(layout.columns || [], layout.rows || [], layout.filters || []));

					for (var i = 0; i < dimensions.length; i++) {
						objectNameDimensionMap[dimensions[i].dimension] = dimensions[i];
					}

					if (layout.filters && layout.filters.length) {
						for (var i = 0; i < layout.filters.length; i++) {

							// Indicators as filter
							if (layout.filters[i].dimension === dimConf.indicator.objectName) {
								web.message.alert(NS.i18n.indicators_cannot_be_specified_as_filter || 'Indicators cannot be specified as filter');
								return;
							}

							// Categories as filter
							if (layout.filters[i].dimension === dimConf.category.objectName) {
								web.message.alert(NS.i18n.categories_cannot_be_specified_as_filter || 'Categories cannot be specified as filter');
								return;
							}

							// Data sets as filter
							if (layout.filters[i].dimension === dimConf.dataSet.objectName) {
								web.message.alert(NS.i18n.data_sets_cannot_be_specified_as_filter || 'Data sets cannot be specified as filter');
								return;
							}
						}
					}

					// dc and in
					if (objectNameDimensionMap[dimConf.operand.objectName] && objectNameDimensionMap[dimConf.indicator.objectName]) {
						web.message.alert('Indicators and detailed data elements cannot be specified together');
						return;
					}

					// dc and de
					if (objectNameDimensionMap[dimConf.operand.objectName] && objectNameDimensionMap[dimConf.dataElement.objectName]) {
						web.message.alert('Detailed data elements and totals cannot be specified together');
						return;
					}

					// dc and ds
					if (objectNameDimensionMap[dimConf.operand.objectName] && objectNameDimensionMap[dimConf.dataSet.objectName]) {
						web.message.alert('Data sets and detailed data elements cannot be specified together');
						return;
					}

					// dc and co
					if (objectNameDimensionMap[dimConf.operand.objectName] && objectNameDimensionMap[dimConf.category.objectName]) {
						web.message.alert('Categories and detailed data elements cannot be specified together');
						return;
					}

					return true;
				};

				return function() {
					var objectNames = [],
						dimConf = conf.finals.dimension;

					// config must be an object
					if (!(config && Ext.isObject(config))) {
						alert('Layout: config is not an object (' + init.el + ')');
						return;
					}

					config.columns = getValidatedDimensionArray(config.columns);
					config.rows = getValidatedDimensionArray(config.rows);
					config.filters = getValidatedDimensionArray(config.filters);

					// at least one dimension specified as column or row
					if (!(config.columns || config.rows)) {
						alert(NS.i18n.at_least_one_dimension_must_be_specified_as_row_or_column);
						return;
					}

					// get object names
					for (var i = 0, dims = Ext.Array.clean([].concat(config.columns || [], config.rows || [], config.filters || [])); i < dims.length; i++) {

						// Object names
						if (api.layout.Dimension(dims[i])) {
							objectNames.push(dims[i].dimension);
						}
					}

					// at least one period
					if (!Ext.Array.contains(objectNames, dimConf.period.objectName)) {
						//alert(NS.i18n.at_least_one_period_must_be_specified_as_column_row_or_filter);
						//return;
					}

					// favorite
					if (config.id) {
						layout.id = config.id;
					}

					if (config.name) {
						layout.name = config.name;
					}

					// layout
					layout.columns = config.columns;
					layout.rows = config.rows;
					layout.filters = config.filters;

                    layout.dataType = Ext.isString(config.dataType) ? config.dataType : 'aggregated_values';
                    layout.program = config.program;
                    layout.programStage = config.programStage;

                    // dates
                    if (config.startDate && config.endDate) {
                        layout.startDate = config.startDate;
                        layout.endDate = config.endDate;
                    }

					// properties
					layout.showTotals = Ext.isBoolean(config.totals) ? config.totals : (Ext.isBoolean(config.showTotals) ? config.showTotals : true);
					layout.showSubTotals = Ext.isBoolean(config.subtotals) ? config.subtotals : (Ext.isBoolean(config.showSubTotals) ? config.showSubTotals : true);
					layout.hideEmptyRows = Ext.isBoolean(config.hideEmptyRows) ? config.hideEmptyRows : false;
					layout.countType = Ext.isString(config.countType) && !Ext.isEmpty(config.countType) ? config.countType : 'events';
                    layout.aggregationType = Ext.isString(config.aggregationType) ? config.aggregationType : 'default';

					layout.showHierarchy = Ext.isBoolean(config.showHierarchy) ? config.showHierarchy : false;

					layout.displayDensity = Ext.isString(config.displayDensity) && !Ext.isEmpty(config.displayDensity) ? config.displayDensity : 'normal';
					layout.fontSize = Ext.isString(config.fontSize) && !Ext.isEmpty(config.fontSize) ? config.fontSize : 'normal';
					layout.digitGroupSeparator = Ext.isString(config.digitGroupSeparator) && !Ext.isEmpty(config.digitGroupSeparator) ? config.digitGroupSeparator : 'space';
					layout.legendSet = Ext.isObject(config.legendSet) && Ext.isString(config.legendSet.id) ? config.legendSet : null;

					layout.parentGraphMap = Ext.isObject(config.parentGraphMap) ? config.parentGraphMap : null;

					layout.sorting = Ext.isObject(config.sorting) && Ext.isDefined(config.sorting.id) && Ext.isString(config.sorting.direction) ? config.sorting : null;

					layout.reportingPeriod = Ext.isObject(config.reportParams) && Ext.isBoolean(config.reportParams.paramReportingPeriod) ? config.reportParams.paramReportingPeriod : (Ext.isBoolean(config.reportingPeriod) ? config.reportingPeriod : false);
					layout.organisationUnit =  Ext.isObject(config.reportParams) && Ext.isBoolean(config.reportParams.paramOrganisationUnit) ? config.reportParams.paramOrganisationUnit : (Ext.isBoolean(config.organisationUnit) ? config.organisationUnit : false);
					layout.parentOrganisationUnit =  Ext.isObject(config.reportParams) && Ext.isBoolean(config.reportParams.paramParentOrganisationUnit) ? config.reportParams.paramParentOrganisationUnit : (Ext.isBoolean(config.parentOrganisationUnit) ? config.parentOrganisationUnit : false);

					layout.regression = Ext.isBoolean(config.regression) ? config.regression : false;
					layout.cumulative = Ext.isBoolean(config.cumulative) ? config.cumulative : false;
					layout.sortOrder = Ext.isNumber(config.sortOrder) ? config.sortOrder : 0;
					layout.topLimit = Ext.isNumber(config.topLimit) ? config.topLimit : 0;

					if (!validateSpecialCases()) {
						return;
					}

					return layout;
				}();
			};

			api.response = {};

			api.response.Header = function(config) {
				var config = Ext.clone(config);

				// name: string

				// meta: boolean

				return function() {
					if (!Ext.isObject(config)) {
						console.log('Header: config is not an object: ' + config);
						return;
					}

					if (!Ext.isString(config.name)) {
						console.log('Header: name is not a string: ' + config);
						return;
					}

					if (!Ext.isBoolean(config.meta)) {
						console.log('Header: meta is not boolean: ' + config);
						return;
					}

					return config;
				}();
			};

			api.response.Response = function(config) {
				var config = Ext.clone(config);

				// headers: [Header]

				return function() {
					if (!(config && Ext.isObject(config))) {
						console.log('Response: config is not an object');
						return;
					}

					if (!(config.headers && Ext.isArray(config.headers))) {
						console.log('Response: headers is not an array');
						return;
					}

					for (var i = 0, header; i < config.headers.length; i++) {
						config.headers[i] = api.response.Header(config.headers[i]);
					}

					config.headers = Ext.Array.clean(config.headers);

					if (!config.headers.length) {
						console.log('Response: no valid headers');
						return;
					}

					if (!(Ext.isArray(config.rows) && config.rows.length > 0)) {
						alert('No values found');
						return;
					}

					if (config.headers.length !== config.rows[0].length) {
						console.log('Response: headers.length !== rows[0].length');
					}

					return config;
				}();
			};
		}());

		// support
		(function() {

			// prototype
			support.prototype = {};

				// array
			support.prototype.array = {};

			support.prototype.array.getLength = function(array, suppressWarning) {
				if (!Ext.isArray(array)) {
					if (!suppressWarning) {
						console.log('support.prototype.array.getLength: not an array');
					}

					return null;
				}

				return array.length;
			};

			support.prototype.array.sort = function(array, direction, key) {
				// supports [number], [string], [{key: number}], [{key: string}], [[string]], [[number]]

				if (!support.prototype.array.getLength(array)) {
					return;
				}

				key = !!key || Ext.isNumber(key) ? key : 'name';

				array.sort( function(a, b) {

					// if object, get the property values
					if (Ext.isObject(a) && Ext.isObject(b)) {
						a = a[key];
						b = b[key];
					}

					// if array, get from the right index
					if (Ext.isArray(a) && Ext.isArray(b)) {
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

					return -1;
				});

				return array;
			};

            support.prototype.array.uniqueByProperty = function(array, property) {
                var names = [],
                    uniqueItems = [];

                for (var i = 0, item; i < array.length; i++) {
                    item = array[i];

                    if (!Ext.Array.contains(names, item[property])) {
                        uniqueItems.push(item);
                        names.push(item[property]);
                    }
                }

                return uniqueItems;
            };

				// object
			support.prototype.object = {};

			support.prototype.object.getLength = function(object, suppressWarning) {
				if (!Ext.isObject(object)) {
					if (!suppressWarning) {
						console.log('support.prototype.object.getLength: not an object');
					}

					return null;
				}

				var size = 0;

				for (var key in object) {
					if (object.hasOwnProperty(key)) {
						size++;
					}
				}

				return size;
			};

			support.prototype.object.hasObject = function(object, property, value) {
				if (!support.prototype.object.getLength(object)) {
					return null;
				}

				for (var key in object) {
					var record = object[key];

					if (object.hasOwnProperty(key) && record[property] === value) {
						return true;
					}
				}

				return null;
			};

				// str
			support.prototype.str = {};

			support.prototype.str.replaceAll = function(str, find, replace) {
				return str.replace(new RegExp(find, 'g'), replace);
			};

			support.prototype.str.toggleDirection = function(direction) {
				return direction === 'DESC' ? 'ASC' : 'DESC';
			};

				// number
			support.prototype.number = {};

			support.prototype.number.getNumberOfDecimals = function(number) {
				var str = new String(number);
				return (str.indexOf('.') > -1) ? (str.length - str.indexOf('.') - 1) : 0;
			};

			support.prototype.number.roundIf = function(number, precision) {
				number = parseFloat(number);
				precision = parseFloat(precision);

				if (Ext.isNumber(number) && Ext.isNumber(precision)) {
					var numberOfDecimals = support.prototype.number.getNumberOfDecimals(number);
					return numberOfDecimals > precision ? Ext.Number.toFixed(number, precision) : number;
				}

				return number;
			};

			support.prototype.number.prettyPrint = function(number, separator) {
				separator = separator || 'space';

				if (separator === 'none') {
					return number;
				}

				return number.toString().replace(/\B(?=(\d{3})+(?!\d))/g, conf.report.digitGroupSeparator[separator]);
			};

			// color
			support.color = {};

			support.color.hexToRgb = function(hex) {
				var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i,
					result;

				hex = hex.replace(shorthandRegex, function(m, r, g, b) {
					return r + r + g + g + b + b;
				});

				result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);

				return result ? {
					r: parseInt(result[1], 16),
					g: parseInt(result[2], 16),
					b: parseInt(result[3], 16)
				} : null;
			};

		}());

		// service
		(function() {

			// layout
			service.layout = {};

			service.layout.cleanDimensionArray = function(dimensionArray) {
				if (!support.prototype.array.getLength(dimensionArray)) {
					return null;
				}

				var array = [];

				for (var i = 0; i < dimensionArray.length; i++) {
					array.push(api.layout.Dimension(dimensionArray[i]));
				}

				array = Ext.Array.clean(array);

				return array.length ? array : null;
			};

			service.layout.sortDimensionArray = function(dimensionArray, key) {
				if (!support.prototype.array.getLength(dimensionArray, true)) {
					return null;
				}

				// Clean dimension array
				dimensionArray = service.layout.cleanDimensionArray(dimensionArray);

				if (!dimensionArray) {
					console.log('service.layout.sortDimensionArray: no valid dimensions');
					return null;
				}

				key = key || 'dimensionName';

				// Dimension order
				Ext.Array.sort(dimensionArray, function(a,b) {
					if (a[key] < b[key]) {
						return -1;
					}
					if (a[key] > b[key]) {
						return 1;
					}
					return 0;
				});

				// Sort object items, ids
				for (var i = 0, items; i < dimensionArray.length; i++) {
					support.prototype.array.sort(dimensionArray[i].items, 'ASC', 'id');

					if (support.prototype.array.getLength(dimensionArray[i].ids)) {
						support.prototype.array.sort(dimensionArray[i].ids);
					}
				}

				return dimensionArray;
			};

			service.layout.getObjectNameDimensionMapFromDimensionArray = function(dimensionArray) {
				var map = {};

				if (!support.prototype.array.getLength(dimensionArray)) {
					return null;
				}

				for (var i = 0, dimension; i < dimensionArray.length; i++) {
					dimension = api.layout.Dimension(dimensionArray[i]);

					if (dimension) {
						map[dimension.dimension] = dimension;
					}
				}

				return support.prototype.object.getLength(map) ? map : null;
			};

			service.layout.getObjectNameDimensionItemsMapFromDimensionArray = function(dimensionArray) {
				var map = {};

				if (!support.prototype.array.getLength(dimensionArray)) {
					return null;
				}

				for (var i = 0, dimension; i < dimensionArray.length; i++) {
					dimension = api.layout.Dimension(dimensionArray[i]);

					if (dimension) {
						map[dimension.dimension] = dimension.items;
					}
				}

				return support.prototype.object.getLength(map) ? map : null;
			};

			service.layout.getItemName = function(layout, response, id, isHtml) {
				var metaData = response.metaData,
					name = '';

				if (service.layout.isHierarchy(layout, response, id)) {
					var a = metaData.names[id].split('/');

                    if (a.length === 1) {
                        return a[0];
                    }

					a.shift();

					for (var i = 0, isLast; i < a.length; i++) {
						isLast = !!(i === a.length - 1);

						name += (isHtml && !isLast ? '<span class="text-weak">' : '') + a[i] + (isHtml && !isLast ? '</span>' : '') + (!isLast ? ' / ' : '');
					}

					return name;
				}

				name += metaData.names[id];

				return name;
			};

			service.layout.getExtendedLayout = function(layout) {
				var layout = Ext.clone(layout),
					xLayout;

				xLayout = {
					columns: [],
					rows: [],
					filters: [],

					columnObjectNames: [],
					columnDimensionNames: [],
					rowObjectNames: [],
					rowDimensionNames: [],

					// axis
					axisDimensions: [],
					axisObjectNames: [],
					axisDimensionNames: [],

						// for param string
					sortedAxisDimensionNames: [],

					// Filter
					filterDimensions: [],
					filterObjectNames: [],
					filterDimensionNames: [],

						// for param string
					sortedFilterDimensions: [],

					// all
					dimensions: [],
					objectNames: [],
					dimensionNames: [],

					// oject name maps
					objectNameDimensionsMap: {},
					objectNameItemsMap: {},
					objectNameIdsMap: {},

					// dimension name maps
					dimensionNameDimensionsMap: {},
					dimensionNameItemsMap: {},
					dimensionNameIdsMap: {},

						// for param string
					dimensionNameSortedIdsMap: {}

					// sort table by column
					//sortableIdObjects: []
				};

				Ext.applyIf(xLayout, layout);

				// columns, rows, filters
				if (layout.columns) {
                    //layout.columns = support.prototype.array.uniqueByProperty(layout.columns, 'dimension');

					for (var i = 0, dim, items, xDim; i < layout.columns.length; i++) {
						dim = layout.columns[i];
						items = dim.items;
						xDim = {};

						xDim.dimension = dim.dimension;
						xDim.objectName = dim.dimension;
						xDim.dimensionName = dimConf.objectNameMap.hasOwnProperty(dim.dimension) ? dimConf.objectNameMap[dim.dimension].dimensionName || dim.dimension : dim.dimension;

						xDim.items = [];
						xDim.ids = [];

						if (items) {
							xDim.items = items;

							for (var j = 0; j < items.length; j++) {
								xDim.ids.push(items[j].id);
							}
						}

						xLayout.columns.push(xDim);

						xLayout.columnObjectNames.push(xDim.objectName);
						xLayout.columnDimensionNames.push(xDim.dimensionName);

						xLayout.axisDimensions.push(xDim);
						xLayout.axisObjectNames.push(xDim.objectName);
						xLayout.axisDimensionNames.push(dimConf.objectNameMap.hasOwnProperty(xDim.objectName) ? dimConf.objectNameMap[xDim.objectName].dimensionName || xDim.objectName : xDim.objectName);

						xLayout.objectNameDimensionsMap[xDim.objectName] = xDim;
						xLayout.objectNameItemsMap[xDim.objectName] = xDim.items;
						xLayout.objectNameIdsMap[xDim.objectName] = xDim.ids;
					}
				}

				if (layout.rows) {
                    //layout.rows = support.prototype.array.uniqueByProperty(layout.rows, 'dimension');

					for (var i = 0, dim, items, xDim; i < layout.rows.length; i++) {
						dim = Ext.clone(layout.rows[i]);
						items = dim.items;
						xDim = {};

						xDim.dimension = dim.dimension;
						xDim.objectName = dim.dimension;
						xDim.dimensionName = dimConf.objectNameMap.hasOwnProperty(dim.dimension) ? dimConf.objectNameMap[dim.dimension].dimensionName || dim.dimension : dim.dimension;

						xDim.items = [];
						xDim.ids = [];

						if (items) {
							xDim.items = items;

							for (var j = 0; j < items.length; j++) {
								xDim.ids.push(items[j].id);
							}
						}

						xLayout.rows.push(xDim);

						xLayout.rowObjectNames.push(xDim.objectName);
						xLayout.rowDimensionNames.push(xDim.dimensionName);

						xLayout.axisDimensions.push(xDim);
						xLayout.axisObjectNames.push(xDim.objectName);
						xLayout.axisDimensionNames.push(dimConf.objectNameMap.hasOwnProperty(xDim.objectName) ? dimConf.objectNameMap[xDim.objectName].dimensionName || xDim.objectName : xDim.objectName);

						xLayout.objectNameDimensionsMap[xDim.objectName] = xDim;
						xLayout.objectNameItemsMap[xDim.objectName] = xDim.items;
						xLayout.objectNameIdsMap[xDim.objectName] = xDim.ids;
					}
				}

				if (layout.filters) {
                    //layout.filters = support.prototype.array.uniqueByProperty(layout.filters, 'dimension');

					for (var i = 0, dim, items, xDim; i < layout.filters.length; i++) {
						dim = layout.filters[i];
						items = dim.items;
						xDim = {};

						xDim.dimension = dim.dimension;
						xDim.objectName = dim.dimension;
						xDim.dimensionName = dimConf.objectNameMap.hasOwnProperty(dim.dimension) ? dimConf.objectNameMap[dim.dimension].dimensionName || dim.dimension : dim.dimension;

						xDim.items = [];
						xDim.ids = [];

						if (items) {
							xDim.items = items;

							for (var j = 0; j < items.length; j++) {
								xDim.ids.push(items[j].id);
							}
						}

						xLayout.filters.push(xDim);

						xLayout.filterDimensions.push(xDim);
						xLayout.filterObjectNames.push(xDim.objectName);
						xLayout.filterDimensionNames.push(dimConf.objectNameMap.hasOwnProperty(xDim.objectName) ? dimConf.objectNameMap[xDim.objectName].dimensionName || xDim.objectName : xDim.objectName);

						xLayout.objectNameDimensionsMap[xDim.objectName] = xDim;
						xLayout.objectNameItemsMap[xDim.objectName] = xDim.items;
						xLayout.objectNameIdsMap[xDim.objectName] = xDim.ids;
					}
				}

				// legend set
				xLayout.legendSet = layout.legendSet ? init.idLegendSetMap[layout.legendSet.id] : null;

				if (layout.legendSet && layout.legendSet.mapLegends) {
					xLayout.legendSet = init.idLegendSetMap[layout.legendSet.id];
					support.prototype.array.sort(xLayout.legendSet.mapLegends, 'ASC', 'startValue');
				}

				// unique dimension names
				xLayout.axisDimensionNames = Ext.Array.unique(xLayout.axisDimensionNames);
				xLayout.filterDimensionNames = Ext.Array.unique(xLayout.filterDimensionNames);

				xLayout.columnDimensionNames = Ext.Array.unique(xLayout.columnDimensionNames);
				xLayout.rowDimensionNames = Ext.Array.unique(xLayout.rowDimensionNames);
				xLayout.filterDimensionNames = Ext.Array.unique(xLayout.filterDimensionNames);

					// for param string
				xLayout.sortedAxisDimensionNames = Ext.clone(xLayout.axisDimensionNames).sort();
				xLayout.sortedFilterDimensions = service.layout.sortDimensionArray(Ext.clone(xLayout.filterDimensions));

				// all
				xLayout.dimensions = [].concat(xLayout.axisDimensions, xLayout.filterDimensions);
				xLayout.objectNames = [].concat(xLayout.axisObjectNames, xLayout.filterObjectNames);
				xLayout.dimensionNames = [].concat(xLayout.axisDimensionNames, xLayout.filterDimensionNames);

				// dimension name maps
				for (var i = 0, dimName; i < xLayout.dimensionNames.length; i++) {
					dimName = xLayout.dimensionNames[i];

					xLayout.dimensionNameDimensionsMap[dimName] = [];
					xLayout.dimensionNameItemsMap[dimName] = [];
					xLayout.dimensionNameIdsMap[dimName] = [];
				}

				for (var i = 0, xDim; i < xLayout.dimensions.length; i++) {
					xDim = xLayout.dimensions[i];

					xLayout.dimensionNameDimensionsMap[xDim.dimensionName].push(xDim);
					xLayout.dimensionNameItemsMap[xDim.dimensionName] = xLayout.dimensionNameItemsMap[xDim.dimensionName].concat(xDim.items);
					xLayout.dimensionNameIdsMap[xDim.dimensionName] = xLayout.dimensionNameIdsMap[xDim.dimensionName].concat(xDim.ids);
				}

					// for param string
				for (var key in xLayout.dimensionNameIdsMap) {
					if (xLayout.dimensionNameIdsMap.hasOwnProperty(key)) {
						xLayout.dimensionNameSortedIdsMap[key] = Ext.clone(xLayout.dimensionNameIdsMap[key]).sort();
					}
				}

				// Uuid
				xLayout.tableUuid = init.el + '_' + Ext.data.IdGenerator.get('uuid').generate();

				return xLayout;
			};

			service.layout.getSyncronizedXLayout = function(xLayout, xResponse) {
				var removeDimensionFromXLayout,
					getHeaderNames,
					dimensions = Ext.Array.clean([].concat(xLayout.columns || [], xLayout.rows || [], xLayout.filters || []));

				removeDimensionFromXLayout = function(objectName) {
					var getUpdatedAxis;

					getUpdatedAxis = function(axis) {
						var dimension;
						axis = Ext.clone(axis);

						for (var i = 0; i < axis.length; i++) {
							if (axis[i].dimension === objectName) {
								dimension = axis[i];
							}
						}

						if (dimension) {
							Ext.Array.remove(axis, dimension);
						}

						return axis;
					};

					if (xLayout.columns) {
						xLayout.columns = getUpdatedAxis(xLayout.columns);
					}
					if (xLayout.rows) {
						xLayout.rows = getUpdatedAxis(xLayout.rows);
					}
					if (xLayout.filters) {
						xLayout.filters = getUpdatedAxis(xLayout.filters);
					}
				};

				getHeaderNames = function() {
					var headerNames = [];

					for (var i = 0; i < xResponse.headers.length; i++) {
						headerNames.push(xResponse.headers[i].name);
					}

					return headerNames;
				};

				return function() {

					// items
					for (var i = 0, dim, header; i < dimensions.length; i++) {
						dim = dimensions[i];
						dim.items = [];
						header = xResponse.nameHeaderMap[dim.dimension];

						if (header) {
							for (var j = 0, id; j < header.ids.length; j++) {
								id = header.ids[j];

								dim.items.push({
									id: id,
									name: xResponse.metaData.names[id] || id
								});
							}
						}
					}

					// Re-layout
					layout = api.layout.Layout(xLayout);

					if (layout) {
						return service.layout.getExtendedLayout(layout);
					}

					return null;
				}();
			};

			service.layout.getExtendedAxis = function(xLayout, type) {
				var dimensionNames,
					spanType,
					aDimensions = [],
					nAxisWidth = 1,
					nAxisHeight,
					aaUniqueFloorIds,
					aUniqueFloorWidth = [],
					aAccFloorWidth = [],
					aFloorSpan = [],
					aaGuiFloorIds = [],
					aaAllFloorIds = [],
					aCondoId = [],
					aaAllFloorObjects = [],
					uuidObjectMap = {};

				if (type === 'col') {
					dimensionNames = Ext.clone(xLayout.columnDimensionNames);
					spanType = 'colSpan';
				}
				else if (type === 'row') {
					dimensionNames = Ext.clone(xLayout.rowDimensionNames);
					spanType = 'rowSpan';
				}

				if (!(Ext.isArray(dimensionNames) && dimensionNames.length)) {
					return;
				}
	//dimensionNames = ['pe', 'ou'];

				// aDimensions: array of dimension objects with dimensionName property
				for (var i = 0; i < dimensionNames.length; i++) {
					aDimensions.push({
						dimensionName: dimensionNames[i]
					});
				}
	//aDimensions = [{
		//dimensionName: 'pe'
	//}]

				// aaUniqueFloorIds: array of arrays with unique ids for each dimension floor
				aaUniqueFloorIds = function() {
					var a = [];

					for (var i = 0; i < aDimensions.length; i++) {
						a.push(xLayout.dimensionNameIdsMap[aDimensions[i].dimensionName]);
					}

					return a;
				}();
	//aaUniqueFloorIds	= [ [de-id1, de-id2, de-id3],
	//					    [pe-id1],
	//					    [ou-id1, ou-id2, ou-id3, ou-id4] ]


				// nAxisHeight
				nAxisHeight = aaUniqueFloorIds.length;
	//nAxisHeight = 3


				// aUniqueFloorWidth, nAxisWidth, aAccFloorWidth
				for (var i = 0, nUniqueFloorWidth; i < nAxisHeight; i++) {
					nUniqueFloorWidth = aaUniqueFloorIds[i].length;

					aUniqueFloorWidth.push(nUniqueFloorWidth);
					nAxisWidth = nAxisWidth * nUniqueFloorWidth;
					aAccFloorWidth.push(nAxisWidth);
				}
	//aUniqueFloorWidth	= [3, 1, 4]
	//nAxisWidth		= 12 (3 * 1 * 4)
	//aAccFloorWidth	= [3, 3, 12]

				// aFloorSpan
				for (var i = 0; i < nAxisHeight; i++) {
					if (aUniqueFloorWidth[i] === 1) {
						if (i === 0) { // if top floor
							aFloorSpan.push(nAxisWidth); // span max
						}
						else {
							if (xLayout.hideEmptyRows && type === 'row') {
								aFloorSpan.push(nAxisWidth / aAccFloorWidth[i]);
							}
							else {
								aFloorSpan.push(aFloorSpan[0]); //if just one item and not top level, span same as top level
							}
						}
					}
					else {
						aFloorSpan.push(nAxisWidth / aAccFloorWidth[i]);
					}
				}
	//aFloorSpan			= [4, 12, 1]


				// aaGuiFloorIds
				aaGuiFloorIds.push(aaUniqueFloorIds[0]);

				if (nAxisHeight.length > 1) {
					for (var i = 1, a, n; i < nAxisHeight; i++) {
						a = [];
						n = aUniqueFloorWidth[i] === 1 ? aUniqueFloorWidth[0] : aAccFloorWidth[i-1];

						for (var j = 0; j < n; j++) {
							a = a.concat(aaUniqueFloorIds[i]);
						}

						aaGuiFloorIds.push(a);
					}
				}
	//aaGuiFloorIds	= [ [d1, d2, d3], (3)
	//					[p1, p2, p3, p4, p5, p1, p2, p3, p4, p5, p1, p2, p3, p4, p5], (15)
	//					[o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2...] (30)
	//		  	  	  ]


				// aaAllFloorIds
				for (var i = 0, aAllFloorIds, aUniqueFloorIds, span, factor; i < nAxisHeight; i++) {
					aAllFloorIds = [];
					aUniqueFloorIds = aaUniqueFloorIds[i];
					span = aFloorSpan[i];
					factor = nAxisWidth / (span * aUniqueFloorIds.length);

					for (var j = 0; j < factor; j++) {
						for (var k = 0; k < aUniqueFloorIds.length; k++) {
							for (var l = 0; l < span; l++) {
								aAllFloorIds.push(aUniqueFloorIds[k]);
							}
						}
					}

					aaAllFloorIds.push(aAllFloorIds);
				}
	//aaAllFloorIds	= [ [d1, d1, d1, d1, d1, d1, d1, d1, d1, d1, d2, d2, d2, d2, d2, d2, d2, d2, d2, d2, d3, d3, d3, d3, d3, d3, d3, d3, d3, d3], (30)
	//					[p1, p2, p3, p4, p5, p1, p2, p3, p4, p5, p1, p2, p3, p4, p5, p1, p2, p3, p4, p5, p1, p2, p3, p4, p5, p1, p2, p3, p4, p5], (30)
	//					[o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2, o1, o2] (30)
	//		  	  	  ]


				// aCondoId
				for (var i = 0, id; i < nAxisWidth; i++) {
					id = '';

					for (var j = 0; j < nAxisHeight; j++) {
						id += aaAllFloorIds[j][i];
					}

					if (id) {
						aCondoId.push(id);
					}
				}
	//aCondoId	= [ id11+id21+id31, id12+id22+id32, ... ]


				// allObjects
				for (var i = 0, allFloor; i < aaAllFloorIds.length; i++) {
					allFloor = [];

					for (var j = 0, obj; j < aaAllFloorIds[i].length; j++) {
						obj = {
							id: aaAllFloorIds[i][j],
							uuid: Ext.data.IdGenerator.get('uuid').generate(),
							dim: i,
							axis: type
						};

						// leaf?
						if (i === aaAllFloorIds.length - 1) {
							obj.leaf = true;
						}

						allFloor.push(obj);
					}

					aaAllFloorObjects.push(allFloor);
				}

				// add span and children
				for (var i = 0, aAboveFloorObjects, doorIds, uniqueDoorIds; i < aaAllFloorObjects.length; i++) {
                    doorIds = [];

					for (var j = 0, obj, doorCount = 0, oldestObj; j < aaAllFloorObjects[i].length; j++) {

						obj = aaAllFloorObjects[i][j];
                        doorIds.push(obj.id);

						if (doorCount === 0) {

							// span
							obj[spanType] = aFloorSpan[i];

							// children
                            if (obj.leaf) {
                                obj.children = 0;
                            }

							// first sibling
							obj.oldest = true;

							// root?
							if (i === 0) {
								obj.root = true;
							}

							// tmp oldest uuid
							oldestObj = obj;
						}

						obj.oldestSibling = oldestObj;

						if (++doorCount === aFloorSpan[i]) {
							doorCount = 0;
						}
					}

                    // set above floor door children to number of unique door ids on this floor
                    if (i > 0) {
                        aAboveFloorObjects = aaAllFloorObjects[i-1];
                        uniqueDoorIds = Ext.Array.unique(doorIds);

                        for (var j = 0; j < aAboveFloorObjects.length; j++) {
                            aAboveFloorObjects[j].children = uniqueDoorIds.length;
                        }
                    }
				}

				// add parents if more than 1 floor
				if (nAxisHeight > 1) {
					for (var i = 1, aAllFloor; i < nAxisHeight; i++) {
						aAllFloor = aaAllFloorObjects[i];

						//for (var j = 0, obj, doorCount = 0, span = aFloorSpan[i - 1], parentObj = aaAllFloorObjects[i - 1][0]; j < aAllFloor.length; j++) {
						for (var j = 0, doorCount = 0, span = aFloorSpan[i - 1]; j < aAllFloor.length; j++) {
							aAllFloor[j].parent = aaAllFloorObjects[i - 1][j];

							//doorCount++;

							//if (doorCount === span) {
								//parentObj = aaAllFloorObjects[i - 1][j + 1];
								//doorCount = 0;
							//}
						}
					}
				}

				// add uuids array to leaves
				if (aaAllFloorObjects.length) {

					// set span to second lowest span number: if aFloorSpan == [15,3,15,1], set span to 3
					var nSpan = nAxisHeight > 1 ? support.prototype.array.sort(Ext.clone(aFloorSpan))[1] : nAxisWidth,
						aAllFloorObjectsLast = aaAllFloorObjects[aaAllFloorObjects.length - 1];

					for (var i = 0, leaf, parentUuids, obj, leafUuids = []; i < aAllFloorObjectsLast.length; i++) {
						leaf = aAllFloorObjectsLast[i];
						leafUuids.push(leaf.uuid);
						parentUuids = [];
						obj = leaf;

						// get the uuid of the oldest sibling
						while (obj.parent) {
							obj = obj.parent;
							parentUuids.push(obj.oldestSibling.uuid);
						}

						// add parent uuids to leaf
						leaf.uuids = Ext.clone(parentUuids);

						// add uuid for all leaves
						if (leafUuids.length === nSpan) {
							for (var j = (i - nSpan) + 1, leaf; j <= i; j++) {
								leaf = aAllFloorObjectsLast[j];
								leaf.uuids = leaf.uuids.concat(Ext.clone(leafUuids));
							}

							leafUuids = [];
						}
					}
				}

				// populate uuidObject map
				for (var i = 0; i < aaAllFloorObjects.length; i++) {
					for (var j = 0, object; j < aaAllFloorObjects[i].length; j++) {
						object = aaAllFloorObjects[i][j];
//console.log(object.uuid, object);
						uuidObjectMap[object.uuid] = object;
					}
				}

//console.log("aaAllFloorObjects", aaAllFloorObjects);

				return {
					type: type,
					items: aDimensions,
					xItems: {
						unique: aaUniqueFloorIds,
						gui: aaGuiFloorIds,
						all: aaAllFloorIds
					},
					objects: {
						all: aaAllFloorObjects
					},
					ids: aCondoId,
					span: aFloorSpan,
					dims: nAxisHeight,
					size: nAxisWidth,
					uuidObjectMap: uuidObjectMap
				};
			};

			service.layout.isHierarchy = function(layout, response, id) {
				return layout.showHierarchy && Ext.isObject(response.metaData.ouHierarchy) && response.metaData.ouHierarchy.hasOwnProperty(id);
			};

            service.layout.getHierarchyName = function(ouHierarchy, names, id) {
                var graph = ouHierarchy[id],
                    ids = Ext.Array.clean(graph.split('/')),
                    hierarchyName = '';

                if (ids.length < 2) {
                    return names[id];
                }

                for (var i = 0; i < ids.length; i++) {
                    hierarchyName += names[ids[i]] + ' / ';
                }

                hierarchyName += names[id];

                return hierarchyName;
            };

			service.layout.layout2plugin = function(layout, el) {
				var layout = Ext.clone(layout),
					dimensions = Ext.Array.clean([].concat(layout.columns || [], layout.rows || [], layout.filters || []));

				layout.url = init.contextPath;

				if (el) {
					layout.el = el;
				}

				if (Ext.isString(layout.id)) {
					return {id: layout.id};
				}

				for (var i = 0, dimension, item; i < dimensions.length; i++) {
					dimension = dimensions[i];

					delete dimension.id;
					delete dimension.ids;
					delete dimension.type;
					delete dimension.dimensionName;
					delete dimension.objectName;

					for (var j = 0, item; j < dimension.items.length; j++) {
						item = dimension.items[j];

						delete item.name;
						delete item.code;
						delete item.created;
						delete item.lastUpdated;
						delete item.value;
					}
				}

				if (layout.showTotals) {
					delete layout.showTotals;
				}

				if (layout.showSubTotals) {
					delete layout.showSubTotals;
				}

				if (!layout.hideEmptyRows) {
					delete layout.hideEmptyRows;
				}

				if (!layout.showHierarchy) {
					delete layout.showHierarchy;
				}

				if (layout.displayDensity === 'normal') {
					delete layout.displayDensity;
				}

				if (layout.fontSize === 'normal') {
					delete layout.fontSize;
				}

				if (layout.digitGroupSeparator === 'space') {
					delete layout.digitGroupSeparator;
				}

				if (!layout.legendSet) {
					delete layout.legendSet;
				}

				if (!layout.sorting) {
					delete layout.sorting;
				}

				delete layout.parentGraphMap;
				delete layout.reportingPeriod;
				delete layout.organisationUnit;
				delete layout.parentOrganisationUnit;
				delete layout.regression;
				delete layout.cumulative;
				delete layout.sortOrder;
				delete layout.topLimit;

				return layout;
			};

            service.layout.getDataDimensionsFromLayout = function(layout) {
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

			// response
			service.response = {};

				// aggregate
			service.response.aggregate = {};

			service.response.aggregate.getExtendedResponse = function(xLayout, response) {
				var emptyId = 'N/A',
                    meta = ['ou', 'pe'],
                    ouHierarchy,
                    names,
					headers;

				response = Ext.clone(response);
				headers = response.headers;
                ouHierarchy = response.metaData.ouHierarchy,
                names = response.metaData.names;
                names[emptyId] = emptyId;

				response.nameHeaderMap = {};
				response.idValueMap = {};

				// add to headers: size, index, response ids
				for (var i = 0, header, isMeta; i < headers.length; i++) {
					header = headers[i];
                    header.ids = [];
                    isMeta = Ext.Array.contains(meta, header.name);

                    // overwrite row ids, update metadata, set unique header ids
                    if (header.meta) {
                        if (header.type === 'java.lang.Double') {
                            var objects = [];

                            for (var j = 0, id, fullId, parsedId, displayId; j < response.rows.length; j++) {
                                id = response.rows[j][i] || emptyId;
                                fullId = header.name + id;
                                parsedId = parseFloat(id);

                                displayId = Ext.isNumber(parsedId) ? parsedId : (names[id] || id);

								// update names
                                names[fullId] = (isMeta ? '' : header.column + ' ') + displayId;

								// update rows
                                response.rows[j][i] = fullId;

								// number sorting
                                objects.push({
                                    id: fullId,
                                    sortingId: Ext.isNumber(parsedId) ? parsedId : Number.MAX_VALUE
                                });
                            }

                            support.prototype.array.sort(objects, 'ASC', 'sortingId');
                            header.ids = Ext.Array.pluck(objects, 'id');
                        }
                        else {
							var objects = [];

                            for (var j = 0, id, fullId, name, isHierarchy; j < response.rows.length; j++) {
                                id = response.rows[j][i] || emptyId;
                                fullId = header.name + id;
                                isHierarchy = service.layout.isHierarchy(xLayout, response, id);

                                // add dimension name prefix if not pe/ou
                                name = isMeta ? '' : header.column + ' ';

                                // add hierarchy if ou and showHierarchy
                                name = isHierarchy ? service.layout.getHierarchyName(ouHierarchy, names, id) : (names[id] || id);

                                names[fullId] = name;

                                // update rows
                                response.rows[j][i] = fullId;

                                // update ou hierarchy
                                if (isHierarchy) {
									ouHierarchy[fullId] = ouHierarchy[id];
								}

								objects.push({
									id: fullId,
									sortingId: header.name === 'pe' ? fullId : name
								});
                            }

                            support.prototype.array.sort(objects, 'ASC', 'sortingId');
                            header.ids = Ext.Array.pluck(objects, 'id');
                        }
                    }

					header.ids = Ext.Array.unique(header.ids);

					header.size = header.ids.length;
					header.index = i;

					response.nameHeaderMap[header.name] = header;
				}

				// idValueMap: vars
				var valueHeaderIndex = response.nameHeaderMap[conf.finals.dimension.value.value].index,
					dx = dimConf.data.dimensionName,
					axisDimensionNames = xLayout.axisDimensionNames,
					idIndexOrder = [];

				// idValueMap: idIndexOrder
				for (var i = 0; i < axisDimensionNames.length; i++) {
					idIndexOrder.push(response.nameHeaderMap[axisDimensionNames[i]].index);
				}

				// idValueMap
				for (var i = 0, row, id; i < response.rows.length; i++) {
					row = response.rows[i];
					id = '';

					for (var j = 0; j < idIndexOrder.length; j++) {
						id += row[idIndexOrder[j]];
					}

					response.idValueMap[id] = row[valueHeaderIndex];
				}

				return response;
			};

				// query
			service.response.query = {};

			service.response.query.getExtendedResponse = function(layout, response) {
				var xResponse = Ext.clone(response),
					metaData = xResponse.metaData,
                    dimensionNames = Ext.Array.unique(Ext.Array.pluck(layout.columns, 'dimension')),
                    dimensionHeaders = [],
					headers = xResponse.headers,
					nameHeaderMap = {},
                    nameMap = {},
                    ouIndex;

                nameMap['pe'] = 'eventdate';
                nameMap['ou'] = 'ouname';

                // get ou index
                for (var i = 0, header; i < headers.length; i++) {
					if (headers[i].name === 'ou') {
						ouIndex = i;
						break;
					}
				}

				// update rows
				for (var i = 0, header; i < headers.length; i++) {
					header = headers[i];
					header.index = i;

					nameHeaderMap[header.name] = header;

					if (header.type === 'java.lang.Double') {
						for (var j = 0, value; j < xResponse.rows.length; j++) {
                            value = xResponse.rows[j][i];
							xResponse.rows[j][i] = value ? parseFloat(value) : value;
						}
					}

					if (header.name === 'eventdate') {
						for (var j = 0; j < xResponse.rows.length; j++) {
							xResponse.rows[j][i] = xResponse.rows[j][i].substr(0,10);
						}
					}

					// TODO, using descendants -> missing orgunits in ouHierarchy

					//else if (header.name === 'ouname' && layout.showHierarchy && metaData.ouHierarchy) {
						//for (var j = 0, ouId; j < xResponse.rows.length; j++) {
							//ouId = xResponse.rows[j][ouIndex];
							//xResponse.rows[j][i] = service.layout.getHierarchyName(metaData.ouHierarchy, metaData.names, ouId);
						//}
					//}
				}

				// dimension headers
                for (var i = 0, name; i < dimensionNames.length; i++) {
                    name = nameMap[dimensionNames[i]] || dimensionNames[i];

                    dimensionHeaders.push(nameHeaderMap[name]);
                }

				xResponse.dimensionHeaders = dimensionHeaders;
				xResponse.nameHeaderMap = nameHeaderMap;

				return xResponse;
			};
		}());

		// web
		(function() {

			// mask
			web.mask = {};

			web.mask.show = function(component, message) {
				if (!Ext.isObject(component)) {
					console.log('web.mask.show: component not an object');
					return null;
				}

				message = message || 'Loading..';

				if (component.mask) {
					component.mask.destroy();
					component.mask = null;
				}

				component.mask = new Ext.create('Ext.LoadMask', component, {
					shadow: false,
					msg: message,
					style: 'box-shadow:0',
					bodyStyle: 'box-shadow:0'
				});

				component.mask.show();
			};

			web.mask.hide = function(component) {
				if (!Ext.isObject(component)) {
					console.log('support.gui.mask.hide: component not an object');
					return null;
				}

				if (component.mask) {
					component.mask.destroy();
					component.mask = null;
				}
			};

			// message
			web.message = {};

			web.message.alert = function(message) {
				console.log(message);
			};

			// analytics
			web.analytics = {};

			web.analytics.getParamString = function(view, format, skipPaging) {
                var paramString,
                    dimensions = Ext.Array.clean([].concat(view.columns || [], view.rows || [])),
                    ignoreKeys = ['longitude', 'latitude'],
                    dataTypeMap = {
                        'aggregated_values': 'aggregate',
                        'individual_cases': 'query'
                    },
                    nameItemsMap;

                format = format || 'json';

                paramString = '/api/analytics/events/' + dataTypeMap[view.dataType] + '/' + view.program.id + '.' + format + '?';

				// stage
				paramString += 'stage=' + view.programStage.id;

                // dimensions
                if (dimensions) {
					for (var i = 0, dim; i < dimensions.length; i++) {
						dim = dimensions[i];

						if (Ext.Array.contains(ignoreKeys, dim.dimension) || (dim.dimension === 'pe' && !dim.items && !dim.filter)) {
							continue;
						}

						paramString += '&dimension=' + dim.dimension;

						if (dim.items && dim.items.length) {
							paramString += ':';

							for (var j = 0, item; j < dim.items.length; j++) {
								item = dim.items[j];

								paramString += encodeURIComponent(item.id) + ((j < (dim.items.length - 1)) ? ';' : '');
							}
						}
						else {
							paramString += dim.filter ? ':' + encodeURIComponent(dim.filter) : '';
						}
					}
				}

                // filters
                if (view.filters) {
					for (var i = 0, dim; i < view.filters.length; i++) {
						dim = view.filters[i];

                        paramString += '&filter=' + dim.dimension;

                        if (dim.items) {
                            paramString += ':';

                            for (var j = 0; j < dim.items.length; j++) {
                                paramString += encodeURIComponent(dim.items[j].id);
                                paramString += j < dim.items.length - 1 ? ';' : '';
                            }
                        }
                        else {
                            paramString += dim.filter ? ':' + encodeURIComponent(dim.filter) : '';
                        }
					}
				}

                // dates
                if (view.startDate && view.endDate) {
                    paramString += '&startDate=' + view.startDate + '&endDate=' + view.endDate;
                }

				// hierarchy
				paramString += view.showHierarchy ? '&hierarchyMeta=true' : '';

                // limit
                if (view.dataType === 'aggregated_values' && (view.sortOrder && view.topLimit)) {
                    paramString += '&limit=' + view.topLimit + '&sortOrder=' + (view.sortOrder < 0 ? 'ASC' : 'DESC');
                }

                // count type
                if (view.dataType === 'aggregated_values' && view.countType) {
                    if (view.countType === 'tracked_entity_instances') {
                        paramString += '&uniqueInstances=true';
                    }
                }

                // sorting
                if (view.dataType === 'individual_cases' && view.sorting) {
                    if (view.sorting.id && view.sorting.direction) {
                        paramString += '&' + view.sorting.direction.toLowerCase() + '=' + view.sorting.id;
                    }
                }

                // paging
                if (view.dataType === 'individual_cases' && view.paging && !skipPaging) {
                    paramString += view.paging.pageSize ? '&pageSize=' + view.paging.pageSize : '';
                    paramString += view.paging.page ? '&page=' + view.paging.page : '';
                }

                return paramString;
            };

			web.analytics.validateUrl = function(url) {
				var msg;

                if (Ext.isIE) {
                    msg = 'Too many items selected (url has ' + url.length + ' characters). Internet Explorer accepts maximum 2048 characters.';
                }
                else {
					var len = url.length > 8000 ? '8000' : (url.length > 4000 ? '4000' : '2000');
					msg = 'Too many items selected (url has ' + url.length + ' characters). Please reduce to less than ' + len + ' characters.';
                }

                msg += '\n\n' + 'Hint: A good way to reduce the number of items is to use relative periods and level/group organisation unit selection modes.';

                alert(msg);
			};

			// report
			web.report = {};

				// aggregate
			web.report.aggregate = {};

			web.report.aggregate.sort = function(xLayout, xResponse, xColAxis) {
				var condoId = xLayout.sorting.id,
					name = xLayout.rows[0].dimension,
					ids = xResponse.nameHeaderMap[name].ids,
					valueMap = xResponse.idValueMap,
					direction = xLayout.sorting ? xLayout.sorting.direction : 'DESC',
					objects = [],
					layout;

				// relative id?
				if (Ext.isString(condoId)) {
					condoId = condoId.toLowerCase() === 'total' ? 'total_' : condoId;
				}
				else if (Ext.isNumber(condoId)) {
					if (condoId === 0) {
						condoId = 'total_';
					}
					else {
						condoId = xColAxis.ids[parseInt(condoId) - 1];
					}
				}
				else {
					return xResponse;
				}

				// collect values
				for (var i = 0, key, value; i < ids.length; i++) {
					key = condoId + ids[i];
					value = parseFloat(valueMap[key]);

					objects.push({
						id: ids[i],
						value: Ext.isNumber(value) ? value : (Number.MAX_VALUE * -1)
					});
				}

				support.prototype.array.sort(objects, direction, 'value');

				// new id order
				xResponse.nameHeaderMap[name].ids = Ext.Array.pluck(objects, 'id');

				return xResponse;
			};

			web.report.aggregate.getHtml = function(xLayout, xResponse, xColAxis, xRowAxis) {
				var getRoundedHtmlValue,
					getTdHtml,
					doSubTotals,
					doTotals,
					getColAxisHtmlArray,
					getRowHtmlArray,
					rowAxisHtmlArray,
					getColTotalHtmlArray,
					getGrandTotalHtmlArray,
					getTotalHtmlArray,
					getHtml,
					getUniqueFactor = function(xAxis) {
						if (!xAxis) {
							return null;
						}

						var unique = xAxis.xItems.unique;

						if (unique) {
							return unique.length < 2 ? 1 : (xAxis.size / unique[0].length);
						}

						return null;
					},
					colUniqueFactor = getUniqueFactor(xColAxis),
					rowUniqueFactor = getUniqueFactor(xRowAxis),
					valueItems = [],
					valueObjects = [],
					totalColObjects = [],
					uuidDimUuidsMap = {},
					isLegendSet = Ext.isObject(xLayout.legendSet) && Ext.isArray(xLayout.legendSet.mapLegends) && xLayout.legendSet.mapLegends.length,
                    tdCount = 0,
					htmlArray;

				xResponse.sortableIdObjects = [];

				getRoundedHtmlValue = function(value, dec) {
					dec = dec || 2;
					return parseFloat(support.prototype.number.roundIf(value, 2)).toString();
				};

				getTdHtml = function(config, metaDataId) {
					var bgColor,
						mapLegends,
						colSpan,
						rowSpan,
						htmlValue,
						displayDensity,
						fontSize,
						isNumeric = Ext.isObject(config) && Ext.isString(config.type) && config.type.substr(0,5) === 'value' && !config.empty,
						isValue = Ext.isObject(config) && Ext.isString(config.type) && config.type === 'value' && !config.empty,
						cls = '',
						html = '';

					if (!Ext.isObject(config)) {
						return '';
					}

					if (config.hidden || config.collapsed) {
						return '';
					}

                    // number of cells
                    tdCount = tdCount + 1;

					// background color from legend set
					if (isNumeric && xLayout.legendSet) {
						var value = parseFloat(config.value);
						mapLegends = xLayout.legendSet.mapLegends;

						for (var i = 0; i < mapLegends.length; i++) {
							if (Ext.Number.constrain(value, mapLegends[i].startValue, mapLegends[i].endValue) === value) {
								bgColor = mapLegends[i].color;
							}
						}
					}

					colSpan = config.colSpan ? 'colspan="' + config.colSpan + '" ' : '';
					rowSpan = config.rowSpan ? 'rowspan="' + config.rowSpan + '" ' : '';
					htmlValue = config.collapsed ? '' : config.htmlValue || config.value || '';
					htmlValue = config.type !== 'dimension' ? support.prototype.number.prettyPrint(htmlValue, xLayout.digitGroupSeparator) : htmlValue;
					displayDensity = conf.report.displayDensity[config.displayDensity] || conf.report.displayDensity[xLayout.displayDensity];
					fontSize = conf.report.fontSize[config.fontSize] || conf.report.fontSize[xLayout.fontSize];

					cls += config.hidden ? ' td-hidden' : '';
					cls += config.collapsed ? ' td-collapsed' : '';
					//cls += isValue ? ' pointer' : '';
					cls += bgColor ? ' legend' : (config.cls ? ' ' + config.cls : '');

					// sorting
					if (Ext.isString(metaDataId)) {
						cls += ' td-sortable';

						xResponse.sortableIdObjects.push({
							id: metaDataId,
							uuid: config.uuid
						});
					}

					html += '<td ' + (config.uuid ? ('id="' + config.uuid + '" ') : '');
					html += ' class="' + cls + '" ' + colSpan + rowSpan;


					//if (bgColor) {
						//html += '>';
						//html += '<div class="legendCt">';
						//html += '<div class="number ' + config.cls + '" style="padding:' + displayDensity + '; padding-right:3px; font-size:' + fontSize + '">' + htmlValue + '</div>';
						//html += '<div class="arrowCt ' + config.cls + '">';
						//html += '<div class="arrow" style="border-bottom:8px solid transparent; border-right:8px solid ' + bgColor + '">&nbsp;</div>';
						//html += '</div></div></div></td>';

						//cls = 'legend';
						//cls += config.hidden ? ' td-hidden' : '';
						//cls += config.collapsed ? ' td-collapsed' : '';

						//html += '<td class="' + cls + '" ';
						//html += colSpan + rowSpan + '>';
						//html += '<div class="legendCt">';
						//html += '<div style="display:table-cell; padding:' + displayDensity + '; font-size:' + fontSize + '"';
						//html += config.cls ? ' class="' + config.cls + '">' : '';
						//html += htmlValue + '</div>';
						//html += '<div class="legendColor" style="background-color:' + bgColor + '">&nbsp;</div>';
						//html += '</div></td>';
					//}
					//else {
						//html += 'style="padding:' + displayDensity + '; font-size:' + fontSize + ';"' + '>' + htmlValue + '</td>';
                        html += 'style="' + (bgColor && isValue ? 'color:' + bgColor + '; ' : '') + 'padding:' + displayDensity + '; font-size:' + fontSize + ';"' + '>' + htmlValue + '</td>';
					//}

					return html;
				};

				doSubTotals = function(xAxis) {
					return !!xLayout.showSubTotals && xAxis && xAxis.dims > 1;
				};

				doTotals = function() {
					return !!xLayout.showTotals;
				};

				doSortableColumnHeaders = function() {
					return (xRowAxis && xRowAxis.dims === 1);
				};

				getColAxisHtmlArray = function() {
					var a = [],
						getEmptyHtmlArray;

					getEmptyHtmlArray = function() {
						return (xColAxis && xRowAxis) ? getTdHtml({
							cls: 'pivot-dim-empty cursor-default',
							colSpan: xRowAxis.dims,
							rowSpan: xColAxis.dims,
							htmlValue: '&nbsp;'
						}) : '';
					};

					if (!(xColAxis && Ext.isObject(xColAxis))) {
						return a;
					}

					// for each col dimension
					for (var i = 0, dimHtml; i < xColAxis.dims; i++) {
						dimHtml = [];

						if (i === 0) {
							dimHtml.push(getEmptyHtmlArray());
						}

						for (var j = 0, obj, spanCount = 0, condoId, totalId; j < xColAxis.size; j++) {
							spanCount++;
							condoId = null;
							totalId = null;

							obj = xColAxis.objects.all[i][j];
							obj.type = 'dimension';
							obj.cls = 'pivot-dim';
							obj.noBreak = false;
							obj.hidden = !(obj.rowSpan || obj.colSpan);
							obj.htmlValue = service.layout.getItemName(xLayout, xResponse, obj.id, true);

							// sortable column headers. last dim only.
							if (i === xColAxis.dims - 1 && doSortableColumnHeaders()) {
								//condoId = xColAxis.ids[j].split('-').join('');
								condoId = xColAxis.ids[j];
							}

							dimHtml.push(getTdHtml(obj, condoId));

							if (i === 0 && spanCount === xColAxis.span[i] && doSubTotals(xColAxis) ) {
								dimHtml.push(getTdHtml({
									type: 'dimensionSubtotal',
									cls: 'pivot-dim-subtotal cursor-default',
									rowSpan: xColAxis.dims,
									htmlValue: '&nbsp;'
								}));

								spanCount = 0;
							}

							if (i === 0 && (j === xColAxis.size - 1) && doTotals()) {
								totalId = doSortableColumnHeaders() ? 'total_' : null;

								dimHtml.push(getTdHtml({
									uuid: Ext.data.IdGenerator.get('uuid').generate(),
									type: 'dimensionTotal',
									cls: 'pivot-dim-total',
									rowSpan: xColAxis.dims,
									htmlValue: 'Total'
								}, totalId));
							}
						}

						a.push(dimHtml);
					}

					return a;
				};

				getRowHtmlArray = function() {
					var a = [],
						axisAllObjects = [],
						xValueObjects,
						totalValueObjects = [],
						mergedObjects = [],
						valueItemsCopy,
						colAxisSize = xColAxis ? xColAxis.size : 1,
						rowAxisSize = xRowAxis ? xRowAxis.size : 1,
						recursiveReduce;

					recursiveReduce = function(obj) {
						if (!obj.children) {
							obj.collapsed = true;

							if (obj.parent) {
								obj.parent.oldestSibling.children--;
							}
						}

						if (obj.parent) {
							recursiveReduce(obj.parent.oldestSibling);
						}
					};

					// dimension
					if (xRowAxis) {
						var aLineBreak = new Array(xRowAxis.dims);

						for (var i = 0, row; i < xRowAxis.size; i++) {
							row = [];

							for (var j = 0, obj, newObj; j < xRowAxis.dims; j++) {
								obj = xRowAxis.objects.all[j][i];
								obj.type = 'dimension';
								obj.cls = 'pivot-dim ' + (service.layout.isHierarchy(xLayout, xResponse, obj.id) ? ' align-left' : '');
								obj.noBreak = true;
								obj.hidden = !(obj.rowSpan || obj.colSpan);
								obj.htmlValue = service.layout.getItemName(xLayout, xResponse, obj.id, true);

								row.push(obj);

								// allow line break for this dim?
								if (obj.htmlValue.length > 50) {
									aLineBreak[j] = true;
								}
							}

							axisAllObjects.push(row);
						}

						// add nowrap line break cls
						for (var i = 0, dim; i < aLineBreak.length; i++) {
							dim = aLineBreak[i];

							if (!dim) {
								for (var j = 0, obj; j < xRowAxis.size; j++) {
									obj = axisAllObjects[j][i];

									obj.cls += ' td-nobreak';
									obj.noBreak = true;
								}
							}
						}
					}
	//axisAllObjects = [ [ dim, dim ]
	//				     [ dim, dim ]
	//				     [ dim, dim ]
	//				     [ dim, dim ] ];

					// value
					for (var i = 0, valueItemsRow, valueObjectsRow, idValueMap = xResponse.idValueMap; i < rowAxisSize; i++) {
						valueItemsRow = [];
						valueObjectsRow = [];

						for (var j = 0, id, value, htmlValue, empty, uuid, uuids; j < colAxisSize; j++) {
							empty = false;
							uuids = [];

							// meta data uid
							//id = (xColAxis ? support.prototype.str.replaceAll(xColAxis.ids[j], '-', '') : '') + (xRowAxis ? support.prototype.str.replaceAll(xRowAxis.ids[i], '-', '') : '');
							id = (xColAxis ? xColAxis.ids[j] : '') + (xRowAxis ? xRowAxis.ids[i] : '');

							// value html element id
							uuid = Ext.data.IdGenerator.get('uuid').generate();

							// get uuids array from colaxis/rowaxis leaf
							if (xColAxis) {
								uuids = uuids.concat(xColAxis.objects.all[xColAxis.dims - 1][j].uuids);
							}
							if (xRowAxis) {
								uuids = uuids.concat(xRowAxis.objects.all[xRowAxis.dims - 1][i].uuids);
							}

							if (idValueMap[id]) {
								value = parseFloat(idValueMap[id]);
								htmlValue = value.toString();
							}
							else {
								value = 0;
								htmlValue = '&nbsp;';
								empty = true;
							}

							valueItemsRow.push(value);
							valueObjectsRow.push({
								uuid: uuid,
								type: 'value',
								cls: 'pivot-value' + (empty ? ' cursor-default' : ''),
								value: value,
								htmlValue: htmlValue,
								empty: empty,
								uuids: uuids
							});

							// map element id to dim element ids
							uuidDimUuidsMap[uuid] = uuids;
						}

						valueItems.push(valueItemsRow);
						valueObjects.push(valueObjectsRow);
					}

					// totals
					if (xColAxis && doTotals()) {
						for (var i = 0, empty = [], total = 0; i < valueObjects.length; i++) {
							for (j = 0, obj; j < valueObjects[i].length; j++) {
								obj = valueObjects[i][j];

								empty.push(obj.empty);
								total += obj.value;
							}

							// row totals
							totalValueObjects.push({
								type: 'valueTotal',
								cls: 'pivot-value-total',
								value: total,
								htmlValue: Ext.Array.contains(empty, false) ? getRoundedHtmlValue(total) : '',
								empty: !Ext.Array.contains(empty, false)
							});

							// add row totals to idValueMap to make sorting on totals possible
							if (doSortableColumnHeaders()) {
								var totalId = 'total_' + xRowAxis.ids[i],
									isEmpty = !Ext.Array.contains(empty, false);

								xResponse.idValueMap[totalId] = isEmpty ? null : total;
							}

							empty = [];
							total = 0;
						}
					}

					// hide empty rows (dims/values/totals)
					if (xColAxis && xRowAxis) {
						if (xLayout.hideEmptyRows) {
							for (var i = 0, valueRow, isValueRowEmpty, dimLeaf; i < valueObjects.length; i++) {
								valueRow = valueObjects[i];
								isValueRowEmpty = !Ext.Array.contains(Ext.Array.pluck(valueRow, 'empty'), false);

								// if value row is empty
								if (isValueRowEmpty) {

									// Hide values by adding collapsed = true to all items
									for (var j = 0; j < valueRow.length; j++) {
										valueRow[j].collapsed = true;
									}

									// Hide totals by adding collapsed = true to all items
									if (doTotals()) {
										totalValueObjects[i].collapsed = true;
									}

									// Hide/reduce parent dim span
									dimLeaf = axisAllObjects[i][xRowAxis.dims-1];
									recursiveReduce(dimLeaf);
								}
							}
						}
					}

					xValueObjects = valueObjects;

					// col subtotals
					if (doSubTotals(xColAxis)) {
						var tmpValueObjects = [];

						for (var i = 0, row, rowSubTotal, colCount; i < xValueObjects.length; i++) {
							row = [];
							rowSubTotal = 0;
							colCount = 0;

							for (var j = 0, item, collapsed = [], empty = []; j < xValueObjects[i].length; j++) {
								item = xValueObjects[i][j];
								rowSubTotal += item.value;
								empty.push(!!item.empty);
								collapsed.push(!!item.collapsed);
								colCount++;

								row.push(item);

								if (colCount === colUniqueFactor) {
									var isEmpty = !Ext.Array.contains(empty, false);
									row.push({
										type: 'valueSubtotal',
										cls: 'pivot-value-subtotal' + (isEmpty ? ' cursor-default' : ''),
										value: rowSubTotal,
										htmlValue: isEmpty ? '&nbsp;' : getRoundedHtmlValue(rowSubTotal),
										empty: isEmpty,
										collapsed: !Ext.Array.contains(collapsed, false)
									});

									colCount = 0;
									rowSubTotal = 0;
									empty = [];
									collapsed = [];
								}
							}

							tmpValueObjects.push(row);
						}

						xValueObjects = tmpValueObjects;
					}

					// row subtotals
					if (doSubTotals(xRowAxis)) {
						var tmpAxisAllObjects = [],
							tmpValueObjects = [],
							tmpTotalValueObjects = [],
							getAxisSubTotalRow;

						getAxisSubTotalRow = function(collapsed) {
							var row = [];

							for (var i = 0, obj; i < xRowAxis.dims; i++) {
								obj = {};
								obj.type = 'dimensionSubtotal';
								obj.cls = 'pivot-dim-subtotal cursor-default';
								obj.collapsed = Ext.Array.contains(collapsed, true);

								if (i === 0) {
									obj.htmlValue = '&nbsp;';
									obj.colSpan = xRowAxis.dims;
								}
								else {
									obj.hidden = true;
								}

								row.push(obj);
							}

							return row;
						};

						// tmpAxisAllObjects
						for (var i = 0, row, collapsed = []; i < axisAllObjects.length; i++) {
							tmpAxisAllObjects.push(axisAllObjects[i]);
							collapsed.push(!!axisAllObjects[i][0].collapsed);

							// Insert subtotal after last objects
							if (!Ext.isArray(axisAllObjects[i+1]) || !!axisAllObjects[i+1][0].root) {
								tmpAxisAllObjects.push(getAxisSubTotalRow(collapsed));

								collapsed = [];
							}
						}

						// tmpValueObjects
						for (var i = 0; i < tmpAxisAllObjects.length; i++) {
							tmpValueObjects.push([]);
						}

						for (var i = 0; i < xValueObjects[0].length; i++) {
							for (var j = 0, rowCount = 0, tmpCount = 0, subTotal = 0, empty = [], collapsed, item; j < xValueObjects.length; j++) {
								item = xValueObjects[j][i];
								tmpValueObjects[tmpCount++].push(item);
								subTotal += item.value;
								empty.push(!!item.empty);
								rowCount++;

								if (axisAllObjects[j][0].root) {
									collapsed = !!axisAllObjects[j][0].collapsed;
								}

								if (!Ext.isArray(axisAllObjects[j+1]) || axisAllObjects[j+1][0].root) {
									var isEmpty = !Ext.Array.contains(empty, false);

									tmpValueObjects[tmpCount++].push({
										type: item.type === 'value' ? 'valueSubtotal' : 'valueSubtotalTotal',
										value: subTotal,
										htmlValue: isEmpty ? '&nbsp;' : getRoundedHtmlValue(subTotal),
										collapsed: collapsed,
										cls: (item.type === 'value' ? 'pivot-value-subtotal' : 'pivot-value-subtotal-total') + (isEmpty ? ' cursor-default' : '')
									});
									rowCount = 0;
									subTotal = 0;
									empty = [];
								}
							}
						}

						// tmpTotalValueObjects
						for (var i = 0, obj, collapsed = [], empty = [], subTotal = 0, count = 0; i < totalValueObjects.length; i++) {
							obj = totalValueObjects[i];
							tmpTotalValueObjects.push(obj);

							collapsed.push(!!obj.collapsed);
							empty.push(!!obj.empty);
							subTotal += obj.value;
							count++;

							if (count === xRowAxis.span[0]) {
								var isEmpty = !Ext.Array.contains(empty, false);

								tmpTotalValueObjects.push({
									type: 'valueTotalSubgrandtotal',
									cls: 'pivot-value-total-subgrandtotal' + (isEmpty ? ' cursor-default' : ''),
									value: subTotal,
									htmlValue: isEmpty ? '&nbsp;' : getRoundedHtmlValue(subTotal),
									empty: isEmpty,
									collapsed: !Ext.Array.contains(collapsed, false)
								});

								collapsed = [];
								empty = [];
								subTotal = 0;
								count = 0;
							}
						}

						axisAllObjects = tmpAxisAllObjects;
						xValueObjects = tmpValueObjects;
						totalValueObjects = tmpTotalValueObjects;
					}

					// Merge dim, value, total
					for (var i = 0, row; i < xValueObjects.length; i++) {
						row = [];

						if (xRowAxis) {
							row = row.concat(axisAllObjects[i]);
						}

						row = row.concat(xValueObjects[i]);

						if (xColAxis) {
							row = row.concat(totalValueObjects[i]);
						}

						mergedObjects.push(row);
					}

					// Create html items
					for (var i = 0, row; i < mergedObjects.length; i++) {
						row = [];

						for (var j = 0; j < mergedObjects[i].length; j++) {
							row.push(getTdHtml(mergedObjects[i][j]));
						}

						a.push(row);
					}

					return a;
				};

				getColTotalHtmlArray = function() {
					var a = [];

					if (xRowAxis && doTotals()) {
						var xTotalColObjects;

						// Total col items
						for (var i = 0, total = 0, empty = []; i < valueObjects[0].length; i++) {
							for (var j = 0, obj; j < valueObjects.length; j++) {
								obj = valueObjects[j][i];

								total += obj.value;
								empty.push(!!obj.empty);
							}

							// col total
							totalColObjects.push({
								type: 'valueTotal',
								value: total,
								htmlValue: Ext.Array.contains(empty, false) ? getRoundedHtmlValue(total) : '',
								empty: !Ext.Array.contains(empty, false),
								cls: 'pivot-value-total'
							});

							total = 0;
							empty = [];
						}

						xTotalColObjects = totalColObjects;

						if (xColAxis && doSubTotals(xColAxis)) {
							var tmp = [];

							for (var i = 0, item, subTotal = 0, empty = [], colCount = 0; i < xTotalColObjects.length; i++) {
								item = xTotalColObjects[i];
								tmp.push(item);
								subTotal += item.value;
								empty.push(!!item.empty);
								colCount++;

								if (colCount === colUniqueFactor) {
									tmp.push({
										type: 'valueTotalSubgrandtotal',
										value: subTotal,
										htmlValue: Ext.Array.contains(empty, false) ? getRoundedHtmlValue(subTotal) : '',
										empty: !Ext.Array.contains(empty, false),
										cls: 'pivot-value-total-subgrandtotal'
									});

									subTotal = 0;
									colCount = 0;
								}
							}

							xTotalColObjects = tmp;
						}

						// Total col html items
						for (var i = 0; i < xTotalColObjects.length; i++) {
							a.push(getTdHtml(xTotalColObjects[i]));
						}
					}

					return a;
				};

				getGrandTotalHtmlArray = function() {
					var total = 0,
						empty = [],
						a = [];

					if (doTotals()) {
						for (var i = 0, obj; i < totalColObjects.length; i++) {
							obj = totalColObjects[i];

							total += obj.value;
							empty.push(obj.empty);
						}

						if (xColAxis && xRowAxis) {
							a.push(getTdHtml({
								type: 'valueGrandTotal',
								cls: 'pivot-value-grandtotal',
								value: total,
								htmlValue: Ext.Array.contains(empty, false) ? getRoundedHtmlValue(total) : '',
								empty: !Ext.Array.contains(empty, false)
							}));
						}
					}

					return a;
				};

				getTotalHtmlArray = function() {
					var dimTotalArray,
						colTotal = getColTotalHtmlArray(),
						grandTotal = getGrandTotalHtmlArray(),
						row,
						a = [];

					if (doTotals()) {
						if (xRowAxis) {
							dimTotalArray = [getTdHtml({
								type: 'dimensionSubtotal',
								cls: 'pivot-dim-total',
								colSpan: xRowAxis.dims,
								htmlValue: 'Total'
							})];
						}

						row = [].concat(dimTotalArray || [], colTotal || [], grandTotal || []);

						a.push(row);
					}

					return a;
				};

				getHtml = function() {
					var s = '<table id="' + xLayout.tableUuid + '" class="pivot">';

					for (var i = 0; i < htmlArray.length; i++) {
						s += '<tr>' + htmlArray[i].join('') + '</tr>';
					}

					return s += '</table>';
				};

				// get html
				return function() {
					htmlArray = Ext.Array.clean([].concat(getColAxisHtmlArray() || [], getRowHtmlArray() || [], getTotalHtmlArray() || []));

					return {
						html: getHtml(htmlArray),
						uuidDimUuidsMap: uuidDimUuidsMap,
						xColAxis: xColAxis,
						xRowAxis: xRowAxis,
                        tdCount: tdCount
					};
				}();
			};

				// query
			web.report.query = {};

			web.report.query.sort = function(layout, xResponse) {
				var id = layout.sorting.id,
					direction = layout.sorting ? layout.sorting.direction : 'DESC',
					index = xResponse.nameHeaderMap[id].index,
					rows = xResponse.rows;

				support.prototype.array.sort(rows, direction, index);

				return xResponse;
			};

			web.report.query.format = function(str) {
				var n = parseFloat(str);

				if (!Ext.isNumber(n)) {
					return str;
				}

				if (new Date(str).toString() === 'Invalid Date') {
					return n;
				}

				return str;
			};

			web.report.query.getHtml = function(layout, xResponse) {
				var dimensionHeaders = xResponse.dimensionHeaders,
					rows = xResponse.rows,
					tableCls = 'pivot',
					html = '';

				xResponse.sortableIdObjects = [];

				tableCls += layout.displayDensity ? ' ' + layout.displayDensity : '';
				tableCls += layout.fontSize ? ' ' + layout.fontSize : '';

				html += '<table class="' + tableCls + '"><tr>';

				// get header indexes
				for (var i = 0, header, uuid; i < dimensionHeaders.length; i++) {
					header = dimensionHeaders[i];
					uuid = Ext.data.IdGenerator.get('uuid').generate();

					html += '<td id="' + uuid + '" class="pivot-dim td-sortable">' + header.column + '</td>';

					xResponse.sortableIdObjects.push({
						id: header.name,
						uuid: uuid
					});
				}

				html += '</tr>';

				// rows
				for (var i = 0, row; i < rows.length; i++) {
					row = rows[i];
					html += '<tr>';

					for (var j = 0, str, header, name; j < dimensionHeaders.length; j++) {
						header = dimensionHeaders[j];
						str = row[header.index];
						name = web.report.query.format(str);

						//if (header.name === 'ouname' && layout.showHierarchy) {
							//var a = Ext.Array.clean(name.split('/'));
							//name = '';

							//for (var k = 0, isLast; k < a.length; k++) {
								//isLast = !!(i === a.length - 1);

								//name += (!isLast ? '<span class="text-weak">' : '') + a[i] + (!isLast ? '</span>' : '') + (!isLast ? ' / ' : '');
							//}
						//}

						html += '<td class="pivot-value align-left">' + name + '</td>';
					}

					html += '</tr>';
				}

				html += '</table>';

				return {
					html: html
				};
			};

		}());

		// extend init
		(function() {

			// sort and extend dynamic dimensions
			if (Ext.isArray(init.dimensions)) {
				support.prototype.array.sort(init.dimensions);

				for (var i = 0, dim; i < init.dimensions.length; i++) {
					dim = init.dimensions[i];
					dim.dimensionName = dim.id;
					dim.objectName = conf.finals.dimension.dimension.objectName;
					conf.finals.dimension.objectNameMap[dim.id] = dim;
				}
			}

			// sort ouc
			if (init.user && init.user.ouc) {
				support.prototype.array.sort(init.user.ouc);
			}

			// legend set map
			//init.idLegendSetMap = {};

			//for (var i = 0, set; i < init.legendSets.length; i++) {
				//set = init.legendSets[i];
				//init.idLegendSetMap[set.id] = set;
			//}
		}());

		// instance
		return {
			conf: conf,
			api: api,
			support: support,
			service: service,
			web: web,
			init: init
		};
	};
});
