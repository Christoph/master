
// Socket test

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


// Import data
//d3.csv("tags_final.csv")
d3.csv("tags_cleaned.csv")
//d3.csv("raw_spotify.csv")
.row(function(d) {
    return { id: +d.ID, tagName: d.TagName, importance: +d.Importance , songName: d.SongName, last: +d.LastFMWeight };
})
.get(function(error, tags) {
    //Due to the asyncronity of d3.import, the data is only available within this function
    //
    // START
    //
    //
    
    // 
    //
    // Data
    //
    //
    
    // Crossfilter 
    var cf = crossfilter(tags);
    
    //  By name crossfilter and mapping
    var byName = cf.dimension(function(d) { return d.tagName; });
    var filterByName = cf.dimension(function(d) { return d.tagName; });

    var byImportance = cf.dimension(function(d) { return d.importance; });
    var filterByImportance = cf.dimension(function(e) { return e.importance; });

    var byLastFM = cf.dimension(function(d) { return d.last; })
    var filterByLastFM = cf.dimension(function(d) { return d.last; })


    //
    //
    // Visualizition
    //
    //
    
    // Create variables
    var hex1 = hexPlot()
        .area("#hex1")
        .height(550)
        .width(580)
        .dimension(byImportance,"importance", "tagName")
        .binSize(25)
        .axisNames("Importance", "Occurrence")
        .colors(["#80A1C1", "#3a3e4a"]);

    var hex2 = hexPlot()
        .area("#hex2")
        .height(550)
        .width(580)
        //.dimension(byLastFM ,"last", "tagName")
        .dimension(byImportance,"importance", "tagName")
        .binSize(25)
        .axisNames("Importance", "Occurrence")
        //.axisNames("LastFMWeight", "Occurrence")
        .colors(["lightgreen", "#3a3e4a"]);
    
    var hist1 = histogram()
        .area("#hist1")
        .height(250)
        .width(580)
        .isNumeric(true)
        //.dimension(byLastFM,filterByLastFM)
        .dimension(byImportance,filterByImportance)
        .reloadAll(renderAll)
        .bins(20)
        .axisNames("Importance", "#")
        //.axisNames("LastFMWeight", "#")
        .title("Importance Histogram");
        //.title("LastFMWeight Histogram");

    var hist2 = histogram()
        .area("#hist2")
        .height(250)
        .width(580)
        .isNumeric(false)
        .dimension(byName,filterByName)
        .reloadAll(renderAll)
        .bins(30)
        .axisNames("Occurrence", "#")
        .title("Occurrence Histogram");

   function renderAll()
   {
        // Render
        hex1.render();
        hex2.render();
        hist1.render();
        hist2.render();
   }

   renderAll();

    //
    //
    // END
});
