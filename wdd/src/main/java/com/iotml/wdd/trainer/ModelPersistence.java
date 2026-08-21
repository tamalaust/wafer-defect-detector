package com.iotml.wdd.trainer;

import smile.classification.RandomForest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plain Java serialization rather than Smile's own I/O helpers - RandomForest
 * implements Serializable, so this works regardless of which Smile I/O API
 * variant a given version ships.
 */
public class ModelPersistence {

    public void save(RandomForest model, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            out.writeObject(model);
        }
    }

    public RandomForest load(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (RandomForest) in.readObject();
        }
    }
}