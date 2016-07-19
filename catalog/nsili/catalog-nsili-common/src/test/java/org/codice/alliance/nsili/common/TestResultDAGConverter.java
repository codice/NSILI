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
package org.codice.alliance.nsili.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.codice.alliance.nsili.common.UCO.DAG;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;

public class TestResultDAGConverter {

    @Test
    public void testResultAttributes() throws Exception {
        String id = UUID.randomUUID()
                .toString();
        MetacardImpl card = new MetacardImpl();
        card.setId(id);
        card.setTitle("Test Title");
        card.setSourceId("Test Source");

        ResultImpl result = new ResultImpl();
        result.setMetacard(card);

        ORB orb = ORB.init(new String[0], null);
        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager()
                .activate();

        String sourceAttr = NsiliConstants.NSIL_PRODUCT + ":" + NsiliConstants.NSIL_CARD + "."
                + NsiliConstants.SOURCE_LIBRARY;

        DAG dag = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                new ArrayList<>(),
                new HashMap<>());
        assertThat(checkDagContains(dag, sourceAttr), is(true));

        List<String> singleAttrList = new ArrayList<>();
        singleAttrList.add(NsiliConstants.NSIL_PRODUCT + ":" + NsiliConstants.NSIL_CARD + "."
                + NsiliConstants.IDENTIFIER);
        DAG oneAttrDAG = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                singleAttrList,
                new HashMap<>());
        assertThat(checkDagContains(oneAttrDAG, sourceAttr), is(false));
    }

    @Test
    public void testAdvancedGeospatial() throws Exception {
        String id = UUID.randomUUID()
                .toString();
        MetacardImpl card = new MetacardImpl();
        card.setId(id);
        card.setTitle("Test Title");
        card.setSourceId("Test Source");
        card.setLocation("POLYGON((1 1,1 2,2 2,2 1,1 1))");

        ResultImpl result = new ResultImpl();
        result.setMetacard(card);

        ORB orb = ORB.init(new String[0], null);
        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager()
                .activate();

        String advGeoAttr = NsiliConstants.NSIL_PRODUCT + ":" + NsiliConstants.NSIL_PART + ":"
                + NsiliConstants.NSIL_COVERAGE + "." + NsiliConstants.ADVANCED_GEOSPATIAL;

        String boundingGeoAttr = NsiliConstants.NSIL_PRODUCT + ":" + NsiliConstants.NSIL_PART + ":"
                + NsiliConstants.NSIL_COVERAGE + "." + NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX;

        DAG dag = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                new ArrayList<>(),
                new HashMap<>());
        assertThat(checkDagContains(dag, advGeoAttr), is(true));
        assertThat(checkDagContains(dag, boundingGeoAttr), is(true));
    }

    @Test(expected = DagParsingException.class)
    public void testMandatoryAttributesFail() throws Exception {
        String id = UUID.randomUUID()
                .toString();
        MetacardImpl card = new MetacardImpl();
        card.setId(id);
        card.setTitle("Test Title");
        card.setSourceId("Test Source");

        ResultImpl result = new ResultImpl();
        result.setMetacard(card);

        ORB orb = ORB.init(new String[0], null);
        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager()
                .activate();

        String sourceAttr = NsiliConstants.NSIL_PRODUCT + ":" + NsiliConstants.NSIL_CARD + "."
                + NsiliConstants.SOURCE_LIBRARY;

        Map<String, List<String>> mandatoryAttrs = new HashMap<>();
        mandatoryAttrs.put(NsiliConstants.NSIL_COMMON,
                Arrays.asList(NsiliConstants.IDENTIFIER_MISSION));
        DAG dag = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                new ArrayList<>(),
                mandatoryAttrs);
    }

    @Test
    public void testMandatoryAttributesSuccess() throws Exception {
        String id = UUID.randomUUID()
                .toString();
        MetacardImpl card = new MetacardImpl();
        card.setId(id);
        card.setTitle("Test Title");
        card.setSourceId("Test Source");

        ResultImpl result = new ResultImpl();
        result.setMetacard(card);

        ORB orb = ORB.init(new String[0], null);
        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager()
                .activate();

        Map<String, List<String>> mandatoryAttrs = new HashMap<>();
        mandatoryAttrs.put(NsiliConstants.NSIL_CARD, Arrays.asList(NsiliConstants.IDENTIFIER));
        DAG dag = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                new ArrayList<>(),
                mandatoryAttrs);
        assertThat(dag, notNullValue());
    }

    private static boolean checkDagContains(DAG dag, String attribute) {
        List<String> dagAttrs = ResultDAGConverter.getAttributes(dag);
        return dagAttrs.contains(attribute);
    }
}
