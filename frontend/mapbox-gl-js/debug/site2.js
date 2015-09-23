
mapboxgl.accessToken = 'pk.eyJ1IjoidGltcm9iZXJ0c29uMTAwIiwiYSI6IlRxTFEzOUkifQ.UlS8frv1bZZPdUfDp-zolQ';

var map = new mapboxgl.Map({
    container: 'map',
    zoom: 12.5,
    center: [-77.01866, 38.888],
    style: 'simple.json',
    hash: true
});

map.addControl(new mapboxgl.Navigation());

map.on('style.load', function() {

    map.addSource('gbif', {
        "type": "datacube",
        "tiles": [
            "http://localhost:8080/api/{z}/{x}/{y}/datacube.pbf"
        ],
        "maxzoom": 10        
    });

    var circleRadius = 1.5;
    map.addLayer({
        "id": "gbif0",
        "source": "gbif",
        "source-layer": "occurrence",
        "type": "circle",
        "filter": ["all", [">=", "count", 0],["<", "count", 10]],
        "minzoom": 0,
        "maxzoom": 20,
        "paint": {
          "circle-radius": circleRadius,
          "circle-color": "#FFFF00"
        }
    });
    map.addLayer({
        "id": "gbif1",
        "source": "gbif",
        "source-layer": "occurrence",
        "type": "circle",
        "filter": ["all", [">=", "count", 10],["<", "count", 100]],
        "minzoom": 0,
        "maxzoom": 20,
        "paint": {
          "circle-radius": circleRadius,
          "circle-color": "#FFCC00"
        }
    });
    map.addLayer({
        "id": "gbif2",
        "source": "gbif",
        "source-layer": "occurrence",
        "type": "circle",
        "filter": ["all", [">=", "count", 100],["<", "count", 1000]],
        "minzoom": 0,
        "maxzoom": 20,
        "paint": {
          "circle-radius": circleRadius,
          "circle-color": "#FF9900"
        }
    });
    map.addLayer({
        "id": "gbif3",
        "source": "gbif",
        "source-layer": "occurrence",
        "type": "circle",
        "filter": ["all", [">=", "count", 1000],["<", "count", 10000]],
        "minzoom": 0,
        "maxzoom": 20,
        "paint": {
          "circle-radius": circleRadius,
          "circle-color": "#FF6600"
        }
    });
    map.addLayer({
        "id": "gbif4",
        "source": "gbif",
        "source-layer": "occurrence",
        "type": "circle",
        "filter": ["all", [">=", "count", 10000],["<", "count", 100000]],
        "minzoom": 0,
        "maxzoom": 20,
        "paint": {
          "circle-radius": circleRadius,
          "circle-color": "#FF3300"
        }
    });
    map.addLayer({
        "id": "gbif5",
        "source": "gbif",
        "source-layer": "occurrence",
        "type": "circle",
        "filter": [">=", "count", 100000],
        "minzoom": 0,
        "maxzoom": 20,
        "paint": {
          "circle-radius": circleRadius,
          "circle-color": "#CC0000"
        }
    });
    /*
    map.addSource('occtest', {
        "type": "geojson",
        "data": getGeoJson(500)
    });

    map.addLayer({
        "id": "tester",
        "type": "circle",
        "source": "occtest",
        //"filter": ["all", ["==", "name", 1]],
        "paint": {
            "circle-radius": 2,
            "circle-color": "#f0f"
        }
    });

    map.addSource('vecortest', {
        "type": "vector",
        "tiles": [
            "http://localhost:3001/test/{z}/{x}/{y}.pbf"
        ],
        "maxzoom": 14
    });

    map.addLayer({
        "id": "vtest",
        "source": "vecortest",
        "source-layer": "occurrence",
        "type": "circle",
        "paint": {
            "circle-radius": 1,
            "circle-color": "#f0f"
        }
    });

    map.addSource('geojson-random-points', {
        "type": "geojson",
        "data": "/debug/random.geojson"
    });

    map.addLayer({
        "id": "random-points",
        "type": "circle",
        "source": "geojson-random-points",
        "paint": {
            "circle-radius": 5,
            "circle-color": "#f0f"
        }
    });
*/

    var rangeslider = document.getElementById('myRange');
    rangeslider.addEventListener("input", function() {
        document.title = rangeslider.value;
        //map.setFilter("tester", ["all", ["==", "name", parseInt(rangeslider.value)]]);
        //map.render();
    }, false); 

});

map.on('click', function(e) {
    (new mapboxgl.Popup())
        .setLngLat(map.unproject(e.point))
        .setHTML("<h1>Hello World!</h1>")
        .addTo(map);
});

// keyboard shortcut for comparing rendering with Mapbox GL native
document.onkeypress = function(e) {
    if (e.charCode === 109 && !e.shiftKey && !e.metaKey && !e.altKey) {
        var center = map.getCenter();
        location.href = "mapboxgl://?center=" + center.lat + "," + center.lng + "&zoom=" + map.getZoom() + "&bearing=" + map.getBearing();
        return false;
    }
};

function getGeoJson(len) {
    var collection = {"type":"FeatureCollection","features":[]};
    for (var i = 0; i < len; i++){
        var number = parseInt(101*i/len);
        var feature = {"type":"Feature","geometry":{"type":"Point","coordinates":[Math.random()*20-170+(280*i/len),Math.random()*160-80]},"properties":{"name": number}};
        //var feature = {"type":"Feature","geometry":{"type":"Point","coordinates":[Math.random()*340-170,Math.random()*160-80]},"properties":{"name": number}};
        collection.features.push(feature);
    }
    return collection;
}


