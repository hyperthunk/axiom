

helper_for /route/i do
  def check_basic_route route_builder
    route_builder.expects(:from).with("direct:start").returns(route_builder)
    route_builder.expects(:to).with("mock:result")
    route_builder.configure
  end
end

