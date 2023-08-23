$(function() {
    var viewport;
    var nominalWidth = 700;
    var smallWidth = 230;
    var charts = {};
    // Global options for markdown charts
    Highcharts.setOptions({
        lang: {
            thousandsSep: ','
        }
    });

    var chartContainer = $(".markdown-chart");
    if (!chartContainer.length) {
        return;
    }

    //set the annotation and chart size based on the viewport
    if ($("body").hasClass("viewport-sm")) {
        viewport = 'sm';
    }else if ($("body").hasClass("viewport-md")) {
        viewport = 'md';
    }else  {
        viewport = 'lg';
    }

    /**
     * Create a global getSVG method that takes an array of charts as an
     * argument
     */
    Highcharts.getSVG = function (charts) {
        var svgArr = [],
        topBorder =20,
            top = topBorder,
            left = 0,
            width = 0,
            height = 0,
            columns = 0,
            rows = 0,
            maxColumns = 3;

        Highcharts.each(charts, function (chart) {
            var svg = chart.getSVG(),
                // Get width/height of SVG for export
                svgWidth = +svg.match(
                    /^<svg[^>]*width\s*=\s*\"?(\d+)\"?[^>]*>/
                )[1],
                svgHeight = +svg.match(
                    /^<svg[^>]*height\s*=\s*\"?(\d+)\"?[^>]*>/
                )[1];

            svg = svg.replace(
                '<svg',
                '<g transform="translate(' + left + ',' + top + ')" '
            );
            svg = svg.replace('</svg>', '</g>');

            //lay out the svg in columns and rows
            columns++;
            if(columns>=maxColumns){
                columns = 0;
                left = 0;
                rows++;
                top = svgHeight * rows + topBorder;
                height = top + svgHeight;
            }else{
                left += svgWidth;
                width += svgWidth;
            }

            svgArr.push(svg);
        });
        
        return '<svg height="' + height + '" width="' + width +
            '" version="1.1" xmlns="http://www.w3.org/2000/svg">' +
            '<rect x="0" y="0" width="' + width + '" height="' + height + '" fill="white" />' +
            svgArr.join('') + '</svg>';
    };

    /**
     * Create a global exportCharts method that takes an array of charts as an
     * argument, and exporting options as the second argument
     */
    Highcharts.exportCharts = function (charts, options) {
        var svg;
        // Merge the options
        options = Highcharts.merge(Highcharts.getOptions().exporting, options);

        // get export url fromm the chart config
        if(options.exporting.url){
            options.url = options.exporting.url;
        }

        if(charts.length>1){
            svg = Highcharts.getSVG(charts)
        }else{
            svg = charts[0];
        }

        // Post to export server
        Highcharts.post(options.url, {
            filename: options.filename || 'chart',
            type: options.type,
            width: options.width,
            svg: svg
        });
    };

    $('#export-png').click(function () {
        Highcharts.exportCharts(chartList, chartConfig);
    });


    var chartList = [];
    var chartConfig;
    var chart;

    chartContainer.each(function() {
        var $this = $(this);
        var id = $this.attr('id');
        var chartId = $this.data('filename');
        var chartWidth = $this.width();
        var chartUri = $this.data('uri'); 

        $this.width(smallWidth);
        $this.empty();

        //Read chart configuration from server using container's width
        var jqxhr = $.ajax({
            url: "/chartconfig",
            data: {
                uri: chartUri,
                width: nominalWidth
            },
            success: function() {
                chartConfig = window["chart-" + chartId];

                // remove the title, subtitle and any renderers for client side display
                // these are only used by the template for export/printing
                chartConfig.chart.marginTop = null;
                chartConfig.chart.marginBottom = null;
                //use this to adjust the render y position based on the height of marginTop
                chartConfig.chart.offset = 0;
                chartConfig.title = {text:''};
                chartConfig.subtitle = {text:''};
                
                if(chartConfig.legend.verticalAlign==='top'){
                    chartConfig.legend.y = -10;
                }else{
                    chartConfig.legend.y = 10;
                }
                
                chartConfig.viewport = viewport;


                if (chartConfig) {
                    // small multiples have an attribute to show specific series
                    var smallMultipleSeries = $this.data('series');
                    var smallMultipleRef = id.split("-")[2];

                    //adjust size and notes to match viewport
                    var aspectRatio = parseFloat(chartConfig.aspectRatio);
                    
                    //if we have devices then set annotations dependant on viewport
                    if(chartConfig.devices){
                        if(chartConfig.devices[viewport]){

                            //set the aspect ratio
                            aspectRatio = chartConfig.devices[viewport].aspectRatio;

                            if(!chartConfig.devices[viewport].isHidden){

                                chartConfig.xAxis.tickInterval = chartConfig.devices[viewport].labelInterval;

                                //loop thru and update annotations if reqd
                                if(chartConfig.annotations.length>0){
                                    $.each(chartConfig.annotations, function(idx, itm){
                                        chartConfig.annotations[idx].x = chartConfig.annotations[idx]['position_'+viewport].x;
                                        chartConfig.annotations[idx].y = chartConfig.annotations[idx]['position_'+viewport].y;
                                    })
                                }
                                //loop thru X AXIS plotline
                                if(chartConfig.xAxis.plotLines){

                                    if(chartConfig.xAxis.plotLines.length>0){
                                        $.each(chartConfig.xAxis.plotLines, function(idx, itm){
                                            chartConfig.xAxis.plotLines[idx].value = chartConfig.xAxis.plotLines[idx]['position_'+viewport].x;
                                        })
                                    }

                                }

                                //loop thru Y AXIS plotline
                                if(chartConfig.yAxis.plotLines){

                                    if(chartConfig.yAxis.plotLines.length>0){
                                        $.each(chartConfig.yAxis.plotLines, function(idx, itm){
                                            chartConfig.yAxis.plotLines[idx].value = chartConfig.yAxis.plotLines[idx]['position_'+viewport].y;
                                        })
                                    }

                                }

                            }else{
                                //add hidden notes to footnotes
                                var str = '<h6 class="flush--third--bottom js-notes-title">Annotations:</h6><ol>';

                                $.each(chartConfig.annotations, function(idx, itm){
                                    str+= '<li>'+itm.title+'</li>'
                                })

                                str+='</ol>';
                                $('#notes-'+chartId).append(str);
                                // clear any defaults
                                chartConfig.xAxis.plotBands = [];
                                chartConfig.xAxis.plotLines = [];
                                chartConfig.yAxis.plotBands = [];
                                chartConfig.yAxis.plotLines = [];
                                chartConfig.annotations = [];
                            }
                        }
                    }

                    if(smallMultipleSeries){
                        $('.sm-multiple-holder').width(nominalWidth);
                        //pick individual series from whole chart series and create mini-charts
                        var tempSeries = chartConfig.series;
                        chartConfig.chart.width = smallWidth;
                        chartConfig.chart.height = smallWidth;
                        chartConfig.isSmallMultiple = 'small-multiples';
                        
                        chartConfig.series = [tempSeries[smallMultipleRef]];
                        chartConfig.chart.renderTo = id;
                        chart = new Highcharts.Chart(chartConfig);
                        //add to array for printing
                        chartList.push(chart);

                    }else{

                        if(chartConfig.chart.type==='table'){

                        }else{
                            // Build chart from config endpoint
                            chartConfig.chart.renderTo = id;
                            chartConfig.annotations = [];
                            //need to set height to override extra height for print version
                            chartConfig.chart.height = chartConfig.chart.width * aspectRatio;
                            if(viewport!=='lg' && chartConfig.series.length>3 ){
                                chartConfig.chart.height = chartConfig.chart.height + 200;
                                chartConfig.chart.marginBottom = 200;
                                chartConfig.legend.verticalAlign = 'bottom';
                            }
                            chart = new Highcharts.Chart(chartConfig);

                            charts[chartId] = chart;
                        }
                    }

                    delete window["chart-" + chartId]; //clear data from window object after rendering



                }
            }, 
            dataType: "script",
            cache: true
        });
    });
});
