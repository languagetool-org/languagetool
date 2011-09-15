#!/bin/sh
# install LanguageTool as an add-on for OpenOffice.org/LibreOffice

echo "Deploying LanguageTool to OpenOffice.org/LibreOffice..."
unopkg add dist/LanguageTool-1.5-dev.oxt
