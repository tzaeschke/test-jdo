/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tck.model;

import tck.util.ConvertiblePointToStringConverter;

import javax.jdo.annotations.Convert;

/** A simple point class with two fields. The whole class/type is declared convertible. */
@Convert(value = ConvertiblePointToStringConverter.class)
public class ConvertiblePoint {
  public int x;
  public Integer y;

  public ConvertiblePoint() {}

  public ConvertiblePoint(int x, int y) {
    this.x = x;
    this.y = Integer.valueOf(y);
  }

  public ConvertiblePoint(int x, Integer y) {
    this.x = x;
    this.y = y;
  }

  public String toString() {
    String rc = null;
    try {
      rc = "ConvertiblePoint(" + name() + ")";
    } catch (NullPointerException ex) {
      rc = "NPE getting ConvertiblePoint's values";
    }
    return rc;
  }

  public int getX() {
    System.out.println("Hello from ConvertiblePoint.getX");
    return x;
  }

  public Integer getY() {
    System.out.println("Hello from ConvertiblePoint.getY");
    return y;
  }

  public String name() {
    return "x: " + getX() + ", y: " + getY().intValue();
  }
}
