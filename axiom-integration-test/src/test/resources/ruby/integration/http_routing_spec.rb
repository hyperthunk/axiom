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
require 'spec'

require 'uri'
require 'net/http'
require 'ping'
require 'socket'

require 'axiom'
require 'axiom/core/configuration'
require 'axiom/plugins'

require File.join($axiom_testdir, 'spec_helper.rb')

import org.axiom.integration.Environment
import org.axiom.configuration.ExternalConfigurationSourceFactory
import org.axiom.service.RouteScriptLoader
import org.axiom.service.Launcher
import org.apache.commons.configuration.PropertiesConfiguration
import java.lang.System

describe "proxying inter-system communications over http" do

  include HTTPSpecSupport
  include Axiom::Core::Configuration

  def inbound_uri
    URI.parse("http://#{config >> 'http.test.inbound.uri'}")
  end

  before :all do
    @requests = []
    @camel = $AXIOM[:camel]
    conf = ExternalConfigurationSourceFactory.get_registered_configuration(@camel)
    config_path = File.join($axiom_testdir, 'http.routing.properties')
    logger.debug("Loading properties from #{config_path}.")
    conf.add_configuration(PropertiesConfiguration.new config_path)
    self.setProperties conf

    logger.debug("Launching http listener")
    @listener = URI.parse("http://#{config >> 'http.test.outbound.uri'}")
    start_http! @listener do |request, response|
      #logger.debug("request-body: #{request.body}")
      @requests.push request
      response.status = 200
    end

    logger.debug("Launching control channel.")
    @channel = Launcher.new.launch(@camel)
    eval_svc = @channel.route_script_evaluator
    @channel.load(RouteScriptLoader.new(File.join($axiom_testdir, 'http_routes.rb'), eval_svc))
  end

  after :all do
    logger.debug("Stopping http test listener.")
    stop_http!
  end

  it "should spool up an http endpoint listening on the given port" do
    http_interaction inbound_uri, 'ignored....' do |response|
      response.code.should eql('200')
    end
  end

  it "should forward incoming requests to the specified outbound http endpoint" do
    timeout = 25 # seconds
    # Ping.pingecho(@listener.host, timeout, @listener.port).should be_true
    post_data=<<-EOF
      <request>
        <data />
      </request>
    EOF
    http_interaction inbound_uri, post_data do |response|
      response.code.should eql('200')
    end
    request = @requests.pop
    request.body.should == post_data.chop
  end
    # TODO: spool up a Net::Http based listener, POST the inbound http endpoint, check for receipt

end

describe "validating inter-system communications over http" do

  #it "should validate and proceed to route schema compliant messages"

  #it "should fail and stop routing non-compliant messages"

end
