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

# TODO: use the jtestr helper support for this.....

module ProcessorTestSupport
  def stubbed_exchange mock_channel
    mock_exchange = Exchange.new
    mock_exchange.stubs(:getIn).returns mock_channel
    mock_exchange
  end

  def processor_for context
    config = ExRouteBuilderConfigurator.new
    processor = ControlChannelProcessor.new context, config
  end
end

describe "stopping and starting via the control channel" do

  include ProcessorTestSupport

  [:start, :stop].each do |instruction|

    it "should #{instruction} the camel context when the relevant header is supplied" do
      context = CamelContext.new
      context.expects(instruction).once

      # first message is a dud, second one says stop...
      # same stub will do for both cases, as well as for the header values
      mock_channel = Message.new
      mock_channel.stubs(:getHeader).returns "test", instruction.to_s

      processor = processor_for context
      2.times { processor.process (stubbed_exchange mock_channel) }
    end

  end

  it "should configure the camel context when routes are supplied" do
    context = CamelContext.new
    (context.expects :addRoutes).once.with{ |x| x.is_a? List }

    mock_channel = Message.new
    mock_channel.stubs(:getHeader).returns "configure"
    mock_channel.stubs(:getBody).returns ExRouteBuilder.new {}

    config = ExRouteBuilderConfigurator.new
    processor = ControlChannelProcessor.new context, config
    processor.process (stubbed_exchange mock_channel)
  end

end
