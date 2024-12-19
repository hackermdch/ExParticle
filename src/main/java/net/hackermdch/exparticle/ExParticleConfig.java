package net.hackermdch.exparticle;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExParticleConfig {
    public static ConfigData config = new ConfigData();

    public static void init() throws IOException {
        var gson = new Gson();
        var file = Paths.get(".").resolve("exparticle.json");
        if (!Files.exists(file)) {
            try (var writer = Files.newBufferedWriter(file)) {
                gson.toJson(config, writer);
            }
        }
        try (var reader = Files.newBufferedReader(file)) {
            config = gson.fromJson(reader, ConfigData.class);
        }
    }

    public static class ConfigData {
        public int maxParticleCount = 65536;
        public int maxParticleTickMillis = 1000;
        public boolean ParallelParticleUpdate = false;
    }
}
