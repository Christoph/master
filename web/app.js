angular.module('app', ['ui.bootstrap','ui.grid','d3'])
.controller('appController', function($http, uiGridConstants) {
    var cont = this;

    cont.overviewGrid = {
        enableFiltering: true,
        showGridFooter: true,
        fastWatch: true,
        columnDefs: [
        { field: 'key'},
        { field: 'value', cellFilter: 'number:6', filters: [
            {
              condition: uiGridConstants.filter.GREATER_THAN,
              placeholder: 'greater than'
            },
            {
              condition: uiGridConstants.filter.LESS_THAN,
              placeholder: 'less than'
            }
        ]}
        ]
    };

    cont.historyGrid = {
        columnDefs: [
        { field: 'Origin'},
        { field: 'Step 1'},
        { field: 'Step 2'},
        { field: 'Step 3'}
        ]
    };

    cont.wordGrid = {
        columnsDef: [
        { field: 'key' },
        { field: 'value', cellFilter: 'number:6' }
        ]
    }

    cont.simGrid = {
        columnDefs: [
        { field: 'Tag' },
        { field: 'Similarity' }
        ]
    }

    $http.get('data.json')
        .success(function(data) {
            cont.overviewGrid.data = data;
            cont.wordGrid.data = data;
        });

    cont.alert = function() {
        alert("Alert");
    };

}).directive('barChart', ['d3Service', function(d3Service) {
    return {
      link: function(scope, element, attrs) {
        d3Service.d3().then(function(d3) {

          // d3 is the raw d3 object
          var margin = parseInt(attrs.margin) || 20,
          barHeight = parseInt(attrs.barHeight) || 20,
          barPadding = parseInt(attrs.barPadding) || 5;
            console.log(margin)
        });
      }}
  }])
.directive('d3Bars', ['d3Service', function(d3Service) {
    return {
        restrict: 'EA',
        scope: {},
        link: function(scope, element, attrs) {
            d3Service.d3().then(function(d3) {

            var svg = d3.select(element[0])
            .append('svg')
            .style('width', '100%');
 
          // Browser onresize event
          window.onresize = function() {
            scope.$apply();
          };
 
          // hard-code data
          scope.data = [
            {name: "Greg", score: 98},
            {name: "Ari", score: 96},
            {name: 'Q', score: 75},
            {name: "Loser", score: 48}
          ];
 
          // Watch for resize event
          scope.$watch(function() {
            return angular.element(window)[0].innerWidth;
          }, function() {
            scope.render(scope.data);
          });
 
          scope.render = function(data) {
            // remove all previous items before render
            svg.selectAll('*').remove();

            var margin = 10;
            var barHeight = 30;
            var barPadding = 5;
         
            // If we don't pass any data, return out of the element
            if (!data) return;
         
            // setup variables
            var width = d3.select(element[0]).node().offsetWidth - margin,
                // calculate the height
                height = scope.data.length * (barHeight + barPadding),
                // Use the category20() scale function for multicolor support
                color = d3.scale.category20(),
                // our xScale
                xScale = d3.scale.linear()
                  .domain([0, d3.max(data, function(d) {
                    return d.score;
                  })])
                  .range([0, width]);
         
            // set the height based on the calculations above
            svg.attr('height', height);
 
    //create the rectangles for the bar chart
    svg.selectAll('rect')
      .data(data).enter()
        .append('rect')
        .attr('height', barHeight)
        .attr('width', 140)
        .attr('x', Math.round(margin/2))
        .attr('y', function(d,i) {
          return i * (barHeight + barPadding);
        })
        .attr('fill', function(d) { return color(d.score); })
        .transition()
          .duration(1000)
          .attr('width', function(d) {
            return xScale(d.score);
          });
          }
            });
        }};
}]);
;
