require 'ruby/processor'

module Axiom
  class DefaultProcessingNode
    include org.apache.camel.Processor

    attr_accessor :camel_context

    def process(exchange)
      in_channel = exchange.getIn
      command = in_channel.getHeader("command").to_s.downcase
      return unless [:start, :stop, :configure].include? command.to_sym
      
      if command.eql? 'configure'
        @camel_context.addRoutes(additional_routes in_channel)
      else
        @camel_context.send command
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
