#!/bin/bash

for varname in ${!langtool_*}
do
  config_injected=true
  echo "${varname#'langtool_'}="${!varname} >> config.properties
done

if [ "$config_injected" = true ] ; then
    echo 'The following configuration is passed to LanguageTool:'
	echo "$(cat config.properties)"
fi

Xms=${Java_Xms:-256m}
Xmx=${Java_Xmx:-512m}

set -x
java -Xms$Xms -Xmx$Xmx -cp languagetool-server.jar org.languagetool.server.HTTPServer --port 8010 --public --allow-origin '*' --config config.properties
