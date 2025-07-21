/**
 * Chart Builder Utility - Matches Next.js implementation exactly
 * Provides a fluent API for building Highcharts configurations
 */

// Chart Builder Base Class
class ChartBuilder {
    constructor(type) {
        this.options = {
            chart: { type: type },
            series: [],
            credits: { enabled: false }
        };
    }

    getOptions() {
        return this.options;
    }

    chartOptions(opts) {
        Object.assign(this.options, opts);
        return this;
    }

    type(type) {
        this.options.chart.type = type;
        return this;
    }

    title(title) {
        this.options.title = {
            text: title,
            style: {
                fontSize: '16px',
                fontWeight: 'bold'
            }
        };
        return this;
    }

    pane(opts) {
        this.options.pane = opts;
        return this;
    }

    plotOptions(opts) {
        this.options.plotOptions = opts;
        return this;
    }

    tooltip(config) {
        this.options.tooltip = config.getOptions();
        return this;
    }

    xAxis(config) {
        this.options.xAxis = config.getOptions();
        return this;
    }

    yAxis(config) {
        this.options.yAxis = config.getOptions();
        return this;
    }

    yMultiAxis(configs) {
        this.options.yAxis = configs.map(config => config.getOptions());
        return this;
    }

    addSeries(config) {
        this.options.series.push(config);
        return this;
    }

    sortSeries(key = 'legendIndex') {
        this.options.series.sort((a, b) => {
            const aVal = a[key] || 0;
            const bVal = b[key] || 0;
            return aVal - bVal;
        });
        return this;
    }

    onLegendItemClick(handler) {
        if (!this.options.plotOptions) {
            this.options.plotOptions = {};
        }
        if (!this.options.plotOptions.series) {
            this.options.plotOptions.series = {};
        }
        if (!this.options.plotOptions.series.events) {
            this.options.plotOptions.series.events = {};
        }
        this.options.plotOptions.series.events.legendItemClick = handler;
        return this;
    }
}

// Axis Classes
class CategoryAxis {
    constructor(categories = []) {
        this.options = {
            type: 'category',
            categories: categories
        };
    }

    addCategory(val) {
        this.options.categories.push(val);
        return this;
    }

    getOptions() {
        return this.options;
    }
}

class DateAxis {
    constructor(opts = {}) {
        this.options = {
            type: 'datetime',
            ...opts
        };
    }

    getOptions() {
        return this.options;
    }
}

class WeeklyAxis {
    constructor(tickPositions = []) {
        this.options = {
            type: 'datetime',
            tickPositions: tickPositions,
            labels: {
                format: '{value:%b %d}'
            }
        };
    }

    getOptions() {
        return this.options;
    }
}

class MonthlyAxis {
    getOptions() {
        return {
            categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 
                       'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
        };
    }
}

class NumericAxis {
    constructor() {
        this.options = {
            min: 0,
            labels: {
                formatter: function() {
                    return Highcharts.numberFormat(this.value, 0);
                }
            }
        };
    }

    title(val) {
        this.options.title = { text: val };
        return this;
    }

    min(val) {
        this.options.min = val;
        return this;
    }

    max(val) {
        this.options.max = val;
        return this;
    }

    color(color) {
        if (this.options.labels) {
            this.options.labels.style = { color: color };
        }
        return this;
    }

    titleColor(color) {
        if (this.options.title) {
            this.options.title.style = { color: color };
        }
        return this;
    }

    labelColor(color) {
        if (this.options.labels) {
            this.options.labels.style = { color: color };
        }
        return this;
    }

    opposite(val) {
        this.options.opposite = val;
        return this;
    }

    getOptions() {
        return this.options;
    }
}

class GenericAxisConfig {
    constructor(options) {
        this.options = options;
    }

    getOptions() {
        return this.options;
    }
}

// Tooltip Classes
class DecimalTooltip {
    getOptions() {
        return {
            formatter: function() {
                return `<b>${this.series.name}</b><br/>
                        ${this.x}: ${Highcharts.numberFormat(this.y, 2)}`;
            }
        };
    }
}

class CustomSeriesTooltip {
    constructor(suffix) {
        this.suffix = suffix;
    }

    getOptions() {
        return {
            formatter: function() {
                return `<b>${this.series.name}</b><br/>
                        ${this.x}: ${Highcharts.numberFormat(this.y, 2)} ${this.suffix}`;
            }
        };
    }
}

class WeeklyTooltip {
    getOptions() {
        return {
            formatter: function() {
                return `<b>${this.series.name}</b><br/>
                        Week ${Highcharts.dateFormat('%U', this.x)}: ${Highcharts.numberFormat(this.y, 2)}`;
            }
        };
    }
}

class PercentageTooltip {
    getOptions() {
        return {
            formatter: function() {
                return `<b>${this.series.name}</b><br/>
                        ${this.x}: ${Highcharts.numberFormat(this.y, 1)}%`;
            }
        };
    }
}

class MonthYearTooltip {
    getOptions() {
        return {
            formatter: function() {
                return `<b>${this.series.name}</b><br/>
                        ${Highcharts.dateFormat('%b %Y', this.x)}: ${Highcharts.numberFormat(this.y, 2)}`;
            }
        };
    }
}

// Chart Color Utilities
function getChartColor(index) {
    const colors = [
        '#7cb5ec', '#434348', '#90ed7d', '#f7a35c', '#8085e9',
        '#f15c80', '#e4d354', '#2ecc71', '#e74c3c', '#3498db'
    ];
    return colors[index % colors.length];
}

// Export the classes and utilities
window.ChartBuilder = ChartBuilder;
window.CategoryAxis = CategoryAxis;
window.DateAxis = DateAxis;
window.WeeklyAxis = WeeklyAxis;
window.MonthlyAxis = MonthlyAxis;
window.NumericAxis = NumericAxis;
window.GenericAxisConfig = GenericAxisConfig;
window.DecimalTooltip = DecimalTooltip;
window.CustomSeriesTooltip = CustomSeriesTooltip;
window.WeeklyTooltip = WeeklyTooltip;
window.PercentageTooltip = PercentageTooltip;
window.MonthYearTooltip = MonthYearTooltip;
window.getChartColor = getChartColor;

console.log('📊 Chart Builder Utility loaded successfully'); 