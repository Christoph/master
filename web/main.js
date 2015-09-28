
// Socket test

/*
var socket = io.connect('http://localhost:9092');

socket.on('connect', function() {
    console.log("Connected")
});

socket.on('response', function(data) {
    console.log(JSON.parse(data));
});

socket.on('disconnect', function() {
    console.log("Disconnected")
});

$("#request").click(function() {

    var jsonObj = {
        '@class': "server.SecondTestObject",
        text: "christoph",
        number: 1
    };

    // Sends event object and @class is not necessary
    socket.emit('json', jsonObj);
});
*/

data = [
{
    x: 0.8,
    text: "ROck Song",
    y: 100
},
{
    x: 0.7,
    text: "Pop Song",
    y: 90
},
{
    x: 0.1,
    text: "Custom Song",
    y: 10
}
];

data_hist = [
{
    value: 1.0,
    attribute: "ROck Song"
},
{
    value: 0.1,
    attribute: "Bla Song"
},
{
    value: 0.2,
    attribute: "Pop Song"
},
{
    value: 0.5,
    attribute: "Custom Song"
}
];

var hex1 = hexPlot()
    .area("#hex1")
    .height(550)
    .width(580)
    .json(data)
    .binSize(25)
    .axisNames("Importance", "Occurrence")
    .colors(["#80A1C1", "#3a3e4a"]);

var hist1 = histogram()
    .area("#hist1")
    .height(250)
    .width(580)
    .json(data_hist)
    .reloadAll(renderAll)
    .bins(10)
    .axisNames("Importance", "#")
    .title("Importance Histogram");
    

function renderAll()
{
    // Render
    hex1.render();
    hist1.render();
}

renderAll();
