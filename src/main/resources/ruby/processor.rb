require 'ruby/functor'

module Axiom

  # implements a simple camel processor that
  # delegates to a block for defered execution
  class Processor
    include org.apache.camel.Processor
    include Axiom::Functor
    alias process __call__
  end

end  
