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

#x="--dest=/usr/local/axiom"
#if [[ $x == *--dest=* ]]; then
#    x=${x:7}
#fi
#echo $x

INSTALL_DIR=/opt/axiom
if [ "$1" ]; then
	INSTALL_DIR="$1"
fi
if ensure_dir $INSTALL_DIR; then
	echo "Installing axiom to $INSTALL_DIR."
else
	echo "Cancelling installation."
	exit 1	# TODO: use the correct exit code
fi

AXIOM_HOME=~/.axiom
if [ "$2" ]; then
	AXIOM_HOME="$2"
fi
if ensure_dir $AXIOM_HOME; then
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

for d in 'lib' 'bin'; do
	if cp -r "$d" "$INSTALL_DIR/"; then  
		echo "cp -r $d $INSTALL_DIR/"
	fi
done

echo "Installing init.d script to $INIT_DIR"
#x=ensure_dir "$INIT_DIR"
if true; then
	echo "Failed to install init.d script. This can be installed manually later on."
	echo "Installation complete. You can start the server by running $INSTALL_DIR/bin/startup.sh"
else
	echo "Installation complete. You can start the server by running '> $INIT_DIR start'"
fi
