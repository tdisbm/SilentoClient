package services;

import com.fasterxml.jackson.databind.node.TextNode;

import java.util.ArrayList;
import java.util.Random;

public class AvatarCollection {
    private ArrayList<String> collection;
    private int size;
    private Random random;

    public AvatarCollection() {
        collection = new ArrayList<>();
        random = new Random();
    }

    public void add(TextNode url) {
        collection.add(url.asText());
        size = collection.size();
    }

    public String random() {
        int index = random.nextInt(size);
        return collection.get(index);
    }
}
