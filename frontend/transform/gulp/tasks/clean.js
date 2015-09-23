var gulp = require('gulp');
var del = require('del');
var config = require('../config').clean;

gulp.task('clean-all', function (cb) {
    del(config.all, cb);
});