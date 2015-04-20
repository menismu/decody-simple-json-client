/*
 * Copyright (C) 2015 Decody Software.
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

import com.decody.android.core.json.exceptions.ResourceNotFoundException;
import com.decody.android.core.json.exceptions.UnauthorizedException;

/**
 * The Client defines the operations to be implemented by a client class.
 */
public interface Client {

	/**
	 * Perform a GET call to the remote url.
	 * 
	 * @param url
	 *            Remote url which do the call.
	 * @param classOfT
	 *            Class to consider for the communication.
	 * @return The response received from the remote server.
	 * @throws ResourceNotFoundException
	 * @throws UnauthorizedException
	 */
	<T> T get(String url, Class<T> classOfT) throws ResourceNotFoundException, UnauthorizedException;

	/**
	 * Perform a POST call to the remote url.
	 * 
	 * @param url
	 *            Remote url which do the call.
	 * @param classOfT
	 *            Class to consider for the communication.
	 * @param data
	 *            Data to send over the post call.
	 * @return The response received from the remote server.
	 * @throws ResourceNotFoundException
	 * @throws UnauthorizedException
	 */
	<T> T post(String url, Class<T> classOfT, T data) throws ResourceNotFoundException, UnauthorizedException;
}
