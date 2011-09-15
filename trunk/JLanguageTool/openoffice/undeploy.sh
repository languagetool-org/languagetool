#!/bin/sh
# de-install LanguageTool as an add-on from OpenOffice.org/LibreOffice

echo "Undeploying LanguageTool from OpenOffice.org/LibreOffice..."
unopkg remove LanguageTool-1.5-dev.oxt
