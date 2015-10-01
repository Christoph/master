"use strict";

function toggleClass(element, className) {
        d3.select(element)
            .classed(className, function (d, i) {
                return !d3.select(element).classed(className);
            });
    }

function hexPlot() {
    //
    //
    // Variable declaration with default values
    //
    //
    
    var _chart = {};
    var formatCount = d3.format(".0f");
    var formatImportance = d3.format(".4f");
    var dataPoints = [];
    var _width = 500, _height = 500, 
        _margins = {top: 30, left: 50, right: 50, bottom: 40},
        _binSize = 20,
        _title,
        _xName, _yName,
        _dimension, _dim, _text,
        _data,
        _area,
        _x, _y,
        _xAxis, _yAxis,
        _xDomain, _yDomain,
        _hexbin,
        _zoom,
        _dict,
        _color = ["lightblue", "#3a3e4a"],
        _svg, _bodyG;


    //
    //
    // Main render function
    //
    //
    
    _chart.render = function () {
        if (!_svg) {
           definitions();

           initializeSkeleton();

           // Better start position
           showAll();
        }

        renderBins();
    };
    
    //
    //
    // Definitions
    //
    //
    
    function definitions()
    {
        // Color scale
        _color = d3.scale.linear()
            .domain([0,4])
            .range(_color)
            .interpolate(d3.interpolateLab);

        // Get X scaling
        _xDomain = d3.extent(_data, function(d) { return d.x; });

        _x = d3.scale.linear()
            .domain(_xDomain).nice()
            .range([0,quadrantWidth()]);

        
        // Get Y scaling
        _yDomain = d3.extent(_data, function(d) { return d.y; });

        _y = d3.scale.linear()
            .domain(_yDomain).nice()
            .range([quadrantHeight(),0]);

        // Hexbins
        _hexbin = d3.hexbin()
            .size([quadrantWidth(),quadrantHeight()])
            .radius(_binSize);

        // Define axis and scales
        _xAxis = d3.svg.axis()
                    .scale(_x)
                    .orient("bottom");        

        _yAxis = d3.svg.axis()
                    .scale(_y)
                    .orient("left");

        // Zoom
        _zoom = d3.behavior.zoom()
            .x(_x)
            .y(_y)
            .scaleExtent(d3.extent(_yDomain.concat(_xDomain)))
            .on("zoom", zoomed);

        // Set initial mapping
        _dict = d3.map(_data.map(function(d) { return {point: scalePoint([d.x, d.y]), text: d.text}; }), function(d) { return d.point; });
    }

    // 
    //
    // Basic rendering
    //
    //
    
    function initializeSkeleton()
    {
        var padding = 0;

        // SVG
        _svg = d3.select(_area).append("svg")
            .attr("height", _height)
            .attr("width", _width)
            .style("background-color", "white")
            .call(_zoom)
            .call(tip);
        
        // title
        _svg.append("text")
            .attr("class", "title")
            .attr("text-anchor", "middle")
            .attr("x",(_width/2))
            .attr("y", 20)
            .text(_title);

        // Number of tags
        _svg.append("text")
            .attr("class", "records")
            .attr("text-anchor", "start")
            .attr("x",_width - 220)
            .attr("y", 25)
            .on("click", showAll);

        // x axis
        _svg.append("g")
                .attr("class", "x axis")
                .attr("transform", function () {
                    return "translate(" + xStart() + "," + yStart() + ")";
                })
                .call(_xAxis);

        // x axis label
        _svg.append("text")
            .attr("class", "x label")
            .attr("text-anchor", "middle")
            .attr("x",(_width/2))
            .attr("y", _height - 5)
            .text("["+_xName+"]");

        // y axis
        _svg.append("g")
                .attr("class", "y axis")
                .attr("transform", function () {
                    return "translate(" + xStart() + "," + yEnd() + ")";
                })
                .call(_yAxis);

        // y axis label
        _svg.append("text")
            .attr("class", "y label")
            .attr("text-anchor", "middle")
            .attr("x",-(_height/2))
            .attr("y", 5)
            .attr("dy", ".75em")
            .attr("transform", "rotate(-90)")
            .text("["+_yName+"]");

        // body clip
        _svg.append("defs")
                .append("clipPath")
                .attr("id", "body-clip-hex")
                .append("rect")
                .attr("x", 0 - padding)
                .attr("y", 0)
                .attr("width", quadrantWidth() + 2 * padding)
                .attr("height", quadrantHeight());

        // create chart body
        _bodyG = _svg.append("g")
                .attr("class", "body")
                .attr("transform", "translate(" 
                        + xStart() 
                        + "," 
                        + yEnd() + ")")
                .attr("clip-path", "url(#body-clip-hex)");
    }

    //
    //
    // Render Data
    // 
    //

    function renderBins() {
        // Prepare data structure
        var data = _hexbin([].map.call(_data, function(d) { return scalePoint([d.x, d.y]); }));

        // Get data for the relaxation function
        dataPoints = [];
        data.filter(visibiltyFilter).filter(function(d) { return d.length == 1; }).forEach(function(d) {
            dataPoints.push({
                x: d.x,
                y: d.y,
                label: _dict.get(d[0]).text,
                labelX: d.x,
                labelY: d.y
            })
        })

        // Groups
        var bin = _bodyG.selectAll(".hexagon")
            .data(data.filter(visibiltyFilter).filter(function(d) { return d.length > 1; }), function(d) { return d[0]; })
            .enter()
            .append("g")
            .attr("class","hexagon");

        // Enter
        bin.append("path")
            .style("fill", function(d) { return _color(d.length); })
            .attr("d",_hexbin.hexagon(_binSize));

        bin.append("text")
            .attr("dy", ".25em")
            .attr("y", function(d) { return d.y; })
            .attr("x", function(d) { return d.x; })
            .attr("text-anchor", "middle")
            .style("fill", "black");

        _bodyG.selectAll(".place-label")
            .data(dataPoints, function(d) { return [d.x,d.y] })
            .enter()
            .append("text")
            .attr("class", "place-label")
            .attr("x", function(d) { return d.labelX; })
            .attr("y", function(d) { return d.labelY; })
            .text(function(d) { return d.label; })
            .on("mouseover",tip.show)
            .on("mouseout", tip.hide);

        // Update
        _bodyG.selectAll(".hexagon").select("path")
                .data(data, function(d) { return d[0]; })
                .attr("transform", function(d) { return "translate("+d.x+","+d.y+")"; })
                .transition()
                .duration(300)
                .attr("d", function(d) { return _hexbin.hexagon(_binSize); })
                .style("fill", function(d) { return _color(d.length); });

        _bodyG.selectAll(".hexagon").select("text")
                .data(data, function(d) { return d[0]; })
                .attr("y", function(d) { return d.y; })
                .attr("x", function(d) { return d.x; })
                .transition()
                .duration(500)
                .style("fill", "#535f6c")
                .text(function(d) { return d.length; });

        // Exit
        _bodyG.selectAll(".hexagon")
            .data(data, function(d) { return d[0]; })
            .exit()
            .remove();

        _bodyG.selectAll(".place-label")
            .data(dataPoints, function(d) { return [d.x, d.y] })
            .exit()
            .remove();

        // Update text
        d3.select(".records").text("show all tags: "+_data.length);
        //relax();
}

    //
    //
    // Internal Functions
    //
    //

    function xStart() {
        return _margins.left;
    }

    function yStart() {
        return _height - _margins.bottom;
    }

    function xEnd() {
        return _width - _margins.right;
    }

    function yEnd() {
        return _margins.top;
    }

    function quadrantWidth() {
        return _width - _margins.left - _margins.right;
    }

    function quadrantHeight() {
        return _height - _margins.top - _margins.bottom;
    }

    function getXDomain() {
        return d3.extent(_data, function(d) { return d.y });
    }

    function getYDomain() {
        return d3.extent(_data, function(d) { return d.y });
    }

    function getCurrentXDomain() {
        var domain = d3.extent(_data, function(d) { return d.x });
        return [domain[0]-(_xDomain[1]/15),domain[1]+(_xDomain[1]/15)];
    }

    function getCurrentYDomain() {
        var domain = d3.extent(_data, function(d) { return d.y }); 
        return [domain[0]-(_yDomain[1]/20),domain[1]+(_yDomain[1]/20)];
    }

    function scalePoint(point) {
        return [_x(point[0]), _y(point[1])];
    }
    
    // Dont draw DOM elements out of vision
    function visibiltyFilter(d) {
        if(d.x < 0||d.x>quadrantWidth()) { return false; }
        if(d.y < 0||d.y>quadrantHeight()) { return false; }
        return true;
    }
    
    // Zoom functions
    function zoomed() {
      // Update mapping
      _dict = d3.map(_data.map(function(d) { return {point: scalePoint([d.x, d.y]), text: d.text}; }), function(d) { return d.point; });

      renderBins();

      _svg.select(".x.axis").call(_xAxis);
      _svg.select(".y.axis").call(_yAxis);

    }

    // Show all entrys
    function showAll() {
      d3.transition().duration(750).tween("zoom", function() {
        var ix = d3.interpolate(_x.domain(), getCurrentXDomain()),
            iy = d3.interpolate(_y.domain(), getCurrentYDomain());
        return function(t) {
          _zoom.x(_x.domain(ix(t))).y(_y.domain(iy(t)));
          zoomed();
        };
      });
    }

    // Moves element to the front
    d3.selection.prototype.moveToFront = function() { 
      return this.each(function() { 
        this.parentNode.appendChild(this); 
      }); 
    }; 

    // Label relaxing
    var relax = function() {
        var again = true;
        var spacingX = 50;
        var spacingY = 10;
        var step = .5;
        while(again) {
            again = false
            dataPoints.forEach(function(a,index) {
                dataPoints.slice(index+1).forEach(function(b) {
                    // Checking bounding boxes and moving them
                    var dy = a.labelY - b.labelY;
                    var dx = a.labelX - b.labelX;
                    if(Math.abs(dy) < spacingY && Math.abs(dx) < spacingX) {
                        var sign = (dy > 0) ? 1 : -1;
                        var deltaPos = sign*step;
                        a.labelY += deltaPos;
                        b.labelY -= deltaPos;
                        again = true;
                    }
                });
            });
        }

        //Update
        _bodyG.selectAll("text.place-label")
            .attr("y",function(d) { return d.labelY })
            .moveToFront();
    }

    var tip = d3.tip()
      .attr('class', 'd3-tip')
      .offset([-10, 0])
      .html(function(d) {
        return "<strong>"+_xName+":</strong> <span>" + formatImportance(_x.invert(d.x)) + "</span><br>" + "<strong>"+_yName+":</strong> <span>" + Math.round(_y.invert(d.y)) + "</span>";
      })

    // 
    //
    // External function
    //
    //

    _chart.width = function (w) {
        if (!arguments.length) return _width;
        _width = w;
        return _chart;
    };

    _chart.height = function (h) {
        if (!arguments.length) return _height;
        _height = h;
        return _chart;
    };

    _chart.margins = function (m) {
        if (!arguments.length) return _margins;
        _margins = m;
        return _chart;
    };

    _chart.colors = function (c) {
        if (!arguments.length) return _color;
        _color = c;
        return _chart;
    };

    _chart.json = function (d) {
        if (arguments.length < 1) {
            return _data;
        }
        _data = d;
        return _chart;
    };

    _chart.area = function (a) {
        if (!arguments.length) return _area;
        _area = a;
        return _chart;
    };

    _chart.binSize = function (b) {
        if (!arguments.length) return _binSize;
        _binSize = b;
        return _chart;
    };

    _chart.axisNames = function (a,b) {
        if (!arguments.length) return _xName, _yName;
        _xName = a;
        _yName = b;
        return _chart;
    };

    return _chart;
}

