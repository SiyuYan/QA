'use strict';

var env           = require('../test-environment'),
    SwaggerParser = require('../../node_modules/swagger-express-middleware/node_modules/swagger-parser/');

describe('parsed event', function() {
  it('should fire when created with a local file',
    function(done) {
      var server = env.swaggerServer(env.files.petStoreExternalRefs);
      server.on('parsed', function(err, api, parser) {
        expect(err).to.be.null;
        expect(api).to.deep.equal(env.files.parsed.petStoreExternalRefs);
        expect(parser).to.be.an.instanceOf(SwaggerParser);
        done();
      });
    }
  );

  it('should fire when created with a URL',
    function(done) {
      env.nock()
        .get('/petstore-external-refs.yaml').replyWithFile(200, env.files.petStoreExternalRefs)
        .get('/pet.yaml').replyWithFile(200, env.files.pet)
        .get('/error.yaml').replyWithFile(200, env.files.error);

      var server = env.swaggerServer(env.urls.petStoreExternalRefs);
      server.on('parsed', function(err, api, parser) {
        expect(err).to.be.null;
        expect(api).to.deep.equal(env.files.parsed.petStoreExternalRefs);
        expect(parser).to.be.an.instanceOf(SwaggerParser);
        done();
      });
    }
  );

  it('should only fire once for multiple requests',
    function(done) {
      env.disableWarningsForThisTest();
      var onParsed = sinon.spy();

      // Instantiate the server.
      // This will trigger once it's done parsing the file
      var server = env.swaggerServer(env.files.petStoreExternalRefs);
      server.on('parsed', onParsed);

      // Start the server.
      // This should NOT trigger the "parsed' event.
      var httpServer = server.app.listen(function() {
        sinon.assert.calledOnce(onParsed);
        var supertest = env.supertest(httpServer);

        // Make a request.
        // This should NOT trigger the "parsed' event.
        supertest
          .get('/foo')
          .expect(404)
          .end(function(err) {
            if (err) {
              return done(err);
            }

            // Make a second request.
            // This should NOT trigger the "parsed' event
            supertest
              .get('/bar')
              .expect(404)
              .end(function(err) {
                if (err) {
                  return done(err);
                }

                sinon.assert.calledOnce(onParsed);
                done();
              })
          });
      });
    }
  );

  it('should only fire once for multiple requests when mounted in a parent app',
    function(done) {
      env.disableWarningsForThisTest();
      var onParsed = sinon.spy(makeRequest);

      // Instantiate the server.
      // This will trigger once it's done parsing the file
      var server = env.swaggerServer(env.files.petStoreExternalRefs);
      server.on('parsed', onParsed);

      // Mount the server in an Express app
      // This should NOT trigger the "parsed' event
      var express = env.express(server.app);
      var supertest = env.supertest(express);
      sinon.assert.notCalled(onParsed);

      function makeRequest() {
        // Make a request.
        // This should NOT trigger the "parsed' event.
        supertest
          .get('/foo')
          .expect(404)
          .end(function(err) {
            if (err) {
              return done(err);
            }
            sinon.assert.calledOnce(onParsed);

            // Make a second request.
            // This should NOT trigger the "parsed' event
            supertest
              .get('/bar')
              .expect(404)
              .end(function(err) {
                if (err) {
                  return done(err);
                }

                sinon.assert.calledOnce(onParsed);
                done();
              })
          });
      }
    }
  );

  it('should fire again when the Swagger file is changed',
    function(done) {
      var onParsed = sinon.spy();
      var server = env.swaggerServer(env.files.petStore);
      server.on('parsed', onParsed);

      server.app.listen(function(err) {
        if (err) {
          return done(err);
        }
        sinon.assert.calledOnce(onParsed);

        // Touch the main Swagger file, to trigger a re-parse
        env.touchFile(env.files.petStore);

        server.once('parsed', function() {
          sinon.assert.calledTwice(onParsed);
          expect(onParsed.secondCall.args[0]).to.be.null;
          expect(onParsed.secondCall.args[1]).to.deep.equal(env.files.parsed.petStore);
          expect(onParsed.secondCall.args[2]).to.be.an.instanceOf(SwaggerParser);
          done();
        });
      });
    }
  );

  it('should fire again when any Swagger file is changed',
    function(done) {
      var onParsed = sinon.spy();
      var server = env.swaggerServer(env.files.petStoreExternalRefs);
      server.on('parsed', onParsed);

      server.app.listen(function(err) {
        if (err) {
          return done(err);
        }
        sinon.assert.calledOnce(onParsed);

        // Touch the main Swagger file, to trigger a re-parse
        env.touchFile(env.files.petStoreExternalRefs);

        server.once('parsed', function() {
          if (err) {
            return done(err);
          }
          sinon.assert.calledTwice(onParsed);

          // Touch a referenced Swagger file, to trigger another re-parse
          env.touchFile(env.files.pet);

          server.once('parsed', function() {
            sinon.assert.calledThrice(onParsed);
            expect(onParsed.thirdCall.args[0]).to.be.null;
            expect(onParsed.thirdCall.args[1]).to.deep.equal(env.files.parsed.petStoreExternalRefs);
            expect(onParsed.thirdCall.args[2]).to.be.an.instanceOf(SwaggerParser);
            done();
          })
        });
      });
    }
  );

  it('should only fire once when multiple file changes occur rapidly',
    function(done) {
      var onParsed = sinon.spy();
      var server = env.swaggerServer(env.files.petStoreExternalRefs);

      server.app.listen(function() {
        // Wait for the file-watchers to start watching the files
        setTimeout(function() {
          // Touch all of the files. This should only trigger one re-parse
          env.touchFile(env.files.petStoreExternalRefs, env.files.pet, env.files.error);
          server.on('parsed', onParsed);

          // Wait a sec for all the file-watchers to trigger
          setTimeout(function() {
            sinon.assert.calledOnce(onParsed);
            expect(onParsed.firstCall.args[0]).to.be.null;
            expect(onParsed.firstCall.args[1]).to.deep.equal(env.files.parsed.petStoreExternalRefs);
            expect(onParsed.firstCall.args[2]).to.be.an.instanceOf(SwaggerParser);
            done();
          }, 1500);
        }, 500);
      });
    }
  );

  describe('Failure tests', function() {
    beforeEach(env.disableWarningsForThisTest);

    it('should fire even if a parsing error occurs',
      function(done) {
        var server = env.swaggerServer(env.files.blank);
        server.app.listen();

        server.on('parsed', function(err, api, parser) {
          expect(api).to.be.null;
          expect(parser).to.be.an.instanceOf(SwaggerParser);
          done();
        });
      }
    );

    it('should fire even if an HTTP error occurs',
      function(done) {
        env.nock().get('/404.yaml').reply(404);

        var server = env.swaggerServer(env.urls.error404);
        server.app.listen();

        server.on('parsed', function(err, api, parser) {
          expect(api).to.be.null;
          expect(parser).to.be.an.instanceOf(SwaggerParser);
          done();
        });
      }
    );

    it('should fire again if an error is resolved',
      function(done) {
        // Create a copy of "blank.yaml", so we can safely modify it
        env.copyFile(env.files.blank, env.files.temp);

        var server = env.swaggerServer(env.files.temp);
        server.app.listen();

        server.once('parsed', function(err, api, parser) {
          expect(api).to.be.null;
          expect(parser).to.be.an.instanceOf(SwaggerParser);

          // The parse failed, but the server is still running and watching the file for changes.
          // So change it to be valid, and the parse should succeed.
          env.copyFile(env.files.petStore, env.files.temp);
          env.touchFile(env.files.temp);

          server.once('parsed', function(err, api, parser) {
            expect(api).to.deep.equal(env.files.parsed.petStore);
            expect(parser).to.be.an.instanceOf(SwaggerParser);
            done();
          })
        });
      }
    );

  });
});
