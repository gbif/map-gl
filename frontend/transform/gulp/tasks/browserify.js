var gulp = require('gulp');
var source = require('vinyl-source-stream');
var browserify = require('browserify');
var notify = require('gulp-notify');
var config = require('../config').javascript;

gulp.task('browserify', function () {
    return browserify(config.main)
        .bundle()
        .on("error", notify.onError(function (error) {
            return error.message;
        }))
        .pipe(source('script.js'))
        .pipe(gulp.dest(config.dest));
});