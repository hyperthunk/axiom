import org.apache.camel.builder.RouteBuilder

require File.join(File.dirname(__FILE__), '../../../main/resources/ruby/route_builder.rb')

# TODO: use the jtestr helper support for this.....

module ExpectationSupport
  def check_expectations builder
    builder.expects(:from).with("direct:start").returns(builder)
    builder.expects(:to).with("mock:result")
    builder.configure
  end
end

describe ExProcessor do

  it "should puke if the expected block is missing" do
    lambda { ExProcessor.new }.should raise_error
  end

  it "should call the supplied block to perform processing" do
    # NB: this is really ugly, maybe there's a better way???
    block_called = false
    ExProcessor.new {|_| block_called = true }.process nil
    block_called.should be_true
  end

  it "should pass the exchange instance to the processing block" do
    mock_exchange = Exchange.new
    ExProcessor.new { |exchange|
      exchange.should === mock_exchange
    }.process mock_exchange
  end

end

describe ExRouteBuilder, "configuring routes using a user defined block of java DSL code" do

  include ExpectationSupport

  it "should puke the the supplied block is nil" do
    lambda do
      ExRouteBuilder.new
    end.should raise_error
  end

  it "should execute the routing instructions in the context of the builder" do
    check_expectations(
      ExRouteBuilder.new do
        from("direct:start").to("mock:result")
      end
    )
  end

end

describe ExRouteBuilder, "adding headers dynamically with the DSL wrapper method" do

  include ExpectationSupport

  it "should generate a processor instance for calls to set_header" do
    ExRouteBuilder.new{}.add_header({}).class.should == ExProcessor
  end

  it "should not puke if the supplied header values are nil" do
    lambda do
      ExRouteBuilder.new{}.add_header(nil)
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

    processor = ExRouteBuilder.new{}.add_header(new_headers)
    processor.process(ex)
  end

end

describe ExRouteBuilderConfigurator, "when configuring routes" do

  include ExpectationSupport

  it "should evaluate the supplied script source and configure a builder" do
    config = ExRouteBuilderConfigurator.new
    builder = config.configure 'route { from("direct:start").to("mock:result") }'
    check_expectations builder
  end

end
