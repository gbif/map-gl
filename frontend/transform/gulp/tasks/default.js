var gulp = require('gulp');
var runSequence = require('run-sequence');

gulp.task('development', function(callback) {
    runSequence(
        ['clean-all'],
        ['markup', 'lint', 'codestyle', 'proto'],
        ['browserify', 'watch'],
        callback);
});