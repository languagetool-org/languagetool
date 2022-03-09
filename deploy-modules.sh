#!/usr/bin/env bash
#
# LanguageTool, a natural language style checker
# Copyright (C) 2021 Stefan Viol (https://stevio.de)
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
# USA
#

# Exit script if you try to use an uninitialized variable.
set -o nounset
# Exit script if a statement returns a non-true return value.
set -o errexit
# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail
#
# LanguageTool, a natural language style checker
# Copyright (C) 2021 Stefan Viol (https://stevio.de)
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
# USA
#

PROJECTS=""
DEPLOY_COMMAND=""
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$BRANCH" != "master" ]]; then
  echo "No deployment for $BRANCH."
  exit 0
else
  if grep -q -e "languagetool-core/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-core",$PROJECTS; fi
  if grep -q -e "languagetool-commandline/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-commandline,$PROJECTS"; fi
  if grep -q -e "languagetool-dev/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-dev,$PROJECTS"; fi
  if grep -q -e "languagetool-gui-commons/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-gui-commons,$PROJECTS"; fi
  if grep -q -e "languagetool-http-client/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-http-client,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/ar/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/ar,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/ast/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/ast,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/be/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/be,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/br/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/br,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/ca/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/ca,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/da/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/da,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/de/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/de,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/el/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/el,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/en/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/en,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/eo/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/eo,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/es/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/es,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/fa/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/fa,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/fr/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/fr,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/ga/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/ga,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/gl/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/gl,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/is/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/is,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/it/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/it,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/ja/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/ja,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/km/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/km,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/lt/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/lt,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/ml/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/ml,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/nl/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/nl,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/pl/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/pl,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/pt/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/pt,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/ro/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/ro,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/ru/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/ru,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/sk/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/sk,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/sl/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/sl,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/sv/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/sv,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/ta/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/ta,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/tl/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/tl,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/uk/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/uk,$PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/zh/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/zh,$PROJECTS"; fi
  #if grep -q -e "languagetool-office-extension/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-office-extension,$PROJECTS"; fi
  if grep -q -e "languagetool-server/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-server,$PROJECTS"; fi
  #if grep -q -e "languagetool-standalone/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-standalone,$PROJECTS"; fi
  if grep -q -e "languagetool-tools/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-tools,$PROJECTS"; fi
  if grep -q -e "languagetool-wikipedia/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-wikipedia,$PROJECTS"; fi
fi
if [ -z "$PROJECTS" ]; then
  echo "No changes in any module detected"
  exit 0
else
  PROJECTS=${PROJECTS::-1}
  DEPLOY_COMMAND=(mvn -s .circleci.settings.xml --projects "$PROJECTS" -DskipTests deploy)
fi
echo "${DEPLOY_COMMAND[@]}"
"${DEPLOY_COMMAND[@]}"
