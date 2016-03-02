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
package org.codice.alliance.nsili.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.codice.alliance.nsili.common.NsiliApprovalStatus;
import org.codice.alliance.nsili.common.NsiliCommonUtils;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.NsiliCxpMetacardType;
import org.codice.alliance.nsili.common.NsiliCxpStatusType;
import org.codice.alliance.nsili.common.NsiliExploitationSubQualCode;
import org.codice.alliance.nsili.common.NsiliGmtiMetacardType;
import org.codice.alliance.nsili.common.NsiliIRMetacardType;
import org.codice.alliance.nsili.common.NsiliImageryDecompressionTech;
import org.codice.alliance.nsili.common.NsiliImageryMetacardType;
import org.codice.alliance.nsili.common.NsiliImageryType;
import org.codice.alliance.nsili.common.NsiliMessageMetacardType;
import org.codice.alliance.nsili.common.NsiliMetacardType;
import org.codice.alliance.nsili.common.NsiliMetadataEncodingScheme;
import org.codice.alliance.nsili.common.NsiliProductType;
import org.codice.alliance.nsili.common.NsiliReportMetacardType;
import org.codice.alliance.nsili.common.NsiliReportPriority;
import org.codice.alliance.nsili.common.NsiliReportType;
import org.codice.alliance.nsili.common.NsiliRfiMetacardType;
import org.codice.alliance.nsili.common.NsiliRfiStatus;
import org.codice.alliance.nsili.common.NsiliRfiWorkflowStatus;
import org.codice.alliance.nsili.common.NsiliScanningMode;
import org.codice.alliance.nsili.common.NsiliSdsOpStatus;
import org.codice.alliance.nsili.common.NsiliTaskMetacardType;
import org.codice.alliance.nsili.common.NsiliTaskStatus;
import org.codice.alliance.nsili.common.NsiliTdlMetacardType;
import org.codice.alliance.nsili.common.NsiliVideoCategoryType;
import org.codice.alliance.nsili.common.NsiliVideoEncodingScheme;
import org.codice.alliance.nsili.common.NsiliVideoMetacardType;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.AbsTimeHelper;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.Edge;
import org.codice.alliance.nsili.common.UCO.Node;
import org.codice.alliance.nsili.common.UCO.NodeType;
import org.codice.alliance.nsili.common.UCO.RectangleHelper;
import org.codice.alliance.nsili.common.UCO.Time;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;

public class TestDAGConverter {

    private static final String CARD_ID = "Card ID";

    private static final String SOURCE_PUBLISHER = "Source Publisher";

    private static final String SOURCE_LIBRARY = "SourceLibrary";

    private static final String ARCHIVE_INFORMATION = "Archive Information";

    private static final Boolean STREAM_ARCHIVED = false;

    private static final String STREAM_CREATOR = "Stream Creator";

    private static final String STREAM_STANDARD = "STANAG4609";

    private static final String STREAM_STANDARD_VER = "1.0";

    private static final String STREAM_SOURCE_URL = "http://localhost:1234/stream";

    private static final Short STREAM_PROGRAM_ID = 3;

    private static final String CLASS_POLICY = "NATO/EU";

    private static final String CLASS_CLASSIFICATION = "UNCLASSIFIED";

    private static final String CLASS_RELEASABILITY = "NATO";

    private static final String COM_DESCRIPTION_ABSTRACT = "Product Description";

    private static final String COM_ID_MSN = "CX100";

    private static final String COM_ID_UUID = UUID.randomUUID()
            .toString();

    private static final Integer COM_JC3ID = 1234;

    private static final String COM_LANGUAGE = "eng";

    private static final String COM_SOURCE = "TestSourceSystem";

    private static final String COM_SUBJECT_CATEGORY_TARGET = "Airfields";

    private static final String COM_TARGET_NUMBER = "123-456-7890";

    private static final NsiliProductType COM_TYPE =
            NsiliProductType.COLLECTION_EXPLOITATION_PLAN;

    private static final String COVERAGE_COUNTRY_CD = "USA";

    private static final Double UPPER_LEFT_LAT = 5.0;

    private static final Double UPPER_LEFT_LON = 1.0;

    private static final Double LOWER_RIGHT_LAT = 1.0;

    private static final Double LOWER_RIGHT_LON = 5.0;

    private static final String EXPLOITATION_DESC = "Exploitation Info Description";

    private static final Short EXPLOITATION_LEVEL = 0;

    private static final Boolean EXPLOITATION_AUTO_GEN = false;

    private static final String EXPLOITATION_SUBJ_QUAL_CODE =
            NsiliExploitationSubQualCode.GOOD.toString();

    private static final String IMAGERY_CATEGORY = NsiliImageryType.VIS.toString();

    private static final Short IMAGERY_CLOUD_COVER_PCT = 35;

    private static final String IMAGERY_COMMENTS = "Imagery Comments";

    private static final String IMAGERY_DECOMPRESSION_TECH =
            NsiliImageryDecompressionTech.C1.toString();

    private static final String IMAGERY_IDENTIFIER = "1234";

    private static final Short IMAGERY_NIIRS = 2;

    private static final Integer IMAGERY_NUM_BANDS = 5000;

    private static final Integer IMAGERY_NUM_ROWS = 500;

    private static final Integer IMAGERY_NUM_COLS = 400;

    private static final String IMAGERY_TITLE = "Imagery Title";

    private static final String WKT_LOCATION = "POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1))";

    private static final Boolean FILE_ARCHIVED = false;

    private static final String FILE_ARCHIVE_INFO = "File Archive Info";

    private static final String FILE_CREATOR = "File Creator";

    private static final Double FILE_EXTENT = 25.5;

    private static final String FILE_FORMAT = "JPEG";

    private static final String FILE_FORMAT_VER = "1.0";

    private static final String FILE_PRODUCT_URL = "http://localhost/file.jpg";

    private static final String FILE_TITLE = "File Title";

    private static final Double GMTI_JOB_ID = 2.3;

    private static final Integer GMTI_TARGET_REPORTS = 2;

    private static final String MESSAGE_RECIPIENT = "john@doe.com";

    private static final String MESSAGE_SUBJECT = "Test Subject";

    private static final String MESSAGE_BODY = "Test Message Body";

    private static final String MESSAGE_TYPE = "XMPP";

    private static final Double VIDEO_AVG_BIT_RATE = 22.5;

    private static final String VIDEO_CATEGORY = NsiliVideoCategoryType.VIS.name();

    private static final NsiliVideoEncodingScheme VIDEO_ENCODING_SCHEME =
            NsiliVideoEncodingScheme.MPEG2;

    private static final Double VIDEO_FRAME_RATE = 15.4;

    private static final Integer VIDEO_NUM_ROWS = 500;

    private static final Integer VIDEO_NUM_COLS = 400;

    private static final String VIDEO_METADATA_ENC_SCHEME =
            NsiliMetadataEncodingScheme.KLV.name();

    private static final Short VIDEO_MISM_LEVEL = 4;

    private static final String VIDEO_SCANNING_MODE = NsiliScanningMode.PROGRESSIVE.name();

    private static final String REPORT_REQ_SERIAL_NUM = "112233";

    private static final String REPORT_PRIORITY = NsiliReportPriority.FLASH.name();

