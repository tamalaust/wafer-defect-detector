package com.iotml.wdd.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plain Java serialization - ModelBundle (and the RandomForest it wraps) is
 * Serializable, so this works regardless of which Smile I/O API variant a
 * given version ships.
 *
 * Moved here from com.iotml.wdd.trainer since both the trainer (writes) and
 * the predict path (reads) need it.
 */
public class ModelPersistence {

    public void save(ModelBundle bundle, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            out.writeObject(bundle);
        }
    }

    public ModelBundle load(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (ModelBundle) in.readObject();
        }
    }
}