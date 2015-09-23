var gulp = require('gulp');
var config = require('../config').proto;

gulp.task('proto', function () {
    return gulp.src(config.src)
        .pipe(gulp.dest(config.dest));
});