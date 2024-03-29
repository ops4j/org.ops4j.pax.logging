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

package org.apache.log4j.pattern;


/**
 * Modifies the output of a pattern converter for a specified minimum
 * and maximum width and alignment.
 *
 * @author <a href=mailto:jim_cakalic@na.biomerieux.com>Jim Cakalic</a>
 * @author Ceki G&uuml;lc&uuml;
 * @author Curt Arnold
 */
public final class ExtrasFormattingInfo {
    /**
     * Array of spaces.
     */
    private static final char[] SPACES =
            new char[]{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};

    /**
     * Default instance.
     */
    private static final ExtrasFormattingInfo DEFAULT =
            new ExtrasFormattingInfo(false, 0, Integer.MAX_VALUE);

    /**
     * Minimum length.
     */
    private final int minLength;

    /**
     * Maximum length.
     */
    private final int maxLength;

    /**
     * Alignment.
     */
    private final boolean leftAlign;

    /**
     * Right truncation.
     *
     * @since 1.2.17
     */
    private final boolean rightTruncate;

    /**
     * Creates new instance.
     *
     * @param leftAlign left align if true.
     * @param minLength minimum length.
     * @param maxLength maximum length.
     * @deprecated since 1.2.17
     */
    public ExtrasFormattingInfo(
            final boolean leftAlign,
            final int minLength,
            final int maxLength) {
        this.leftAlign = leftAlign;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.rightTruncate = false;
    }

    /**
     * Creates new instance.
     *
     * @param leftAlign     left align if true.
     * @param rightTruncate right truncate if true.
     * @param minLength     minimum length.
     * @param maxLength     maximum length.
     * @since 1.2.17
     */
    public ExtrasFormattingInfo(
            final boolean leftAlign,
            final boolean rightTruncate,
            final int minLength,
            final int maxLength) {
        this.leftAlign = leftAlign;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.rightTruncate = rightTruncate;
    }

    /**
     * Gets default instance.
     *
     * @return default instance.
     */
    public static ExtrasFormattingInfo getDefault() {
        return DEFAULT;
    }

    /**
     * Determine if left aligned.
     *
     * @return true if left aligned.
     */
    public boolean isLeftAligned() {
        return leftAlign;
    }

    /**
     * Determine if right truncated.
     *
     * @return true if right truncated.
     * @since 1.2.17
     */
    public boolean isRightTruncated() {
        return rightTruncate;
    }

    /**
     * Get minimum length.
     *
     * @return minimum length.
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Get maximum length.
     *
     * @return maximum length.
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Adjust the content of the buffer based on the specified lengths and alignment.
     *
     * @param fieldStart start of field in buffer.
     * @param buffer     buffer to be modified.
     */
    public void format(final int fieldStart, final StringBuffer buffer) {
        final int rawLength = buffer.length() - fieldStart;

        if (rawLength > maxLength) {
            if (rightTruncate) {
                buffer.setLength(fieldStart + maxLength);
            } else {
                buffer.delete(fieldStart, buffer.length() - maxLength);
            }
        } else if (rawLength < minLength) {
            if (leftAlign) {
                final int fieldEnd = buffer.length();
                buffer.setLength(fieldStart + minLength);

                for (int i = fieldEnd; i < buffer.length(); i++) {
                    buffer.setCharAt(i, ' ');
                }
            } else {
                int padLength = minLength - rawLength;

                for (; padLength > 8; padLength -= 8) {
                    buffer.insert(fieldStart, SPACES);
                }

                buffer.insert(fieldStart, SPACES, 0, padLength);
            }
        }
    }
}
