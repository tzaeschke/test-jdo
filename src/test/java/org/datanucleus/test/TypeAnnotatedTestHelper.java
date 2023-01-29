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
import tck.pc.PCRectPointTypeAnnotated;

/**
 * Note: This file mostly is copied from PointAttributeConverterTest.java here:
 * https://github.com/apache/db-jdo/blob/main/tck/src/main/java/org/apache/jdo/tck/api/converter/PointAttributeConverterTest.java
 */
public class TypeAnnotatedTestHelper extends AnnotationTestHelper {

    /**
     * Test method creating and storing a PCRectStringAnnotated instance.
     */
    @Test
    public void testStorePCRectPointTypeAnnotatedInstance() {
        runStoreIPCRectInstance(PCRectPointTypeAnnotated.class);
    }

    /**
     * Test method reading a PCRectStringAnnotated instance from the datastore.
     */
    @Test
    public void testReadPCRectPointTypeAnnotatedInstance() {
        runReadIPCRectInstance(PCRectPointTypeAnnotated.class);
    }

    /**
     * Test method modifying a PCRectStringAnnotated instance and storing in the datastore.
     */
    @Test
    public void testModifyPCRectPointTypeAnnotatedInstance() {
        runModifyIPCRectInstance(PCRectPointTypeAnnotated.class);
    }

    /**
     * Test method running a PCRectStringAnnotated query with a query parameter of type String.
     */
    @Test
    public void testPCRectPointTypeAnnotatedQueryWithPointParam() {
        runQueryWithPointParameter(PCRectPointTypeAnnotated.class, true);
    }

    /**
     * Test method running a PCRectStringAnnotated query with a query parameter of type Point.
     */
    @Test
    public void testPCRectPointTypeAnnotatedQueryWithStringParam() throws Exception {
        runQueryWithStringParameter(PCRectPointTypeAnnotated.class);
    }
}
