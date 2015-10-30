#!/usr/bin/env python
import nfc
import nfc.snep
import base64
import hashlib
from ecdsa import VerifyingKey, NIST256p, SigningKey

class DefaultSnepServer(nfc.snep.SnepServer):
    def __init__(self, llc):
        nfc.snep.SnepServer.__init__(self, llc, "urn:nfc:sn:snep")

    def put(self, ndef_message):
        print "client has put an NDEF message"
        #print ndef_message.pretty()
        keyRecord = ndef_message.pop()
        sigRecord = ndef_message.pop()
        actionRecord = ndef_message.pop()

        vk = VerifyingKey.from_pem(keyRecord.data.decode("utf-8"))
        print vk.to_pem() == keyRecord.data.decode("utf-8")
        
        print "Key created. Yay!"
        try:
            tmp = vk.verify(sigRecord.data, "test", hashfunc=hashlib.sha256())
            print tmp
        except Exception, e:
            print "Signature Error"
        return nfc.snep.Success

def startup(clf, llc):
    global my_snep_server
    my_snep_server = DefaultSnepServer(llc)
    return llc

def connected(llc):
    my_snep_server.start()
    return True

my_snep_server = None
try:
    clf = nfc.ContactlessFrontend("usb")
    clf.connect(llcp={'on-startup': startup, 'on-connect': connected})
except Exception, e:
    print "No NFC reader found. Please try again"