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
package com.connexta.alliance.nsili.source;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.codice.ddf.cxf.SecureCxfClientFactory;
import org.codice.ddf.spatial.ogc.catalog.common.AvailabilityCommand;
import org.codice.ddf.spatial.ogc.catalog.common.AvailabilityTask;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.opengis.filter.sort.SortBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connexta.alliance.nsili.common.GIAS.AccessCriteria;
import com.connexta.alliance.nsili.common.GIAS.AttributeInformation;
import com.connexta.alliance.nsili.common.GIAS.CatalogMgr;
import com.connexta.alliance.nsili.common.GIAS.CatalogMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.DataModelMgr;
import com.connexta.alliance.nsili.common.GIAS.DataModelMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.HitCountRequest;
import com.connexta.alliance.nsili.common.GIAS.Library;
import com.connexta.alliance.nsili.common.GIAS.LibraryDescription;
import com.connexta.alliance.nsili.common.GIAS.LibraryHelper;
import com.connexta.alliance.nsili.common.GIAS.LibraryManager;
import com.connexta.alliance.nsili.common.GIAS.OrderMgr;
import com.connexta.alliance.nsili.common.GIAS.OrderMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.Polarity;
import com.connexta.alliance.nsili.common.GIAS.ProductMgr;
import com.connexta.alliance.nsili.common.GIAS.ProductMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.SortAttribute;
import com.connexta.alliance.nsili.common.GIAS.SubmitQueryRequest;
import com.connexta.alliance.nsili.common.GIAS.View;
import com.connexta.alliance.nsili.common.Nsili;
import com.connexta.alliance.nsili.common.NsiliConstants;
import com.connexta.alliance.nsili.common.UCO.DAG;
import com.connexta.alliance.nsili.common.UCO.DAGListHolder;
import com.connexta.alliance.nsili.common.UCO.InvalidInputParameter;
import com.connexta.alliance.nsili.common.UCO.NameValue;
import com.connexta.alliance.nsili.common.UCO.ProcessingFault;
import com.connexta.alliance.nsili.common.UCO.SystemFault;
import com.connexta.alliance.nsili.transformer.DAGConverter;

import ddf.catalog.data.ContentType;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.operation.Query;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.operation.impl.SourceResponseImpl;
import ddf.catalog.resource.ResourceNotFoundException;
import ddf.catalog.resource.ResourceNotSupportedException;
import ddf.catalog.resource.ResourceReader;
import ddf.catalog.service.ConfiguredService;
import ddf.catalog.source.ConnectedSource;
import ddf.catalog.source.FederatedSource;
import ddf.catalog.source.SourceMonitor;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.catalog.util.impl.MaskableImpl;

