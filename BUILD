The axiom project is built using maven2, and requires Java >= 1.5 to compile. In
order to run the jtestr tests on os-x, it seems to be necessary that you use
Java 1.6. I've therefore made this the default source and JDK level in the pom
build section - you can probably change these without great issue on other platforms.

Local Install:

You should be able to accomplish a local maven install by running the command:

    $ mvn clean install

To create a binary distribution you need to run the package task, specifying that 
the maven-assembly-plugin should run, like so:

    $ mvn package assembly:assembly

For installing the binaries and/or running axiom, please see the INSTALL doc that
sits alongside this document. 


Testing:

Some of the unit/integration tests for axiom are written using the jtestr test
library, which in turn uses jruby. For some reason (that I've not investigated
at length) maven seems to require that you explicitly specify the plugin should
be run, which you can do like so:

    $ mvn clean install org.jtestr:jtestr:test

The startup time for this can be very slow, therefore to mitigate this problem,
you can launch a background server to host the jruby runtime and act as a sandbox
for running the tests. To do so, you run the following command:

    $ mvn org.jtestr:jtestr:server

Platforms:

All development of axiom takes place on linux and osx/darwin but it should work in
most unix like environments. Testing on windows will take place prior to RC1.

Other Notes:

Code Coverage:

Running the cobertura maven plugin seems to eroeously cause test failures in the build.
To date I've been using code coverage in my IDE and leaving it out of the build, but
getting this in place is on our 'TODO' list

Surefire/Test Reports:

The jtestr maven plugin we're currently using doesn't generate surefire reports 
for the rspec test runs. The alternative approach (using the maven-antrun plugin
instead) is somewhat prone to classpath issues, so we're going to live with this
for the time being.

Installing on OS-X:

There are some minor issues when installing on OSX, not the least of which 
is that Java 1.6 is still not the default JDK on Leopard, therefore you should 
probably take some additional steps to ensure that both maven and its plugins
use the correct JDK (1.6). I seem to recall that one of the plugins we're using
requires maven >= 2.0.9 so I recommend using at least that (or the latest version)
and checking your maven version if you run into build problems.

The following environment variables will need to be exported into the bash session
in which you're launching maven:

export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home
export JAVA_VERSION=1.6

If you're trying to run maven from within an IDE, then look at the configuration 
settings to see if you can control the environment in this (or in some other) way.

