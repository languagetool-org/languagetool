#!/bin/bash

# Made by Innovative Inventor at https://github.com/innovativeinventor.
# If you like this code, star it on GitHub!
# Contributions are always welcome.

# MIT License
# Copyright (c) 2017 InnovativeInventor

# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -p|--package)
    package="$2"
    specifypackage=YES
    shift # past argument
    shift # past value
    ;;
    -o|--override)
    OSTYPE="$2"
    shift # past argument
    shift # past value
    ;;
    -c|--command)
    command="$2"
    shift # past argument
    shift # past value
    ;;
    -b|--build)
    build=YES
    shift # past argument
    ;;
    -h|--help)
    help=YES
    shift # past argument
    ;;
esac
done

display_help() {
    echo
    echo 'An installer for Language Tools stable releases.'
    echo 'Usage: install.sh <option> <package>'
    echo 'Options:'
    echo '   -h --help                   Show help'
    echo '   -o --override <OS>          Override automatic OS detection with <OS>'
    echo '   -b --build                  Builds packages from the bleeding edge development copy of LanguageTool'
    echo '   -p --package <package>      Specifies package to install when building (default all)'
    echo '   -c --command <command>      Specifies post-installation command to run (default gui)'
    echo
    echo 'Commands:'
    echo '   GUI                           Runs GUI version of LanguageTool'
    echo '   commandline                   Runs command line version of LanguageTool'
    echo '   server                        Runs server version of LanguageTool'
    echo
    echo 'Packages(only if -b is specified):'
    echo '   standalone                  Installs standalone package'
    echo '   wikipedia                   Installs Wikipedia package'
    echo '   office-extension            Installs the LibreOffice/OpenOffice extension package'
    echo
    echo 'Submit a GitHub issue if you are encountering problems or want to suggest new features'
    echo
    exit 1
}

install() {
    echo "Removing any old copy of LanguageTools Stable in this directory"
    rm LanguageTool-stable.zip &>/dev/null

    echo "Installing LanguageTools Stable"
    version=stable
    RELEASE_URL="https://languagetool.org/download/LanguageTool-stable.zip"
    curl -l $RELEASE_URL -o LanguageTool-stable.zip
    DIR=$(unzip LanguageTool-stable.zip | grep -m1 'creating:' | cut -d' ' -f5- )
    rm -r $DIR &>/dev/null
    RELEASE=${DIR%/}

    # Getting rid of any old folders with the same name
    rm -r $RELEASE-$version

    echo "Unzipping"
    unzip -u LanguageTool-stable.zip

    mv $RELEASE "$RELEASE-$version"
    echo "Cleaning up"
    rm LanguageTool-stable.zip
}

build () {
    # Cloning from GitHub
    git clone --depth 1 https://github.com/languagetool-org/languagetool.git
    cd languagetool || exit 2

    # Seeing if maven is installed, and installing it if not
    detect_maven

    mvn clean test
    if [ "$specifypackage" = YES ]; then
        ./build.sh languagetool-$package package
    else
        ./build.sh languagetool-standalone package
        ./build.sh languagetool-wikipedia package -DskipTests
        ./build.sh languagetool-office-extension package -DskipTests
    fi
}

install_maven() {
    echo "Installing maven . . ."
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        sudo apt-get update -y
        sudo apt-get install maven

    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if ! [ -x "$(brew -v)" ]; then
               /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
        fi
        brew update
        brew install maven

    else
            echo "Error: java is not installed and operating system detection error"
            echo "   OS type not supported!"
            echo "   Please install maven yourself or override automatic OS detection with -o <OS> See help for more details."
    fi
}

install_java() {
    echo "Java is not installed"
    echo "Installing java . . ."

    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        sudo apt-get update
        sudo apt-get install java -y

    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if ! [ -x "$(brew -v)" ]; then
               /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
        fi
        brew update
        brew cask install java

    else
            echo "Error: java is not installed and operating system detection error"
            echo "   OS type not supported!"
            echo "   Please install maven yourself or override automatic OS detection with -o <OS> See help for more details."
    fi
}

detect_maven() {
    if ! [ -x "$(mvn -v)" ]; then
        install_maven
    fi
}

detect_java() {
    if ! [ -x "$(java -version)" ]; then
        install_maven
    fi
}

postinstall_command () {
    if [ "$command" = GUI ] || [ "$command" = gui ] || [ "$command" = standalone ]; then
        cmd="languagetool-standalone/"
    elif [ "$command" = commandline ] || [ "$command" = cmdline ] || [ "$command" = cmd ] || [ "$command" = CMD ] || [ "$command" = "command line" ]; then
        cmd="languagetool-commandline"
    elif [ "$command" = server ] || [ "$command" = web ]; then
        cmd="languagetool-server"
    else
        cmd="languagetool"
    fi
}


# Detect if Java is installed
detect_java

# Help option
if [ "$help" == YES ]; then
    display_help
fi

# Build or install
if [ "$build" == YES ]; then
    build
    echo "Your build is done."
    echo "Post-installation commands are not availble for the build option. Contributions are welcome."
else
    install
    postinstall_command
    echo "Running $cmd, press CTRL-C to cancel"
    java -jar "$RELEASE-$version"/$cmd.jar
fi
