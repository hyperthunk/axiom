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

require 'axiom'

import org.apache.camel.Predicate

describe Axiom::Plugins, "when adding plugins via the registry" do

  include Axiom::Plugins

  it "should implicitly pass on the lookup query to the context's registry" do
    mock_ctx = org.apache.camel.CamelContext.new
    self.stubs(:context).returns mock_ctx
    mock_reg = org.apache.camel.spi.Registry.new
    mock_ctx.stubs(:registry).returns mock_reg

    beanId = 'expectedBeanId'
    mock_reg.expects(:lookup).with beanId

    # action...
    lookup_plugin :bean_me_up_scotty, beanId
    bean_me_up_scotty
  end

  it "should fail if you try and pass positional arguments to the generated method" do
    this = self
    [:context, :registry, :lookup].each { |m| this.stubs(m).returns this }

    lookup_plugin :self_hosted!, 'anyid'
    lambda do
      self_hosted! :this_should_go_bang
    end.should raise_error
  end

  it "should fail if you try and pass more than one argument (for another reason)"

  it "should fail if you try and set an unsupported named argument (property)"

  it "should set all supplied named arguments on the resulting object/plugin"

end


describe Axiom::Plugins, "when registering an existing class as a plugin" do

  include Axiom::Plugins

  it "should pull the ctor method from the supplied class and pass to 'plugin'" do
    register_plugin :sample, org.axiom.SamplePlugin

    sample("foo", "bar").should be_instance_of(org.axiom.SamplePlugin)
  end

  it "should puke if the supplied class doesn't have a public ctor" do
    lambda do
      register_plugin :bad_example, org.axiom.SamplePlugin.PluginWithNoPublicCtor
    end.should raise_error
  end

end 
