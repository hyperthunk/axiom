
require 'ruby/route_builder'

# provides a mechanism for evaluating a script (source) in the
# context of the current JRuby runtime (which is nigh on impossible to
# get out of spring otherwise - creating a second runtime is semantically
# incorrect), and having the result evaluated as a block passed to RouteBuider
class RouteBuilderConfigurator
  include org.axiom.management.RouteConfigurationScriptEvaluator
  include Configuration

  # convenience hook for script writers
  def route &block
    SimpleRouteBuilder.new &block
  end

  # configures the supplied script source in the context of a RouteBuilder instance
  def configure(scriptSource)
    eval scriptSource
  end
end

# This return value (for the script) is a hook for spring-framework integration
RouteBuilderConfigurator.new