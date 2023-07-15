#!/bin/bash

# Arguments:
# $1 - Let's Encrypt directory with certificates
# Java keystore file

if [ ! -d "$1" ]; then
	echo "$1 is not a directory, aborting ..."
	exit 1
fi

# Check if required files exits
if [ ! -f "$1"/privkey1.pem ]; then
	echo "Private key file \"$1\"/privkey1.pem does not exist, aborting ..."
	exit 1
fi

if [ ! -f "$1"/cert1.pem ]; then
	echo "Certificate file \"$1\"/cert1.pem does not exist, aborting ..."
	exit 1
fi

if [ ! -f "$1"/chain1.pem ]; then
	echo "CA cert chain \"$1\"/chain1.pem does not exist, aborting ..."
	exit 1
fi

TMPCERT=/tmp/$$-cert.pem
TMPP12=/tmp/$$-cert.p12

# Concatenate certificate and primary key
cat "$1"/privkey1.pem "$1"/cert1.pem "$1"/chain1.pem > ${TMPCERT}

# Convert to PKCS12 format
openssl pkcs12 -export -out ${TMPP12} -in ${TMPCERT}

# Import all to keystore
keytool -importkeystore -srcstoretype pkcs12 -srckeystore ${TMPP12} -deststoretype jks -destkeystore "$2"

rm -f ${TMPCERT} ${TMPP12}

# To test SSL connection with curl, run:
# curl -v -key /etc/letsencrypt/archive/<your.domain>/privkey1.pem \
# -cert /etc/letsencrypt/archive/<your.domain>/cert1.pem \
# --data "language=en-US&text=a simple test" https://localhost:<port>/v2/check

