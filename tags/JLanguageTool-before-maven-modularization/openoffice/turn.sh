#!/bin/sh
# Builds LanguageTool and deploys it to OpenOffice.org

openoffice/undeploy.sh
ant fast
openoffice/deploy.sh
