/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.nsili.endpoint;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.connexta.alliance.nsili.common.GIAS.LibraryDescription;
import com.connexta.alliance.nsili.common.UCO.ProcessingFault;
import com.connexta.alliance.nsili.common.UCO.SystemFault;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ddf.security.service.SecurityManager;
import ddf.security.service.SecurityServiceException;

public class TestNsiliLibraryImpl extends TestNsiliCommon {

    private static final int TEST_CORBA_PORT = 20012;

    private static final String TEST_HOSTNAME = "ddfhost";

    private static final String TEST_COUNTRY = "US";

    private static final String TEST_ORGANIZATION = "Connexta";

    private static final String TEST_VERSION = "NSILI";

    private LibraryImpl library = null;

    private NsiliEndpoint nsiliEndpoint = null;

    private SecurityManager securityManager = mock(SecurityManager.class);

    @Before
    public void setUp() throws SecurityServiceException {
        System.setProperty("org.codice.ddf.system.hostname", TEST_HOSTNAME);
        System.setProperty("user.country", TEST_COUNTRY);
        System.setProperty("org.codice.ddf.system.organization", TEST_ORGANIZATION);

        setupCommonMocks();
        createNsiliEndpoint();
        library = nsiliEndpoint.getLibrary();
    }

    @Test
    public void testManagerTypes() throws ProcessingFault, SystemFault {
        String[] managerTypes = library.get_manager_types();

        assertThat(managerTypes, notNullValue());
        assertThat(managerTypes.length, is(4));
    }

    @Test
    public void testLibraryDescription() throws ProcessingFault, SystemFault {
        LibraryDescription libraryDescription = library.get_library_description();
        assertThat(libraryDescription.library_name, is(TEST_HOSTNAME));
        assertThat(libraryDescription.library_description, containsString(TEST_ORGANIZATION));
        assertThat(libraryDescription.library_version_number, containsString(TEST_VERSION));
    }

    @After
    public void tearDown() {
        if (nsiliEndpoint != null) {
            nsiliEndpoint.shutdown();
        }
    }

    private void createNsiliEndpoint() {
        nsiliEndpoint = new NsiliEndpoint();
        nsiliEndpoint.setSecurityManager(securityManager);
        nsiliEndpoint.setCorbaPort(TEST_CORBA_PORT);
    }
}
