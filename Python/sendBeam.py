#!/usr/bin/env python
import logging
log = logging.getLogger('main')

import threading
import sys
import os
import hashlib
import base64

#sys.path.insert(1, os.path.split(sys.path[0])[0])
import nfc
import nfc.snep
import nfc.ndef


#aar = nfc.ndef.Record('urn:nfc:ext:android.com:pkg', '', 'vs.in.de.uni_ulm.mreuter.login')
#test = nfc.ndef.TextRecord("English UTF-8 encoded")

#message = nfc.ndef.Message(test, aar)

class DefaultServer(nfc.snep.SnepServer):
    def __init__(self, llc):
        super(DefaultServer, self).__init__(llc)

    def put(self, ndef_message):
        print "client hast put an NDEF message"
        print ndef_message.pretty()
        return nfc.snep.Success

def send_ndef_message(llc):
    aar = nfc.ndef.Record('urn:nfc:ext:android.com:pkg', '', 'vs.in.de.uni_ulm.mreuter.login')

    md = hashlib.sha256()
    md.update("username@www.google.com")
    digest = md.digest()
    jointhash = base64.b64encode(digest)

   # action = nfc.ndef.Record('application/vnd.vs.in.de.uni_ulm.mreuter.login', '', 'process')
    data = nfc.ndef.Record('application/vnd.vs.in.de.uni_ulm.mreuter.login', 'update', jointhash)
    #data = nfc.ndef.TextRecord(jointhash);
    snep = nfc.snep.SnepClient(llc)
    snep.put(nfc.ndef.Message(data))

def on_startup(clf, llc):
    global my_snep_server
    my_snep_server = nfc.snep.SnepServer(llc, "urn:nfc:sn:snep")
    return llc
        
def on_connect(llc):
    my_snep_server.start()
    threading.Thread(target=send_ndef_message, args=(llc,)).start()
    return True

try:
    clf = nfc.ContactlessFrontend('usb')
    clf.connect(llcp = {'on-startup': on_startup, 'on-connect': on_connect})
except Exception, e:
    print "No NFC reader found. Please try again"
