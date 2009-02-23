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

require File.join(File.dirname(__FILE__), '../../../main/resources/ruby/route_builder.rb')

describe DelegatingProcessor do

  it "should puke if the expected block is missing" do
    lambda { DelegatingProcessor.new }.should raise_error
  end

  it "should call the supplied block to perform processing" do
    # NB: this is really ugly, maybe there's a better way???
    block_called = false
    DelegatingProcessor.new {|_| block_called = true }.process nil
    block_called.should be_true
  end

  it "should pass the exchange instance to the processing block" do
    mock_exchange = Exchange.new
    DelegatingProcessor.new { |exchange|
      exchange.should === mock_exchange
    }.process mock_exchange
  end

end

describe SimpleRouteBuilder, "configuring routes using a user defined block of java DSL code" do

  it "should puke the the supplied block is nil" do
    lambda do
      SimpleRouteBuilder.new
    end.should raise_error
  end

  it "should execute the routing instructions in the context of the builder" do
    route_builder = SimpleRouteBuilder.new do
        from("direct:start").to("mock:result")
    end
    check_basic_route route_builder
  end

end

describe SimpleRouteBuilder, "adding headers dynamically with the DSL wrapper method" do

  it "should generate a processor instance for calls to set_header" do
    SimpleRouteBuilder.new{}.add_header({}).class.should == DelegatingProcessor
  end

  it "should not puke if the supplied header values are nil" do
    lambda do
      SimpleRouteBuilder.new{}.add_header(nil)
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

    processor = SimpleRouteBuilder.new{}.add_header(new_headers)
    processor.process(ex)
  end

end

describe RouteBuilderConfigurator, "when configuring routes" do

  it "should evaluate the supplied script source and configure a builder" do
    config = RouteBuilderConfigurator.new
    route_builder = config.configure 'route { from("direct:start").to("mock:result") }'
    check_basic_route route_builder
  end

end

describe RouteBuilderConfigurator, "when ...." do

  before :each do
    Configuration.any_instance.stubs(:containsKey).returns true
  end

  [:control_channel, :control_node, :xml2code_transformer].each do |config|
    it "should lookup #{config} in the supplied configuration source" do
      properties = Configuration.new
      properties.expects(:getString).with(config.to_s).once.returns true
      builder = RouteBuilderConfigurator.new
      builder.setProperties properties
      builder.send(:[], config).should be_true
    end
  end

end
