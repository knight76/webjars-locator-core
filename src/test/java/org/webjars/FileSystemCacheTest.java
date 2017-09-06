package org.webjars;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Set;

import static org.junit.Assert.*;
import static org.webjars.WebJarExtractor.Cacheable;

public class FileSystemCacheTest {

    private File tmpFile;

    private Cacheable c1 = new Cacheable("c1", 123);
    private Cacheable c2 = new Cacheable("c2", 456);

    @Test
    public void cacheShouldPersistNewEntries() throws Exception {
        FileSystemCache cache = new FileSystemCache(createTmpFile());
        cache.put("foo", c1);
        cache.put("bar", c2);
        cache.save();
        cache = new FileSystemCache(createTmpFile());
        assertTrue(cache.isUpToDate("foo", c1));
        assertTrue(cache.isUpToDate("bar", c2));
    }

    @Test
    public void cacheShouldFailOnNonExistentEntries() throws Exception {
        FileSystemCache cache = new FileSystemCache(createTmpFile());
        cache.put("foo", c1);
        cache.save();
        cache = new FileSystemCache(createTmpFile());
        assertFalse(cache.isUpToDate("bar", c2));
    }

    @Test
    public void cacheShouldNotRewriteFileIfAllFilesTouched() throws Exception {
        FileSystemCache cache = new FileSystemCache(createTmpFile());
        cache.put("foo", c1);
        cache.put("bar", c2);
        cache.save();
        cache = new FileSystemCache(createTmpFile());
        tmpFile.delete();
        assertTrue(cache.isUpToDate("foo", c1));
        assertTrue(cache.isUpToDate("bar", c2));
        cache.save();
        assertFalse(tmpFile.exists());
    }

    @Test
    public void cacheShouldDeleteUntouchedEntries() throws Exception {
        FileSystemCache cache = new FileSystemCache(createTmpFile());
        cache.put("foo", c1);
        cache.put("bar", c2);
        cache.save();
        cache = new FileSystemCache(createTmpFile());
        assertTrue(cache.isUpToDate("foo", c1));
        cache.save();
        assertFalse(cache.isUpToDate("bar", c2));
    }

    @Test
    public void getExistingUntouchedFilesShouldReturnUntouchedFiles() throws Exception {
        File untouchedDir = new File(this.getClass().getClassLoader().getResource("untouched/a.txt").toURI()).getParentFile();

        FileSystemCache cache = new FileSystemCache(createTmpFile());
        cache.put("a.txt", new Cacheable("a.txt", 123));
        cache.put("b.txt", new Cacheable("b.txt", 123));
        cache.put("z.txt", new Cacheable("z.txt", 123));
        cache.put("sub/c.txt", new Cacheable("sub/c.txt", 123));
        cache.put("sub/d.txt", new Cacheable("sub/d.txt", 123));
        cache.save();

        cache.isUpToDate("a.txt", new Cacheable("a.txt", 123));
        cache.isUpToDate("sub/c.txt", new Cacheable("sub/c.txt", 123));

        Set<File> files = cache.getExistingUntouchedFiles(untouchedDir);
        assertEquals(2, files.size());
        assertTrue(files + " doesn't contain b.txt", files.contains(new File(untouchedDir, "b.txt").getCanonicalFile()));
        assertTrue(files + " doesn't contain sub/d.txt", files.contains(new File(untouchedDir, "sub/d.txt").getCanonicalFile()));
    }

    private File createTmpFile() throws Exception {
        if (tmpFile == null) {
            tmpFile = File.createTempFile("filesystemcache-", ".cache");
        }
        return tmpFile;
    }

    @After
    public void deleteTmpFile() {
        if (tmpFile != null) {
            tmpFile.delete();
        }
    }
}
