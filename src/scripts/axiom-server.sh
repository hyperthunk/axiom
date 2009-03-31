#!/bin/sh
#
# axiom-server		Starts, restarts, hups and stops the standalone axiom test server
#
# TODO: setup chk-config
# description: Starts and stops the standalone axiom test server at boot time and shutdown.
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

# Utility functions......

# error "description"
error () {
  echo $0: $* 2>&1
  exit 1
}

# find the named process(es)
findproc() {
  pid=`ps -ef |
  grep -w $1 |
  grep -v grep |
  awk '{print $2}'`
  echo $pid
}

# kill the named process(es)
killproc() {
   pid=`findproc $1`
   [ "$pid" != "" ] && kill $pid
}

#
# TODO: require presense of the vital directories
#

# Locate the startup script
SCRIPT="$AXIOM_INSTALL/bin/startup.sh"

#
# Start/stop Axiom Test Server
#
case "$1" in
'start')

    if [ $DEBUG ] ; then
        echo "Command \c"
        echo "$SCRIPT $@ ...\n"
    fi

    # Check if the server is already running.
    if [ -n "`findproc $SCRIPT`" ]; then
        if [ $DEBUG ] ; then 
            echo "$SCRIPT is already running.\n"
        fi
        exit 0
    fi
    $AXIOM_INSTALL/bin/startup.sh $@
	;;

'stop')

    if [ $DEBUG ] ; then
	    echo -n "Stopping $SCRIPT ... "
    fi
	if [ -z "`findproc $SCRIPT`" ]; then
        if [ $DEBUG ] ; then
	        echo "$SCRIPT is not running."
        fi
	    exit 0
	fi
	killproc $SCRIPT
        if [ $DEBUG ] ; then
	        echo "done"
        fi
	;;

*)
	echo "Usage: $0 { start | stop }"
	;;
esac