    private static final String REPORT_TYPE = NsiliReportType.MTIEXREP.name();

    private static final Short TDL_ACTIVITY = 99;

    private static final String TDL_MESSAGE_NUM = "J3.2";

    private static final Short TDL_PLATFORM_NUM = 42;

    private static final String TDL_TRACK_NUM = "AK320";

    private static final String CXP_STATUS = NsiliCxpStatusType.CURRENT.name();

    private static final String RFI_FOR_ACTION = "USAF";

    private static final String RFI_FOR_INFORMATION = "USMC";

    private static final String RFI_SERIAL_NUM = "123456";

    private static final String RFI_STATUS = NsiliRfiStatus.APPROVED.name();

    private static final String RFI_WORKFLOW_STATUS = NsiliRfiWorkflowStatus.ACCEPTED.name();

    private static final String TASK_COMMENTS = "Task Comments";

    private static final String TASK_STATUS = NsiliTaskStatus.INTERRUPTED.name();

    private static final String SOURCE_ID = "myNsiliSource";

    private static final Integer NUM_ASSOCIATIONS = 5;

    private static final NsiliApprovalStatus APPROVAL_STATUS =
            NsiliApprovalStatus.NOT_APPLICABLE;

    private static final String APPROVED_BY = "ApprovedBy";

    private static final NsiliSdsOpStatus SDS_OP_STATUS =
            NsiliSdsOpStatus.LIMITED_OPERATIONAL;

    private ORB orb;

    private Calendar cal;

    private static final boolean SHOULD_PRINT_CARD = false;

    @Before
    public void setUp() {
        this.orb = ORB.init();

        int year = 2016;
        int month = 01;
        int dayOfMonth = 29;
        int hourOfDay = 17;
        int minute = 05;
        int second = 10;
        cal = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
    }

