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

import org.datanucleus.util.NucleusLogger;
import org.junit.After;
import org.junit.Before;
import tck.model.ConvertiblePoint;
import tck.model.Point;
import tck.pc.IPCRect;
import tck.pc.PCRectAnnotated;
import tck.pc.PCRectPointTypeAnnotated;
import tck.util.PointConversionCounter;

import javax.jdo.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * <B>Title:</B>PointAttributeConverterTest <br>
 * <B>Keywords:</B> mapping <br>
 * <B>Assertion ID:</B> [not identified] <br>
 * <B>Assertion Description: </B> A IPCRect instance refers two Point instances, that are stored as
 * strings in the datastore. A Point instance is converted using an AttributeConverter.
 */
public class AnnotationTestHelper {

    private static final int UL_X = 1;
    private static final int UL_Y = 10;
    private static final int LR_X = 10;
    private static final int LR_Y = 1;

    private static final Collection<Class<?>> tearDownClasses = new LinkedList<>();

    private PersistenceManager pm;
    private PersistenceManagerFactory pmf;

    private PersistenceManager getPM() {
        if (pmf == null) {
            //PMFPropertiesObject = loadProperties(PMFProperties); // will exit here if no properties
            //pmf = JDOHelper.getPersistenceManagerFactory(PMFPropertiesObject);
            pmf = JDOHelper.getPersistenceManagerFactory("MyTest");
        }
        if (pm == null) {
            pm = pmf.getPersistenceManager();
        }
        return pm;
    }

    @Before
    public void localSetUp() {
//    tearDownClasses.add(PCRect.class); // TODO
        tearDownClasses.add(PCRectAnnotated.class);
        tearDownClasses.add(PCRectPointTypeAnnotated.class);
    }

    @After
    public void localTearDown() {
        //deleteTearDownInstances();
        deleteTearDownClasses();
    }

    private void deleteTearDownClasses() {
        if (tearDownClasses.size() > 0) {
            getPM();
            try {
                this.pm.currentTransaction().begin();
                for (Class<?> tearDownClass : tearDownClasses) {
                    this.pm.deletePersistentAll(getAllObjects(this.pm, tearDownClass));
                }
                this.pm.currentTransaction().commit();
            } finally {
                tearDownClasses.clear();
                cleanupPM();
            }
        }
    }

    private <T> Collection<T> getAllObjects(PersistenceManager pm, Class<T> pcClass) {
        Query<T> query = pm.newQuery(pcClass);
        Extent<T> candidates = null;
        try {
            candidates = pm.getExtent(pcClass, false);
        } catch (JDOException ex) {
            NucleusLogger.GENERAL.error("Exception thrown for getExtent of class " + pcClass.getName());
            //throw ex;
            //if (debug) logger.debug("Exception thrown for getExtent of class " + pcClass.getName());
            return Collections.emptyList();
        }
        query.setCandidates(candidates);
        return query.executeList();
    }

    private void cleanupPM() {
        cleanupPM(pm);
        pm = null;
    }

    private static void cleanupPM(PersistenceManager pm) {
        if ((pm != null) && !pm.isClosed()) {
            if (pm.currentTransaction().isActive()) {
                pm.currentTransaction().rollback();
            }
            pm.close();
        }
    }

    // Helper methods

    /**
     * Helper method creating a IPCRect instance. It should call AttributeConverter method
     * convertToDatastore.
     */
    protected <T extends IPCRect> void runStoreIPCRectInstance(Class<T> pcrectClass) {
        int nrOfDbCalls = PointConversionCounter.getNrOfConvertToDatastoreCalls();
        int nrOfAttrCalls = PointConversionCounter.getNrOfConvertToAttributeCalls();

        // Create a persistent IPCRect instance and store its oid
        // AttributeConverter method convertToDatastore is called when persisting instance
        createIPCRectInstances(pcrectClass, 1);

        // convertToDatastore should be called twice
        assertEquals(2, PointConversionCounter.getNrOfConvertToDatastoreCalls() - nrOfDbCalls);
        // convertToAttribute should not be called
        assertEquals(0, PointConversionCounter.getNrOfConvertToAttributeCalls() - nrOfAttrCalls);
    }

