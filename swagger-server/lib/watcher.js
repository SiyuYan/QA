'use strict';

module.exports = SwaggerWatcher;

var fs   = require('fs'),
    util = require('./util');

/**
 * Watches files for changes and updates the server accordingly.
 *
 * @param {SwaggerServer} server
 * @constructor
 */
function SwaggerWatcher(server) {
  var self = this;
  self.server = server;

  /**
   * The Swagger API files that are being watched.
   * @type {FSWatcher[]}
   */
  self.watchedSwaggerFiles = [];

  /**
   * The Handler files that are being watched.
   * @type {FSWatcher[]}
   */
  self.watchedHandlerFiles = [];

  // Watch Swagger API files
  server.on('parsed', function(err, api, parser) {
    // Watch all the files that were parsed
    self.unwatchSwaggerFiles();
    self.watchSwaggerFiles(parser.$refs.paths('fs'));
  });

  // Watch Handler Files
  server.on('handled', function(err, filePaths) {
    self.unwatchHandlerFiles(filePaths);
    self.watchHandlerFiles(filePaths);
  });
}

/**
 * Stops watching Swagger files for changes.
 */
SwaggerWatcher.prototype.unwatchSwaggerFiles = function(files) {
  var watcher;
  while (watcher = this.watchedSwaggerFiles.pop()) {
    watcher.close();
  }
};

/**
 * Watches the given Swagger file, and re-parses the API if any of them change.
 * @param {string[]} files
 */
SwaggerWatcher.prototype.watchSwaggerFiles = function(files) {
  var self = this;

  if (self.server.app.enabled('watch files')) {
    files.forEach(function(path) {
      var watcher = watchFile(path, onChange);
      if (watcher) {
        self.watchedSwaggerFiles.push(watcher);
      }
    });
  }

  function onChange(event, path) {
    util.debug('File change detected: %s %s', event, path);
    self.server.emit('change', event, path);
    self.server.__parser.parse();
  }
};

/**
 * Stops watching Handler files for changes.
 */

SwaggerWatcher.prototype.unwatchHandlerFiles = function(files) {
  var watcher;

  while (watcher = this.watchedHandlerFiles.pop()) {
    watcher.close();
  }

  //Unreference the handler modules that have already been loaded into node.
  files.forEach(function(file) {
    if (require.cache[file] !== undefined) {
      delete require.cache[file];
    }
  });
};

/**
 * Watches the given Handler files, re-loads the Handler modules if any of them change.
 */

SwaggerWatcher.prototype.watchHandlerFiles = function(files) {
  var self = this;

  if (self.server.app.enabled('watch files')) {
    files.forEach(function(path) {
      var watcher = watchFile(path, onChange);
      if (watcher) {
        self.watchedHandlerFiles.push(watcher);
      }
    });
  }

  function onChange(event, path) {
    util.debug('File change detected: %s %s', event, path);
    self.server.emit('change', event, path);

    self.server.__removeMiddleWare();
    self.server.addHandlers();
  }
};

/**
 * Watches a file, and calls the given callback whenever the file changes.
 *
 * @param {string} path
 * The full path of the file.
 *
 * @param {function} onChange
 * Callback signature is `function(event, filename)`. The event param will be "change", "delete", "move", etc.
 * The filename param is the full path of the file, and it is guaranteed to be present (unlike Node's FSWatcher).
 *
 * @returns {FSWatcher|undefined}
 * Returns the file watcher, unless the path does not exist.
 */
function watchFile(path, onChange) {
  try {
    var oldStats = fs.statSync(path);
    var watcher = fs.watch(path, {persistent: false});
    watcher.on('error', watchError);

    watcher.on('change', function(event) {
      fs.stat(path, function(err, stats) {
        if (err) {
          /* istanbul ignore next: not easy to repro this error in tests */
          watchError(err);
        }
        else if (stats.mtime > oldStats.mtime) {
          oldStats = stats;
          onChange(event, path);
        }
        else {
          util.debug('Ignoring %s event for "%s" because %j <= %j', event, path, stats.mtime, oldStats.mtime);
        }
      });
    });

    return watcher;
  }
  catch (e) {
    watchError(e);
  }

  function watchError(e) {
    util.warn('Error watching file "%s": %s', path, e.stack);
  }
}