function histogram() {
    //
    //
    // Variable declaration
    //
    //
    
    var _chart = {};

    var _width = 600, _height = 250,
        _margins = {top: 30, left: 70, right: 50, bottom: 40},
        _colors = d3.scale.category10(),
        _title,
        _x, _y,
        _xAxis, _yAxis,
        _xName, _yName,
        _svg,
        _data,
        _bins, _binWidth,
        _ticks,
        _dimension,
        _filter, 
        _chartDiv,
        _hist,
        _histOccu,
        _xHist,
        _isNumeric,
        _brush, _gBrush,
        _bodyG;
    
    // A formatter for counts.
    var _formatCount = d3.format(",.0f");

    //
    //
    // Main render function
    //
    //
    
    _chart.render = function () {
        if (!_svg) {
            definitions();

            initialzeSkeleton();
        }

        renderBars();
    };

    //
    //
    // Definitions
    //
    //
    
    function definitions()
    {
        // Get X scaling
        _x = d3.scale.linear()
            .domain(getXDomain()).nice()
            .range([0, quadrantWidth()]);

        // Histogram bins
        _ticks = _x.ticks(_bins);

        // Compute bin width
        _binWidth = (quadrantWidth() / (_ticks.length - 1))-1;

        // Generate histogram data
        _hist = d3.layout.histogram()
                .bins(_ticks)
                (_data.map(function(d) { return d.value; }));

        // Get Y scaling
         _y = d3.scale.linear()
        .domain(getYDomain())
        .range([quadrantHeight(), 0]);   

        // Define axis
        _xAxis = d3.svg.axis()
                    .scale(_x)
                    .orient("bottom");        

        _yAxis = d3.svg.axis()
                    .scale(_y)
                    .orient("left");
    }

    // 
    //
    // Basic rendering
    //
    //
    
    function initialzeSkeleton()
    {
        var padding = 5;

        // SVG
        _svg = d3.select(_chartDiv).append("svg")
                .attr("height", _height)
                .attr("width", _width)
                .style("background-color", "white")
                .attr("class", "chart");

        // title
        _svg.append("text")
            .attr("class", "title")
            .attr("text-anchor", "middle")
            .attr("x",(_width/2))
            .attr("y", 20)
            .text(_title);

        // x axis
        _svg.append("g")
                .attr("class", "x axis")
                .attr("transform", function () {
                    return "translate(" + xStart() + "," + yStart() + ")";
                })
                .call(_xAxis);

        // x axis label
        _svg.append("text")
            .attr("class", "x label")
            .attr("text-anchor", "middle")
            .attr("x",(_width/2))
            .attr("y", _height - 5)
            .text("["+_xName+"]");

        // y axis
        _svg.append("g")
                .attr("class", "y axis")
                .attr("transform", function () {
                    return "translate(" + xStart() + "," + yEnd() + ")";
                })
                .call(_yAxis);

        // y axis label
        _svg.append("text")
            .attr("class", "y label")
            .attr("text-anchor", "middle")
            .attr("x",-(_height/2))
            .attr("y", 5)
            .attr("dy", ".75em")
            .attr("transform", "rotate(-90)")
            .text("["+_yName+"]");

        // body clip
        _svg.append("defs")
                .append("clipPath")
                .attr("id", "body-clip")
                .append("rect")
                .attr("x", 0 - 1.5 * padding)
                .attr("y", 0)
                .attr("width", quadrantWidth() + 3 * padding)
                .attr("height", quadrantHeight());

        // create chart body
        _bodyG = _svg.append("g")
                .attr("class", "body")
                .attr("transform", "translate(" 
                        + xStart() 
                        + "," 
                        + yEnd() + ")")
                .attr("clip-path", "url(#body-clip)");
        
        // create bars
        var bar = _bodyG.selectAll(".bar")
            .data(_hist)
            .enter()
            .append("g")
            .attr("class","bar")
            .attr("transform", function(d) { return "translate("+_x(d.x)+","+_y(0)+")"; });

        bar.append("rect");

        bar.append("text")
            .attr("dy", ".75em")
            .attr("y", -12)
            .attr("text-anchor", "middle");

        // create brush
        _brush = d3.svg.brush()
            .x(_x)
            .on("brush", brushing)
            .on("brushend", brushended);

        _gBrush = _bodyG.append("g")
            .attr("class", "brush")
            .call(_brush)
            .call(_brush.event);

        _gBrush.selectAll("rect")
            .attr("rx", 5)
            .attr("height", quadrantHeight())
            .attr("y", 1);

        _gBrush.selectAll(".resize").append("path").attr("d", resizePath);
    }
    
    //
    //
    // Render Data
    // 
    //

    function renderBars() {
        // Update histogram data
        _hist = d3.layout.histogram()
                .bins(_ticks)
                (_data.map(function(d) { return d.value; }));

        // Update
        var bar = _bodyG.selectAll(".bar")
            .data(_hist)
            .transition()
            .duration(500)
            .attr("transform", function(d) { return "translate("+_x(d.x)+","+_y(d.y)+")"; });

        bar.select("rect")
            .attr("x", 1)
            .attr("width", _binWidth)
            .attr("height", function(d) { return yStart() - _y(d.y); });
            
        bar.select("text")
            .attr("x", _binWidth / 2)
            .text(function(d) { return _formatCount(d.y); });

        // Exit
        _bodyG.selectAll(".bar")
            .data(_hist)
            .exit()
            .remove();
    }

    //
    //
    // Internal Functions
    //
    //

    function xStart() {
        return _margins.left;
    }

    function yStart() {
        return _height - _margins.bottom;
    }

    function xEnd() {
        return _width - _margins.right;
    }

    function yEnd() {
        return _margins.top;
    }

    function quadrantWidth() {
        return _width - _margins.left - _margins.right;
    }

    function quadrantHeight() {
        return _height - _margins.top - _margins.bottom;
    }

    function getXDomain() {
        return d3.extent(_data, function(d) { return d.value; });
    }

    function getYDomain() {
        var max = d3.max(_hist, function(d) { return d.y; });
        return [0,max+(max/10)];
    }

    // Creates the nice drag handle
    function resizePath(d) {
        var e = +(d == "e"),
            x = e ? 1 : -1,
            y = quadrantHeight() / 3;
        return "M" + (.5 * x) + "," + y
            + "A6,6 0 0 " + e + " " + (6.5 * x) + "," + (y + 6)
            + "V" + (2 * y - 6)
            + "A6,6 0 0 " + e + " " + (.5 * x) + "," + (2 * y)
            + "Z"
            + "M" + (2.5 * x) + "," + (y + 8)
            + "V" + (2 * y - 8)
            + "M" + (4.5 * x) + "," + (y + 8)
            + "V" + (2 * y - 8);
      }

    // Histogram bin size round function
    function round(number, increment, offset) {
        return Math.ceil((number - offset) / increment ) * increment + offset;
    }

    // Brush functions
    function brushended() {
      if (!d3.event.sourceEvent) return; // only transition after input

      var inc = _ticks[1]-_ticks[0];
      var xDomain = _x.domain();

      var extent0 = _brush.extent(),
          extent1 = extent0.map(function(d) { 
              return round(d,inc,xDomain[0]) 
          });

      // Add increment to max because _filter.range is [x,y)
      if(extent1[1] >= xDomain[1]) extent1[1] = xDomain[1]+0.0000001;

      // If selection is empty, clear filter
      if ((extent1[1] - extent1[0]) < inc) {
          extent1[1] = extent1[0];
        _brush.clear();
        removeFilter();
      }
      else
      {
        // Filter
        filter(extent1);        
      }

      // Transition
      d3.select(this).transition()
          .call(_brush.extent(extent1))
          .call(_brush.event);
    }

    // Filter
    function filter(extent1)
    {
        _filter(extent1, _chartDiv);
    }

    function removeFilter()
    {
        _filter([0,0], _chartDiv);
    }

    // Update while brushing
    function brushing()
    {
        if (!d3.event.sourceEvent) return; // only transition after input

        // Filter
        //filter(_brush.extent());        
    }

    // 
    //
    // External function
    //
    //

    _chart.width = function (w) {
        if (!arguments.length) return _width;
        _width = w;
        return _chart;
    };

    _chart.height = function (h) {
        if (!arguments.length) return _height;
        _height = h;
        return _chart;
    };

    _chart.margins = function (m) {
        if (!arguments.length) return _margins;
        _margins = m;
        return _chart;
    };

    _chart.colors = function (c) {
        if (!arguments.length) return _colors;
        _colors = c;
        return _chart;
    };

    _chart.bins = function (b) {
        if (!arguments.length) return _bins;
        _bins = b;
        return _chart;
    };

    _chart.filter = function (f) {
        if (!arguments.length) return _filter;
        _filter = f;
        return _chart;
    };
    
    _chart.area = function (a) {
        if (!arguments.length) return _chartDiv;
        _chartDiv = a;
        return _chart;
    };

    _chart.json = function (d) {
        if (!arguments.length) return _data;
        _data = d;
        return _chart;
    };

    _chart.axisNames = function (a,b) {
        if (arguments.length<2) return _xName, _yName;
        _xName = a;
        _yName = b;
        return _chart;
    };

    _chart.title = function (t) {
        if (!arguments.length) return _title;
        _title = t;
        return _chart;
    };

    _chart.isNumeric = function (t) {
        if (!arguments.length) return _isNumeric;
        _isNumeric = t;
        return _chart;
    };

    return _chart;
}
