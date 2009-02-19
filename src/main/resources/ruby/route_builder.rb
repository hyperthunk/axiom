import org.apache.camel.builder.RouteBuilder
import org.apache.camel.Processor
import org.apache.camel.Exchange
import org.axiom.management.RouteConfigurationScriptEvaluator
import java.lang.IllegalArgumentException

# implements a simple processor that can take a
# proc and execute it on demand
class ExProcessor
  include Processor

  def initialize &block
    # TODO: better choice of exception type....
    raise Exception.new unless block
    @strategy = block
  end

  def process(exchange)
    @strategy.call exchange
  end

end

# wraps the camel RouteBuilder and evaluates a block of
# route configuration code in the instance context (thereby
# providing a convenient and simpilfied syntax for defining
# RouteBuilder instances without messy java noise)
class ExRouteBuilder < RouteBuilder

  # stores the supplied block for later evaluation
  def initialize(&block)
    @configurator = block
  end

  # creates a processor implementation that copies the
  # input channel from the exchange, to the output channel,
  # and adds all the header k=>v pairs from the
  # supplied hash in addition to these (possibly overwriting existing values)
  def add_header hash
    ExProcessor.new { |exchange|
      # exchange = Exchange.new
      out_channel = exchange.out
      # TODO: put this in another processor instance/singleton
      # out_channel.copy_from(exchange.get_in)
      hash.each { |k, v|
        out_channel.set_header k, v
      }
    }
  end

  # configures the block supplied (on initialization)
  # in the context of this instance

  def configure()
    instance_eval &@configurator
  end
end

# provides a mechanism for evaluating a script (source) in the
# context of the current JRuby runtime (which is nigh on impossible to
# get out of spring otherwise - creating a second runtime is semantically
# incorrect), and having the result evaluated as a block passed to RouteBuider
class ExRouteBuilderConfigurator
  include RouteConfigurationScriptEvaluator

  attr_reader :control_channel

  def initialize
    # TODO: these names are bound in spring config - should be externalized...
    @control_channel = "direct:control-channel"
    @channel_processor = "control-channel-processor"
  end

  # convenience hook for script writers

  def route &block
    ExRouteBuilder.new &block
  end

  # configures the supplied script source in the context of a RouteBuilder instance

  def configure(scriptSource)
    eval scriptSource
  end
end

# This return value (for the script) is a hook for spring-framework integration
ExRouteBuilderConfigurator.new
