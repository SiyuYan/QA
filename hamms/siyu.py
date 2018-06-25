from hamms import HammsServer
 
class MyTest(object):
    def setUp(self):
        self.hs = HammsServer()
        self.hs.start()
 
    def tearDown(self):
        self.hs.stop()