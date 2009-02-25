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

require 'ruby/processor'

module Axiom

  # Implements the default processing strategy for the
  # axiom control channel, allowing start/stop/configuration
  # of the other <i>managed</i> camel contexts.
  class DefaultProcessingNode
    include org.axiom.integration.camel.ContextProcessingNode

    attr_accessor :context
    alias getContext context
    alias setContext context=

    def process(exchange)
      in_channel = exchange.getIn
      command = in_channel.getHeader("command").to_s.downcase.to_sym
      return unless [:start, :stop, :configure].include? command
      
      if command == :configure
        @context.addRoutes(additional_routes in_channel)
      else
        @context.send command
      end
    end

    private

    def additional_routes in_channel
      # this next line looks odd in ruby, but ensures that we act as a
      # good citizen in terms of being a data type channel for route builders 
      builder = in_channel.getBody(org.apache.camel.builder.RouteBuilder.class);
      builder.route_list
    end

  end
  
end

# this return vaule of the script is to aid with spring integration
Axiom::DefaultProcessingNode.new
