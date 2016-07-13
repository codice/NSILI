/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.endpoint;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.AuthzDecisionStatement;

import ddf.catalog.CatalogFramework;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.security.Subject;
import ddf.security.assertion.SecurityAssertion;
import ddf.security.service.SecurityManager;
import ddf.security.service.SecurityServiceException;

public class TestNsiliCommon {

    protected SecurityManager securityManager = mock(SecurityManager.class);

    protected Subject mockSubject = mock(Subject.class);

    protected PrincipalCollection mockPrincipalCollection = mock(PrincipalCollection.class);

    protected SecurityAssertion mockSecurityAssertion = mock(SecurityAssertion.class);

    protected SecurityToken mockSecurityToken = mock(SecurityToken.class);

    protected ORB orb = null;

    protected LibraryImpl library;

    protected POA rootPOA;

    protected Thread orbRunThread = null;

    protected CatalogFramework mockCatalogFramework = mock(CatalogFramework.class);

    protected static final String USERID = "NsilTest";

    protected void setupCommonMocks() throws SecurityServiceException {
        when(mockSubject.getPrincipals()).thenReturn(mockPrincipalCollection);
        when(mockPrincipalCollection.getPrimaryPrincipal()).thenReturn(USERID);
        when(mockPrincipalCollection.oneByType(Matchers.any())).thenReturn(mockSecurityAssertion);
        when(mockSecurityAssertion.getSecurityToken()).thenReturn(mockSecurityToken);
        when(mockSecurityToken.isAboutToExpire(Matchers.any(Long.class))).thenReturn(false);
        when(mockSubject.execute(Matchers.any(Callable.class))).thenAnswer(invocationOnMock -> {
            Callable callable = (Callable)invocationOnMock.getArguments()[0];
            return callable.call();
        });
        NsiliEndpoint.setGuestSubject(mockSubject);
        when(securityManager.getSubject(any(Object.class))).thenReturn(mockSubject);
    }

    public void setupOrb()
            throws InvalidName, AdapterInactive, WrongPolicy, ServantNotActive, IOException,
            SecurityServiceException {

        //Let the system find the next avail port and use that
        int port = 0;
        java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", port);

        System.setProperty("com.sun.CORBA.POA.ORBPersistentServerPort", String.valueOf(port));
        System.setProperty("com.sun.CORBA.ORBServerPort", String.valueOf(port));

        orb = ORB.init(new String[0], props);

        rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        rootPOA.the_POAManager()
                .activate();

        library = new LibraryImpl(rootPOA);
        library.setCatalogFramework(mockCatalogFramework);
        Subject guestSubject = mockSubject;
        library.setFilterBuilder(new GeotoolsFilterBuilder());

        org.omg.CORBA.Object objref = rootPOA.servant_to_reference(library);

        rootPOA.the_POAManager()
                .activate();
    }
}
