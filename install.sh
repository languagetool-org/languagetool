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

# Options
VERSION="2.3"
clone_depth="1"
text="spellcheck.txt"

while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -p|--package)
    package="$2"
    build=YES
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
    -t|--text)
    text="$2"
    shift # past argument
    shift # past value
    ;;
    -r|--remove)
    remove="$2"
    uninstall=YES
    shift # past argument
    shift # past value
    ;;
    -b|--build)
    build=YES
    shift # past argument
    ;;
    -d|--depth)
    clone_depth="$2"
    build=YES
    shift # past argument
    shift # past value
    ;;
    -h|--help)
    help=YES
    shift # past argument
    ;;
    -q|--quiet)
    quiet=YES
    shift # past argument
    ;;
    -a|--accept)
    accept=YES
    shift # past argument
    ;;
esac
done

display_help() {
    echo
    echo "Script version $VERSION"
    echo 'A tool for installing or building LanguageTool.'
    echo 'Usage: install.sh <option> <package>'
    echo 'Options:'
    echo '   -h --help                   Show help'
    echo '   -b --build                  Builds packages from the bleeding edge development copy of LanguageTool'
    echo '   -c --command <command>      Specifies post-installation command to run (default gui when screen is detected)'
    echo '   -q --quiet                  Shut up LanguageTool installer! Only tell me important stuff!'
    echo '   -t --text <file>            Specifies what text to be spellchecked by LanguageTool command line (default spellcheck.txt)'
    echo '   -d --depth <value>          Specifies the depth to clone when building LanguageTool yourself (default 1).'
    echo '   -p --package <package>      Specifies package to install when building (default all)'
    echo '   -o --override <OS>          Override automatic OS detection with <OS>'
    echo '   -a --accept                 Agree to all downloading and installing prompts.'
    echo '   -r --remove <all/partial>   Removes LanguageTool install. <all> uninstalls the dependencies that were auto-installed. (default partial)'
    echo
    echo 'Packages(only if -b is specified):'
    echo '   standalone                  Installs standalone package'
    echo '   wikipedia                   Installs Wikipedia package'
    echo '   office-extension            Installs the LibreOffice/OpenOffice extension package'
    echo
    echo 'Commands:'
    echo '   GUI                         Runs GUI version of LanguageTool'
    echo '   commandline                 Runs command line version of LanguageTool'
    echo '   server                      Runs server version of LanguageTool'
    echo
    echo 'Submit a GitHub issue if you are encountering problems or want to suggest new features'
    echo
    exit 1
}

