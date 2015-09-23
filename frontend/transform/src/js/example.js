var Protobuf = require('pbf');
var DatacubeTile = require('./index.js');

function readTile(pbf, end) {
    return pbf.readFields(readTileField, {layers: []}, end);
}
function readLayer(pbf, end) {
    return pbf.readFields(readLayerField, {features: [], keys: [], values: []}, end);
}
function readFeature(pbf, end) {
    return pbf.readFields(readFeatureField, {}, end);
}
function readValue(pbf, end) {
    return pbf.readFields(readValueField, {}, end);
}

function readTileField(tag, tile, pbf) {
    if (tag === 3) tile.layers.push(readMessage(readLayer, pbf));
}
function readLayerField(tag, layer, pbf) {
    if (tag === 1) layer.name = pbf.readString();
    else if (tag === 2) layer.features.push(readMessage(readFeature, pbf));
    else if (tag === 3) layer.keys.push(pbf.readString());
    else if (tag === 4) layer.values.push(readMessage(readValue, pbf));
    else if (tag === 5) layer.extent = pbf.readVarint();
    else if (tag === 15) layer.version = pbf.readVarint();
}
function readFeatureField(tag, feature, pbf) {
    if (tag === 1) feature.id = pbf.readVarint();
    else if (tag === 2) feature.tags = pbf.readPackedVarint();
    else if (tag === 3) feature.type = pbf.readVarint();
    else if (tag === 4) feature.geometry = pbf.readPackedVarint();
}
function readValueField(tag, value, pbf) {
    if (tag === 1) value.string_value = pbf.readString();
    else if (tag === 2) value.float_value = pbf.readFloat();
    else if (tag === 3) value.double_value = pbf.readDouble();
    else if (tag === 4) value.int_value = pbf.readVarint();
    else if (tag === 5) value.uint_value = pbf.readVarint();
    else if (tag === 6) value.sint_value = pbf.readSVarint();
    else if (tag === 7) value.bool_value = pbf.readBoolean();
}
function readMessage(fn, pbf) {
    return fn(pbf, pbf.readVarint() + pbf.pos);
}

function getArrayBuffer(url, callback) {
  var xhr = new XMLHttpRequest();
  xhr.open('GET', url, true);
  xhr.responseType = 'arraybuffer';
  xhr.onerror = function(e) {
    callback(e);
  };
  xhr.onload = function() {
    if (xhr.status >= 200 && xhr.status < 300 && xhr.response) {
      callback(null, xhr.response);
    } else {
      callback(new Error(xhr.statusText));
    }
  };
  xhr.send();
  return xhr;
}
/*
var urlVt = 'proto/vt-1-1-1.pbf';
window.vt = undefined;
getArrayBuffer(urlVt, function(err, buffer) {
    vt =  readTile(new Protobuf(buffer));
    var p = new Protobuf();
    writeTile(vt, p);
    window.vtBuffer = p.finish();
});*/

var urlDC = 'proto/dc-1-1-1.pbf';
getArrayBuffer(urlDC, function(err, buffer) {
    var data = new DatacubeTile(buffer);
    window.data = data.getAsVectorTile();
    window.dcBuffer = data.getAsVectorTileBuffer();
});