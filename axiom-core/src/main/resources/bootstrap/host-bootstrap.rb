#
# Copyright (c) 2009, Tim Watson. All rights reserved.
# This is the default script for booting the control channel in embedded mode.
#

require 'java'
require 'axiom'
require 'axiom/plugins'

import org.axiom.integration.Environment

route {
  
  intercept(header(Environment::PAYLOAD_CLASSIFIER).isEqualTo('code')).
      process(route_config lookup(Environment::ROUTE_SCRIPT_EVALUATOR)).
      proceed

  from(Environment::CONTROL_CHANNEL).
    choice.
      when(header(Environment::SIGNAL).isEqualTo(Environment::SIG_TERMINATE)).
        to(Environment::TERMINATION_CHANNEL).
      otherwise.
        processRef(Environment::DEFAULT_PROCESSOR)

}
