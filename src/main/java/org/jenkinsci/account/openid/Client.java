package org.jenkinsci.account.openid;

import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.*;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.sreg.SRegRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Test client.
 *
 * @author Kohsuke Kawaguchi
 */
public class Client {
    private final ConsumerManager manager;
    public String openid,claimedOpenid;
    private DiscoveryInformation discovered;

    public Client() {
        try {
            manager = new ConsumerManager();
            manager.setAssociations(new InMemoryConsumerAssociationStore());
            manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
        } catch (ConsumerException e) {
            throw new Error(e);
        }
    }

    public void doStart(StaplerRequest request, StaplerResponse response, @QueryParameter String openid) throws IOException, ServletException {
        try {
            // determine a return_to URL where your application will receive
            // the authentication responses from the OpenID provider
            // YOU SHOULD CHANGE THIS TO GO TO THE
            String url = request.getRequestURL().toString();
            String returnToUrl = url.substring(0,url.length()-5/*start*/)+"return";


            // perform discovery on the user-supplied identifier
            List discoveries = manager.discover(openid);

            // attempt to associate with an OpenID provider
            // and retrieve one service endpoint for authentication
            discovered = manager.associate(discoveries);

            // store the discovery information in the user's session

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

            // Attribute Exchange example: fetching the 'email' attribute
            FetchRequest fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute("email",
                    // attribute alias
                    "http://schema.openid.net/contact/email",   // type URI
                    true);                                      // required

            // see http://code.google.com/apis/accounts/docs/OpenID.html
            fetch.addAttribute("ff", "http://axschema.org/namePerson/first", true);
            fetch.addAttribute("ll", "http://axschema.org/namePerson/last", true);

            // attach the extension to the authentication request
            authReq.addExtension(fetch);

            SRegRequest sregReq = SRegRequest.createFetchRequest();
            sregReq.addAttribute("fullname", true);
            sregReq.addAttribute("nickname", true);
            sregReq.addAttribute("email", true);
            authReq.addExtension(sregReq);

            if (! discovered.isVersion2() ) {
                // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
                // The only method supported in OpenID 1.x
                // redirect-URL usually limited ~2048 bytes
                response.sendRedirect(authReq.getDestinationUrl(true));
            } else {
                // Option 2: HTML FORM Redirection
                // Allows payloads > 2048 bytes

                // <FORM action="OpenID Provider's service endpoint">
                // see samples/formredirection.jsp for a JSP example
                //authReq.getOPEndpoint();

                // build a HTML FORM with the message parameters
                //authReq.getParameterMap();

                RequestDispatcher d = request.getView(this, "formRedirect.jelly");
                request.setAttribute("endpoint",authReq.getOPEndpoint());
                request.setAttribute("parameters",authReq.getParameterMap());
                d.forward(request,response);
            }
        } catch (OpenIDException e) {
            // present error to the user
            throw new Error(e);
        }
    }

    public void doReturn(StaplerRequest request, StaplerResponse rsp) throws IOException {
        try {
            // --- processing the authentication response

            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList responselist =
                    new ParameterList(request.getParameterMap());

            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = request.getRequestURL();
            String queryString = request.getQueryString();
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(request.getQueryString());

            // verify the response
            VerificationResult verification = manager.verify(
                    receivingURL.toString(), responselist, discovered);

            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null) {
                AuthSuccess authSuccess =
                        (AuthSuccess) verification.getAuthResponse();

                openid = authSuccess.getIdentity();
                claimedOpenid = authSuccess.getClaimed();
                rsp.sendRedirect(".");
            } else {
                throw HttpResponses.error(500,"Failed to login");
            }
        } catch (OpenIDException e) {
            throw new Error(e);
        }
    }
}
