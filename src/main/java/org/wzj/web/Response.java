/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wzj.web;

import java.io.File;


/**
 * Created by wens on 15-5-15.
 */
public interface Response {

    void setStatus(int statusCode, String reasonPhrase);

    void setContentType(String contentType);

    void setContentLength(long contentLength);

    void writeBody(String body);

    void writeBody(byte[] body);

    void writeFile(File file);

    void redirect(String location);

    void redirect(String location, int httpStatusCode);

    void setHeader(String header, String value);

    void sendError(int code, String reasonPhrase);


}
