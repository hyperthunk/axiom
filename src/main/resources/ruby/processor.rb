
import org.apache.camel.Processor

# implements a simple camel processor that
# delegates to a block for defered execution
class DelegatingProcessor
  include Processor
  include Functor
  alias process __call__
end