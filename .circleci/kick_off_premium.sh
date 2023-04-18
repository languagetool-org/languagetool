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
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$BRANCH" != "master" ]]; then
  echo "No premium tests for $BRANCH."
  exit 0
fi
if grep -q -e "languagetool-core/.*" /home/circleci/git_diffs.txt; then
  PROJECTS="languagetool-core/"
else
  if grep -q -e "languagetool-dev/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-dev/ $PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/de/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/de/ $PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/en/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/en/ $PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/es/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/es/ $PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/fr/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/fr/ $PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/nl/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/nl/ $PROJECTS"; fi
  if grep -q -e "languagetool-language-modules/pt/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-language-modules/pt/ $PROJECTS"; fi
  if grep -q -e "languagetool-server/.*" /home/circleci/git_diffs.txt; then PROJECTS="languagetool-server/ $PROJECTS"; fi
fi
if [ -z "$PROJECTS" ]; then
  echo "No changes in any module detected"
  exit 0
else
  PROJECTS=${PROJECTS::-1}
fi
curl --request POST \
  --url https://circleci.com/api/v2/project/gh/languagetooler-gmbh/languagetool-premium-modules/pipeline \
  --header "Circle-Token: ${API_TOKEN}" \
  --header "Content-Type: application/json" \
  --data "{\"branch\":\"master\",\"parameters\":{\"run_on_pull\":false, \"os-sha\": \"'${CIRCLE_SHA1}'\", \"os-changes\":\"$PROJECTS\"}}"
