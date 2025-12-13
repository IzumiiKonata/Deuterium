package net.minecraft.client.resources;

import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.apache.commons.io.filefilter.DirectoryFileFilter;



import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;

public class FolderResourcePack extends AbstractResourcePack {
    public FolderResourcePack(File resourcePackFileIn) {
        super(resourcePackFileIn);
    }

    protected InputStream getInputStreamByName(String name) throws IOException {
        return new BufferedInputStream(new FileInputStream(new File(this.resourcePackFile, name)));
    }

    protected boolean hasResourceName(String name) {
        return (new File(this.resourcePackFile, name)).isFile();
    }

    public Set<String> getResourceDomains() {
        Set<String> set = Sets.newHashSet();
        File file1 = new File(this.resourcePackFile, "assets/");

        if (file1.isDirectory()) {
            for (File file2 : file1.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)) {
                String s = getRelativeName(file1, file2);

                if (!s.equals(s.toLowerCase())) {
                    this.logNameNotLowercase(s);
                } else {
                    set.add(s.substring(0, s.length() - 1));
                }
            }
        }

        return set;
    }

    private boolean checkIfHasOgg(File dir) {
        File[] files = dir.listFiles();

        if (files == null)
            return false;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".ogg"))
                return true;
            else if (file.isDirectory()) {
                if (checkIfHasOgg(file))
                    return true;
            }
        }

        return false;
    }
}
