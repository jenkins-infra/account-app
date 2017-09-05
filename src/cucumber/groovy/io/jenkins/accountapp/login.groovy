package io.jenkins.accountapp

import cucumber.api.PendingException
import cucumber.api.java.en.*

this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)


Given(~/^that I am unauthenticated$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}

When(~/^I navigate to the home page$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}

Then(~/^I should see a login screen$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}
