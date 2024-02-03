/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author miklish Michael N. Christoff
 *
 * testing / QA
 *   AkashWorkGit
 *   jaydeepparekh1311
 */
package com.networknt.client.simplepool.mock;

import com.networknt.client.simplepool.SimpleConnection;
import com.networknt.client.simplepool.SimpleConnectionMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestConnectionMaker implements SimpleConnectionMaker {
    private static final Logger logger = LoggerFactory.getLogger(TestConnectionMaker.class);
    private Class connectionClass;
    private final ThreadPoolExecutor executor;

    public TestConnectionMaker(Class clas, ThreadPoolExecutor executor) {
        this.connectionClass = clas;
        this.executor = executor;
    }

    @Override
    public SimpleConnection makeConnection(long createConnectionTimeout, boolean isHttp2, URI uri, Set<SimpleConnection> allConnections)
            throws RuntimeException
    {
        SimpleConnection connection = instantiateConnection(createConnectionTimeout, isHttp2, allConnections);
        return connection;
    }

    private SimpleConnection instantiateConnection(long createConnectionTimeout, final boolean isHttp2, final Set<SimpleConnection> allConnections)
            throws RuntimeException
    {
        Future<SimpleConnection> future = executor.submit(() -> {

            Constructor<SimpleConnection> constructor = connectionClass.getConstructor(boolean.class);
            SimpleConnection simpleConnection = constructor.newInstance(isHttp2);

            allConnections.add(simpleConnection);
            logger.debug("allCreatedConnections: {}", logAllConnections(allConnections));

            return simpleConnection;
        });
        SimpleConnection connection;
        try {
            connection = future.get(createConnectionTimeout, TimeUnit.SECONDS);
        } catch(Exception e) {
            throw new RuntimeException("Connection creation timed-out");
        }
        return connection;
    }


    /***
     * For logging
     */
    private String logAllConnections(final Set<SimpleConnection> allConnections) {
        StringBuilder consList = new StringBuilder();
        consList.append("[ ");
        for(SimpleConnection connection: allConnections)
            consList.append(port(connection)).append(" ");
        consList.append("]:");
        return consList.toString();
    }

    /***
     * For logging
     */
    private static String port(SimpleConnection connection) {
        if(connection == null) return "NULL";
        String url = connection.getLocalAddress();
        int semiColon = url.lastIndexOf(":");
        if(semiColon == - 1) return "PORT?";
        return url.substring(url.lastIndexOf(":")+1);
    }
}
