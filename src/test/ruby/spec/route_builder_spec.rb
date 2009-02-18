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

describe ExRouteBuilder, "defining routes" do

  include ExpectationSupport

  it "execute the routing instructions in the context of the builder" do
    check_expectations(
      ExRouteBuilder.new do
        from("direct:start").to("mock:result")
      end
    )
  end

  it "should generate a processor instance for calls to set_header" do
    ExRouteBuilder.new.add_header({}).class.should == ExProcessor
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
