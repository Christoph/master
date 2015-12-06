angular.module('app', ['ui.bootstrap'])
.controller('tabsController', function() {
    var cont = this;

    cont.data = [
    {name: "me", age: 1},
    {name: "you", age: 2},
    {name: "him", age: 3},
    {name: "not", age: 4}
    ];

    cont.alert = function() {
        alert("Alert");
    };

});
