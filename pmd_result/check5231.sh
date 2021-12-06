function check_5231() {
    FORMAT=$1
    DIR=$3
    SUFFIX=txt
    if [[ $FORMAT == *html* ]]; then
        SUFFIX=html
    fi
    cd ~/pmd-bin-6.40.0/bin
    ./run.sh pmd -d ~/languagetool/languagetool-commandline/src/test/java/org/languagetool/commandline/MainTest.java \
        -f $FORMAT -R rulesets/java/quickstart.xml > $DIR/pmd_cmdTest_$2.$SUFFIX
    ./run.sh pmd -d ~/languagetool/languagetool-commandline/src/main/java/org/languagetool/commandline \
        -f $FORMAT -R rulesets/java/quickstart.xml > $DIR/pmd_cmd_$2.$SUFFIX
    ./run.sh pmd -d ~/languagetool/languagetool-core/src/main/java/org/languagetool/tools/Tools.java \
        -f $FORMAT -R rulesets/java/quickstart.xml > $DIR/pmd_tool_$2.$SUFFIX
    cd -
}

FORMAT=text
DIR=pmd_result
if [[ $# -gt 0 ]]; then
    FORMAT=$1
fi
if [[ $# -gt 1 ]]; then
    DIR=$2
fi

printf "Assuming languagetool is in the latest branch\n"
printf "  Format: ${FORMAT}, Dir: ${DIR}\n\n"

if [[ ! -d $DIR ]]; then
    mkdir $DIR 
fi

check_5231 $FORMAT 1 $DIR
printf "\n  - pmd applied to the new version\n"
cd ~/languagetool
COMMIT=$(git rev-parse HEAD)
printf "  - current commit hash:    ${COMMIT}, resetting ...\n\n"
git reset --hard 00b51a
check_5231 $FORMAT 0 $DIR
printf "\n  - pmd applied to the old version\n"
git reset --hard $COMMIT

