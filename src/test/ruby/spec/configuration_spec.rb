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

require 'ruby/configuration'

describe Axiom::Configuration, "when accessing configuration data in DSL code" do

  include Axiom::Configuration

  before :each do
    org.apache.commons.configuration.Configuration.
      any_instance.stubs(:containsKey).returns true
    @properties = org.apache.commons.configuration.Configuration.new
    self.setProperties @properties
  end

  it "should return self from 'config'" do
    config.should == self
  end

  it "should treat symbols as strings when looking up keys" do
    @properties.expects(:getString).with('config_item').once.returns 'ok'
    (config >> :config_item).should eql('ok')
  end

  ['axiom.control.routes.direct.start',
   'axiom.control.nodes.xml2code.transformer',
   'axiom.control.routes.shutdown'].each do |config|
    [ :[], :>> ].each do |method|
      it "should lookup #{config} in the supplied configuration source" do
        @properties.expects(:getString).with(config.to_s).once.returns 'ok'
        self.send(method, config).should eql("ok")
      end
    end
  end
end
