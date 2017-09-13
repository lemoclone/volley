/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2015 Circle Internet Financial
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.HttpStack;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;


public class OkHttpStack implements HttpStack {
    public final OkHttpClient okHttpClient;

    public OkHttpStack(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public HttpResponse performRequest(com.android.volley.Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {

        Headers headerbuild = Headers.of(additionalHeaders).of(request.getHeaders());

        MediaType mediaType = MediaType.parse(request.getBodyContentType());

        RequestBody requestBody = RequestBody.create(mediaType, request.getBody());

        Request okRequest = new Request.Builder().url(request.getUrl()).headers(headerbuild).post(requestBody).
                build();
        Response okResponse = okHttpClient.newCall(okRequest).execute();

        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        StatusLine responseStatus = new BasicStatusLine(protocolVersion,
                okResponse.code(), okResponse.message());
        BasicHttpResponse volleyResponse = new BasicHttpResponse(responseStatus);
        volleyResponse.setEntity(entityFromConnection(okResponse));

        for (String name : okResponse.headers().names()) {
            Header h = new BasicHeader(name, okResponse.headers().get(name));
            volleyResponse.addHeader(h);
        }
        return volleyResponse;
    }

    /**
     * Initializes an {@link HttpEntity} from the given {@link HttpURLConnection}.
     *
     * @param response
     * @return an HttpEntity populated with data from <code>connection</code>.
     */
    private static HttpEntity entityFromConnection(Response response) {
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream;
        try {
            inputStream = response.body().byteStream();
            entity.setContent(inputStream);
            entity.setContentLength(response.body().contentLength());
            entity.setContentEncoding("gzip");
            entity.setContentEncoding(response.body().contentType().type());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entity;
    }
}