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

package org.codice.alliance.nsili.orb.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.codice.alliance.nsili.orb.api.CorbaOrb;
import org.codice.alliance.nsili.orb.api.CorbaServiceListener;
import org.omg.CORBA.ORB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.util.impl.MaskableImpl;

public class CorbaOrbImpl extends MaskableImpl implements CorbaOrb {

    public static final int DEFAULT_CORBA_PORT = 2809;

    public static final String CORBA_TIMEOUT = "corbaTimeout";

    public static final String CORBA_PORT = "corbaPort";

    private static final Logger LOGGER = LoggerFactory.getLogger(CorbaOrbImpl.class);

    private int corbaPort = DEFAULT_CORBA_PORT;

    private int corbaTimeout;

    private ORB orb = null;

    private Thread orbRunThread = null;

    private Set<CorbaServiceListener> corbaServiceListeners = new HashSet<>(5);

    public int getCorbaPort() {
        return corbaPort;
    }

    public void setCorbaPort(int corbaPort) {
        if (this.corbaPort != corbaPort) {
            this.corbaPort = corbaPort;
        }
    }

    @Override
    public ORB getOrb() {
        return orb;
    }

    public void setCorbaTimeout(int corbaTimeout) {
        this.corbaTimeout = corbaTimeout;
        System.setProperty("com.sun.CORBA.transport.ORBTCPRadTimeouts", getCorbaWaitTime());
    }

    @Override
    public void shutdown() {
        if (orb != null) {
            for (CorbaServiceListener listener : corbaServiceListeners) {
                listener.corbaShutdown();
            }

            LOGGER.debug("Stopping ORB on port: {}", corbaPort);
            orb.shutdown(true);
            orb.destroy();
        }

        orbRunThread = null;
        orb = null;
    }


    public void refresh(Map<String, Object> configuration) {
        if (MapUtils.isEmpty(configuration)) {
            LOGGER.error("Received null or empty configuration during refresh.");
            return;
        }

        Integer corbaTimeout = (Integer) configuration.get(CORBA_TIMEOUT);
        if (corbaTimeout != null && corbaTimeout != this.corbaTimeout) {
            setCorbaTimeout(corbaTimeout);
        }

        Integer corbaPort = (Integer) configuration.get(CORBA_PORT);
        if (corbaPort != null && corbaPort != this.corbaPort) {
            setCorbaPort(corbaPort);
        }

        init();
    }

    /**
     * Initializes the Corba ORB with no additional arguments
     */
    public void init() {
        shutdown();

        System.setProperty("com.sun.CORBA.POA.ORBPersistentServerPort", String.valueOf(corbaPort));
        System.setProperty("com.sun.CORBA.ORBServerPort", String.valueOf(corbaPort));
        System.setProperty("com.sun.CORBA.transport.ORBTCPReadTimeouts", getCorbaWaitTime());

        orb = org.omg.CORBA.ORB.init(new String[0], null);
        if (orb != null) {
            LOGGER.debug("Successfully initialized CORBA orb on port: {}", corbaPort);
        } else {
            LOGGER.error("Unable to initialize CORBA orb on port: {}", corbaPort);
        }

        orbRunThread = new Thread(() -> orb.run());
        orbRunThread.start();

        System.clearProperty("com.sun.CORBA.POA.ORBPersistentServerPort");
        System.clearProperty("com.sun.CORBA.ORBServerPort");
        System.clearProperty("com.sun.CORBA.transport.ORBTCPReadTimeouts");

        for (CorbaServiceListener listener : corbaServiceListeners) {
            listener.corbaInitialized();
        }
    }

    @Override
    public void addCorbaServiceListener(CorbaServiceListener corbaServiceListener) {
        corbaServiceListeners.add(corbaServiceListener);
    }

    @Override
    public void removeCorbaServiceListener(CorbaServiceListener corbaServiceListener) {
        corbaServiceListeners.remove(corbaServiceListener);
    }

    private String getCorbaWaitTime() {
        long waitTimeMillis = corbaTimeout * 1000;
        return "1:" + waitTimeMillis + ":" + waitTimeMillis + ":" + 1;
    }
}