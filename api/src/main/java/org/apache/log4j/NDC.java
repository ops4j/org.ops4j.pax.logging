/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
// Modified to work in Pax Logging.
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//      Contributors:      Dan Milstein
//                         Ray Millard

package org.apache.log4j;

import java.util.Stack;

/**
 * Nested Diagnostic Context.
 *
 * NOTE: This class is added to Pax Logging ONLY TO PROVIDE THE API.
 * There is NO SUPPORT for NDC in Pax Logging.
 */

public class NDC
{

    // No instances allowed.
    private NDC()
    {
    }

    /**
     * Dummy method. Does not do anything.
     */
    public static void clear()
    {
    }

    /**
     * Dummy method. Does not do anything.
     *
     * @return an empty stack.
     */
    public static Stack cloneStack()
    {
        return new Stack();
    }

    /**
     * Dummy method. Does not do anything.
     *
     * @param stack ignored
     */
    public static void inherit( Stack stack )
    {
    }

    /**
     * Dummy method. Does not do anything.
     *
     * @return an empty string.
     */
    static public String get()
    {
        return "";
    }

    /**
     * Dummy method. Does not do anything.
     *
     * @return 0 (zero)
     */
    public static int getDepth()
    {
        return 0;
    }

    /**
     * Dummy method. Does not do anything.
     *
     * @return an empty string
     */
    public static String pop()
    {
        return "";
    }

    /**
     * Dummy method. Does not do anything.
     *
     * @return an empty string
     */
    public static String peek()
    {
        return "";
    }

    /**
     * Dummy method. Does not do anything.
     *
     * @param message ignored
     */
    public static void push( String message )
    {
    }

    /**
     * Dummy method. Does not do anything.
     */
    static public void remove()
    {
    }

    /**
     * Dummy method. Does not do anything.
     *
     * @param maxDepth ignored
     */
    static public void setMaxDepth( int maxDepth )
    {
    }
}

