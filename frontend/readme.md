#Frontend

To use the frontend examples the backend must be started first. Once that is done, start the server in the frontend folder.
```
http-server -p7001
```
if http-server isn't installed globally then install and run:
```
npm install http-server
node_modules/http-server/bin/http-server -p7003
```
Go to [http://localhost:7003/examples/](http://localhost:7003/examples/) to see examples.


##mapbox-gl-js
This is just a fork of [mapbox-gl-js](https://github.com/mapbox/mapbox-gl-js)

But with an added layer type that reads data cubes. This is simply a copy of the vector tile layer, with the exception that it use a different way of loading tiles. And the only difference in loading tiles is that it transform the Data Cube to a Vector Tile buffer. 

The new files are:

* js/source/datacube_tile_transformer.js
* js/source/datacube_tile_source.js

edited files:
 * js/source/source.js (new layer type 'datacube')
 * js/source/worker.js ('load datacube tile' function).

###Build
Build mapbox to `dist` using `npm run production`

##transform
A small project for experimenting with reading data cubes and writing vector tiles.


