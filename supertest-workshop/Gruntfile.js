module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    // config a mochaTest task
    mochaTest: {
        test: {
            options: {

                reporter: 'spec',
                captureFile: 'test-report.html',
                quiet: false,
                clearRequireCache: false

            },
            src: ['test/*.js']
        }
     }

  });

  // These plugins provide necessary tasks.
  grunt.loadNpmTasks('grunt-mocha-test');

  // Default task.
  grunt.registerTask('default', 'mochaTest');

};
