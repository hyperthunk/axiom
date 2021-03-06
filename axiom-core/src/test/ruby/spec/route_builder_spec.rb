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

import org.apache.camel.builder.RouteBuilder
import org.apache.commons.configuration.Configuration
import org.apache.camel.Exchange

require 'axiom/core/route_builder'
require 'axiom/core/route_builder_configurator'

describe Axiom::Core::SimpleRouteBuilder,
  "when configuring routes using a user defined block of java DSL code" do

  it "should puke if the block is missing" do
    lambda do
      Axiom::Core::SimpleRouteBuilder.new
    end.should raise_error
  end

  it "should execute the block's routing instructions in the context of the builder object instance" do
    route_builder = Axiom::Core::SimpleRouteBuilder.new do
        from("direct:start").to("mock:result")
    end
    check_basic_route route_builder
  end

end

describe Axiom::Core::SimpleRouteBuilder,
  "when performing a lookup in the underlying camel spi registry" do

  it "should lookup the registry using the underlying camel context" do
    mock_registry = org.apache.camel.spi.Registry.new
    mock_context = org.apache.camel.CamelContext.new
    mock_context.expects(:registry).once.returns(mock_registry)
    mock_registry.stubs(:lookup).returns(:expected_response)

    builder = Axiom::Core::SimpleRouteBuilder.new do
      lookup('foobarbaz')
    end
    builder.context = mock_context
    builder.configure.should eql(:expected_response)
  end

end

describe Axiom::Core::SimpleRouteBuilder,
  "when adding multiple headers dynamically with the DSL wrapper method" do

  it "should generate a processor instance for calls to set_header" do
    Axiom::Core::SimpleRouteBuilder.new{}.add_headers({}).
        class.should == Axiom::Core::Processor
  end

  it "should not puke if the supplied header values are nil" do
    lambda do
      Axiom::Core::SimpleRouteBuilder.new{}.add_headers(nil)
    end.should_not raise_error
  end

  it "should add each of the supplied headers to the given exchange" do
    mock_message = org.apache.camel.Message.new
    ex = org.apache.camel.Exchange.new
    ex.stubs(:out).returns(mock_message)

    new_headers = {
      :route_slip => 'IO8988273TY2232',
      :reply_to => 'jms:topicname?setCorrelationIdIgnored=false'
    }

    new_headers.each do |k,v|
      mock_message.expects(:set_header).with(k,v).at_least_once
    end

    processor = Axiom::Core::SimpleRouteBuilder.new{}.add_headers(new_headers)
    processor.process(ex)
  end

end