    /**
     * Helper method reading a IPCRect instance from the datastore. It should call AttributeConverter
     * method convertToAttribute.
     */
    protected <T extends IPCRect> void runReadIPCRectInstance(Class<T> pcrectClass) {
        IPCRect rect;
        Object oid;
        int nrOfDbCalls;
        int nrOfAttrCalls;

        // Create a persistent IPCRect instance and store its oid
        oid = createIPCRectInstances(pcrectClass, 1);

        // Cleanup the 2nd-level cache and close the pm to make sure PCRect instances are not cached
        pm.getPersistenceManagerFactory().getDataStoreCache().evictAll(false, pcrectClass);
        pm.close();
        pm = null;

        nrOfDbCalls = PointConversionCounter.getNrOfConvertToDatastoreCalls();
        nrOfAttrCalls = PointConversionCounter.getNrOfConvertToAttributeCalls();
        pm = getPM();
        pm.currentTransaction().begin();
        // Read the IPCRect instance from the datastore, this should call convertToAttribute
        rect = (IPCRect) pm.getObjectById(oid);
        Point ul = rect.getUpperLeft();
        Point lr = rect.getLowerRight();
        pm.currentTransaction().commit();

        // convertToDatastore should not be called
        assertEquals(0, PointConversionCounter.getNrOfConvertToDatastoreCalls() - nrOfDbCalls);
        // convertToAttribute should be called twice
        assertEquals(2, PointConversionCounter.getNrOfConvertToAttributeCalls() - nrOfAttrCalls);
        // Check the values of the associated Point instances
        assertEquals(UL_X, ul.getX());
        assertEquals(UL_Y, ul.getY() == null ? 0 : ul.getY().intValue());
        assertEquals(LR_X, lr.getX());
        assertEquals(LR_Y, lr.getY() == null ? 0 : lr.getY().intValue());
    }

    /**
     * Helper method modifying a IPCRect instance. It should call AttributeConverter method
     * convertToDatastore.
     */
    protected <T extends IPCRect> void runModifyIPCRectInstance(Class<T> pcrectClass) {
        Transaction tx;
        IPCRect rect;
        Object oid;
        int nrOfDbCalls;
        int nrOfAttrCalls;

        // Create a persistent IPCRect instance and store its oid
        oid = createIPCRectInstances(pcrectClass, 1);

        // Cleanup the 2nd-level cache and close the pm to make sure PCRect instances are not cached
        pm.getPersistenceManagerFactory().getDataStoreCache().evictAll(false, pcrectClass);
        pm.close();
        pm = null;

        nrOfDbCalls = PointConversionCounter.getNrOfConvertToDatastoreCalls();
        nrOfAttrCalls = PointConversionCounter.getNrOfConvertToAttributeCalls();
        pm = getPM();
        tx = pm.currentTransaction();
        tx.begin();
        rect = (IPCRect) pm.getObjectById(oid);
        // should trigger convertToAttribute
        rect.getLowerRight();
        rect.getUpperLeft();
        // Update IPCRect instance, this should call convertToDatastore
        rect.setUpperLeft(new Point(UL_X + 1, UL_Y + 1));
        rect.setLowerRight(new Point(LR_X + 1, LR_Y + 1));
        // IPCRect instance should be dirty
        assertTrue(JDOHelper.isDirty(rect));
        tx.commit();

        // convertToDatastore should be called twice
        assertEquals(2, PointConversionCounter.getNrOfConvertToDatastoreCalls() - nrOfDbCalls);
        // convertToAttribute should be called twice
        assertEquals(2, PointConversionCounter.getNrOfConvertToAttributeCalls() - nrOfAttrCalls);
    }

    /**
     * Helper method running a query with a Point parameter. The parameter value is converted using
     * the AttributeConverter.
     *
     * @throws Exception
     */
    protected <T extends IPCRect> void runQueryWithPointParameter(
            Class<T> pcrectClass, boolean useConvertiblePoint) {
        int nrOfDbCalls;
        int nrOfAttrCalls;

        nrOfDbCalls = PointConversionCounter.getNrOfConvertToDatastoreCalls();
        nrOfAttrCalls = PointConversionCounter.getNrOfConvertToAttributeCalls();
        createIPCRectInstances(pcrectClass, 5);
        // convertToDatastore should be called twice per instance = 10 times
        assertEquals(10, PointConversionCounter.getNrOfConvertToDatastoreCalls() - nrOfDbCalls);
        // convertToAttribute should not be called
        assertEquals(0, PointConversionCounter.getNrOfConvertToAttributeCalls() - nrOfAttrCalls);

        // Cleanup the 2nd-level cache and close the pm to make sure PCRect instances are not cached
        pm.getPersistenceManagerFactory().getDataStoreCache().evictAll(false, pcrectClass);
        pm.close();
        pm = null;

        nrOfDbCalls = PointConversionCounter.getNrOfConvertToDatastoreCalls();
        nrOfAttrCalls = PointConversionCounter.getNrOfConvertToAttributeCalls();
        pm = getPM();
        pm.currentTransaction().begin();
        try (Query<T> q = pm.newQuery(pcrectClass, "this.upperLeft == :point")) {
            if (useConvertiblePoint) {
                q.setParameters(new ConvertiblePoint(UL_X + 1, UL_Y + 1));
            } else {
                q.setParameters(new Point(UL_X + 1, UL_Y + 1));
            }
            // AttributeConverter method convertToAttribute is called when loading instance from the
            // datastore
            List<T> res = q.executeList();
            assertEquals(1, res.size());
            IPCRect rect = res.get(0);
            Point ul = rect.getUpperLeft();
            Point lr = rect.getLowerRight();

            // Check the coordinates of the associated Point instances
            assertEquals(UL_X + 1, ul.getX());
            assertEquals(UL_Y + 1, ul.getY() == null ? 0 : ul.getY().intValue());
            assertEquals(LR_X + 1, lr.getX());
            assertEquals(LR_Y + 1, lr.getY() == null ? 0 : lr.getY().intValue());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            pm.currentTransaction().commit();
        }

        // convertToDatastore should be called to handle the query parameter
        assertTrue(PointConversionCounter.getNrOfConvertToDatastoreCalls() - nrOfDbCalls >= 1);
        // convertToAttribute should be called at least twice
        assertTrue(PointConversionCounter.getNrOfConvertToAttributeCalls() - nrOfAttrCalls >= 2);
    }

