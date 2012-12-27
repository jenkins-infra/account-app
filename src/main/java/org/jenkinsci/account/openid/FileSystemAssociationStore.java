package org.jenkinsci.account.openid;

import org.apache.commons.io.IOUtils;
import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;
import org.openid4java.server.ServerAssociationStore;

import java.io.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * {@link ServerAssociationStore} that uses a plain file system for storage.
 *
 * <p>
 * We use random long value as the handle and uses the hex encoded value (like D2D4DE6CAAA150AC)
 * as a file name. To avoid creating too many files in a single directory, the actual file name
 * will be D2/D4DE6CAAA150AC.
 *
 * <p>
 * Timestamp is set to the expiration date.
 *
 * TODO: clean up expired associations from the disk
 *
 * @author Kohsuke Kawaguchi
 */
public class FileSystemAssociationStore implements ServerAssociationStore {
    private final File rootDir;
    private static Random random = new Random();

    public FileSystemAssociationStore(File rootDir) {
        this.rootDir = rootDir;
    }

    private File getStoreOf(String handle) {
        return new File(rootDir, handle.substring(0, 2) + '/' + handle.substring(3)+".dat");
    }

    public Association generate(String type, int expiryIn) throws AssociationException {
        String handle = String.format("%08X", random.nextLong());
        Association association = Association.generate(type, handle, expiryIn);

        File store = getStoreOf(handle);
        store.getParentFile().mkdir();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(store));
            try {
                oos.writeObject(association);
            } finally {
                IOUtils.closeQuietly(oos);
            }
            store.setLastModified(association.getExpiry().getTime());
        } catch (IOException x) {
            throw new AssociationException(x);
        }

        return association;
    }

    public Association load(String handle) {
        if (HANDLE_PATTERN.matcher(handle).matches()) {
            File f = getStoreOf(handle);
            if (f.exists()) {
                if (isExpired(f)) {
                    f.delete();
                } else {
                    // still active
                    try {
                        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                        return (Association)ois.readObject();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to load "+f,e);
                        f.delete();
                        return null;
                    } catch (ClassNotFoundException e) {
                        LOGGER.log(Level.WARNING, "Failed to load "+f,e);
                        f.delete();
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private boolean isExpired(File f) {
        return f.lastModified()<System.currentTimeMillis();
    }

    public void remove(String handle) {
        if (HANDLE_PATTERN.matcher(handle).matches())
            getStoreOf(handle).delete();
    }

    private final Pattern HANDLE_PATTERN = Pattern.compile("[0-9A-Fa-f]+");

    private static final Logger LOGGER = Logger.getLogger(FileSystemAssociationStore.class.getName());
}
