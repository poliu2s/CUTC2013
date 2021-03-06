/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.nio.integration;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.BufferingNHttpEntity;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.SimpleNHttpRequestHandler;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

@Deprecated
final class RequestHandler extends SimpleNHttpRequestHandler implements HttpRequestHandler {

    private final boolean chunking;

    RequestHandler() {
        this(false);
    }

    RequestHandler(boolean chunking) {
        super();
        this.chunking = chunking;
    }

    public ConsumingNHttpEntity entityRequest(
            final HttpEntityEnclosingRequest request,
            final HttpContext context) {
        return new BufferingNHttpEntity(request.getEntity(), new HeapByteBufferAllocator());
    }

    @Override
    public void handle(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {

        String content = null;
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            if (entity != null) {
                content = EntityUtils.toString(entity);
            } else {
                response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                content = "Request entity not avaialble";
            }
        } else {
            String s = request.getRequestLine().getUri();
            int idx = s.indexOf('x');
            if (idx == -1) {
                throw new HttpException("Unexpected request-URI format");
            }
            String pattern = s.substring(0, idx);
            int count = Integer.parseInt(s.substring(idx + 1, s.length()));

            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < count; i++) {
                buffer.append(pattern);
            }
            content = buffer.toString();
        }
        NStringEntity entity = new NStringEntity(content, ContentType.DEFAULT_TEXT);
        entity.setChunked(this.chunking);
        response.setEntity(entity);
    }

}
