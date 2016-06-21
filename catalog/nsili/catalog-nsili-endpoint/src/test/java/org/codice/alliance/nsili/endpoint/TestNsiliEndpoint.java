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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.codice.alliance.nsili.orb.api.CorbaOrb;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.codice.alliance.nsili.common.GIAS.AccessCriteria;
import org.codice.alliance.nsili.common.GIAS.LibraryManager;
import org.codice.alliance.nsili.common.NsiliManagerType;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import ddf.security.service.SecurityServiceException;

public class TestNsiliEndpoint extends TestNsiliCommon {
    public static final int TEST_CORBA_PORT = 0;

    private NsiliEndpoint nsiliEndpoint;

    private AccessCriteria testAccessCriteria;

    private CorbaOrb mockCorbaOrb = mock(CorbaOrb.class);

    @Before
    public void setUp()
            throws SecurityServiceException, AdapterInactive, InvalidName, ServantNotActive,
            WrongPolicy, IOException {
        setupCommonMocks();
        setupOrb();
        doReturn(orb).when(mockCorbaOrb).getOrb();
        testAccessCriteria = new AccessCriteria("", "", "");
        createEndpoint();
    }

    @After
    public void tearDown() {
        if (orb != null) {
            orb.destroy();
        }

        orb = null;
    }

    @Test
    public void testIORString() {
        String iorString = nsiliEndpoint.getIorString();
        assertThat(iorString, notNullValue());
    }

    @Test
    public void testEndpointManagers() throws Exception {
        String[] supportedManagerTypes = nsiliEndpoint.getLibrary().get_manager_types();
        assertThat(supportedManagerTypes.length, notNullValue());
    }

    @Test
    public void testGetCatalogMgr() throws Exception {
        LibraryManager catalogMgr = nsiliEndpoint.getLibrary().get_manager(NsiliManagerType.CATALOG_MGR.getSpecName(), testAccessCriteria);
        assertThat(catalogMgr, notNullValue());
    }

    @Test
    public void testGetOrderMgr() throws Exception {
        LibraryManager orderMgr = nsiliEndpoint.getLibrary().get_manager(NsiliManagerType.ORDER_MGR.getSpecName(), testAccessCriteria);
        assertThat(orderMgr, notNullValue());
    }

    @Test
    public void testGetProductMgr() throws Exception {
        LibraryManager productMgr = nsiliEndpoint.getLibrary().get_manager(NsiliManagerType.PRODUCT_MGR.getSpecName(), testAccessCriteria);
        assertThat(productMgr, notNullValue());
    }

    @Test
    public void testGetDataModelMgr() throws Exception {
        LibraryManager dataModelMgr = nsiliEndpoint.getLibrary().get_manager(NsiliManagerType.DATA_MODEL_MGR.getSpecName(), testAccessCriteria);
        assertThat(dataModelMgr, notNullValue());
    }

    @Test
    public void testCreationMgr() throws Exception {
        LibraryManager creationMgr = nsiliEndpoint.getLibrary().get_manager(NsiliManagerType.CREATION_MGR.getSpecName(), testAccessCriteria);
        assertThat(creationMgr, notNullValue());
    }

    @Test
    public void testStandingQueryMgr() throws Exception {
        LibraryManager standingQueryMgr = nsiliEndpoint.getLibrary().get_manager(NsiliManagerType.STANDING_QUERY_MGR.getSpecName(), testAccessCriteria);
        assertThat(standingQueryMgr, notNullValue());
    }

    private void createEndpoint() {
        nsiliEndpoint = new NsiliEndpoint();
        nsiliEndpoint.setCorbaOrb(mockCorbaOrb);
        nsiliEndpoint.setSecurityManager(securityManager);
        nsiliEndpoint.init();
    }
}