public class NsiliSource extends MaskableImpl
        implements FederatedSource, ConnectedSource, ConfiguredService {

    public static final String CXF_PASSWORD = "cxfPassword";

    public static final String CXF_USERNAME = "cxfUsername";

    public static final String ID = "id";

    public static final String KEY = "key";

    public static final String IOR_URL = "iorUrl";

    public static final String POLL_INTERVAL = "pollInterval";

    public static final String MAX_HIT_COUNT = "maxHitCount";

    private static final Logger LOGGER = LoggerFactory.getLogger(NsiliSource.class);

    private static final String DESCRIBABLE_PROPERTIES_FILE = "/describable.properties";

    private static final String DESCRIPTION = "description";

    private static final String ORGANIZATION = "organization";

    private static final String VERSION = "version";

    private static final String TITLE = "name";

    private static final String WGS84 = "WGS84";

    private static final String GEOGRAPHIC_DATUM = "GeographicDatum";

    private static final String ASC = "ASC";

    private static final String UTF8 = "UTF-8";

    private static final String CATALOG_MGR = "CatalogMgr";

    private static final String ORDER_MGR = "OrderMgr";

    private static final String PRODUCT_MGR = "ProductMgr";

    private static final String DATA_MODEL_MGR = "DataModelMgr";

    private static final String DEFAULT_USER_INFO = "Alliance";

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private static final String FILE = "file";

    private static Library library;

    private static Properties describableProperties = new Properties();

    /* Mandatory STANAG 4559 Managers */

    private CatalogMgr catalogMgr;

    private OrderMgr orderMgr;

    private ProductMgr productMgr;

    private DataModelMgr dataModelMgr;

    /* ---------------------------  */

    private AvailabilityTask availabilityTask;

    private String iorUrl;

    private String cxfUsername;

    private String cxfPassword;

    private String id;

    private String iorString;

    private Integer maxHitCount;

    private FilterAdapter filterAdapter;

    private org.omg.CORBA.ORB orb;

    private AccessCriteria accessCriteria = new AccessCriteria(NsiliFilterDelegate.EMPTY_STRING,
            NsiliFilterDelegate.EMPTY_STRING,
            NsiliFilterDelegate.EMPTY_STRING);

    private Set<SourceMonitor> sourceMonitors = new HashSet<>();

    private ScheduledFuture<?> availabilityPollFuture;

    private ScheduledExecutorService scheduler;

    private Integer pollInterval;

    private String configurationPid;

    private SecureCxfClientFactory<Nsili> factory;

    private NsiliFilterDelegate nsiliFilterDelegate;

    private Set<ContentType> contentTypes = NsiliConstants.CONTENT_TYPES;

    private View[] views;

    private HashMap<String, List<AttributeInformation>> queryableAttributes;

    private HashMap<String, String[]> resultAttributes;

    private HashMap<String, List<String>> sortableAttributes;

    private String description;

    private String ddfOrgName = DEFAULT_USER_INFO;

    private ResourceReader resourceReader;

    static {
        try (InputStream properties = NsiliSource.class.getResourceAsStream(
                DESCRIBABLE_PROPERTIES_FILE)) {
            describableProperties.load(properties);
        } catch (IOException e) {
            LOGGER.info("Failed to load properties", e);
        }
    }

    /**
     * Constructor used for testing.
     */
    NsiliSource(SecureCxfClientFactory factory, HashMap<String, String[]> resultAttributes,
            HashMap<String, List<String>> sortableAttributes, NsiliFilterDelegate filterDelegate) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        this.factory = factory;
        this.resultAttributes = resultAttributes;
        this.nsiliFilterDelegate = filterDelegate;
        this.sortableAttributes = sortableAttributes;
        ddfOrgName = System.getProperty("org.codice.ddf.system.organization", DEFAULT_USER_INFO);
        initOrb();
    }

    public NsiliSource() {
        initOrb();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void init() {
        initCorbaClient();
        setupAvailabilityPoll();
    }

    private void createClientFactory() {
        if (StringUtils.isNotBlank(cxfUsername) && StringUtils.isNotBlank(cxfPassword)) {
            factory = new SecureCxfClientFactory(iorUrl,
                    Nsili.class,
                    null,
                    null,
                    true,
                    false,
                    null,
                    null,
                    cxfUsername,
                    cxfPassword);
        } else {
            factory = new SecureCxfClientFactory(iorUrl,
                    Nsili.class,
                    null,
                    null,
                    true,
                    false,
                    null,
                    null);
        }
    }

    /**
     * Initializes the Corba Client ORB and gets the mandatory interfaces that are required for a
     * STANAG 4559 complaint Federated Source, also queries the source for the queryable attributes
     * and views that it provides.
     */
    private void initCorbaClient() {
        getIorString();
        if (iorString != null) {
            initLibrary();
            setSourceDescription();
            initMandatoryManagers();
            initServerViews();
            initQueryableAttributes();
            initSortableAndResultAttributes();
            setFilterDelegate();
        }
    }

    /**
     * Determines which protocol is specified and attempts to retrive the IOR String appropriately.
     * According to ANNEX D Section 2.2, "beginning with Edition 3 FTP is disallowed. CORBA Internet Object
     * References (IOR) are delivered via HTTP or HTTPS to support client binding."
     */
    private void getIorString() {
        URI uri;
        try {
            uri = new URI(iorUrl);
        } catch (URISyntaxException e) {
            LOGGER.error("Invalid URL specified for IOR string: {}", iorUrl);
            return;
        }

        if (uri.getScheme()
                .equals(HTTP) || uri.getScheme()
                .equals(HTTPS)) {
            getIorStringFromSource();
        } else if (uri.getScheme()
                .equals(FILE)) {
            getIorStringFromLocalDisk();
        } else {
            LOGGER.error("Invalid protocol specified for IOR string: {}", iorUrl);
        }
    }

    /**
     * Obtains the IOR string from a local file.
     */
    private void getIorStringFromLocalDisk() {
        try (InputStream inputStream = new FileInputStream(iorUrl.substring(7))) {
            iorString = IOUtils.toString(inputStream);
        } catch (IOException e) {
            LOGGER.error("{} : Unable to process IOR String.", id, e);
        }

        if (StringUtils.isNotBlank(iorString)) {
            LOGGER.debug("{} : Successfully obtained IOR file from {}", getId(), iorUrl);
        } else {
            LOGGER.error("{} : Received an empty or null IOR String.", id);
        }
    }

    /**
     * Uses the SecureClientCxfFactory to obtain the IOR string from the provided URL via HTTP(S).
     */
    private void getIorStringFromSource() {
        createClientFactory();
        Nsili nsili = factory.getClient();

        try (InputStream inputStream = nsili.getIorFile()) {
            iorString = IOUtils.toString(inputStream, UTF8);
            //Remove leading/trailing whitespace as the CORBA init can't handle that.
            iorString = iorString.trim();
        } catch (IOException e) {
            LOGGER.error("{} : Unable to process IOR String. {}", id, e.getMessage());
            LOGGER.debug("{} : Unable to process IOR String.", id, e);
        } catch (Exception e) {
            LOGGER.warn("{} : Error retrieving IOR file for {}. {}", id, iorUrl, e.getMessage());
            LOGGER.debug("{} : Error retrieving IOR file for {}. {}", id, iorUrl, e);
        }

        if (StringUtils.isNotBlank(iorString)) {
            LOGGER.debug("{} : Successfully obtained IOR file from {}", getId(), iorUrl);
        } else {
            LOGGER.error("{} : Received an empty or null IOR String.", id);
        }
    }

    /**
     * Initializes the Corba ORB with no additional arguments
     */
    private void initOrb() {
        orb = org.omg.CORBA.ORB.init(new String[0], null);
        if (orb != null) {
            LOGGER.debug("{} : Successfully initialized CORBA orb.", getId());
        } else {
            LOGGER.error("{} : Unable to initialize CORBA orb.", getId());
        }
    }

    /**
     * Initializes the Root STANAG 4559 Library Interface
     */
    private void initLibrary() {
        if (iorString != null) {
            org.omg.CORBA.Object obj = orb.string_to_object(iorString);

            library = LibraryHelper.narrow(obj);
            if (library != null) {
                LOGGER.debug("{} : Initialized Library Interface", getId());
            } else {
                LOGGER.error("{} : Unable to initialize the library interface.", getId());
            }
        }
    }

    /**
     * Initializes all STANAG 4559 mandatory managers:
     * CatalogMgr
     * OrderMgr
     * DataModelMgr
     * ProductMgr
     */
    private void initMandatoryManagers() {
        try {
            LibraryManager libraryManager = library.get_manager(CATALOG_MGR, accessCriteria);
            setCatalogMgr(CatalogMgrHelper.narrow(libraryManager));

            libraryManager = library.get_manager(ORDER_MGR, accessCriteria);
            setOrderMgr(OrderMgrHelper.narrow(libraryManager));

            libraryManager = library.get_manager(PRODUCT_MGR, accessCriteria);
            setProductMgr(ProductMgrHelper.narrow(libraryManager));

            libraryManager = library.get_manager(DATA_MODEL_MGR, accessCriteria);
            setDataModelMgr(DataModelMgrHelper.narrow(libraryManager));

        } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
            LOGGER.error("{} : Unable to retrieve mandatory managers.", id, e);
        }

        if (catalogMgr != null && orderMgr != null && productMgr != null && dataModelMgr != null) {
            LOGGER.debug("{} : Initialized STANAG mandatory managers.", getId());
        } else {
            LOGGER.error("{} : Unable to initialize mandatory mangers.", getId());
        }
    }

    /**
     * Obtains all possible views that the Federated Source can provide. EX: NSIL_ALL_VIEW, NSIL_IMAGERY
     * According to ANNEX D, TABLE D-6, the passed parameter in get_view_names is an empty list(not used).
     *
     * @return an array of views
     */
    private void initServerViews() {
        View[] views = null;
        try {
            views = dataModelMgr.get_view_names(new NameValue[0]);
        } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
            LOGGER.error("{} : Unable to retrieve views.", id, e);
        }
        if (views == null) {
            LOGGER.error("{} : Unable to retrieve views.", id);
        }
        this.views = views;
    }

    /**
     * Obtains all possible attributes for all possible views that the Federated Source can provide, and
     * populates a sortableAttributes map, as well as resultAttributes map that will be used for querying
     * the server.
     * According to ANNEX D, TABLE D-6, the passed parameter in get_view_names is an empty list(not used).
     *
     * @return a map of each view and the attributes provided by the source for that view
     */
    private void initSortableAndResultAttributes() {
        if (views == null || views.length == 0) {
            return;
        }
        HashMap<String, String[]> resultAttributesMap = new HashMap<>();
        HashMap<String, List<String>> sortableAttributesMap = new HashMap<>();

        try {
            for (int i = 0; i < views.length; i++) {

                List<String> sortableAttributesList = new ArrayList<>();

                AttributeInformation[] attributeInformationArray =
                        dataModelMgr.get_attributes(views[i].view_name, new NameValue[0]);
                String[] resultAttributes = new String[attributeInformationArray.length];

                for (int c = 0; c < attributeInformationArray.length; c++) {
                    AttributeInformation attributeInformation = attributeInformationArray[c];
                    resultAttributes[c] = attributeInformation.attribute_name;

                    if (attributeInformation.sortable) {
                        sortableAttributesList.add(attributeInformation.attribute_name);
                    }

                }
                sortableAttributesMap.put(views[i].view_name, sortableAttributesList);
                resultAttributesMap.put(views[i].view_name, resultAttributes);

            }
        } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
            LOGGER.error("{} : Unable to retrieve queryable attributes.", id, e);
        }

        if (resultAttributesMap.size() == 0) {
            LOGGER.warn("{} : Received empty attributes list from STANAG source.", getId());
        }

        this.sortableAttributes = sortableAttributesMap;
        this.resultAttributes = resultAttributesMap;
    }

    /**
     * Obtains all queryable attributes for all possible views that the Federated Source can provide.
     * According to ANNEX D, TABLE D-6, the passed parameter in get_view_names is an empty list(not used).
     *
     * @return a map of each view and the queryable attributes provided by the source for that view
     */
    private void initQueryableAttributes() {
        if (views == null || views.length == 0) {
            return;
        }
        HashMap<String, List<AttributeInformation>> map = new HashMap<>();

        try {
            for (int i = 0; i < views.length; i++) {
                AttributeInformation[] attributeInformationArray =
                        dataModelMgr.get_queryable_attributes(views[i].view_name, new NameValue[0]);
                List<AttributeInformation> attributeInformationList = new ArrayList<>();
                for (int c = 0; c < attributeInformationArray.length; c++) {
                    attributeInformationList.add(attributeInformationArray[c]);
                }
                map.put(views[i].view_name, attributeInformationList);
            }
        } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
            LOGGER.error("{} : Unable to retrieve queryable attributes.", id, e);
        }

        if (map.size() == 0) {
            LOGGER.warn("{} : Received empty queryable attributes from STANAG source.", getId());
        }
        queryableAttributes = map;
    }

    /**
     * Obtains the description of the source from the Library interface.
     *
     * @return a description of the source
     */
    private void setSourceDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            LibraryDescription libraryDescription = library.get_library_description();
            stringBuilder.append(libraryDescription.library_name + " : ");
            stringBuilder.append(libraryDescription.library_description);
        } catch (ProcessingFault | SystemFault e) {
            LOGGER.error("{} : Unable to retrieve source description. {}", id, e.getMessage());
            LOGGER.debug("{} : Unable to retrieve source description.", id, e);
        }
        String description = stringBuilder.toString();
        if (StringUtils.isBlank(description)) {
            LOGGER.warn("{} :  Unable to retrieve source description.", getId());
        }
        this.description = description;
    }

    public void destroy() {
        orb.shutdown(true);
        availabilityPollFuture.cancel(true);
        scheduler.shutdownNow();
    }

    public void refresh(Map<String, Object> configuration) {
        LOGGER.debug("Entering Refresh : {}", getId());

        if (MapUtils.isEmpty(configuration)) {
            LOGGER.error("{} {} : Received null or empty configuration during refresh.",
                    this.getClass()
                            .getSimpleName(),
                    getId());
            return;
        }

        String cxfUsername = (String) configuration.get(CXF_USERNAME);
        if (StringUtils.isNotBlank(cxfUsername) && !cxfUsername.equals(this.cxfUsername)) {
            setCxfUsername(cxfUsername);
        }
        String cxfPassword = (String) configuration.get(CXF_PASSWORD);
        if (StringUtils.isNotBlank(cxfPassword) && !cxfPassword.equals(this.cxfPassword)) {
            setCxfPassword(cxfPassword);
        }
        String id = (String) configuration.get(ID);
        if (StringUtils.isNotBlank(id) && !id.equals(this.id)) {
            setId(id);
        }
        String iorUrl = (String) configuration.get(IOR_URL);
        if (StringUtils.isNotBlank(iorUrl) && !iorUrl.equals(this.iorUrl)) {
            setIorUrl(iorUrl);
        }
        Integer pollInterval = (Integer) configuration.get(POLL_INTERVAL);
        if (pollInterval != null && !pollInterval.equals(this.pollInterval)) {
            setPollInterval(pollInterval);
        }
        Integer maxHitCount = (Integer) configuration.get(MAX_HIT_COUNT);
        if (maxHitCount != null && !maxHitCount.equals(this.maxHitCount)) {
            setMaxHitCount(maxHitCount);
        }
        init();
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(describableProperties.getProperty(DESCRIPTION))
                .append(System.getProperty(System.lineSeparator()))
                .append(description);
        return sb.toString();
    }

    @Override
    public String getId() {
        String sourceId = super.getId();
        return sourceId;
    }

    @Override
    public SourceResponse query(QueryRequest queryRequest) throws UnsupportedQueryException {
        com.connexta.alliance.nsili.common.GIAS.Query query = createQuery(queryRequest.getQuery());

        String[] results = resultAttributes.get(NsiliConstants.NSIL_ALL_VIEW);

        SortAttribute[] sortAttributes = getSortAttributes(queryRequest.getQuery()
                .getSortBy());
        NameValue[] propertiesList = getDefaultPropertyList();
        LOGGER.debug("{} : Sending BQS query to source.\n Sort Attributes : {}",
                getId(),
                sortAttributes);
        return submitQuery(queryRequest, query, results, sortAttributes, propertiesList);
    }

    /**
     * Uses the NsiliFilterDelegate to create a STANAG 4559 BQS (Boolean Syntax Query) from the DDF Query
     *
     * @param query - the query recieved from the Search-Ui
     * @return - a STANAG4559 complaint query
     * @throws UnsupportedQueryException
     */
    private com.connexta.alliance.nsili.common.GIAS.Query createQuery(Query query)
            throws UnsupportedQueryException {
        String filter = createFilter(query);
        LOGGER.debug("{} : BQS Query : {}", getId(), filter);
        return new com.connexta.alliance.nsili.common.GIAS.Query(NsiliConstants.NSIL_ALL_VIEW,
                filter);
    }

    /**
     * Obtains the number of hits that the given query has received from the server.
     *
     * @param query      - a BQS query
     * @param properties - a list of properties for the query
     * @return - the hit count
     */
    private int getHitCount(com.connexta.alliance.nsili.common.GIAS.Query query,
            NameValue[] properties) {
        IntHolder intHolder = new IntHolder();
        try {
            HitCountRequest hitCountRequest = catalogMgr.hit_count(query, properties);
            hitCountRequest.complete(intHolder);
        } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
            LOGGER.error("{} : Unable to get hit count for query.", getId(), e);
        }

        LOGGER.debug("{} :  Received {} hit(s) from query.", getId(), intHolder.value);
        return intHolder.value;
    }

    /**
     * Submits and completes a BQS Query to the STANAG 4559 server and returns the response.
     *
     * @param queryRequest     - the query request generated from the search
     * @param query            - a BQS query
     * @param resultAttributes - a list of desired result attributes
     * @param sortAttributes   - a list of attributes to sort by
     * @param properties       - a list of properties for the query
     * @return - the server's response
     */
    private SourceResponse submitQuery(QueryRequest queryRequest,
            com.connexta.alliance.nsili.common.GIAS.Query query, String[] resultAttributes,
            SortAttribute[] sortAttributes, NameValue[] properties) {
        DAGListHolder dagListHolder = new DAGListHolder();

        try {
            LOGGER.debug("{} : Submit query: " + query, id);
            LOGGER.debug("{} : Requesting result attributes: " + Arrays.toString(resultAttributes),
                    id);
            LOGGER.debug("{} : Sort Attributes: " + Arrays.toString(sortAttributes), id);
            LOGGER.debug("{} : Properties: " + Arrays.toString(properties), id);
            SubmitQueryRequest submitQueryRequest = catalogMgr.submit_query(query,
                    resultAttributes,
                    sortAttributes,
                    properties);
            submitQueryRequest.set_user_info(ddfOrgName);
            submitQueryRequest.set_number_of_hits(maxHitCount);
            submitQueryRequest.complete_DAG_results(dagListHolder);
        } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
            LOGGER.error("{} : Unable to query source.", getId(), e);
        }

        List<Result> results = new ArrayList<>();
        if (dagListHolder.value != null) {
            for (DAG dag : dagListHolder.value) {
                Metacard card = DAGConverter.convertDAG(dag, getId());
                if (card != null) {
                    DAGConverter.logMetacard(card, getId());
                    results.add(new ResultImpl(card));
                } else {
                    LOGGER.warn("{} : Unable to convert DAG to metacard, returned card is null",
                            getId());
                }
            }
        } else {
            LOGGER.warn("{} : Source returned empty DAG list", getId());
        }

        SourceResponseImpl sourceResponse = new SourceResponseImpl(queryRequest,
                results,
                (long) getHitCount(query, properties));

        return sourceResponse;
    }

    private void setFilterDelegate() {
        nsiliFilterDelegate = new NsiliFilterDelegate(queryableAttributes,
                NsiliConstants.NSIL_ALL_VIEW);
    }

    @Override
    public void maskId(String newSourceId) {
        final String methodName = "maskId";
        LOGGER.debug("ENTERING: {} with sourceId = {}", methodName, newSourceId);
        if (newSourceId != null) {
            super.maskId(newSourceId);
        }
        LOGGER.debug("EXITING: {}", methodName);
    }

    @Override
    public String getOrganization() {
        return describableProperties.getProperty(ORGANIZATION);
    }

    @Override
    public String getTitle() {
        return describableProperties.getProperty(TITLE);
    }

    @Override
    public String getVersion() {
        return describableProperties.getProperty(VERSION);
    }

    @Override
    public boolean isAvailable() {
        return availabilityTask.isAvailable();
    }

    @Override
    public boolean isAvailable(SourceMonitor sourceMonitor) {
        sourceMonitors.add(sourceMonitor);
        return isAvailable();
    }

    @Override
    public Set<String> getOptions(Metacard arg0) {
        return null;
    }

    @Override
    public Set<String> getSupportedSchemes() {
        return null;
    }

    @Override
    public ResourceResponse retrieveResource(URI resourceUri,
            Map<String, Serializable> requestProperties)
            throws IOException, ResourceNotFoundException, ResourceNotSupportedException {
        LOGGER.debug("{}, {}, {}, {}",
                resourceUri.getHost(),
                resourceUri.getPath(),
                resourceUri.getPort(),
                requestProperties.toString());
        return resourceReader.retrieveResource(resourceUri, requestProperties);
    }

    @Override
    public String getConfigurationPid() {
        return configurationPid;
    }

    @Override
    public void setConfigurationPid(String configurationPid) {
        this.configurationPid = configurationPid;
    }

    @Override
    public Set<ContentType> getContentTypes() {
        return contentTypes;
    }

    private String createFilter(Query query) throws UnsupportedQueryException {
        return this.filterAdapter.adapt(query, nsiliFilterDelegate);
    }

    public void setCxfUsername(String cxfUsername) {
        this.cxfUsername = cxfUsername;
    }

    public void setCxfPassword(String cxfPassword) {
        this.cxfPassword = cxfPassword;
    }

    public void setId(String id) {
        this.id = id;
        super.setId(id);
    }

    public void setIorUrl(String iorUrl) {
        this.iorUrl = iorUrl;
    }

    public void setMaxHitCount(Integer maxHitCount) {
        this.maxHitCount = maxHitCount;
    }

    public String getIorUrl() {
        return iorUrl;
    }

    public String getCxfPassword() {
        return cxfPassword;
    }

    public String getCxfUsername() {
        return cxfUsername;
    }

    public Integer getMaxHitCount() {
        return maxHitCount;
    }

    public Integer getPollInterval() {
        return pollInterval;
    }

    public void setResourceReader(ResourceReader resourceReader) {
        this.resourceReader = resourceReader;
    }

    public void setPollInterval(Integer interval) {
        this.pollInterval = interval;
    }

    public void setFilterAdapter(FilterAdapter filterAdapter) {
        this.filterAdapter = filterAdapter;
    }

    public void setCatalogMgr(CatalogMgr catalogMgr) {
        this.catalogMgr = catalogMgr;
    }

    public void setOrderMgr(OrderMgr orderMgr) {
        this.orderMgr = orderMgr;
    }

    public void setDataModelMgr(DataModelMgr dataModelMgr) {
        this.dataModelMgr = dataModelMgr;
    }

    public void setProductMgr(ProductMgr productMgr) {
        this.productMgr = productMgr;
    }

    public void setSortableAttributes(HashMap<String, List<String>> sortableAttributes) {
        this.sortableAttributes = sortableAttributes;
    }

    public void setAvailabilityTask(AvailabilityTask availabilityTask) {
        this.availabilityTask = availabilityTask;
    }

    public void setupAvailabilityPoll() {
        LOGGER.debug("Setting Availability poll task for {} minute(s) on Source {}",
                getPollInterval(),
                getId());
        Stanag4559AvailabilityCommand command = new Stanag4559AvailabilityCommand();
        long interval = TimeUnit.MINUTES.toMillis(getPollInterval());
        if (availabilityPollFuture == null || availabilityPollFuture.isCancelled()) {
            if (availabilityTask == null) {
                availabilityTask = new AvailabilityTask(interval, command, getId());
            } else {
                availabilityTask.setInterval(interval);
            }

            // Run the availability check immediately prior to scheduling it in a thread.
            // This is necessary to allow the catalog framework to have the correct
            // availability when the source is bound
            availabilityTask.run();
            // Schedule the availability check every 1 second. The actually call to
            // the remote server will only occur if the pollInterval has
            // elapsed.
            availabilityPollFuture = scheduler.scheduleWithFixedDelay(availabilityTask,
                    AvailabilityTask.NO_DELAY,
                    AvailabilityTask.ONE_SECOND,
                    TimeUnit.SECONDS);
        } else {
            LOGGER.debug("No changes being made on the poller.");
        }

    }

    private void availabilityChanged(boolean isAvailable) {

        if (isAvailable) {
            LOGGER.info("STANAG 4559 source {} is available.", getId());
        } else {
            LOGGER.info("STANAG 4559 source {} is unavailable.", getId());
        }

        for (SourceMonitor monitor : this.sourceMonitors) {
            if (isAvailable) {
                LOGGER.debug("Notifying source monitor that STANAG 4559 source {} is available.",
                        getId());
                monitor.setAvailable();
            } else {
                LOGGER.debug("Notifying source monitor that STANAG 4559 source {} is unavailable.",
                        getId());
                monitor.setUnavailable();
            }
        }
    }

    /**
     * Returns the Default Property List defined in the STANAG 4559 specification.
     *
     * @return - default WGS84 Geographic Datum.
     */
    private NameValue[] getDefaultPropertyList() {
        Any defaultAnyProperty = orb.create_any();
        defaultAnyProperty.insert_string(WGS84);
        NameValue[] result = {new NameValue(GEOGRAPHIC_DATUM, defaultAnyProperty)};
        return result;
    }

    /**
     * Sets a SortAttribute[] to be used in a query.  The STANAG 4559 Spec has no mechanism to sort
     * queries by RELEVANCE or Shortest/Longest distance from a point, so they are ignored.
     *
     * @param sortBy - sortBy object specified in the Search UI
     * @return - an array of SortAttributes sent in the query to the source.
     */
    private SortAttribute[] getSortAttributes(SortBy sortBy) {
        if (sortBy == null || sortableAttributes == null) {
            //Default to sorting by Date/Time modified if no sorting provided
            return new SortAttribute[] {new SortAttribute(
                    NsiliConstants.NSIL_CARD + "." + NsiliConstants.DATE_TIME_MODIFIED,
                    Polarity.DESCENDING)};
        }

        String sortAttribute = sortBy.getPropertyName()
                .getPropertyName();
        Polarity sortPolarity;

        if (sortBy.getSortOrder()
                .toSQL()
                .equals(ASC)) {
            sortPolarity = Polarity.ASCENDING;
        } else {
            sortPolarity = Polarity.DESCENDING;
        }

        if (sortAttribute.equals(Metacard.MODIFIED)
                && isAttributeSupported(NsiliConstants.DATE_TIME_MODIFIED)) {
            SortAttribute[] sortAttributeArray =
                    {new SortAttribute(NsiliConstants.DATE_TIME_MODIFIED, sortPolarity)};
            return sortAttributeArray;
        } else if (sortAttribute.equals(Metacard.CREATED)
                && isAttributeSupported(NsiliConstants.DATE_TIME_DECLARED)) {
            SortAttribute[] sortAttributeArray =
                    {new SortAttribute(NsiliConstants.DATE_TIME_DECLARED, sortPolarity)};
            return sortAttributeArray;
        } else {
            return new SortAttribute[0];
        }
    }

    /**
     * Verifies that a given attribute exists in the list of sortableAttributes for NSIL_ALL_VIEW
     */
    private boolean isAttributeSupported(String attribute) {
        List<String> attributeInformationList =
                sortableAttributes.get(NsiliConstants.NSIL_ALL_VIEW);
        for (String sortableAttribute : attributeInformationList) {
            if (attribute.equals(sortableAttribute)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Callback class to check the Availability of the NsiliSource.
     * <p>
     * NOTE: Ideally, the framework would call isAvailable on the Source and the SourcePoller would
     * have an AvailabilityTask that cached each Source's availability. Until that is done, allow
     * the command to handle the logic of managing availability.
     */
    private class Stanag4559AvailabilityCommand implements AvailabilityCommand {

        @Override
        public boolean isAvailable() {
            LOGGER.debug("Checking availability for source {} ", getId());
            boolean oldAvailability = NsiliSource.this.isAvailable();
            String[] managers = null;

            // Refresh IOR String when polling for availability in case server conditions change
            try {
                getIorString();
                initLibrary();
                managers = library.get_manager_types();
            } catch (Exception e) {
                LOGGER.error("{} : Connection Failure for source. {}", getId(), e.getMessage());
                LOGGER.debug("{} : Connection Failure for source.", getId(), e);
            }

            // If the IOR string is not valid, or the source cannot communicate with the library, the
            // source is unavailable
            boolean newAvailability = (managers != null && StringUtils.isNotBlank(iorString));
            if (oldAvailability != newAvailability) {
                availabilityChanged(newAvailability);
                // If the source becomes available, configure it.
                if (newAvailability) {
                    initCorbaClient();
                }
            }
            return newAvailability;
        }
    }
}