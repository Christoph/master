// Socket initialization

// Connect to server
var socket = io.connect('http://localhost:9092',{
        reconnection: false
        });

// Status events
socket.on('connect', function() {
    console.log("Connected");
    socket.emit("initialize","init");

});

socket.on('disconnect', function() {
    console.log("Disconnected");
});

// Data events

// Initialize data structures
data_hex1 = [];
data_hist1 = [];

// Update #hex1
socket.on("#hex1", function(d) {
    console.log("#hex1 data");
    temp = JSON.parse(d);
    data_hex1.length = 0;

    temp.forEach(function(e) {
        data_hex1.push({
            x: e.importance,
            y: e.lastFMWeight,
            text: e.tagName
        });
    });
    hex1.render();
});

// Update #hist1
socket.on("#hist1", function(d) {
    console.log("#hist1 data");
    temp = JSON.parse(d);
    data_hist1.length = 0;

    temp.forEach(function(e) {
        data_hist1.push({
            value: e.importance,
            attribute: e.tagName
        });
    });
    hist1.render();
});

// Filter
function filter(extend, chartDiv)
{
    console.log("Filter");
    var json = {
        lower: extend[0],
        upper: extend[1],
        chartDiv: chartDiv
    };

    socket.emit("filter", json);
}




// Dynamic events

// Button
$("#request").click(function() {

    /*
    var json = {
        lower: extend[0],
        upper: extend[1],
        chartDiv: chartDiv
    };
    */

    socket.emit("filter", "test");
});


// Visualization
var hex1 = hexPlot()
    .area("#hex1")
    .height(550)
    .width(580)
    .json(data_hex1)
    .binSize(25)
    .axisNames("Importance", "Occurrence")
    .colors(["#80A1C1", "#3a3e4a"]);

var hist1 = histogram()
    .area("#hist1")
    .height(250)
    .width(580)
    .json(data_hist1)
    .filter(filter)
    .bins(10)
    .axisNames("Importance", "#")
    .title("Importance Histogram");
    

function renderAll()
{
    // Render
    //hex1.render();
    //hist1.render();
}
