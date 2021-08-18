import hashlib

def hashString(string):
    bytestring = bytes(string, 'utf-8')
    hashstring = hashlib.sha256(bytestring).hexdigest()
    return hashstring