install() {
    echo "Removing any old copy of LanguageTools Stable in this directory"
    rm LanguageTool-stable.zip # maybe switch with 2> /dev/null XXX here removals should be verbose

    detect_unzip

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

install_quiet() {
    # Removing any old copy of LanguageTools Stable in this directory
    rm LanguageTool-stable.zip &>/dev/null

    # Detecting unzip
    detect_unzip

    # Installing LanguageTools Stable
    version=stable
    RELEASE_URL="https://languagetool.org/download/LanguageTool-stable.zip"
    curl -s -l $RELEASE_URL -o LanguageTool-stable.zip
    DIR=$(unzip LanguageTool-stable.zip | grep -m1 'creating:' | cut -d' ' -f5- )
    rm -r $DIR &>/dev/null
    RELEASE=${DIR%/}

    # Getting rid of any old folders with the same name
    rm -r $RELEASE-$version &>/dev/null

    # Unzipping
    unzip -q -u LanguageTool-stable.zip

    mv $RELEASE "$RELEASE-$version"

    # Cleaning up
    rm LanguageTool-stable.zip
}

build () {
    if [ -e languagetool ]; then
        echo "Moved current languagetool directory to languagetool-previous"
        mv languagetool languagetool-previous
    fi

    # Cloning from GitHub
    git clone --depth "$clone_depth" https://github.com/languagetool-org/languagetool.git
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

build_quiet () {

    if [ -e languagetool ]; then
        echo "Moved current languagetool directory to languagetool-previous"
        mv languagetool languagetool-previous
    fi

    # Cloning from GitHub
    git clone -q --depth "$clone_depth" https://github.com/languagetool-org/languagetool.git
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

uninstall_loud () {
    touch languagetool
    rm -r "languagetool"
    touch LanguageTool-uninstall
    rm -r "LanguageTool-"*
    if [ "$remove" = "all" ] || [ "$remove" = "ALL" ] || [ "$remove" = "a" ] || [ "$remove" = "A" ]; then
        detect_uninstall
        bash /etc/languagetool/uninstall.sh
        rm /etc/languagetool/uninstall.sh
    fi
    echo "LanguageTool uninstalled!"
    exit
}

uninstall_quiet () {
    touch languagetool
    rm -r "languagetool"
    touch LanguageTool-uninstall
    rm -r "LanguageTool-"*
    if [ "$remove" = "all" ] || [ "$remove" = "ALL" ] || [ "$remove" = "a" ] || [ "$remove" = "A" ]; then
        detect_uninstall
        echo "This may take some time . . ."
        bash /etc/languagetool/uninstall.sh &>/dev/null
        rm -f /etc/languagetool/uninstall.sh # Find way to silence this
    fi
    exit
}

install_maven() {
    detect_uninstall
    echo "Maven is not installed."
    echo "Installing maven . . ."
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        apt update -y
        apt install maven -y
        echo "apt remove maven -y" >> /etc/languagetool/uninstall.sh
    
    # XXX This needs to be reviewed by someone with a MacOS
    #     Uncomment after review
    #
    # elif [[ "$OSTYPE" == "darwin"* ]]; then
    #    if ! [ -x "$(brew -v)" ]; then
    #           install_homebrew
    #    fi
    #    brew update
    #    brew install maven
    #    echo "brew remove maven" >> /etc/languagetool/uninstall.sh
    #
    else
            echo "Error: maven is not installed and operating system detection error"
            echo "   OS type not supported!"
            echo "   Please install maven yourself or override automatic OS detection with -o <OS> See help for more details."
    fi
}

install_java() {
    detect_uninstall
    echo "Java is not installed."
    echo "Installing java . . ."

    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        apt install default-jre default-jdk -y
        echo "apt remove default-jre default-jdk -y" >> /etc/languagetool/uninstall.sh # need to test
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if ! [ -x "$(brew -v)" ]; then
               install_homebrew
        fi
        brew update
        brew cask install java
        echo "brew remove java" >> /etc/languagetool/uninstall.sh

    else
            echo "Error: java is not installed and operating system detection error"
            echo "   OS type not supported!"
            echo "   Please install java yourself or override automatic OS detection with -o <OS> See help for more details."
    fi
}

install_unzip() {
    detect_uninstall
    echo "Unzip is not installed."
    echo "Installing unzip . . ."

    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        apt update
        apt install unzip
        echo "apt remove unzip -y" >> /etc/languagetool/uninstall.sh

    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if ! [ -x "$(brew -v)" ]; then
               install_homebrew
        fi
        brew update
        brew install unzip
        echo "brew remove unzip" >> /etc/languagetool/uninstall.sh

    else
            echo "Error: unzip is not installed and operating system detection error"
            echo "   OS type not supported!"
            echo "   Please install unzip yourself or override automatic OS detection with -o <OS> See help for more details."
    fi
}

install_homebrew() {
    if ! [[ "$accept" == "YES" ]]; then
        /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
    else
        detect_screen
        if ! [[ "$display" == "no" ]]; then
            dialog=$(osascript \
            -e 'on homebrew_dialog()' \
            -e 'tell application "System Events"' \
            -e 'activate' \
            -e 'set dialog_result to display dialog "Do you want to get homebrew to install packages necessary for languagetool?" with title "LanguageTool Installer" buttons {"Yes","No","What is Homebrew?"} default button 1' \
            -e 'end tell' \
            -e 'set button to button returned of dialog_result' \
            -e 'if the button is "What is Homebrew?" then' \
            -e 'open location "https://brew.sh/#question"' \
            -e 'activate' \
            -e 'set dialog_result to display dialog "Do you want to get homebrew to install packages necessary for languagetool?" with title "LanguageTool Installer" buttons {"Yes","No"} default button 1' \
            -e 'set button to button returned of dialog_result' \
            -e 'end if' \
            -e 'return button' \
            -e 'end' \
            -e 'homebrew_dialog()' \
            -e 'tell application "System Events"' \
            -e 'tell process "finder"' \
            -e 'activate' \
            -e 'keystroke tab using {command down}' \
            -e 'end tell' \
            -e 'end tell'
            )
        else
            echo "No display detected"
            read -p "Do you want to install homebrew? " -n 1 -r
            echo    # (optional) move to a new line
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                dialog="Yes"
            fi
        fi
        if [[ "$dialog" = "Yes" ]]; then
            /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
        else
            echo "Can not install homebrew. Packages will not work."
            echo "Exiting"
            exit 1
        fi
    fi
}

detect_maven() {
    if ! [ "$(type -t mvn)" ]; then
        install_maven
    fi
}

detect_java() {
    if ! [ "$(type -t java)" ]; then
        install_java
    fi
}

detect_unzip() {
    if ! [ "$(type -t unzip)" ]; then
        install_unzip
    fi
}

detect_screen() {
    if ! [ "$(type -t DISPLAY)" ]; then
        if [ "$command" ]; then
            command="commandline"
        else
            command="none"
        fi
        display='no'
    fi
}

detect_uninstall() {
    if ! [ -e "/etc/languagetool/uninstall.sh" ]; then
        mkdir -p /etc/languagetool
        touch /etc/languagetool/uninstall.sh
        echo "#!/bin/bash" >> /etc/languagetool/uninstall.sh
    fi
}

postinstall_command () {
    detect_screen
    file=""
    if [ "$command" = GUI ] || [ "$command" = gui ] || [ "$command" = standalone ]; then
        cmd="languagetool-standalone/"
    elif [ "$command" = commandline ] || [ "$command" = cmdline ] || [ "$command" = cmd ] || [ "$command" = CMD ] || [ "$command" = "command line" ]; then
        file="$text"
        check_command_line
    elif [ "$command" = server ] || [ "$command" = web ]; then
        cmd="languagetool-server"
    else
        cmd=""
    fi
}

# Checks if file specified with command line option exists
check_command_line () {
    if [ -e $file ]; then
        cmd="languagetool-commandline"
    else
        echo "Error: spellcheck.txt does not exist, and no text to be checked was specified."
        exit 1
    fi
}

build_or_install_loud () {
    # Build or install loudly
    if [ "$build" == YES ]; then
        build
        echo "Your build is done."
        echo "Post-installation commands are not available for the build option. Contributions are welcome."
    else
        install
        postinstall_command
        if [ "$cmd" ]; then
            echo "Running $cmd, press CTRL-C to cancel"
            java -jar "$RELEASE-$version"/$cmd.jar $file
        fi
    fi
}

build_or_install_quiet () {
    # Build or install quietly
    if [ "$build" == YES ]; then
        build_quiet
        echo "Your build is done."
        echo "Post-installation commands are not available for the build option. Contributions are welcome."
    else
        install_quiet
        postinstall_command
        if [ "$cmd" ]; then
            echo "Running $cmd, press CTRL-C to cancel"
            java -jar "$RELEASE-$version"/$cmd.jar
        fi
    fi
}

# Detect if Java is installed
detect_java

# Help option
if [ "$help" == YES ]; then
    display_help
fi

# Detect if uninstalled was selected
if [ "$uninstall" == YES ]; then
    if [ "$quiet" == YES ]; then
        uninstall_quiet
    else
        uninstall_loud
    fi
fi

# Build or install options
if [ "$quiet" == YES ]; then
    build_or_install_quiet
else
    build_or_install_loud
fi
