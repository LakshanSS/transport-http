/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.util;

import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.Parameter;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.wso2.transport.http.netty.contract.Constants.HTTPS_SCHEME;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_2_0;
import static org.wso2.transport.http.netty.contract.Constants.OPTIONAL;

/**
 * Utilities required for HTTP/2 test cases.
 *
 * @since 6.0.273
 */
public class Http2Util {

    public static ListenerConfiguration getH2ListenerConfigs() {
        Parameter paramServerCiphers = new Parameter("ciphers", "TLS_RSA_WITH_AES_128_CBC_SHA");
        List<Parameter> serverParams = new ArrayList<>(1);
        serverParams.add(paramServerCiphers);
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setParameters(serverParams);
        listenerConfiguration.setPort(TestUtil.SERVER_PORT1);
        listenerConfiguration.setScheme(HTTPS_SCHEME);
        listenerConfiguration.setVersion(String.valueOf(HTTP_2_0));
        listenerConfiguration.setVerifyClient(OPTIONAL);
        listenerConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        listenerConfiguration.setKeyStorePass(TestUtil.KEY_STORE_PASSWORD);
        return listenerConfiguration;
    }

    public static SenderConfiguration getSenderConfigs(String httpVersion) {
        Parameter paramClientCiphers = new Parameter("ciphers", "TLS_RSA_WITH_AES_128_CBC_SHA");
        List<Parameter> clientParams = new ArrayList<>(1);
        clientParams.add(paramClientCiphers);
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setParameters(clientParams);
        senderConfiguration.setTrustStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        senderConfiguration.setTrustStorePass(TestUtil.KEY_STORE_PASSWORD);
        senderConfiguration.setHttpVersion(httpVersion);
        senderConfiguration.setScheme(HTTPS_SCHEME);
        return senderConfiguration;
    }

    /**
     * Get the test client. Each test client has their own connection manager and does not use source pools.
     *
     * @param withPriorKnowledge a boolean indicating whether the prior knowledge support is expected
     * @return HttpClientConnector
     */
    public static HttpClientConnector getTestClient(HttpWsConnectorFactory httpWsConnectorFactory,
                                                    boolean withPriorKnowledge) {
        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        SenderConfiguration senderConfiguration = HttpConnectorUtil.getSenderConfiguration(transportsConfiguration,
                                                                                           Constants.HTTP_SCHEME);
        senderConfiguration.setHttpVersion(String.valueOf(Constants.HTTP_2_0));
        if (withPriorKnowledge) {
            senderConfiguration.setForceHttp2(true);       // Force to use HTTP/2 without an upgrade
        }
        return httpWsConnectorFactory.createHttpClientConnector(
            HttpConnectorUtil.getTransportProperties(transportsConfiguration), senderConfiguration);
    }

    public static void assertResult(String response1, String response2, String response3, String response4) {
        assertNotEquals(response1, response2,
                        "Client uses two different pools, hence response 1 and 2 should not be equal");
        assertNotEquals(response3, response4,
                        "Client uses two different pools, hence response 3 and 4 should not be equal");
        assertEquals(response1, response3, "Client uses the same pool, hence response 1 and 3 should be equal");
        assertEquals(response2, response4, "Client uses the same pool, hence response 2 and 4 should be equal");
    }
}
