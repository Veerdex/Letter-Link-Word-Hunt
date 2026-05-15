package com.grantkoupal.letterlink.quantum.core;

import com.badlogic.gdx.graphics.Texture;

import java.util.*;

public class TextureSet {
    private final HashMap<String, Pair> textureMap;

    private final FileLocator fl = new FileLocator();

    public TextureSet() {
        textureMap = new HashMap<>(25);
    }

    public TextureSet(String dataLocation, String... data) {
        if (data.length % 2 != 0) throw new IllegalArgumentException("Data must be in Name, Path pairs.");

        textureMap = new HashMap<>(Math.max(16, data.length / 2));

        fl.setPathSkip(dataLocation);

        for (int i = 0; i < data.length; i += 2) {
            textureMap.put(data[i], new Pair(data[i + 1]));
        }
    }

    public void loadNewTextureSet(String dataLocation, String... data) {
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("Data must be in Name, Path pairs.");
        }

        fl.setPathSkip(dataLocation);

        // requested name -> path
        Map<String, String> requested = new LinkedHashMap<>();
        for (int i = 0; i < data.length; i += 2) {
            requested.put(data[i], data[i + 1]);
        }

        // old reusable textures by path
        Map<String, Texture> reusableTextures = new HashMap<>();
        for (Pair pair : textureMap.values()) {
            reusableTextures.putIfAbsent(pair.path, pair.texture);
        }

        // build new map
        HashMap<String, Pair> newTextureMap = new HashMap<>(Math.max(16, requested.size()));

        for (Map.Entry<String, String> entry : requested.entrySet()) {
            String name = entry.getKey();
            String path = entry.getValue();

            Pair existingPair = textureMap.get(name);

            // Same name and same path: keep exact existing pair
            if (existingPair != null && existingPair.path.equals(path)) {
                newTextureMap.put(name, existingPair);
                continue;
            }

            // Reuse texture if another old entry already has this path
            Texture texture = reusableTextures.get(path);
            if (texture == null) {
                texture = new Texture(fl.getPNG(path));
            }

            newTextureMap.put(name, new Pair(path, texture));
        }

        // Dispose only textures whose path is no longer needed
        Set<String> neededPaths = new HashSet<>(requested.values());
        Set<Texture> disposed = Collections.newSetFromMap(new IdentityHashMap<>());

        for (Pair pair : textureMap.values()) {
            if (!neededPaths.contains(pair.path) && disposed.add(pair.texture)) {
                pair.texture.dispose();
            }
        }

        textureMap.clear();
        textureMap.putAll(newTextureMap);
    }

    public Texture get(String name) {
        Pair pair = textureMap.get(name);
        return pair == null ? null : pair.texture;
    }

    public void dispose() {
        Set<Texture> disposed = Collections.newSetFromMap(new IdentityHashMap<>());

        for (Pair p : textureMap.values()) {
            if (disposed.add(p.texture)) {
                p.texture.dispose();
            }
        }

        textureMap.clear();
    }

    class Pair {
        protected String path;
        protected Texture texture;

        public Pair(String path) {
            this.path = path;
            this.texture = new Texture(fl.getPNG(path));
        }

        public Pair(String path, Texture texture) {
            this.path = path;
            this.texture = texture;
        }
    }
}
