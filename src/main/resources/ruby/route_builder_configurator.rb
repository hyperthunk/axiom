require 'ruby/route_builder'
require 'ruby/configuration'

module Axiom

  # provides a mechanism for evaluating a script (source) in the
  # context of the current JRuby runtime (which is nigh on impossible to
  # get out of spring otherwise - creating a second runtime is semantically
  # incorrect), and having the result evaluated as a block passed to RouteBuider
  class RouteBuilderConfigurator
    include org.axiom.management.RouteConfigurationScriptEvaluator
    include Axiom::Configuration

    # convenience hook for script writers

    def route &block
      Axiom::SimpleRouteBuilder.new &block
    end

    # configures the supplied script source in the context of a RouteBuilder instance

    def configure(scriptSource)
      eval scriptSource
    end
  end

end

# This return value (for the script) is a hook for spring-framework integration
Axiom::RouteBuilderConfigurator.new