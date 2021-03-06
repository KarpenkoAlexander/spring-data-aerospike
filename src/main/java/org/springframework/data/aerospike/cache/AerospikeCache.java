/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.aerospike.cache;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;

/**
 * 
 * @author Venil Noronha
 */
//TODO: extract this to the separate repository aerospike-spring-cache
public class AerospikeCache implements Cache {

	private static final String VALUE = "value";

	protected AerospikeClient client;
	protected String namespace;
	protected String set;
	protected WritePolicy createOnly;

	public AerospikeCache(String namespace, String set, AerospikeClient client,
			long expiration){
		this.client = client;
		this.namespace = namespace;
		this.set = set;
		this.createOnly = new WritePolicy(client.writePolicyDefault);
		this.createOnly.recordExistsAction = RecordExistsAction.CREATE_ONLY;
	}

	protected Key getKey(Object key){
		return new Key(namespace, set, key.toString());
	}

	private ValueWrapper toWrapper(Record record) {
		return (record != null ? new SimpleValueWrapper(record.getValue(VALUE)) : null);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
	}

	@Override
	public void evict(Object key) {
		this.client.delete(null, getKey(key));

	}

	@Override
	public ValueWrapper get(Object key) {
		Record record =  client.get(null, getKey(key));
		ValueWrapper vr = toWrapper(record);
		return vr;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object key, Class<T> type) {
		return (T) client.get(null, getKey(key));
	}

	@Override
	public String getName() {
		return this.namespace+":"+this.set;
	}

	@Override
	public Object getNativeCache() {
		return client;
	}

	@Override
	public void put(Object key, Object value) {
		client.put(null, getKey(key), new Bin(VALUE, value));
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		Record record = client.operate(this.createOnly, getKey(key), Operation.put(new Bin(VALUE, value)), Operation.get(VALUE));
		return toWrapper(record);
	}

	@Override
	public <T> T get(Object key, Callable<T> valueLoader) {
		// TODO Auto-generated method stub
		return null;
	}


}
