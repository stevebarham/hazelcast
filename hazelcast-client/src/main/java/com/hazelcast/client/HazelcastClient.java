/*
 * Copyright (c) 2007-2008, Hazel Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hazelcast.client;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.hazelcast.client.core.IMap;
import com.hazelcast.client.core.Transaction;
import com.hazelcast.client.impl.ListenerManager;

public class HazelcastClient {
	final Map<Long,Call> calls  = new HashMap<Long, Call>();
	ListenerManager listenerManager = new ListenerManager();
	OutRunnable out;
	InRunnable in;
	private ConnectionManager connectionManager;
	
	private HazelcastClient(InetSocketAddress[] clusterMembers) {
		connectionManager = new ConnectionManager(clusterMembers);
		
		
		out = new OutRunnable(new PacketWriter());
		out.setCallMap(calls);
		out.setConnectionManager(connectionManager);
		new Thread(out).start();
		
		in = new InRunnable(listenerManager, new PacketReader());
		in.setCallMap(calls);
		in.setConnectionManager(connectionManager);
		new Thread(in).start();
		
		connectionManager.setOutRunnable(out);

		new Thread(listenerManager).start();
	}

	public static HazelcastClient getHazelcastClient(InetSocketAddress... clusterMembers){
		return new HazelcastClient(clusterMembers);
	}
	
	
	

	public <K, V> IMap<K,V> getMap(String name){
		MapClientProxy<K, V> proxy = new MapClientProxy<K, V>(this,name);
		proxy.setOutRunnable(out);
		return proxy;
	}


	public Transaction getTransaction() {
		TransactionClientProxy proxy = new TransactionClientProxy();
		proxy.setOutRunnable(out);
		return proxy;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}
}
