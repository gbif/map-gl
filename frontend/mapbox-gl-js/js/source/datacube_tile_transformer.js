'use strict';

var Protobuf = require('pbf');

module.exports = DatacubeTile;

/*
 * Transform a datacube to a vector tile. Used for sending compressed data and using the standard pipeline in mapbox gl
 */
function DatacubeTile(buffer) {
    var data = {};
    data.channel = [];
    var protobuf = new Protobuf(new Uint8Array(buffer));
    
    function readDcTile(tag, tile, pbf) {
        if (tag === 1) {
            tile.x = readRepeatedVarint(pbf);
        } else if (tag === 2) {
            tile.y = readRepeatedVarint(pbf);
        } else if (tag === 3) {
            tile.channel.push(readChannelMessage(pbf));
        }
    }

    function readRepeatedVarint(pbf) {
        var bytes = pbf.readVarint(), // first bytes holds length
            posEnd = pbf.pos + bytes,
            list = [];

        while (pbf.pos < posEnd) {
            var data = pbf.readVarint();
            list.push(data);
        }
        return list;
    }

    function readChannelMessage(pbf) {
        var channel = null,
            bytes = pbf.readVarint(),
            end = pbf.pos + bytes;

        while (pbf.pos < end) {
            var tag = pbf.readVarint() >> 3;
            channel = tag === 1 ? readRepeatedVarint(pbf) : null;
        }
        return channel;
    }

    function getAsVectorTileBuffer(resolution){
        resolution = resolution || 4096;
        var pbf = new Protobuf(),
            tile = getAsVectorTile(resolution);
        writeTile(tile, pbf);
        var buffer = pbf.finish();
        return buffer;
    }

    function getAsVectorTile(resolution){
        var tile = {layers: []};
        tile.layers.push(getVtLayer(resolution));
        return tile;
    }

    function getVtLayer(resolution) {
        var layer = {
            features: [],
            keys: [],
            values: [],
            name: 'occurrence',
            extent: 4096,
            version: 1
        };
        var norm = layer.extent/resolution;
        layer.keys = ['count'];
        layer.values = [];
        var tmpMap = {};
        layer.features = data.x.map(function(e, i) {
            var feature = {
                type: 1,
                geometry: [9, getZigZagEncoded(e*norm), getZigZagEncoded(data.y[i]*norm)]
            };

            var channelData = data.channel[0][i];
            var pos = tmpMap[channelData];
            if (typeof pos === 'undefined') {
                pos = layer.values.push({sint_value: channelData})-1;
                tmpMap[channelData] = pos;
            }
            feature.tags = [0, pos];
            
            return feature;
        });
        return layer;
    }

    function getZigZagEncoded(n) {
        return (n << 1) ^ (n >> 31);
    }

    protobuf.readFields(readDcTile, data);
    data.getAsVectorTile = getAsVectorTile;
    data.getAsVectorTileBuffer = getAsVectorTileBuffer;
    return data;
}

//Vector tiles
// encoding vector tile
function writeTile(tile, pbf) {
    if (tile.layers !== undefined) for (var i = 0; i < tile.layers.length; i++) pbf.writeMessage(3, writeLayer, tile.layers[i]);
}
function writeLayer(layer, pbf) {
    if (layer.name !== undefined) pbf.writeStringField(1, layer.name);
    var i;
    if (layer.features !== undefined) for (i = 0; i < layer.features.length; i++) pbf.writeMessage(2, writeFeature, layer.features[i]);
    if (layer.keys !== undefined) for (i = 0; i < layer.keys.length; i++) pbf.writeStringField(3, layer.keys[i]);
    if (layer.values !== undefined) for (i = 0; i < layer.values.length; i++) pbf.writeMessage(4, writeValue, layer.values[i]);
    if (layer.extent !== undefined) pbf.writeVarintField(5, layer.extent);
    if (layer.version !== undefined) pbf.writeVarintField(15, layer.version);
}
function writeFeature(feature, pbf) {
    if (feature.id !== undefined) pbf.writeVarintField(1, feature.id);
    if (feature.tags !== undefined) pbf.writePackedVarint(2, feature.tags);
    if (feature.type !== undefined) pbf.writeVarintField(3, feature.type);
    if (feature.geometry !== undefined) pbf.writePackedVarint(4, feature.geometry);
}
function writeValue(value, pbf) {
    if (value.string_value !== undefined) pbf.writeStringField(1, value.string_value);
    if (value.float_value !== undefined) pbf.writeFloatField(2, value.float_value);
    if (value.double_value !== undefined) pbf.writeDoubleField(3, value.double_value);
    if (value.int_value !== undefined) pbf.writeVarintField(4, value.int_value);
    if (value.uint_value !== undefined) pbf.writeVarintField(5, value.uint_value);
    if (value.sint_value !== undefined) pbf.writeSVarintField(6, value.sint_value);
    if (value.bool_value !== undefined) pbf.writeBooleanField(7, value.bool_value);
}


