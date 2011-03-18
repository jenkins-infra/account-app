package org.jenkinsci.account;

/**
 * @author Kohsuke Kawaguchi
 */
public class Foo {
    public static void main(String[] args) throws Exception {
        Application a = new WebAppMain().createApplication();
        String kohsuke = "cn=kohsuke,ou=people,dc=jenkins-ci,dc=org";
        System.out.println(a.getGroups(kohsuke, a.connect()));
    }
}
