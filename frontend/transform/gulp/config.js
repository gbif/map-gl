var dest = "./dist",
    src = './src';

module.exports = {
    src: src,
    dest: dest,
    markup: {
        src: src + "/html/**/*.html",
        dest: dest
    },
    javascript: {
        main: src + "/js/example.js",
        src: [src + "/js/**/*.js", '!' + src + '/js/vendor/**/*.*'],
        folder: src + "/js/**/*.*",
        dest: dest
    },
    clean: {
        all: [dest + '/**/*.*'],
        css: [dest + '/**/*.css']
    },
    proto: {
        src: src + "/proto/**/*.*",
        dest: dest + '/proto'
    }
};
