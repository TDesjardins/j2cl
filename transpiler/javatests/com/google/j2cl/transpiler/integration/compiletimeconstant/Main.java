/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.j2cl.transpiler.integration.compiletimeconstant;

/**
 * This test verifies that compile time constants can be accessed and that they don't call the
 * clinit.
 */
public class Main {
  public static class CompileTimeConstants {
    static {
      ranClinit = true;
    }
    // compile time constants
    public static final int CONSTANT = 10;
    public static final int CONSTANT_PLUS_ONE = CONSTANT + 1;
    public static final int CONSTANT_COMPOSED = CONSTANT_PLUS_ONE + CONSTANT;
    public static final String CONSTANT_STRING = "constant";
    public static final String CONSTANT_COMPOSED_STRING = "constant" + CONSTANT_STRING;
    public static final Object OBJ = null;

    // non-compile time constants
    public static int nonConstant = 20;
  }

  public static boolean ranClinit = false;

  public static void main(String... args) {
    verifyClinitOrder();
  }

  private static void verifyClinitOrder() {
    int total = 0;

    total += CompileTimeConstants.CONSTANT;
    assert ranClinit == false;
    assert total == 10;

    total += CompileTimeConstants.CONSTANT_PLUS_ONE; // 11 + 10
    assert ranClinit == false;
    assert total == 21;

    total += CompileTimeConstants.CONSTANT_COMPOSED; // 21 + 11 + 10
    assert ranClinit == false;
    assert total == 42;

    total += CompileTimeConstants.CONSTANT_COMPOSED_STRING.length(); // 42 + 16
    assert ranClinit == false;
    assert total == 58;

    total += CompileTimeConstants.nonConstant;
    assert ranClinit == true;
    assert total == 78;

    assert CompileTimeConstants.OBJ == null;
  }
}
