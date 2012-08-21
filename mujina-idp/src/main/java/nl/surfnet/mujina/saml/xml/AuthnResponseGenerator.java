/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.mujina.saml.xml;

import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.impl.ResponseBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.security.credential.Credential;
import org.springframework.security.core.AuthenticationException;

import nl.surfnet.mujina.model.IdpConfiguration;
import nl.surfnet.mujina.model.SimpleAuthentication;
import nl.surfnet.mujina.util.IDService;
import nl.surfnet.mujina.util.TimeService;

public class AuthnResponseGenerator {

    private final XMLObjectBuilderFactory builderFactory = org.opensaml.Configuration.getBuilderFactory();

    private final String issuingEntityName;

    private final IssuerGenerator issuerGenerator;
    private final AssertionGenerator assertionGenerator;
    private final IDService idService;
    private final TimeService timeService;

    StatusGenerator statusGenerator;

    public AuthnResponseGenerator(final Credential signingCredential, String issuingEntityName, TimeService timeService, IDService idService, IdpConfiguration idpConfiguration) {
        super();
        this.issuingEntityName = issuingEntityName;
        this.idService = idService;
        this.timeService = timeService;
        issuerGenerator = new IssuerGenerator(issuingEntityName);
        assertionGenerator = new AssertionGenerator(signingCredential, issuingEntityName, timeService, idService, idpConfiguration);
        statusGenerator = new StatusGenerator();
    }


    public Response generateAuthnResponse(String remoteIP, SimpleAuthentication authToken, String recepientAssertionConsumerURL, int validForInSeconds, String inResponseTo, DateTime authnInstant) {

        ResponseBuilder responseBuilder = (ResponseBuilder) builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME);
        Response authResponse = responseBuilder.buildObject();

        Issuer responseIssuer = issuerGenerator.generateIssuer();

        Assertion assertion = assertionGenerator.generateAssertion(
            remoteIP,
            authToken,
            recepientAssertionConsumerURL,
            validForInSeconds,
            inResponseTo,
            authnInstant
        );

        authResponse.setIssuer(responseIssuer);
        authResponse.setID(idService.generateID());
        authResponse.setIssueInstant(timeService.getCurrentDateTime());
        authResponse.setInResponseTo(inResponseTo);
        authResponse.getAssertions().add(assertion);
        authResponse.setDestination(recepientAssertionConsumerURL);
        authResponse.setStatus(statusGenerator.generateStatus(StatusCode.SUCCESS_URI));

        return authResponse;
    }

    public Response generateAuthnResponseFailure(String recepientAssertionConsumerURL, String inResponseTo, AuthenticationException ae) {

        ResponseBuilder responseBuilder = (ResponseBuilder) builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME);
        Response authResponse = responseBuilder.buildObject();

        Issuer responseIssuer = issuerGenerator.generateIssuer();

        authResponse.setIssuer(responseIssuer);
        authResponse.setID(idService.generateID());
        authResponse.setIssueInstant(timeService.getCurrentDateTime());
        authResponse.setInResponseTo(inResponseTo);
        authResponse.setDestination(recepientAssertionConsumerURL);
        authResponse.setStatus(statusGenerator.generateStatus(StatusCode.RESPONDER_URI, StatusCode.AUTHN_FAILED_URI, ae.getClass().getName()));

        return authResponse;

    }


}