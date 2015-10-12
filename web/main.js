// Socket initialization
var initialized = false;
var cf = crossfilter();

// Connect to server
var socket = io.connect('http://localhost:9092',{
        reconnection: true,
        });

// Status events
socket.on('connect', function() {
    console.log("Connected");

    if(initialized == false)
    {
        socket.emit("initialize","init");
        initialized = true;
    }
});

socket.on('disconnect', function() {
    console.log("Disconnected");
});

// Data events

// Update data
socket.on("data", function(d) {
    temp = JSON.parse(d);
    console.log(temp);

    resetData(cf,[byImportance, filterByImportance]);
    console.log("Reset finished")
    cf.add(temp);
    console.log("Add finished")

    renderAll();
});

// Dynamic events

// Button
$("#request").click(function() {
    socket.emit("run", "test");
});

// Crossfilter 

//  By name crossfilter and mapping
var byImportance = cf.dimension(function(d) { return d.importance; });
var filterByImportance = cf.dimension(function(e) { return e.importance; });

var byWeight = cf.dimension(function(d) { return d.tagWeight; });
var filterByWeight = cf.dimension(function(d) { return d.tagWeight; });

var byTagName = cf.dimension(function(d) { return d.tagName; });

// Visualization
var hex = hexPlot()
    .area("#hex1")
    .height(600)
    .width(600)
    .dimension(byTagName,"tagWeight", "tagName")
    .binSize(25);

var hist1 = histogram()
    .area("#hist1")
    .height(250)
    .width(600)
    .isNumeric(true)
    .dimension(byWeight,filterByWeight)
    .property("tagWeight")
    .reloadAll(renderAll)
    .bins(20)
    .axisNames("Weight", "#")
    .title("Weight Histogram");

function renderAll()
{
    // Render
    hex.render();
    hist1.render();
}

// reset the filter for a dimension
function resetDimensionFilter (dimension) {
  dimension.filter(null);
}

// reset filters for all given dimensions, 
// remove all data from index and
// return empty index
function resetData(ndx, dimensions) {
  // Clear all filters from dimensions, because `ndx.remove` 
  // only removes records matching the current filters.
  dimensions.forEach(resetDimensionFilter);

  // Remove all data from the cross filter
  ndx.remove();
}
