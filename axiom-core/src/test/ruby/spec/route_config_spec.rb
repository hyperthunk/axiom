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

require 'axiom'
require 'axiom/plugins'
require 'axiom/plugins/route_config'

import org.apache.camel.Exchange
import org.apache.camel.Message
import org.axiom.integration.Environment
import org.axiom.integration.camel.RouteConfigurationScriptEvaluator

describe "The built in route_config plugin" do

  include Axiom::Plugins

  it "should return a processor implementation" do
    processor = route_config mock()
    processor.should be_instance_of(Axiom::Core::Processor)
  end

  it "should puke if the required configurator is missing" do
    lambda {
      route_config nil
    }.should raise_error 
  end

  it "should pull the body from the input channel and pass it to the configurator" do
    body = 'BODY'
    configurator = RouteConfigurationScriptEvaluator.new 
    configurator.expects(:configure).once.with(body)

    route_config(configurator).
      process(
        stub 'exchange-stub',
          :getIn => stub('in_channel', :body => body),
          :getOut => stub('out_channel', :body= => nil, :set_header => nil)
      )
  end

  it "should set the configure signal header on the output channel" do
    out_channel = Message.new
    out_channel.stubs(:body=)
    out_channel.expects(:set_header).once.with(
        Environment::SIGNAL, Environment::SIG_CONFIGURE)

    route_config(stub 'configurator', :configure => nil).
      process(
        stub 'exchange-stub',
          :getIn => stub('in_channel', :body => 'BODY'),
          :getOut => out_channel
      )
  end

end
