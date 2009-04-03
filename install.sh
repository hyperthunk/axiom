#!/usr/bin/env sh
#
# install		Install script for the Axiom Integration Testing Framework
#
#################################################################################################
#
# Copyright (c) 2009, Tim Watson
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification,
# are permitted provided that the following conditions are met:
#
#     * Redistributions of source code must retain the above copyright notice,
#       this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright notice,
#       this list of conditions and the following disclaimer in the documentation
#       and/or other materials provided with the distribution.
#     * Neither the name of the author nor the names of its contributors
#       may be used to endorse or promote products derived from this software
#       without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
# GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
# OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

ensure_dir() {
	if [ ! -d "$1" ]; then
		echo "Creating $1 directory."
		if mkdir -p $1; then
			echo "mkdir -p $1"
			return 0
		else
			echo "Failed to create $1"
  			return 1
		fi
	fi
}

if [[ "$1" == *--help*  ]]; then
	echo "install.sh [Options]"
	echo "--dest	Destination folder (default = /opt/axiom)"
	echo "--home 	Axiom Home folder (default = $INSTALL/.axiom)"
	echo "--bin		Location for the executable script (optional, default = /usr/bin)"
	exit 1
fi

INSTALL_DIR=/opt/axiom
AXIOM_HOME="$INSTALL_DIR/.axiom"
EXECUTABLE_PATH=/usr/bin

for arg in $@; do
	if [[ $arg == *--dest=* ]]; then
	    INSTALL_DIR=${arg:7}
	fi	
	if [[ $arg == *--home=* ]]; then
	    AXIOM_HOME=${arg:7}
	fi
	if [[ $arg == *--bin=* ]]; then
		EXECUTABLE_PATH=${arg:6}
	fi
done

if ensure_dir "$INSTALL_DIR"; then
	echo "Installing axiom to $INSTALL_DIR."
else
	echo "Cancelling installation."
	exit 1	# TODO: use the correct exit code
fi

if ensure_dir "$AXIOM_HOME"; then
	echo "Setting axiom home to $AXIOM_HOME."
else
	echo "Cancelling installation."
	rm -drf "$INSTALL_DIR"
	exit 1	# TODO: use the correct exit code
fi
# TODO: check these as well
ensure_dir "$AXIOM_HOME/conf"
ensure_dir "$AXIOM_HOME/endorsed"
ensure_dir "$AXIOM_HOME/endorsed/lib"

# move over a set of default logging properties
cp config/log4j.properties "$AXIOM_HOME/endorsed/"

# link the app distribution to the home directory
ln -s "$INSTALL_DIR" "$AXIOM_HOME/dist"

# make a symlink to the startup script in /usr/bin (or equiv)
ln -s "$INSTALL_DIR/bin/axiom-server.sh" "$EXECUTABLE_PATH/axiom"

for d in 'lib' 'bin'; do
	if cp -r "$d" "$INSTALL_DIR/"; then  
		echo "cp -r $d $INSTALL_DIR/"
	else
		echo "Failed to copy $d to $INSTALL_DIR/"
		echo "Cancelling installation."
		rm -drf "$INSTALL_DIR $AXIOM_HOME"
		exit 1
	fi
done

echo $AXIOM_HOME | sed 's/\//\\\//g' | awk '/.*/ { print $0 }' >> tmpfile
home=`cat tmpfile`
rm tmpfile

# overwrite the original launch/init.d script with the actual home folder location
echo "sed -E -i .bak s/AXIOM_HOME=%AXIOM_HOME%/AXIOM_HOME=$home/ $INSTALL_DIR/bin/axiom-server.sh"
sed -E -i ".bak" "s/AXIOM_HOME=%AXIOM_HOME%/AXIOM_HOME=$home/" "$INSTALL_DIR/bin/axiom-server.sh"

# TODO: find a way to do this that isn't platform specific
# TODO: generate a launchd script (interpolating settings using sed?) for osx 

# echo "Installing init.d script to $INIT_DIR"
# x=ensure_dir "$INIT_DIR"
if true; then
	echo "Failed to install init.d script. This can be installed manually later on."
	echo "Installation complete. You can start the server by running $INSTALL_DIR/bin/axiom-server.sh start"
else
	echo "Installation complete. You can start the server by running '> $INIT_DIR start'"
fi
