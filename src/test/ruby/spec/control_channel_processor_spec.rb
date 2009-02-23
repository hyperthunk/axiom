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

import org.axiom.management.ControlChannelProcessor
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Message
import java.util.List

describe "initializing the control channel processor" do
  it "should puke if the ctor is passed null context" do
    lambda {
      ControlChannelProcessor.new nil, nil
    }.should raise_error(java.lang.IllegalArgumentException)
  end

  it "should puke if the ctor is passed null evaluator" do
    lambda {
      ControlChannelProcessor.new CamelContext.new, nil
    }.should raise_error(java.lang.IllegalArgumentException)
  end
end

describe "stopping and starting via the control channel" do

  [:start, :stop].each do |instruction|
    it "should #{instruction} the camel context when the relevant header is supplied" do
      context = CamelContext.new
      context.expects(instruction).once

      # first message is a dud, second one says stop...
      # same stub will do for both cases, as well as for the header values
      mock_channel = Message.new
      mock_channel.stubs(:getHeader).returns "test", instruction.to_s

      processor = processor_for context
      2.times { processor.process(stubbed_exchange mock_channel) }
    end
  end

  it "should configure the camel context when routes are supplied" do
    context = CamelContext.new
    context.expects(:addRoutes).once.with{ |x| x.is_a? List }

    mock_channel = Message.new
    mock_channel.stubs(:getHeader).returns "configure"
    mock_channel.stubs(:getBody).returns SimpleRouteBuilder.new {}

    config = RouteBuilderConfigurator.new
    processor = ControlChannelProcessor.new context, config
    processor.process(stubbed_exchange mock_channel)
  end

end
