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

require 'java'
require 'uri'
require 'axiom'
require 'axiom/plugins'
require 'axiom/core/processor'

import org.apache.camel.component.http.HttpProducer

route do

  # the global request log
  request_log = File.join *[:dir, :file].collect { |x| config >> "http.test.data.#{x}" }
  logger.debug "intercepting all requests to #{request_log}"
  
  # the invalid schema log
  invalid_schema_log = File.join *[:dir, :file].collect { |x| config >> "http.test.failures.#{x}" }
  logger.debug "intercepting all requests with invalid schema to #{invalid_schema_log}"

  # the request schema (xsd) file
  xsd_file = config >> 'http.test.data.schema.file'
  logger.debug "reading schema from #{xsd_file}"

#  intercept.
#    to("file://#{request_log}")

  intercept(is_not(valid_schema?(xsd_file))).
    to("file://#{invalid_schema_log}")

  in_uri = URI.parse("jetty:http://#{config >> 'http.test.inbound.uri'}")
  in_context_path = in_uri.path
  
  out_uri = URI.parse("http://#{config >> 'http.test.outbound.uri'}")
  out_context_path = out_uri.path

  from(in_uri.to_s).
    process(Axiom::Core::Processor.new do |ex|
      logger.warn("EXCHANGE INFO: " + ex.inspect)
      logger.warn("CHANNEL INFO: " + ex.in.inspect)
      logger.warn("HTTP INFO: " + ex.in.getExchange())
      url = "foobar"
      context_path = url.sub(/#{in_context_path}/, out_context_path)
      ex.out.headers[HttpProducer.HTTP_URI] = 'nil'
    end).to(out_uri.to_s)

#  outbound_route = "http://#{config >> 'http.test.outbound.uri'}"

#  from("jetty:http://#{config >> 'http.test.inbound.uri'}").
#    choice.when().
#      to("file://#{invalid_schema_log}").
#      to(outbound_route).
#    otherwise.
#      to(outbound_route)

end
