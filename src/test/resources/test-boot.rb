
route {
  from("direct:start").to("seda:q1")
=begin
  from("jms:topic1:q5").
    filter(header("foo").contains("bar")).
      to("xmpp:user4@jabber.bt.com?conversion=blah").
    choose.when(xpath("issuetype/data[@lob = 'BT Retail']")).
      to("mail:tim.watson@bt.com").
    otherwise.to("irc:channel42")
=end
}