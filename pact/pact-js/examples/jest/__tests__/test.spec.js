'use strict'

const path = require('path')
const Pact = require('../../../src/pact.js')
const getMeDogs = require('../index').getMeDogs

describe("Dog's API", () => {
  let url = 'http://localhost'

  const port = 8988
  const provider = Pact({
    port: port,
    log: path.resolve(process.cwd(), 'logs', 'mockserver-integration.log'),
    dir: path.resolve(process.cwd(), 'pacts'),
    spec: 2,
    consumer: 'MyConsumer',
    provider: 'MyProvider'
  })

  const EXPECTED_BODY = [{dog: 1}]

  beforeAll(() => provider.setup())

  afterAll(() => provider.finalize())
  jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
  describe("works", () => {
    beforeAll(done => {
      const interaction = {
        state: 'i have a list of projects',
        uponReceiving: 'a request for projects',
        withRequest: {
          method: 'GET',
          path: '/dogs',
          headers: { 'Accept': 'application/json' }
        },
        willRespondWith: {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
          body: EXPECTED_BODY
        }
      }
      provider.addInteraction(interaction).then(done, done)
    })

    // add expectations
    it('returns a sucessful body', done => {
      return getMeDogs({ url, port })
        .then(response => {
          expect(response.headers['content-type']).toEqual('application/json')
          expect(response.data).toEqual(EXPECTED_BODY)
          expect(response.status).toEqual(200)
          done()
        })
    })

    // verify with Pact, and reset expectations
    it('successfully verifies', () => provider.verify())
  })
})
