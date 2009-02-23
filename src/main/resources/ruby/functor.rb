
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