/*
 *   Copyright 2020-2021 Rosemoe
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package io.github.rosemoe.editor.struct;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.github.rosemoe.editor.syntax.EditorColorScheme;

/**
 * The span model
 *
 * @author Rose
 */
public class Span {
    
    private static final BlockingQueue<Span> cacheQueue = new ArrayBlockingQueue<> (8192 * 2);
    public int line;
    public int column;
    public int colorId;
    public int underlineColor = 0;
    public float underlineHeight;
    
    @NonNull
    @Override
    public String toString () {
        return "Span{" +
                "line=" + line +
                ", column=" + column +
                ", colorId=" + colorId +
                ", underlineColor=" + underlineColor +
                ", underlineHeight=" + underlineHeight +
                '}';
    }
    
    public static final float DEFAULT_UNDERLINE_HEIGHT = 0.1f;
    public static final float HEX_COLOR_UNDERLINE_HEIGHT = 0.27f;
    
    /**
     * Create a new span
     *
     * @param line    Start line of span
     * @param column  Start column of span
     * @param colorId Type of span
     * @see Span#obtain(int, int)
     */
    private Span (int line, int column, int colorId) {
        this.line = line;
        this.column = column;
        this.colorId = colorId;
        this.underlineColor = 0;
        this.underlineHeight = DEFAULT_UNDERLINE_HEIGHT;
    }
    
    public static Span obtain (int line, int column, int colorId) {
        Span span = cacheQueue.poll ();
        if (span == null) {
            return new Span (line, column, colorId);
        } else {
            span.line = line;
            span.column = column;
            span.colorId = colorId;
            return span;
        }
    }
    
    public static void recycleAll (Collection<Span> spans) {
        for (Span span : spans) {
            if (!span.recycle ()) {
                return;
            }
        }
    }
    
    /**
     * Set a underline for this region
     * Zero for no underline
     *
     * @param color Color for this underline (not color id of {@link EditorColorScheme})
     * @return Self
     */
    public Span setUnderlineColor (int color) {
        underlineColor = color;
        return this;
    }
    
    /**
     * Set the height of the underline of this span
     *
     * @param height The new height of the span's underline
     */
    public void setUnderlineHeight (float height) {
        this.underlineHeight = height;
    }
    
    /**
     * Get span start column
     *
     * @return Start column
     */
    public int getColumn () {
        return column;
    }
    
    /**
     * Set column of this span
     */
    public Span setColumn (int column) {
        this.column = column;
        return this;
    }
    
    /**
     * Make a copy of this span
     */
    public Span copy () {
        Span copy = obtain (line, column, colorId);
        copy.setUnderlineColor (underlineColor);
        return copy;
    }
    
    public boolean recycle () {
        colorId = column = underlineColor = 0;
        return cacheQueue.offer (this);
    }
}