    /**
     * Helper method running a query with a Point parameter. The string parameter is compared to the
     * converted Point field.
     *
     * @throws Exception
     */
    protected <T extends IPCRect> void runQueryWithStringParameter(Class<T> pcrectClass)
            throws Exception {
        int nrOfDbCalls;
        int nrOfAttrCalls;

        nrOfDbCalls = PointConversionCounter.getNrOfConvertToDatastoreCalls();
        nrOfAttrCalls = PointConversionCounter.getNrOfConvertToAttributeCalls();
        createIPCRectInstances(pcrectClass, 5);
        // convertToDatastore should be called twice per instance = 10 times
        assertEquals(10, PointConversionCounter.getNrOfConvertToDatastoreCalls() - nrOfDbCalls);
        // convertToAttribute should not be called
        assertEquals(0, PointConversionCounter.getNrOfConvertToAttributeCalls() - nrOfAttrCalls);

        // Cleanup the 2nd-level cache and close the pm to make sure PCRect instances are not cached
        pm.getPersistenceManagerFactory().getDataStoreCache().evictAll(false, pcrectClass);
        pm.close();
        pm = null;

        nrOfDbCalls = PointConversionCounter.getNrOfConvertToDatastoreCalls();
        nrOfAttrCalls = PointConversionCounter.getNrOfConvertToAttributeCalls();
        pm = getPM();
        pm.currentTransaction().begin();
        try (Query<T> q = pm.newQuery(pcrectClass, "this.upperLeft == str")) {
            q.declareParameters("String str");
            q.setParameters("3:12");
            // AttributeConverter method convertToAttribute is called when loading instance from the
            // datastore
            List<T> res = q.executeList();
            assertEquals(1, res.size());
            IPCRect rect = res.get(0);
            Point ul = rect.getUpperLeft();
            Point lr = rect.getLowerRight();

            // Check the coordinates of the associated Point instances
            assertEquals(UL_X + 2, ul.getX());
            assertEquals(UL_Y + 2, ul.getY() == null ? 0 : ul.getY().intValue());
            assertEquals(LR_X + 2, lr.getX());
            assertEquals(LR_Y + 2, lr.getY() == null ? 0 : lr.getY().intValue());
        } finally {
            pm.currentTransaction().commit();
        }

        // convertToDatastore should not be called
        assertTrue(PointConversionCounter.getNrOfConvertToDatastoreCalls() - nrOfDbCalls == 0);
        // convertToAttribute should be called at least twice
        assertTrue(PointConversionCounter.getNrOfConvertToAttributeCalls() - nrOfAttrCalls >= 2);
    }

    /**
     * Helper method to create IPCRect instances.
     *
     * @param pcrectClass class instance of the IPCRect implementation class to be created
     * @param nrOfObjects number of IPCRect instances to be created
     * @return ObjectId of the first IPCRect instance
     */
    private <T extends IPCRect> Object createIPCRectInstances(Class<T> pcrectClass, int nrOfObjects) {
        IPCRect rect;
        Object oid = null;

        if (nrOfObjects < 1) {
            return null;
        }

        pm = getPM();
        try {
            pm.currentTransaction().begin();
            rect = pcrectClass.getConstructor().newInstance();
            rect.setUpperLeft(new Point(UL_X, UL_Y));
            rect.setLowerRight(new Point(LR_X, LR_Y));
            pm.makePersistent(rect);
            oid = pm.getObjectId(rect);
            for (int i = 1; i < nrOfObjects; i++) {
                rect = pcrectClass.getConstructor().newInstance();
                rect.setUpperLeft(new Point(UL_X + i, UL_Y + i));
                rect.setLowerRight(new Point(LR_X + i, LR_Y + i));
                pm.makePersistent(rect);
            }
            pm.currentTransaction().commit();
        } catch (NoSuchMethodException
                 | SecurityException
                 | InstantiationException
                 | IllegalAccessException
                 | IllegalArgumentException
                 | InvocationTargetException ex) {
            fail("Error creating IPCRect instance: " + ex.getMessage());
        } finally {
            if (pm.currentTransaction().isActive()) {
                pm.currentTransaction().rollback();
            }
        }
        return oid;
    }
}
