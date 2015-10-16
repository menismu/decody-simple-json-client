/*
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
 */

package com.decody.android.core.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.util.Log;

import com.decody.android.core.json.exceptions.ResourceNotFoundException;
import com.decody.android.core.json.exceptions.UnauthorizedException;
import com.decody.android.core.json.integration.DefaultGsonFactory;
import com.decody.android.core.json.integration.GsonFactory;
import com.google.gson.JsonSyntaxException;

/**
 * JSON Client implementation using Gson library as data serialization /
 * deserialization.
 */
public class JSONClient implements Client {

	private static String TAG = "JSONClient";
	
	private static String CONTENT_TYPE = "application/json";

	protected DefaultHttpClient client;

	protected GsonFactory factory;

	public JSONClient() {
		this(new DefaultGsonFactory(), false);
	}

	public JSONClient(GsonFactory factory) {
		this(factory, false);
	}

	public JSONClient(boolean https) {
		this(new DefaultGsonFactory(), https);
	}

	public JSONClient(GsonFactory factory, boolean https) {
		this.factory = factory;

		if (https) {
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			DefaultHttpClient client = new DefaultHttpClient();

			SchemeRegistry registry = new SchemeRegistry();
			SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
			socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
			registry.register(new Scheme("https", socketFactory, 443));
			SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);

			this.client = new DefaultHttpClient(mgr, client.getParams());
		} else {
			client = new DefaultHttpClient();
		}
	}

	public <T> T get(String url, Class<T> classOfT) throws ResourceNotFoundException, UnauthorizedException {
		return performCall(new HttpGet(url), classOfT);
	}

	public <T> T post(String url, Class<T> classOfT, T data) throws ResourceNotFoundException, UnauthorizedException {
		HttpPost post = new HttpPost(url);
		post.addHeader("Content-Type", CONTENT_TYPE);

		try {
			post.setEntity(new StringEntity(factory.newInstance().toJson(data)));
		} catch (UnsupportedEncodingException e1) {
			throw new IllegalArgumentException(
					"input data is not valid, check the input data to call the post service: " + url + " - "
							+ classOfT.toString());
		}

		return performCall(post, classOfT);
	}

	public <T> T put(String url, Class<T> classOfT, T data) throws ResourceNotFoundException, UnauthorizedException {
		HttpPut put = new HttpPut(url);
		put.addHeader("Content-Type", CONTENT_TYPE);

		try {
			put.setEntity(new StringEntity(factory.newInstance().toJson(data)));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(
					"input data is not valid, check the input data to call the post service: " + url + " - "
							+ classOfT.toString());
		}

		return performCall(put, classOfT);
	}
	
	public <T> T delete(String url, Class<T> classOfT) throws ResourceNotFoundException, UnauthorizedException {
		return performCall(new HttpDelete(url), classOfT);
	}

	private <T> T performCall(HttpRequestBase request, Class<T> classOfT) throws ResourceNotFoundException,
			UnauthorizedException {
		String url = request.getURI().toString();

		T toReturn = null;

		Log.i(TAG, "Calling to get the resource in the url: " + url);

		try {
			HttpResponse response = client.execute(request);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				Log.w(TAG, "Resource not found, throwing an exception");

				throw new ResourceNotFoundException("Resource not found in " + url);
			} else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				Log.w(TAG, "Not authorized to get the given resource");

				throw new UnauthorizedException("Your user is not authorized to access to the resource: " + url);
			} else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				Log.w(TAG, "Bad request from the server");

				throw new UnauthorizedException("Bad request to the resource: " + url);
			}

			HttpEntity entity = response.getEntity();
			Reader reader;
			reader = new InputStreamReader(entity.getContent());

			toReturn = (T) factory.newInstance().fromJson(reader, classOfT);
		} catch (IllegalStateException e) {
			Log.e(TAG, "Unexpected illegal state reading the content from the given resource: " + e.toString());

			throw new ResourceNotFoundException("Unexpected illegal state reading the given resource: " + e.toString());
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Unexpected communication exception: " + e.toString());

			throw new ResourceNotFoundException("Unexpected communication problem with the given resource");
		} catch (IOException e) {
			Log.e(TAG, "Unexpected communication reading exception: " + e.toString());

			throw new ResourceNotFoundException("Unexpected communication reading problem with the given resource");
		} catch (JsonSyntaxException e) {
			Log.e(TAG, "Unexpected response received from the server: " + e.toString());

			throw new ResourceNotFoundException("Unexpected response received");
		}

		return toReturn;
	}
}
