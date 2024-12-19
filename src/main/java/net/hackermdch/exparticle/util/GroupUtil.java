package net.hackermdch.exparticle.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.particle.Particle;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GroupUtil {
    private static final Map<String, List<Particle>> GROUPS = Maps.newHashMap();

    public static String[] getGroups() {
        return GROUPS.entrySet().stream().filter((entry) -> !entry.getValue().isEmpty()).map(Map.Entry::getKey).toArray(String[]::new);
    }

    public static void add(String group, Particle particle) {
        if (group != null && !group.equals("null")) {
            String[] groups = group.split("\\|");
            for (String str : groups) {
                if (!GROUPS.containsKey(str)) {
                    GROUPS.put(str, Lists.newArrayList());
                }
                GROUPS.get(str).add(particle);
            }
        }
    }

    public static void remove(String group, String expression, double cx, double cy, double cz) {
        if (group != null && !group.equals("null")) {
            var groups = group.split("\\|");
            for (var str : groups) {
                if (GROUPS.containsKey(str)) {
                    var particles = GROUPS.get(str);
                    if (expression != null && !expression.equals("null")) {
                        var exe = ExpressionUtil.parse(expression);
                        var data = Objects.requireNonNull(exe).getData();
                        for (Particle particle : particles) {
                            if (particle.isAlive()) {
                                double dx = particle.x - cx;
                                double dy = particle.y - cy;
                                double dz = particle.z - cz;
                                data.x = dx;
                                data.y = dy;
                                data.z = dz;
                                data.s1 = Math.atan2(dz, dx);
                                data.s2 = Math.atan2(dy, Math.hypot(dx, dz));
                                data.dis = Math.sqrt(dx * dx + dy * dy + dz * dz);
                                data.age = particle.age;
                                if (exe.invoke() != 0) particle.remove();
                            }
                        }
                        particles.removeIf(p -> !p.isAlive());
                    } else {
                        for (var particle : particles) particle.remove();
                        particles.clear();
                    }
                }
            }
        }
    }

    public static List<Particle> get(String group) {
        if (group != null && !group.equals("null")) {
            var particles = Lists.<Particle>newArrayList();
            var groups = group.split("\\|");
            for (String str : groups) {
                if (GROUPS.containsKey(str)) {
                    particles.addAll(GROUPS.get(str));
                }
            }
            return particles;
        } else {
            return Collections.emptyList();
        }
    }

    public static void clear() {
        for (var particles : GROUPS.values()) particles.clear();
        GROUPS.clear();
    }
}
