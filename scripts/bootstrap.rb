# route configurator for booting the control channel context
# note that you don't have to configure logging/tracing here as it is
# enabled in the properties/configuration stage instead

# TODO: provide a canned mechanism for reading properties in here

route {
  from("direct:start").inOut.to(control_channel)

  from("jetty://0.0.0.0:8088/axiom/control-channel").
    inOut.
    to(xml2code_transformer).
    process(add_header("payload-type" => "code")).
    to(control_channel)

  from(control_channel).
    processRef(channel_processor).
      proceed.
      choice.
        when(header("command").isEqualTo("shutdown")).
          to("direct:shutdown"). 
        otherwise.stop
}
