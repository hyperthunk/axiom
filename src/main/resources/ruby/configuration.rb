

module Configuration

  attr_accessor :properties
  alias setProperties properties=

  def [] key
    key = key.to_s
    # fail "no configuration exists for key #{key}" unless @properties.containsKey key
    @properties.getString key
  end
  alias_method :>>, :[]

  def config
    self
  end

end