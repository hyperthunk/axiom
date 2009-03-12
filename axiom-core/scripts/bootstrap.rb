=begin
Copyright (c) 2009, Tim Watson
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of the author nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
=end

# route configurator for booting the control channel context
# note that you don't have to configure logging/tracing here as it is
# enabled in the properties/configuration stage instead

import org.axiom.integration.Environment

control_channel = config >> 'axiom.control.channel'

route {
  
  from("direct:start").inOut.to(control_channel)

  from("jetty://#{config >> 'jetty.host'}/axiom/control-channel").
    inOut.to(control_channel)

  intercept(xpath('/config[count(routes) > 0]')).
    to(config >> 'axiom.control.processors.xml2code')
    process(add_header "payload-classifier" => 'code').proceed

  intercept(header('payload-classifier').isEqualTo('code')).
    processRef(config >> Environment.ROUTE_SCRIPT_EVALUATOR).proceed

  # TODO: this implementation is badly broken - change it to use an axiom component instead
  # that processRef has no CamelContext associated with it, for example
  from(control_channel).
    processRef(config >> Environment.DEFAULT_PROCESSOR).
      proceed.choice.
        when(header("command").isEqualTo("shutdown")).
          to(config >> 'axiom.control.channel.shutdown').
        otherwise.stop # stops the routing, not the server itself!
}
