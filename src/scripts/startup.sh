#!/bin/sh
#
# startup		Starts the standalone axiom test server
#
# TODO: setup chk-config
# description: Starts the standalone axiom test server.
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

###### START LOCAL CONFIGURATION
# You may wish to modify these variables to suit your local configuration

# AXIOM_HOME    Location of the Axiom Test Framework home folder
AXIOM_HOME=~/.axiom
#
# Set this AXIOM_HOME value if you want the Axiom Test Framework
# to use a location other than the default ~/.axiom as its home folder
#
# AXIOM_HOME=_AXIOM_HOME_
export AXIOM_HOME

# AXIOM_INSTALL   Location into which the Axiom Test Framework was installed
AXIOM_INSTALL=$AXIOM_HOME/dist
#
# Set this AXIOM_INSTALL value if you installed the Axiom Test Framework
# to a location other than the default (symlinkd) ~/.axiom/dist -> /opt/axiom
#
# AXIOM_INSTALL=_AXIOM_INSTALL_
export AXIOM_INSTALL

# ENDORSED_DIR      Location of the endorsed folder (not overridable)
ENDORSED_DIR=$AXIOM_HOME/endorsed

# LIB_DIR   Location of the lib folder containing dependant jars (not overridable)
LIB_DIR=$AXIOM_INSTALL/lib

# ARGS  Command line arguments to this script
ARGS="$@"

CLASSPATH="-classpath $AXIOM_INSTALL/lib/*:$ENDORSED_DIR/lib/*"
JAVA_MEM_OPTS="-Xmx1024m"
JAVA_OPTIONS="-server -showversion $JAVA_MEM_OPTS $CLASSPATH"

if [ $DEBUG ] ; then
    echo "Command \c"
    echo "java $JAVA_OPTIONS org.axiom.HostService $ARGS...\n"
fi
java $JAVA_OPTIONS org.axiom.HostService $ARGS
