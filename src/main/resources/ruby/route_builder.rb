import org.apache.camel.builder.RouteBuilder
import org.apache.camel.Processor
import org.axiom.management.RouteConfigurationScriptEvaluator

# a mapping between a proc/lambda and a type/module
module Functor
  def initialize &func
    fail if func.nil?
    @func = func
  end
  def __call__ *args
    @func.call *args
  end
  def to_proc
    @func
  end
end

# implements a simple camel processor that
# takes a block for later execution
class DelegatingProcessor
  include Processor
  include Functor
  alias process __call__
end

# wraps the camel RouteBuilder and evaluates a block of
# route configuration code in the instance context (thereby
# providing a convenient and simpilfied syntax for defining
# RouteBuilder instances without messy java noise)
class SimpleRouteBuilder < RouteBuilder
  include Functor

  # creates a processor implementation that copies the
  # input channel from the exchange, to the output channel,
  # and adds all the header k=>v pairs from the
  # supplied hash in addition to these (possibly overwriting existing values)
  def add_header hash
    DelegatingProcessor.new do |exchange|
      out_channel = exchange.out
      hash.each { |k, v| out_channel.set_header k, v } unless hash.nil?
    end
  end

  # configures the block supplied (on initialization)
  # in the context of this instance
  def configure()
    instance_eval &self
  end
end

# provides a mechanism for evaluating a script (source) in the
# context of the current JRuby runtime (which is nigh on impossible to
# get out of spring otherwise - creating a second runtime is semantically
# incorrect), and having the result evaluated as a block passed to RouteBuider
class RouteBuilderConfigurator
  include RouteConfigurationScriptEvaluator

  attr_reader :control_channel, :channel_processor

  def initialize
    # TODO: these names are bound in spring config - should be externalized...
    @control_channel = "direct:control-channel"
    @channel_processor = "control-channel-processor"
  end

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
