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
		x=`mkdir -p $1`
		# TODO: this (next) approach is horrible - do it a cleaner way when there's time
		if [ $x ]; then
			echo "Failed to create $1"
		fi
	fi
}

INSTALL_DIR=/opt/axiom
if [ "$1" ]; then
	INSTALL_DIR="$1"
fi
echo "Installing axiom to $INSTALL_DIR."
x=`ensure_dir $INSTALL_DIR`
if [[ $x == *Failed* ]]; then
	echo "$x:\nCancelling installation."
	exit 1	# TODO: use the correct exit code
fi

AXIOM_HOME=~/.axiom
if [ "$2" ]; then
	AXIOM_HOME="$2"
fi
echo "Setting axiom home to $AXIOM_HOME."
x=`ensure_dir "$AXIOM_HOME"`
if [[ $x == *Failed* ]]; then
	echo "$x:\nCancelling installation."
	rm -drf "$INSTALL_DIR"
	exit 1	# TODO: use the correct exit code
fi
ensure_dir "$AXIOM_HOME/conf"
ensure_dir "$AXIOM_HOME/endorsed"
ensure_dir "$AXIOM_HOME/endorsed/lib"

cd ..
for d in 'lib' 'bin'; do 
	echo "cp -r $d $INSTALL_DIR/"
	cp -r "$d" "$INSTALL_DIR/"
done

echo "Installing init.d script to $INIT_DIR"
x=ensure_dir "$INIT_DIR"
if [[ $x = *Failed* ]]; then
	echo "Failed to install init.d script. This can be installed manually later on."
	echo "Installation complete. You can start the server by running $INSTALL_DIR/bin/startup.sh"
else
	echo "Installation complete. You can start the server by running '> $INIT_DIR start'"
fi
