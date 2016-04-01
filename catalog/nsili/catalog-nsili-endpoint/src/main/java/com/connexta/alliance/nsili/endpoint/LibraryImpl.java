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

import java.nio.charset.Charset;

import com.connexta.alliance.nsili.common.GIAS.AccessCriteria;
import com.connexta.alliance.nsili.common.GIAS.CatalogMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.DataModelMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.LibraryDescription;
import com.connexta.alliance.nsili.common.GIAS.LibraryManager;
import com.connexta.alliance.nsili.common.GIAS.LibraryManagerHelper;
import com.connexta.alliance.nsili.common.GIAS.LibraryPOA;
import com.connexta.alliance.nsili.common.GIAS.OrderMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.ProductMgrHelper;
import com.connexta.alliance.nsili.common.NsiliManagerType;
import com.connexta.alliance.nsili.common.UCO.InvalidInputParameter;
import com.connexta.alliance.nsili.common.UCO.ProcessingFault;
import com.connexta.alliance.nsili.common.UCO.SystemFault;
import com.connexta.alliance.nsili.common.UCO.exception_details;
import com.connexta.alliance.nsili.endpoint.managers.CatalogMgrImpl;
import com.connexta.alliance.nsili.endpoint.managers.DataModelMgrImpl;
import com.connexta.alliance.nsili.endpoint.managers.OrderMgrImpl;
import com.connexta.alliance.nsili.endpoint.managers.ProductMgrImpl;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.LoggerFactory;

public class LibraryImpl extends LibraryPOA {

    private static final String LIBRARY_VERSION = "NSILI|1.0";
    //
    //    private List<String> manager = Arrays.asList("OrderMgr",
    //            "CatalogMgr",
    //            "ProductMgr",
    //            "DataModelMgr"
    //            /* Optional :
    //            "QueryOrderMgr",
    //            "StandingQueryMgr",
    //            "CreationMgr",
    //            "UpdateMgr" */);

    private static final String ENCODING = "UTF-8";

    private POA poa;

    public LibraryImpl(POA poa) {
        this.poa = poa;
    }

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LibraryImpl.class);

    @Override
    public String[] get_manager_types() throws ProcessingFault, SystemFault {
        // return (String[]) manager.toArray();
        //No Managers currently implemented, return empty array
        return new String[] {};
    }

    @Override
    public LibraryManager get_manager(String manager_type, AccessCriteria access_criteria)
            throws ProcessingFault, InvalidInputParameter, SystemFault {

        org.omg.CORBA.Object obj;

        if (manager_type.equals(NsiliManagerType.CATALOG_MGR.getSpecName())) {
            CatalogMgrImpl catalogMgr = new CatalogMgrImpl(poa);
            try {
                poa.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                        catalogMgr);
            } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                LOGGER.error("Error activating CatalogMgr", e);
            }

            obj = poa.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                    CatalogMgrHelper.id());
        } else if (manager_type.equals(NsiliManagerType.ORDER_MGR.getSpecName())) {
            OrderMgrImpl orderMgr = new OrderMgrImpl();
            try {
                poa.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                        orderMgr);
            } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                LOGGER.error("Error activating OrderMgr", e);
            }

            obj = poa.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                    OrderMgrHelper.id());
        } else if (manager_type.equals(NsiliManagerType.PRODUCT_MGR.getSpecName())) {
            ProductMgrImpl productMgr = new ProductMgrImpl();
            try {
                poa.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                        productMgr);
            } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                LOGGER.error("Error activating ProductMgr", e);
            }

            obj = poa.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                    ProductMgrHelper.id());
        } else if (manager_type.equals(NsiliManagerType.DATA_MODEL_MGR.getSpecName())) {
            DataModelMgrImpl dataModelMgr = new DataModelMgrImpl();
            try {
                poa.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                        dataModelMgr);
            } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                LOGGER.error("Error activating DataModelMgr", e);
            }

            obj = poa.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                    DataModelMgrHelper.id());
        } else {
            String[] bad_params = {manager_type};
            throw new InvalidInputParameter("UnknownMangerType",
                    new exception_details("UnknownMangerType", true, manager_type),
                    bad_params);

        }

        LibraryManager libraryManager = LibraryManagerHelper.narrow(obj);
        return libraryManager;
    }

    @Override
    public LibraryDescription get_library_description() throws ProcessingFault, SystemFault {
        String host = System.getProperty("org.codice.ddf.system.hostname");
        String country = System.getProperty("user.country");
        String organization = System.getProperty("org.codice.ddf.system.organization");
        String libraryDescr = country + "|" + organization;

        return new LibraryDescription(host, libraryDescr, LIBRARY_VERSION);
    }

    @Override
    public LibraryDescription[] get_other_libraries(AccessCriteria access_criteria)
            throws ProcessingFault, InvalidInputParameter, SystemFault {
        throw new NO_IMPLEMENT();
    }

}
