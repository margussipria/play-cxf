/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
 */

package org.apache.cxf.transport.play;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.*;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.http.AddressType;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PlayTransportFactory
  extends AbstractTransportFactory
  implements DestinationFactory, ConduitInitiator {

  public static final String TRANSPORT_ID = "http://cxf.apache.org/transports/play";
  public static final List<String> DEFAULT_NAMESPACES = Collections.singletonList(TRANSPORT_ID);

  private static final Set<String> URI_PREFIXES = new HashSet<>();

  static {
    URI_PREFIXES.add("play://");
  }

  private static final String NULL_ADDRESS = PlayTransportFactory.class.getName() + ".nulladdress";
  private final ConcurrentMap<String, PlayDestination> destinations = new ConcurrentHashMap<>();

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock r = lock.readLock();

  public PlayTransportFactory() {
    super(DEFAULT_NAMESPACES);
  }

  @Override
  public Destination getDestination(EndpointInfo ei, Bus bus) throws IOException {
    return getDestination(ei, createReference(ei), bus);
  }

  private EndpointReferenceType createReference(EndpointInfo ei) {
    EndpointReferenceType epr = new EndpointReferenceType();
    AttributedURIType address = new AttributedURIType();
    address.setValue(ei.getAddress());
    epr.setAddress(address);
    return epr;
  }

  private PlayDestination getDestination(EndpointInfo ei, EndpointReferenceType reference, Bus bus) throws IOException {
    r.lock();
    try {
      synchronized (destinations) {
        final String factoryKey = computeFactoryKey(ei, reference);
        PlayDestination d = destinations.get(factoryKey);
        if (d == null) {
          d = new PlayDestination(this, factoryKey, reference, ei, bus);
          PlayDestination tmpD = destinations.putIfAbsent(factoryKey, d);
          if (tmpD != null) {
            d = tmpD;
          }
        }

        return d;
      }
    } finally {
      r.unlock();
    }
  }

  public PlayDestination getDestination(String endpointAddress) {
    return destinations.get(endpointAddress);
  }

  private static String computeFactoryKey(EndpointInfo ei, EndpointReferenceType reference) {
    String addr = reference.getAddress().getValue();

    if (addr == null) {
      AddressType tp = ei.getExtensor(AddressType.class);
      if (tp != null) {
        addr = tp.getLocation();
      }
    }

    if (addr == null) {
      addr = NULL_ADDRESS;
    }

    return addr;
  }

  void remove(PlayDestination destination) {
    destinations.remove(destination.getFactoryKey(), destination);
  }

  public String getDestinationsDebugInfo() {
    StringBuilder debugInfo = new StringBuilder("Available destinations: [");
    boolean first = true;
    for (String destKey : destinations.keySet()) {
      if (first) {
        first = false;
      } else {
        debugInfo.append(", ");
      }
      debugInfo.append(destKey);
    }
    debugInfo.append("]");
    return debugInfo.toString();
  }

  @Override
  public Conduit getConduit(EndpointInfo targetInfo, Bus bus) throws IOException {
    throw new UnsupportedOperationException("Play! Transport doesn't support client operation mode!");
  }

  @Override
  public Conduit getConduit(EndpointInfo localInfo, EndpointReferenceType target, Bus bus) throws IOException {
    throw new UnsupportedOperationException("Play! Transport doesn't support client operation mode!");
  }
}
