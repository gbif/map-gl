var gulp = require('gulp');
var runSequence = require('run-sequence');
var config = require('../config').markup;


gulp.task('markup', function () {
    return gulp.src(config.src)
        .pipe(gulp.dest(config.dest));
});

