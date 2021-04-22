#!/bin/bash
# restart the LT API server if it's not responding with code 200 to a simple query

if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters, you need to specify the server number"
    exit
fi

SLEEP=15
SERVER=$1
if [ "$SERVER" -eq 1 ]; then
    PORT=8099
fi
if [ "$SERVER" -eq 2 ]; then
    PORT=8098
fi

function isOkay {
    local RESULT=`curl -s --dump-head - "http://localhost:$PORT/v2/check?language=en-US&text=Test" | grep -c " 200 OK"`
    if [ "$RESULT" -eq "1" ]; then
        echo "OK"
    else
        echo "FAILED"
    fi
}

result=$(isOkay)
if [ "$result" == "FAILED" ]; then
    sleep $SLEEP
    result=$(isOkay)
    if [ "$result" == "FAILED" ]; then
        echo "`date` SERVER FAILED, restarting server $SERVER"
        /home/languagetool/restart-api-server$SERVER.sh
    fi
fi