    /**
     * Test the Imagery View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_FILE
     * NSIL_STREAM
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_IMAGERY
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testImageryViewConversion() {
        DAG imageryDAG = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addImageryPart(graph, productNode);
        addAssocationNode(graph, productNode);
        addApprovalNode(graph, productNode);
        addSdsNode(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        imageryDAG.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        imageryDAG.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(imageryDAG, SOURCE_ID);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-imagery.txt");
            if (file.exists()) {
                file.delete();
            }

            try (PrintStream outStream = new PrintStream(file)) {
                printMetacard(metacard, outStream);
            } catch (IOException ioe) {
                //Ignore the error
            }
        }

        //Check top-level meta-card attributes
        assertTrue(NsiliImageryMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(NsiliProductType.IMAGERY.toString().equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime()
                .equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI()
                .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkImageryAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
        checkAssociationAttribute(metacard);
        checkApprovalAttribute(metacard);
        checkSdsAttribute(metacard);

        DAGConverter.logMetacard(metacard, "123");
    }

    private void checkExploitationInfoAttributes(MetacardImpl metacard) {
        Attribute exploitationDescAttr =
                metacard.getAttribute(NsiliMetacardType.EXPLOITATION_DESCRIPTION);
        assertNotNull(exploitationDescAttr);
        assertTrue(EXPLOITATION_DESC.equals(exploitationDescAttr.getValue()
                .toString()));

        Attribute exploitationLevelAttr =
                metacard.getAttribute(NsiliMetacardType.EXPLOITATION_LEVEL);
        assertNotNull(exploitationLevelAttr);
        assertTrue(EXPLOITATION_LEVEL == (short) exploitationLevelAttr.getValue());

        Attribute exploitationAutoGenAttr =
                metacard.getAttribute(NsiliMetacardType.EXPLOITATION_AUTO_GEN);
        assertNotNull(exploitationAutoGenAttr);
        assertTrue(EXPLOITATION_AUTO_GEN == (boolean) exploitationAutoGenAttr.getValue());

        Attribute subjQualCodeAttr =
                metacard.getAttribute(NsiliMetacardType.EXPLOITATION_SUBJ_QUAL_CODE);
        assertNotNull(subjQualCodeAttr);
        assertTrue(EXPLOITATION_SUBJ_QUAL_CODE.equals(subjQualCodeAttr.getValue()
                .toString()));
    }

    private void checkStreamAttributes(MetacardImpl metacard) {
        Attribute archivedAttr = metacard.getAttribute(NsiliMetacardType.STREAM_ARCHIVED);
        assertNotNull(archivedAttr);
        assertTrue(STREAM_ARCHIVED == (boolean) archivedAttr.getValue());

        Attribute archivalInfoAttr =
                metacard.getAttribute(NsiliMetacardType.STREAM_ARCHIVAL_INFO);
        assertNotNull(archivalInfoAttr);
        assertTrue(ARCHIVE_INFORMATION.equals(archivalInfoAttr.getValue()));

        Attribute creatorAttr = metacard.getAttribute(NsiliMetacardType.STREAM_CREATOR);
        assertNotNull(creatorAttr);
        assertTrue(STREAM_CREATOR.equals(creatorAttr.getValue()
                .toString()));

        Attribute dateTimeDeclaredAttr =
                metacard.getAttribute(NsiliMetacardType.STREAM_DATETIME_DECLARED);
        assertNotNull(dateTimeDeclaredAttr);
        assertTrue(cal.getTime()
                .equals(dateTimeDeclaredAttr.getValue()));

        Attribute programIdAttr = metacard.getAttribute(NsiliMetacardType.STREAM_PROGRAM_ID);
        assertNotNull(programIdAttr);
        assertTrue(STREAM_PROGRAM_ID == (short) programIdAttr.getValue());

    }

    private void checkCommonAttributes(MetacardImpl metacard) {
        Attribute identifierMsnAttr =
                metacard.getAttribute(NsiliMetacardType.IDENTIFIER_MISSION);
        assertNotNull(identifierMsnAttr);
        assertTrue(COM_ID_MSN.equals(identifierMsnAttr.getValue()
                .toString()));

        Attribute identifierJc3idmAttr = metacard.getAttribute(NsiliMetacardType.ID_JC3IEDM);
        assertNotNull(identifierJc3idmAttr);
        assertTrue(COM_JC3ID == (int) identifierJc3idmAttr.getValue());

        Attribute languageAttr = metacard.getAttribute(NsiliMetacardType.LANGUAGE);
        assertNotNull(languageAttr);
        assertTrue(COM_LANGUAGE.equals(languageAttr.getValue()
                .toString()));

        Attribute stanagSourceAttr = metacard.getAttribute(NsiliMetacardType.SOURCE);
        assertNotNull(stanagSourceAttr);
        assertTrue(COM_SOURCE.equals(stanagSourceAttr.getValue()
                .toString()));

        Attribute subjCatTgtAttr =
                metacard.getAttribute(NsiliMetacardType.SUBJECT_CATEGORY_TARGET);
        assertNotNull(subjCatTgtAttr);
        assertTrue(COM_SUBJECT_CATEGORY_TARGET.equals(subjCatTgtAttr.getValue()
                .toString()));

        Attribute tgtNumAttr = metacard.getAttribute(NsiliMetacardType.TARGET_NUMBER);
        assertNotNull(tgtNumAttr);
        assertTrue(COM_TARGET_NUMBER.equals(tgtNumAttr.getValue()
                .toString()));

        Attribute productTypeAttr = metacard.getAttribute(NsiliMetacardType.PRODUCT_TYPE);
        assertNotNull(productTypeAttr);
        assertTrue(COM_TYPE.name()
                .equals(productTypeAttr.getValue()
                        .toString()));
    }

    private void checkImageryAttributes(MetacardImpl metacard) {
        Attribute cloudCoverPctAttr =
                metacard.getAttribute(NsiliImageryMetacardType.CLOUD_COVER_PCT);
        assertNotNull(cloudCoverPctAttr);
        assertTrue(IMAGERY_CLOUD_COVER_PCT == (short) cloudCoverPctAttr.getValue());

        Attribute imageryCommentsAttr =
                metacard.getAttribute(NsiliImageryMetacardType.IMAGERY_COMMENTS);
        assertNotNull(imageryCommentsAttr);
        assertTrue(IMAGERY_COMMENTS.equals(imageryCommentsAttr.getValue()));

        Attribute imageryCategoryAttr =
                metacard.getAttribute(NsiliImageryMetacardType.IMAGERY_CATEGORY);
        assertNotNull(imageryCategoryAttr);
        assertTrue(IMAGERY_CATEGORY.equals(imageryCategoryAttr.getValue()
                .toString()));

        Attribute decompressionTechAttr =
                metacard.getAttribute(NsiliImageryMetacardType.DECOMPRESSION_TECHNIQUE);
        assertNotNull(decompressionTechAttr);
        assertTrue(IMAGERY_DECOMPRESSION_TECH.equals(decompressionTechAttr.getValue()
                .toString()));

        Attribute imageIdAttr = metacard.getAttribute(NsiliImageryMetacardType.IMAGE_ID);
        assertNotNull(imageIdAttr);
        assertTrue(IMAGERY_IDENTIFIER.equals(imageIdAttr.getValue()
                .toString()));

        Attribute niirsAttr = metacard.getAttribute(NsiliImageryMetacardType.NIIRS);
        assertNotNull(niirsAttr);
        assertTrue(IMAGERY_NIIRS == (short) niirsAttr.getValue());

        Attribute numBandsAttr = metacard.getAttribute(NsiliImageryMetacardType.NUM_BANDS);
        assertNotNull(numBandsAttr);
        assertTrue(IMAGERY_NUM_BANDS == (int) numBandsAttr.getValue());

        Attribute numRowsAttr = metacard.getAttribute(NsiliImageryMetacardType.NUM_ROWS);
        assertNotNull(numRowsAttr);
        assertTrue(IMAGERY_NUM_ROWS == (int) numRowsAttr.getValue());

        Attribute numColsAttr = metacard.getAttribute(NsiliImageryMetacardType.NUM_COLS);
        assertNotNull(numColsAttr);
        assertTrue(IMAGERY_NUM_COLS == (int) numColsAttr.getValue());

        Attribute endDateTimeAttr =
                metacard.getAttribute(NsiliImageryMetacardType.END_DATETIME);
        assertNotNull(endDateTimeAttr);
        assertTrue(cal.getTime()
                .equals(endDateTimeAttr.getValue()));

        assertNotNull(metacard.getResourceSize());
        int size = Integer.parseInt(metacard.getResourceSize());
        assertTrue(size == DAGConverter.convertMegabytesToBytes(FILE_EXTENT));
    }

    private void checkSecurityAttributes(MetacardImpl metacard) {
        Attribute classificationAttr =
                metacard.getAttribute(NsiliMetacardType.SECURITY_CLASSIFICATION);
        assertNotNull(classificationAttr);
        assertTrue(CLASS_CLASSIFICATION.equals(classificationAttr.getValue()
                .toString()));

        Attribute policyAttr = metacard.getAttribute(NsiliMetacardType.SECURITY_POLICY);
        assertNotNull(policyAttr);
        assertTrue(CLASS_POLICY.equals(policyAttr.getValue()
                .toString()));

        Attribute releasabilityAttr =
                metacard.getAttribute(NsiliMetacardType.SECURITY_RELEASABILITY);
        assertNotNull(releasabilityAttr);
        assertTrue(CLASS_RELEASABILITY.equals(releasabilityAttr.getValue()
                .toString()));
    }

    private void checkCoverageAttributes(MetacardImpl metacard) {
        Attribute spatialCtryCodeAttr = metacard.getAttribute(NsiliMetacardType.COUNTRY_CODE);
        assertNotNull(spatialCtryCodeAttr);
        assertTrue(COVERAGE_COUNTRY_CD.equals(spatialCtryCodeAttr.getValue()
                .toString()));

        Attribute startTimeAttr = metacard.getAttribute(NsiliMetacardType.START_DATETIME);
        assertNotNull(startTimeAttr);
        assertTrue(cal.getTime()
                .equals(startTimeAttr.getValue()));

        Attribute endTimeAttr = metacard.getAttribute(NsiliMetacardType.END_DATETIME);
        assertNotNull(endTimeAttr);
        assertTrue(cal.getTime()
                .equals(endTimeAttr.getValue()));
    }

    private void checkAssociationAttribute(MetacardImpl metacard) {
        Attribute associationsAttr = metacard.getAttribute(NsiliMetacardType.ASSOCIATIONS);
        assertNotNull(associationsAttr);
        List<Serializable> associations = associationsAttr.getValues();
        assertTrue(NUM_ASSOCIATIONS == associations.size());
    }

    private void checkApprovalAttribute(MetacardImpl metacard) {
        Attribute approvedByAttr = metacard.getAttribute(NsiliMetacardType.APPROVAL_BY);
        assertNotNull(approvedByAttr);
        assertTrue(APPROVED_BY.equals(approvedByAttr.getValue()
                .toString()));

        Attribute modifiedAttr =
                metacard.getAttribute(NsiliMetacardType.APPROVAL_DATETIME_MODIFIED);
        assertNotNull(modifiedAttr);
        assertTrue(cal.getTime()
                .equals(modifiedAttr.getValue()));

        Attribute statusAttr = metacard.getAttribute(NsiliMetacardType.APPROVAL_STATUS);
        assertNotNull(statusAttr);
        assertTrue(APPROVAL_STATUS.name()
                .equals(statusAttr.getValue()
                        .toString()));
    }

    private void checkSdsAttribute(MetacardImpl metacard) {
        Attribute opStatusAttr =
                metacard.getAttribute(NsiliMetacardType.SDS_OPERATIONAL_STATUS);
        assertNotNull(opStatusAttr);
        assertTrue(SDS_OP_STATUS.name()
                .equals(opStatusAttr.getValue()
                        .toString()));
    }

    /**
     * Test the GMTI View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_FILE
     * NSIL_STREAM
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_GMTI
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testGmtiViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addStreamNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addGmtiPart(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-gmti.txt");
            if (file.exists()) {
                file.delete();
            }

            try (PrintStream outStream = new PrintStream(file)) {
                printMetacard(metacard, outStream);
            } catch (IOException ioe) {
                //Ignore the error
            }
        }

        //Check top-level meta-card attributes
        assertTrue(NsiliGmtiMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(NsiliProductType.GMTI.toString()
                .equals(metacard.getContentTypeName()));
        assertTrue(STREAM_STANDARD_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime()
                .equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(STREAM_SOURCE_URL.equals(metacard.getResourceURI()
                .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkGmtiAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkGmtiAttributes(MetacardImpl metacard) {
        Attribute gmtiJobAttr = metacard.getAttribute(NsiliGmtiMetacardType.JOB_ID);
        assertNotNull(gmtiJobAttr);
        assertEquals(GMTI_JOB_ID, gmtiJobAttr.getValue());

        Attribute numTgtAttr = metacard.getAttribute(NsiliGmtiMetacardType.NUM_TARGET_REPORTS);
        assertNotNull(numTgtAttr);
        assertTrue(GMTI_TARGET_REPORTS == (int) numTgtAttr.getValue());
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_FILE
     * NSIL_STREAM
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_MESSAGE
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testMessageViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addStreamNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addMessagePart(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-message.txt");
            if (file.exists()) {
                file.delete();
            }

            try (PrintStream outStream = new PrintStream(file)) {
                printMetacard(metacard, outStream);
            } catch (IOException ioe) {
                //Ignore the error
            }
        }

        //Check top-level meta-card attributes
        assertTrue(NsiliMessageMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertTrue(MESSAGE_SUBJECT.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(NsiliProductType.MESSAGE.toString().equals(metacard.getContentTypeName()));
        assertNull(metacard.getContentTypeVersion());
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime()
                .equals(metacard.getModifiedDate()));
        assertTrue(MESSAGE_BODY.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI()
                .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkMessageAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkMessageAttributes(MetacardImpl metacard) {
        Attribute messageRecipAttr = metacard.getAttribute(NsiliMessageMetacardType.RECIPIENT);
        assertNotNull(messageRecipAttr);
        assertTrue(MESSAGE_RECIPIENT.equals(messageRecipAttr.getValue()));

        Attribute typeAttr = metacard.getAttribute(NsiliMessageMetacardType.MESSAGE_TYPE);
        assertNotNull(typeAttr);
        assertTrue(MESSAGE_TYPE.equals(typeAttr.getValue()
                .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_FILE
     * NSIL_STREAM
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_VIDEO
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testVideoViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addStreamNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addVideoPart(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-video.txt");
            if (file.exists()) {
                file.delete();
            }

            try (PrintStream outStream = new PrintStream(file)) {
                printMetacard(metacard, outStream);
            } catch (IOException ioe) {
                //Ignore the error
            }
        }

        //Check top-level meta-card attributes
        assertTrue(NsiliVideoMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(NsiliProductType.VIDEO.toString().equals(metacard.getContentTypeName()));
        assertNull(metacard.getContentTypeVersion());
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime()
                .equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI()
                .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkVideoAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    @Test
    public void testOnlyRootNodeDAG() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(rootNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);
        assertNull(metacard.getTitle());
    }

    @Test
    public void testRootNodeNotProduct() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = new Node(0,
                NodeType.ROOT_NODE,
                NsiliConstants.NSIL_APPROVAL,
                orb.create_any());
        graph.addVertex(rootNode);

        Node attribNode = new Node(0, NodeType.ATTRIBUTE_NODE, NsiliConstants.NSIL_CARD, null);
        graph.addVertex(attribNode);
        graph.addEdge(rootNode, attribNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(rootNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);
        assertNull(metacard.getTitle());
    }

    @Test
    public void testAttributeWithNoValue() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        Node entityNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_CARD,
                orb.create_any());
        graph.addVertex(entityNode);
        graph.addEdge(rootNode, entityNode);

        Node attrNode = new Node(0, NodeType.ATTRIBUTE_NODE, NsiliConstants.STATUS, null);
        graph.addVertex(attrNode);
        graph.addEdge(entityNode, attrNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(rootNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);
        assertNull(metacard.getTitle());
    }

    @Test
    public void testRecordNodePresent() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        Node recordNode = new Node(0,
                NodeType.RECORD_NODE,
                NsiliConstants.NSIL_CARD,
                orb.create_any());
        graph.addVertex(recordNode);
        graph.addEdge(rootNode, recordNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(rootNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);
        assertNull(metacard.getTitle());
    }

    @Test
    public void testEmptyDAG() {
        DAG dag = new DAG();
        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);
        assertNull(metacard);
    }

    @Test
    public void testDAGNoEdges() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);
        assertNull(metacard);
    }

    @Test
    public void testStartNodeOfEdgeNull() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        Edge[] edges = new Edge[1];
        Edge edge = new Edge(0, 1, "");
        edges[0] = edge;
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);
        dag.edges = edges;

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);
        assertNull(metacard.getTitle());
    }

    @Test
    public void testEndNodeOfEdgeNull() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        Edge[] edges = new Edge[1];
        Edge edge = new Edge(1, 2, "");
        edges[0] = edge;
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);
        dag.edges = edges;

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);
        assertNull(metacard.getTitle());
    }

    private void checkVideoAttributes(MetacardImpl metacard) {
        Attribute avgBitRateAttr = metacard.getAttribute(NsiliVideoMetacardType.AVG_BIT_RATE);
        assertNotNull(avgBitRateAttr);
        assertEquals(VIDEO_AVG_BIT_RATE, avgBitRateAttr.getValue());

        Attribute categoryAttr = metacard.getAttribute(NsiliVideoMetacardType.CATEGORY);
        assertNotNull(categoryAttr);
        assertTrue(VIDEO_CATEGORY.equals(categoryAttr.getValue()
                .toString()));

        Attribute encodingSchemeAttr =
                metacard.getAttribute(NsiliVideoMetacardType.ENCODING_SCHEME);
        assertNotNull(encodingSchemeAttr);
        assertTrue(VIDEO_ENCODING_SCHEME.name()
                .equals(encodingSchemeAttr.getValue()
                        .toString()));

        Attribute frameRateAttr = metacard.getAttribute(NsiliVideoMetacardType.FRAME_RATE);
        assertNotNull(frameRateAttr);
        assertEquals(VIDEO_FRAME_RATE, frameRateAttr.getValue());

        Attribute numRowsAttr = metacard.getAttribute(NsiliVideoMetacardType.NUM_ROWS);
        assertNotNull(numRowsAttr);
        assertTrue(VIDEO_NUM_ROWS == (int) numRowsAttr.getValue());

        Attribute numColsAttr = metacard.getAttribute(NsiliVideoMetacardType.NUM_COLS);
        assertNotNull(numColsAttr);
        assertTrue(VIDEO_NUM_COLS == (int) numColsAttr.getValue());

        Attribute metadataEncSchemeAttr =
                metacard.getAttribute(NsiliVideoMetacardType.METADATA_ENCODING_SCHEME);
        assertNotNull(metadataEncSchemeAttr);
        assertTrue(VIDEO_METADATA_ENC_SCHEME.equals(metadataEncSchemeAttr.getValue()
                .toString()));

        Attribute mismLevelAttr = metacard.getAttribute(NsiliVideoMetacardType.MISM_LEVEL);
        assertNotNull(mismLevelAttr);
        assertTrue(VIDEO_MISM_LEVEL == (short) mismLevelAttr.getValue());

        Attribute scanningModeAttr =
                metacard.getAttribute(NsiliVideoMetacardType.SCANNING_MODE);
        assertNotNull(scanningModeAttr);
        assertTrue(VIDEO_SCANNING_MODE.equals(scanningModeAttr.getValue()
                .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_REPORT
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testReportViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addReportPart(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-report.txt");
            if (file.exists()) {
                file.delete();
            }

            try (PrintStream outStream = new PrintStream(file)) {
                printMetacard(metacard, outStream);
            } catch (IOException ioe) {
                //Ignore the error
            }
        }

        //Check top-level meta-card attributes
        assertTrue(NsiliReportMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(NsiliProductType.REPORT.toString()
                .equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime()
                .equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI()
                .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkReportAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkReportAttributes(MetacardImpl metacard) {
        Attribute origReqSerialAttr =
                metacard.getAttribute(NsiliReportMetacardType.ORIGINATOR_REQ_SERIAL_NUM);
        assertNotNull(origReqSerialAttr);
        assertTrue(REPORT_REQ_SERIAL_NUM.equals(origReqSerialAttr.getValue()
                .toString()));

        Attribute priorityAttr = metacard.getAttribute(NsiliReportMetacardType.PRIORITY);
        assertNotNull(priorityAttr);
        assertTrue(REPORT_PRIORITY.equals(priorityAttr.getValue()
                .toString()));

        Attribute typeAttr = metacard.getAttribute(NsiliReportMetacardType.TYPE);
        assertNotNull(typeAttr);
        assertTrue(REPORT_TYPE.equals(typeAttr.getValue()
                .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_STREAM
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_CXP
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testCCIRMCXPViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addCxpPart(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-ccirm-cxp.txt");
            if (file.exists()) {
                file.delete();
            }

            try (PrintStream outStream = new PrintStream(file)) {
                printMetacard(metacard, outStream);
            } catch (IOException ioe) {
                //Ignore the error
            }
        }

        //Check top-level meta-card attributes
        assertTrue(NsiliCxpMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(NsiliProductType.COLLECTION_EXPLOITATION_PLAN.toString().equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime()
                .equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI()
                .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkCxpAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkCxpAttributes(MetacardImpl metacard) {
        Attribute cxpAttr = metacard.getAttribute(NsiliCxpMetacardType.STATUS);
        assertNotNull(cxpAttr);
        assertTrue(CXP_STATUS.equals(cxpAttr.getValue()
                .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_STREAM
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_IR
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testCCIRMIRViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addIRPart(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-ccirm-ir.txt");
            if (file.exists()) {
                file.delete();
            }

            try (PrintStream outStream = new PrintStream(file)) {
                printMetacard(metacard, outStream);
            } catch (IOException ioe) {
                //Ignore the error
            }
        }

        //Check top-level meta-card attributes
        assertTrue(NsiliIRMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(NsiliProductType.DOCUMENT.toString()
                .equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime()
                .equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI()
                .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkIRAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkIRAttributes(MetacardImpl metacard) {
        //NSIL_IR is a marker node type only, no attributes to check
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_STREAM
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_RFI
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testCCIRMRFIViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addRFIPart(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-ccirm-rfi.txt");
            if (file.exists()) {
                file.delete();
            }

            try (PrintStream outStream = new PrintStream(file)) {
                printMetacard(metacard, outStream);
            } catch (IOException ioe) {
                //Ignore the error
            }
        }

        //Check top-level meta-card attributes
        assertTrue(NsiliRfiMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(NsiliProductType.RFI.toString().equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime()
                .equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI()
                .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkRFIAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkRFIAttributes(MetacardImpl metacard) {
        Attribute forActionAttr = metacard.getAttribute(NsiliRfiMetacardType.FOR_ACTION);
        assertNotNull(forActionAttr);
        assertTrue(RFI_FOR_ACTION.equals(forActionAttr.getValue()
                .toString()));

        Attribute forInfoAttr = metacard.getAttribute(NsiliRfiMetacardType.FOR_INFORMATION);
        assertNotNull(forInfoAttr);
        assertTrue(RFI_FOR_INFORMATION.equals(forInfoAttr.getValue()
                .toString()));

        Attribute serialNumAttr = metacard.getAttribute(NsiliRfiMetacardType.SERIAL_NUMBER);
        assertNotNull(serialNumAttr);
        assertTrue(RFI_SERIAL_NUM.equals(serialNumAttr.getValue()
                .toString()));

        Attribute statusAttr = metacard.getAttribute(NsiliRfiMetacardType.STATUS);
        assertNotNull(statusAttr);
        assertTrue(RFI_STATUS.equals(statusAttr.getValue()
                .toString()));

        Attribute workflowStatusAttr =
                metacard.getAttribute(NsiliRfiMetacardType.WORKFLOW_STATUS);
        assertNotNull(workflowStatusAttr);
        assertTrue(RFI_WORKFLOW_STATUS.equals(workflowStatusAttr.getValue()
                .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_STREAM
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_TASK
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testCCIRMTaskViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addTaskPart(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-ccirm-task.txt");
            if (file.exists()) {
                file.delete();
            }

            try (PrintStream outStream = new PrintStream(file)) {
                printMetacard(metacard, outStream);
            } catch (IOException ioe) {
                //Ignore the error
            }
        }

        //Check top-level meta-card attributes
        assertTrue(NsiliTaskMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(NsiliProductType.TASK.toString().equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime()
                .equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI()
                .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkTaskAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkTaskAttributes(MetacardImpl metacard) {
        Attribute commentAttr = metacard.getAttribute(NsiliTaskMetacardType.COMMENTS);
        assertNotNull(commentAttr);
        assertTrue(TASK_COMMENTS.equals(commentAttr.getValue()
                .toString()));

        Attribute statusAttr = metacard.getAttribute(NsiliTaskMetacardType.STATUS);
        assertNotNull(statusAttr);
        assertTrue(TASK_STATUS.equals(statusAttr.getValue()
                .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_STREAM
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_TDL
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testTdlViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addStreamNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addTdlPart(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag, SOURCE_ID);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-tdl.txt");
            if (file.exists()) {
                file.delete();
            }

            try (PrintStream outStream = new PrintStream(file)) {
                printMetacard(metacard, outStream);
            } catch (IOException ioe) {
                //Ignore the error
            }
        }

        //Check top-level meta-card attributes
        assertTrue(NsiliTdlMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(NsiliProductType.TDL_DATA.toString()
                .equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime()
                .equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI()
                .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkTdlAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkTdlAttributes(MetacardImpl metacard) {
        Attribute activityAttr = metacard.getAttribute(NsiliTdlMetacardType.ACTIVITY);
        assertNotNull(activityAttr);
        assertTrue(TDL_ACTIVITY == (short) activityAttr.getValue());

        Attribute msgNumAttr = metacard.getAttribute(NsiliTdlMetacardType.MESSAGE_NUM);
        assertNotNull(msgNumAttr);
        assertTrue(TDL_MESSAGE_NUM.equals(msgNumAttr.getValue()
                .toString()));

        Attribute platformNumAttr = metacard.getAttribute(NsiliTdlMetacardType.PLATFORM);
        assertNotNull(platformNumAttr);
        assertTrue(TDL_PLATFORM_NUM == (short) platformNumAttr.getValue());

        Attribute trackNumAttr = metacard.getAttribute(NsiliTdlMetacardType.TRACK_NUM);
        assertNotNull(trackNumAttr);
        assertTrue(TDL_TRACK_NUM.equals(trackNumAttr.getValue()
                .toString()));
    }

    private Node createRootNode() {
        return new Node(0, NodeType.ROOT_NODE, NsiliConstants.NSIL_PRODUCT, orb.create_any());
    }

    private void addCardNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node cardNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CARD, any);
        graph.addVertex(cardNode);
        graph.addEdge(productNode, cardNode);

        addStringAttribute(graph, cardNode, NsiliConstants.IDENTIFIER, CARD_ID);
        addDateAttribute(graph, cardNode, NsiliConstants.SOURCE_DATE_TIME_MODIFIED);
        addDateAttribute(graph, cardNode, NsiliConstants.DATE_TIME_MODIFIED);
        addStringAttribute(graph, cardNode, NsiliConstants.PUBLISHER, SOURCE_PUBLISHER);
        addStringAttribute(graph, cardNode, NsiliConstants.SOURCE_LIBRARY, SOURCE_LIBRARY);
    }

    private void addFileNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node fileNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_FILE, any);
        graph.addVertex(fileNode);
        graph.addEdge(productNode, fileNode);

        addBooleanAttribute(graph, fileNode, NsiliConstants.ARCHIVED, FILE_ARCHIVED);
        addStringAttribute(graph,
                fileNode,
                NsiliConstants.ARCHIVE_INFORMATION,
                FILE_ARCHIVE_INFO);
        addStringAttribute(graph, fileNode, NsiliConstants.CREATOR, FILE_CREATOR);
        addDateAttribute(graph, fileNode, NsiliConstants.DATE_TIME_DECLARED);
        addDoubleAttribute(graph, fileNode, NsiliConstants.EXTENT, FILE_EXTENT);
        addStringAttribute(graph, fileNode, NsiliConstants.FORMAT, FILE_FORMAT);
        addStringAttribute(graph, fileNode, NsiliConstants.FORMAT_VERSION, FILE_FORMAT_VER);
        addStringAttribute(graph, fileNode, NsiliConstants.PRODUCT_URL, FILE_PRODUCT_URL);
        addStringAttribute(graph, fileNode, NsiliConstants.TITLE, FILE_TITLE);
    }

    private void addStreamNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node streamNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_STREAM, any);
        graph.addVertex(streamNode);
        graph.addEdge(productNode, streamNode);

        addBooleanAttribute(graph, streamNode, NsiliConstants.ARCHIVED, STREAM_ARCHIVED);
        addStringAttribute(graph,
                streamNode,
                NsiliConstants.ARCHIVE_INFORMATION,
                ARCHIVE_INFORMATION);
        addStringAttribute(graph, streamNode, NsiliConstants.CREATOR, STREAM_CREATOR);
        addDateAttribute(graph, streamNode, NsiliConstants.DATE_TIME_DECLARED);
        addStringAttribute(graph, streamNode, NsiliConstants.STANDARD, STREAM_STANDARD);
        addStringAttribute(graph,
                streamNode,
                NsiliConstants.STANDARD_VERSION,
                STREAM_STANDARD_VER);
        addStringAttribute(graph, streamNode, NsiliConstants.SOURCE_URL, STREAM_SOURCE_URL);
        addShortAttribute(graph, streamNode, NsiliConstants.PROGRAM_ID, STREAM_PROGRAM_ID);
    }

    private void addMetadataSecurity(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node metadataSecurityNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_METADATA_SECURITY,
                any);
        graph.addVertex(metadataSecurityNode);
        graph.addEdge(productNode, metadataSecurityNode);

        addStringAttribute(graph, metadataSecurityNode, NsiliConstants.POLICY, CLASS_POLICY);
        addStringAttribute(graph,
                metadataSecurityNode,
                NsiliConstants.RELEASABILITY,
                CLASS_RELEASABILITY);
        addStringAttribute(graph,
                metadataSecurityNode,
                NsiliConstants.CLASSIFICATION,
                CLASS_CLASSIFICATION);
    }

    private void addSecurityNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node securityNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_SECURITY,
                any);
        graph.addVertex(securityNode);
        graph.addEdge(productNode, securityNode);

        addStringAttribute(graph, securityNode, NsiliConstants.POLICY, CLASS_POLICY);
        addStringAttribute(graph,
                securityNode,
                NsiliConstants.RELEASABILITY,
                CLASS_RELEASABILITY);
        addStringAttribute(graph,
                securityNode,
                NsiliConstants.CLASSIFICATION,
                CLASS_CLASSIFICATION);
    }

    private void addAssocationNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        //First we create the NSIL_ASSOCATION
        Any assocAny = orb.create_any();
        Node associationNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_ASSOCIATION,
                assocAny);
        graph.addVertex(associationNode);
        graph.addEdge(productNode, associationNode);

        //Next create the NSIL_DESTINATION -- 1 per associated card
        for (int i = 0; i < NUM_ASSOCIATIONS; i++) {
            Any destAny = orb.create_any();
            Node destinationNode = new Node(0,
                    NodeType.ENTITY_NODE,
                    NsiliConstants.NSIL_DESTINATION,
                    destAny);
            graph.addVertex(destinationNode);
            graph.addEdge(associationNode, destinationNode);

            Any cardAny = orb.create_any();
            Node cardNode = new Node(0,
                    NodeType.ENTITY_NODE,
                    NsiliConstants.NSIL_CARD,
                    cardAny);
            graph.addVertex(cardNode);
            graph.addEdge(destinationNode, cardNode);

            addStringAttribute(graph,
                    cardNode,
                    NsiliConstants.IDENTIFIER,
                    UUID.randomUUID()
                            .toString());
            addDateAttribute(graph, cardNode, NsiliConstants.SOURCE_DATE_TIME_MODIFIED);
            addDateAttribute(graph, cardNode, NsiliConstants.DATE_TIME_MODIFIED);
            addStringAttribute(graph, cardNode, NsiliConstants.PUBLISHER, SOURCE_PUBLISHER);
            addStringAttribute(graph, cardNode, NsiliConstants.SOURCE_LIBRARY, SOURCE_LIBRARY);
        }
    }

    private void addApprovalNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any approvalAny = orb.create_any();
        Node approvalNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_APPROVAL,
                approvalAny);
        graph.addVertex(approvalNode);
        graph.addEdge(productNode, approvalNode);

        addStringAttribute(graph, approvalNode, NsiliConstants.APPROVED_BY, APPROVED_BY);
        addDateAttribute(graph, approvalNode, NsiliConstants.DATE_TIME_MODIFIED);
        addStringAttribute(graph,
                approvalNode,
                NsiliConstants.STATUS,
                APPROVAL_STATUS.getSpecName());
    }

    private void addSdsNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any sdsAny = orb.create_any();
        Node sdsNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_SDS, sdsAny);
        graph.addVertex(sdsNode);
        graph.addEdge(productNode, sdsNode);

        addStringAttribute(graph,
                sdsNode,
                NsiliConstants.OPERATIONAL_STATUS,
                SDS_OP_STATUS.getSpecName());
    }

    private Node addPartNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node partNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_PART, any);
        graph.addVertex(partNode);
        graph.addEdge(productNode, partNode);
        return partNode;
    }

    private void addImageryPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addImageryNode(graph, partNode3);
    }

    private void addGmtiPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addGmtiNode(graph, partNode3);
    }

    private void addMessagePart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addMessageNode(graph, partNode3);
    }

    private void addVideoPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addVideoNode(graph, partNode3);
    }

    private void addReportPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addReportNode(graph, partNode3);
    }

    private void addTdlPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addTdlNode(graph, partNode3);
    }

    private void addCxpPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addCxpNode(graph, partNode3);
    }

    private void addIRPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addIRNode(graph, partNode3);
    }

    private void addRFIPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addRFINode(graph, partNode3);
    }

    private void addTaskPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addTaskNode(graph, partNode3);
    }

    private void addCommonNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any commonAny = orb.create_any();
        Node commonNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_COMMON,
                commonAny);
        graph.addVertex(commonNode);
        graph.addEdge(parentNode, commonNode);

        addStringAttribute(graph,
                commonNode,
                NsiliConstants.DESCRIPTION_ABSTRACT,
                COM_DESCRIPTION_ABSTRACT);
        addStringAttribute(graph, commonNode, NsiliConstants.IDENTIFIER_MISSION, COM_ID_MSN);
        addStringAttribute(graph, commonNode, NsiliConstants.IDENTIFIER_UUID, COM_ID_UUID);
        addIntegerAttribute(graph, commonNode, NsiliConstants.IDENTIFIER_JC3IEDM, COM_JC3ID);
        addStringAttribute(graph, commonNode, NsiliConstants.LANGUAGE, COM_LANGUAGE);
        addStringAttribute(graph, commonNode, NsiliConstants.SOURCE, COM_SOURCE);
        addStringAttribute(graph,
                commonNode,
                NsiliConstants.SUBJECT_CATEGORY_TARGET,
                COM_SUBJECT_CATEGORY_TARGET);
        addStringAttribute(graph, commonNode, NsiliConstants.TARGET_NUMBER, COM_TARGET_NUMBER);
        addStringAttribute(graph, commonNode, NsiliConstants.TYPE, COM_TYPE.getSpecName());
    }

    private void addImageryNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any imageryAny = orb.create_any();
        Node imageryNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_IMAGERY,
                imageryAny);
        graph.addVertex(imageryNode);
        graph.addEdge(parentNode, imageryNode);

        addStringAttribute(graph, imageryNode, NsiliConstants.CATEGORY, IMAGERY_CATEGORY);
        addShortAttribute(graph,
                imageryNode,
                NsiliConstants.CLOUD_COVER_PCT,
                IMAGERY_CLOUD_COVER_PCT);
        addStringAttribute(graph, imageryNode, NsiliConstants.COMMENTS, IMAGERY_COMMENTS);
        addStringAttribute(graph,
                imageryNode,
                NsiliConstants.DECOMPRESSION_TECHNIQUE,
                IMAGERY_DECOMPRESSION_TECH);
        addStringAttribute(graph, imageryNode, NsiliConstants.IDENTIFIER, IMAGERY_IDENTIFIER);
        addShortAttribute(graph, imageryNode, NsiliConstants.NIIRS, IMAGERY_NIIRS);
        addIntegerAttribute(graph,
                imageryNode,
                NsiliConstants.NUMBER_OF_BANDS,
                IMAGERY_NUM_BANDS);
        addIntegerAttribute(graph,
                imageryNode,
                NsiliConstants.NUMBER_OF_ROWS,
                IMAGERY_NUM_ROWS);
        addIntegerAttribute(graph,
                imageryNode,
                NsiliConstants.NUMBER_OF_COLS,
                IMAGERY_NUM_COLS);
        addStringAttribute(graph, imageryNode, NsiliConstants.TITLE, IMAGERY_TITLE);
    }

    private void addGmtiNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any gmtiAny = orb.create_any();
        Node gmtiNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_GMTI, gmtiAny);
        graph.addVertex(gmtiNode);
        graph.addEdge(parentNode, gmtiNode);

        addDoubleAttribute(graph, gmtiNode, NsiliConstants.IDENTIFIER_JOB, GMTI_JOB_ID);
        addIntegerAttribute(graph,
                gmtiNode,
                NsiliConstants.NUMBER_OF_TARGET_REPORTS,
                GMTI_TARGET_REPORTS);
    }

    private void addMessageNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any messageAny = orb.create_any();
        Node messageNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_MESSAGE,
                messageAny);
        graph.addVertex(messageNode);
        graph.addEdge(parentNode, messageNode);

        addStringAttribute(graph, messageNode, NsiliConstants.RECIPIENT, MESSAGE_RECIPIENT);
        addStringAttribute(graph, messageNode, NsiliConstants.SUBJECT, MESSAGE_SUBJECT);
        addStringAttribute(graph, messageNode, NsiliConstants.MESSAGE_BODY, MESSAGE_BODY);
        addStringAttribute(graph, messageNode, NsiliConstants.MESSAGE_TYPE, MESSAGE_TYPE);
    }

    private void addVideoNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any videoAny = orb.create_any();
        Node videoNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_VIDEO,
                videoAny);
        graph.addVertex(videoNode);
        graph.addEdge(parentNode, videoNode);

        addDoubleAttribute(graph, videoNode, NsiliConstants.AVG_BIT_RATE, VIDEO_AVG_BIT_RATE);
        addStringAttribute(graph, videoNode, NsiliConstants.CATEGORY, VIDEO_CATEGORY);
        addStringAttribute(graph,
                videoNode,
                NsiliConstants.ENCODING_SCHEME,
                VIDEO_ENCODING_SCHEME.getSpecName());
        addDoubleAttribute(graph, videoNode, NsiliConstants.FRAME_RATE, VIDEO_FRAME_RATE);
        addIntegerAttribute(graph, videoNode, NsiliConstants.NUMBER_OF_ROWS, VIDEO_NUM_ROWS);
        addIntegerAttribute(graph, videoNode, NsiliConstants.NUMBER_OF_COLS, VIDEO_NUM_COLS);
        addStringAttribute(graph,
                videoNode,
                NsiliConstants.METADATA_ENC_SCHEME,
                VIDEO_METADATA_ENC_SCHEME);
        addShortAttribute(graph, videoNode, NsiliConstants.MISM_LEVEL, VIDEO_MISM_LEVEL);
        addStringAttribute(graph,
                videoNode,
                NsiliConstants.SCANNING_MODE,
                VIDEO_SCANNING_MODE);
    }

    private void addReportNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any reportAny = orb.create_any();
        Node reportNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_REPORT,
                reportAny);
        graph.addVertex(reportNode);
        graph.addEdge(parentNode, reportNode);

        addStringAttribute(graph,
                reportNode,
                NsiliConstants.ORIGINATORS_REQ_SERIAL_NUM,
                REPORT_REQ_SERIAL_NUM);
        addStringAttribute(graph, reportNode, NsiliConstants.PRIORITY, REPORT_PRIORITY);
        addStringAttribute(graph, reportNode, NsiliConstants.TYPE, REPORT_TYPE);
    }

    private void addTdlNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any tdlAny = orb.create_any();
        Node tdlNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_TDL, tdlAny);
        graph.addVertex(tdlNode);
        graph.addEdge(parentNode, tdlNode);

        addShortAttribute(graph, tdlNode, NsiliConstants.ACTIVITY, TDL_ACTIVITY);
        addStringAttribute(graph, tdlNode, NsiliConstants.MESSAGE_NUM, TDL_MESSAGE_NUM);
        addShortAttribute(graph, tdlNode, NsiliConstants.PLATFORM, TDL_PLATFORM_NUM);
        addStringAttribute(graph, tdlNode, NsiliConstants.TRACK_NUM, TDL_TRACK_NUM);
    }

    private void addCxpNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any cxpAny = orb.create_any();
        Node cxpNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CXP, cxpAny);
        graph.addVertex(cxpNode);
        graph.addEdge(parentNode, cxpNode);

        addStringAttribute(graph, cxpNode, NsiliConstants.STATUS, CXP_STATUS);
    }

    private void addIRNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any irAny = orb.create_any();
        Node irNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_IR, irAny);
        graph.addVertex(irNode);
        graph.addEdge(parentNode, irNode);
    }

    private void addRFINode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any rfiAny = orb.create_any();
        Node rfiNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_RFI, rfiAny);
        graph.addVertex(rfiNode);
        graph.addEdge(parentNode, rfiNode);

        addStringAttribute(graph, rfiNode, NsiliConstants.FOR_ACTION, RFI_FOR_ACTION);
        addStringAttribute(graph,
                rfiNode,
                NsiliConstants.FOR_INFORMATION,
                RFI_FOR_INFORMATION);
        addStringAttribute(graph, rfiNode, NsiliConstants.SERIAL_NUMBER, RFI_SERIAL_NUM);
        addStringAttribute(graph, rfiNode, NsiliConstants.STATUS, RFI_STATUS);
        addStringAttribute(graph,
                rfiNode,
                NsiliConstants.WORKFLOW_STATUS,
                RFI_WORKFLOW_STATUS);
    }

    private void addTaskNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any taskAny = orb.create_any();
        Node taskNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_TASK, taskAny);
        graph.addVertex(taskNode);
        graph.addEdge(parentNode, taskNode);

        addStringAttribute(graph, taskNode, NsiliConstants.COMMENTS, TASK_COMMENTS);
        addStringAttribute(graph, taskNode, NsiliConstants.STATUS, TASK_STATUS);
    }

    private void addCoverageNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any coverageAny = orb.create_any();
        Node coverageNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_COVERAGE,
                coverageAny);
        graph.addVertex(coverageNode);
        graph.addEdge(parentNode, coverageNode);

        addStringAttribute(graph,
                coverageNode,
                NsiliConstants.SPATIAL_COUNTRY_CODE,
                COVERAGE_COUNTRY_CD);
        addDateAttribute(graph, coverageNode, NsiliConstants.TEMPORAL_START);
        addDateAttribute(graph, coverageNode, NsiliConstants.TEMPORAL_END);

        org.codice.alliance.nsili.common.UCO.Coordinate2d upperLeft =
                new org.codice.alliance.nsili.common.UCO.Coordinate2d(UPPER_LEFT_LAT,
                        UPPER_LEFT_LON);
        org.codice.alliance.nsili.common.UCO.Coordinate2d lowerRight =
                new org.codice.alliance.nsili.common.UCO.Coordinate2d(LOWER_RIGHT_LAT,
                        LOWER_RIGHT_LON);
        org.codice.alliance.nsili.common.UCO.Rectangle rectangle =
                new org.codice.alliance.nsili.common.UCO.Rectangle(upperLeft, lowerRight);
        Any spatialCoverage = orb.create_any();
        RectangleHelper.insert(spatialCoverage, rectangle);
        addAnyAttribute(graph,
                coverageNode,
                NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX,
                spatialCoverage);
    }

    private void addExpoloitationInfoNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any exploitationAny = orb.create_any();
        Node exploitationNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_EXPLOITATION_INFO,
                exploitationAny);
        graph.addVertex(exploitationNode);
        graph.addEdge(parentNode, exploitationNode);

        addStringAttribute(graph,
                exploitationNode,
                NsiliConstants.DESCRIPTION,
                EXPLOITATION_DESC);
        addShortAttribute(graph, exploitationNode, NsiliConstants.LEVEL, EXPLOITATION_LEVEL);
        addBooleanAttribute(graph,
                exploitationNode,
                NsiliConstants.AUTO_GENERATED,
                EXPLOITATION_AUTO_GEN);
        addStringAttribute(graph,
                exploitationNode,
                NsiliConstants.SUBJ_QUALITY_CODE,
                EXPLOITATION_SUBJ_QUAL_CODE);
    }

    private void addStringAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, String value) {
        Any any = orb.create_any();
        any.insert_wstring(value);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void addIntegerAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Integer integer) {
        Any any = orb.create_any();
        any.insert_ulong(integer);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void addShortAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Short shortVal) {
        Any any = orb.create_any();
        any.insert_short(shortVal);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void addDoubleAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Double doubleVal) {
        Any any = orb.create_any();
        any.insert_double(doubleVal);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void addBooleanAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Boolean boolVal) {
        Any any = orb.create_any();
        any.insert_boolean(boolVal);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void addAnyAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Any any) {
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void addDateAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key) {
        Any any = orb.create_any();

        AbsTime absTime =
                new AbsTime(new org.codice.alliance.nsili.common.UCO.Date((short) cal.get(
                        Calendar.YEAR), (short) (cal.get(Calendar.MONTH) + 1), (short) cal.get(
                        Calendar.DAY_OF_MONTH)), new Time((short) cal.get(Calendar.HOUR_OF_DAY),
                        (short) cal.get(Calendar.MINUTE),
                        (short) cal.get(Calendar.SECOND)));
        AbsTimeHelper.insert(any, absTime);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void printMetacard(MetacardImpl metacard, PrintStream outStream) {
        MetacardType metacardType = metacard.getMetacardType();
        outStream.println("Metacard Type : " + metacardType.getClass()
                .getCanonicalName());
        outStream.println("ID : " + metacard.getId());
        outStream.println("Title : " + metacard.getTitle());
        outStream.println("Description : " + metacard.getDescription());
        outStream.println("Content Type Name : " + metacard.getContentTypeName());
        outStream.println("Content Type Version : " + metacard.getContentTypeVersion());
        outStream.println("Created Date : " + metacard.getCreatedDate());
        outStream.println("Effective Date : " + metacard.getEffectiveDate());
        outStream.println("Location : " + metacard.getLocation());
        outStream.println("SourceID : " + metacard.getSourceId());
        outStream.println("Modified Date : " + metacard.getModifiedDate());
        outStream.println("Resource URI : " + metacard.getResourceURI()
                .toString());

        Set<AttributeDescriptor> descriptors = metacardType.getAttributeDescriptors();
        for (AttributeDescriptor descriptor : descriptors) {
            Attribute attribute = metacard.getAttribute(descriptor.getName());
            if (attribute != null) {
                if (attribute.getValues() != null) {
                    String valueStr = getValueString(attribute.getValues());
                    outStream.println("  " + descriptor.getName() + " : " +
                            valueStr);
                } else {
                    outStream.println("  " + descriptor.getName() + " : " +
                            attribute.getValue());
                }
            }
        }
    }

    private static String getValueString(Collection<Serializable> collection) {
        return collection.stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
