/************************************************************************************
 * This file is part of AndroidIDE.
 * 
 * AndroidIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * AndroidIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 * 
**************************************************************************************/
package com.itsaky.androidide.project;

import android.os.FileObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.itsaky.androidide.utils.Logger;
import com.itsaky.inflater.IResourceTable;

import java.io.File;
import java.io.FileFilter;

public class ProjectResourceTable implements IResourceTable {
    
    private File resDir;
    private File color;
    private File layout;
    private File values;
    
    private File[] drawables;
    private File[] mipmaps;
    
    private final FileObserver mFileObserver;
    
    public ProjectResourceTable (@NonNull final Iterable<File> resourceDirectories) {
        mFileObserver = new FileObserver (resDir) {
            @Override
            public void onEvent (int event, @Nullable String path) {
        
            }
        };
    }
    
    private static final Logger LOG = Logger.instance ("ProjectResourceFinder");
    
    @Override
    public File findDrawable (@NonNull String name) {
        for (File drawable : drawables) {
            final File file = findFileWithName(drawable.listFiles(), name);
            if (file != null) {
                return file;
            }
        }
        
        return null;
    }

    @Override
    public File inflateLayout(@NonNull String name) {
        return findFileWithName(this.layout.listFiles(), name);
    }

    @Override
    public String findString(@NonNull String name) {
        return name;
    }

    @Override
    public String findColor(@NonNull String name) {
        return name;
    }
    
    @Override
    public String[] findArray(@NonNull String name) {
        return new String[] {name};
    }

    @Override
    public String findDimension(@NonNull String name) {
        return name;
    }

    @Override
    public void setInflatingFile(@NonNull File file) {
        if (file.getParentFile () == null) {
            throw new IllegalArgumentException ("Invalid inflating file");
        }
        
        setupDirectories(file  // layout file
                .getParentFile() // layout dir
                .getParentFile()); // res dir
    }
    
    private void setupDirectories (File resDir) {
        
        if (resDir == null) {
            LOG.error ("Null resource directory passed to resource finder. Ignoring.");
            return;
        }
        
        this.resDir = resDir;
        this.color = new File (resDir, "color");
        this.layout = new File (resDir, "layout");
        this.values = new File (resDir, "values");
        
        final File[] drawables = resDir.listFiles(new NameStartsWith ("drawable"));
        this.drawables = drawables == null ? new File [0] : drawables;
        
        final File[] mipmaps = resDir.listFiles(new NameStartsWith ("mipmap"));
        this.mipmaps = mipmaps == null ? new File [0] : mipmaps;
    }
    
    private File findFileWithName (File[] files, String name) {
        if (files == null || name == null) {
            return null;
        }
        
        for (File file : files) {
            var simpleName = file.getName();
            
            if (simpleName.startsWith (name)) {
                return file;
            }
        }
        
        return null;
    }
    
    private static class NameStartsWith implements FileFilter {
        
        private final String name;
        
        public NameStartsWith(String name) {
            this.name = name;
        }
        
        @Override
        public boolean accept(@NonNull File file) {
            return file.getName().startsWith(name);
        }
    }
}
