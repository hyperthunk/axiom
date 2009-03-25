
require 'java'
require 'axiom'
require 'spec'

spec_dir = File.join Dir.pwd, 'src/test/resources/ruby/integration'
options = ::Spec::Runner::OptionParser.parse([spec_dir, '-f', "n"], STDERR, STDOUT)

$logger.info "Running RSpec in #{spec_dir}"

def run_specs options
  $0 = 'spec_runner.rb'
  cmd = ::Spec::Runner::CommandLine
  return cmd.run(options)
rescue Exception => e
  $logger.warn "#{e.inspect}: #{e.backtrace}"
end

run_specs options
