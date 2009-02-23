=begin
Copyright (c) 2009, Tim Watson
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of the author nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
=end

require 'java'
require 'delegate'

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.Processor
import org.axiom.management.RouteConfigurationScriptEvaluator

# NB: Functor isn't required in ruby 1.9 as something similar is built in

# a mapping between a proc/lambda and a type/module
# so that they are interchangable - no method_missings hooks though!
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
# delegates to a block for defered execution
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

  # adds all the header k=>v pairs from the supplied hash
  # to the current route
  def add_header hash
    DelegatingProcessor.new do |exchange|
      out_channel = exchange.out
      hash.each { |k, v| out_channel.set_header k, v }
    end
  end

  # see the javadoc for org.apache.camel.RouteBuilder
  def configure
    instance_eval &self
  end
end

class ConfigurationHandler

  attr_accessor :properties
  alias setProperties properties=

  def method_missing sym, *args, &blk
    key = sym.to_s
    # fail "no configuration exists for key #{key}" unless @properties.containsKey key
    @properties.getString key
  end

end

# provides a mechanism for evaluating a script (source) in the
# context of the current JRuby runtime (which is nigh on impossible to
# get out of spring otherwise - creating a second runtime is semantically
# incorrect), and having the result evaluated as a block passed to RouteBuider
class RouteBuilderConfigurator < DelegateClass(ConfigurationHandler)
  include RouteConfigurationScriptEvaluator

  def RouteBuilderConfigurator.new_instance
    RouteBuilderConfigurator.new ConfigurationHandler.new
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
RouteBuilderConfigurator.new_instance
