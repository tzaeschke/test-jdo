/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.datanucleus.test;

import org.junit.Test;
import tck.pc.PCRectAnnotated;

/**
 * <B>Title:</B>PointAttributeConverterTest <br>
 * <B>Keywords:</B> mapping <br>
 * <B>Assertion ID:</B> [not identified] <br>
 * <B>Assertion Description: </B> A IPCRect instance refers two Point instances, that are stored as
 * strings in the datastore. A Point instance is converted using an AttributeConverter.
 */
public class StringAnnotatedTest extends AnnotationTestHelper {

    /**
     * Test method creating and storing a PCRectStringAnnotated instance.
     */
    @Test
    public void testStorePCRectStringAnnotatedInstance() {
        runStoreIPCRectInstance(PCRectAnnotated.class);
    }

    /**
     * Test method reading a PCRectStringAnnotated instance from the datastore.
     */
    @Test
    public void testReadPCRectStringAnnotatedInstance() {
        runReadIPCRectInstance(PCRectAnnotated.class);
    }

    /**
     * Test method modifying a PCRectStringAnnotated instance and storing in the datastore.
     */
    @Test
    public void testModifyPCRectStringAnnotatedInstance() {
        runModifyIPCRectInstance(PCRectAnnotated.class);
    }

    /**
     * Test method running a PCRectStringAnnotated query with a query parameter of type String.
     */
    @Test
    public void testPCRectStringAnnotatedQueryWithPointParam() {
        runQueryWithPointParameter(PCRectAnnotated.class, false);
    }

    /**
     * Test method running a PCRectStringAnnotated query with a query parameter of type Point.
     */
    @Test
    public void testPCRectStringAnnotatedQueryWithStringParam() throws Exception {
        runQueryWithStringParameter(PCRectAnnotated.class);
    }
}
