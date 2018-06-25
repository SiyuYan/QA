var config = require('../config/host_config');
var url = config.host['books'];

var request = require('supertest')(url + '/volumes');
var chai = require('chai');
var expect = require('chai').expect;

var id;


// Test Google Books API
describe('GOOGLE BOOKS API - VOLUMES',function(){

    it.skip('Search books with a mandatory parameter(q="test") - Return 200', function(done){
        request
            .get('/?q=test')
            //Check http status is 200
            .expect(200)
            .end(function(err,res){

    //                console.log(res);
                    done(err);

                })

        })


    it('Search books with two parameters(q="cucumber"&maxResult=2) - Return 200 OK', function(done){

        var q = 'cucumber'
        var maxResults = 2

        this.timeout(10000);

        request.get('/')

            .query({
                q: q,
                maxResults: maxResults
            })

            .expect(200)

            .expect(function(res) {
                //Get item id for the first book
                id = res.body.items[0].id;
                var selfLinkLength = res.body.items[0].selfLink.split('/').length;
                var selfLinkId = res.body.items[0].selfLink.split('/')[selfLinkLength - 1];

                //Check book title contains the first parameter - cucumber
                expect(res.body.items[0].volumeInfo.title.toLowerCase()).to.contain(q);
                //Check total itmes <= the second parameter - 2
                expect(res.body.items.length).be.at.most(maxResults);
                //Check item id is the same as the id in selfLink
                expect(selfLinkId).to.equal(id);
                //Check mandatory keys, e.g. kind, totalItems, items....
                expect(res.body).to.include.keys('kind', 'totalItems', 'items');

            })

            .end(function(err,res){

               done(err);

            })

    })

    it('Retrieves a Volume resource based on ID that is from last case - Return 200', function(done){

        this.timeout(10000);

        request.get('/' + id)
            .expect(200)

            .expect(function(res){

                // Check the id in response is the same as parameter
                expect(res.body.id).to.equal(id)

            })

            .end(function(err,res){

                done(err);

            })

    })

})