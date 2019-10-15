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
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractDestination;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import scala.concurrent.Promise;

import java.util.logging.Logger;

public class PlayDestination extends AbstractDestination {
  private static final Logger LOG = LogUtils.getL7dLogger(PlayDestination.class);

  public static final String PLAY_MESSAGE_PROMISE = "PLAY.MESSAGE.PROMISE";

  private final String factoryKey;
  private PlayTransportFactory destinationFactory;

  PlayDestination(
    PlayTransportFactory destinationFactory,
    String factoryKey,
    EndpointReferenceType epr,
    EndpointInfo ei,
    Bus bus
  ) {
    super(bus, epr, ei);
    this.factoryKey = factoryKey;
    this.destinationFactory = destinationFactory;
  }

  public void dispatchMessage(Message inMessage) {
    ExchangeImpl ex = new ExchangeImpl();
    ex.setDestination(this);
    ex.setInMessage(inMessage);
    inMessage.setExchange(ex);
    getMessageObserver().onMessage(inMessage);
  }

  @Override
  protected Conduit getInbuiltBackChannel(Message inMessage) {
    @SuppressWarnings("unchecked")
    Promise<Message> messagePromise = (Promise<Message>) inMessage.get(PLAY_MESSAGE_PROMISE);
    inMessage.remove(PLAY_MESSAGE_PROMISE);

    return new PlayBackChannelConduit(this, messagePromise);
  }

  @Override
  public void shutdown() {
    destinationFactory.remove(this);
    super.shutdown();
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @SuppressWarnings("WeakerAccess")
  public String getFactoryKey() {
    return factoryKey;
  }
}
