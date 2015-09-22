## A simple tile server of DataCube tiles and VectorTileFormat tiles

The protoc library is required to build this, and the version must match that in the pom.
Typically you install this with ```brew install protobuf```

To build: ```mvn clean package```

To run: ```java -Xmx4G -jar target/map-gl-server-1.0-SNAPSHOT-shaded.jar server server.conf```

To change the configuration, edit ```server.conf```

To view the demo: ```http://localhost:8080```
To view a protobuf tile: ```http://localhost:8080/api/0/0/0/vectortile.pbf```
To view a datacube tile: ```http://localhost:8080/api/0/0/0/datacube.pbf```

The URL format is ```/api/{z}/{x}/{y}/{type}.pbf```
