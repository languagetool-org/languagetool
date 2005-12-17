#!/bin/sh

openoffice/undeploy.sh
ant fast
openoffice/deploy.sh
