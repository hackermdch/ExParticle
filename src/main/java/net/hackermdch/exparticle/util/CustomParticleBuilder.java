package net.hackermdch.exparticle.util;

import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;

/**
 * 构建器类，用于从 ParticleStruct 中读取所有属性并应用到粒子上。
 * 依赖原始的 ParticleUtil.spawnParticle 方法创建粒子，然后通过 IParticle 接口设置扩展属性。
 */
public class CustomParticleBuilder {

    /**
     * 创建并初始化一个完全由表达式控制的粒子。
     *
     * @param effect           粒子类型
     * @param x,y,z            生成位置
     * @param cx,cy,cz         中心点（用于表达式中的相对坐标）
     * @param speedExpression  速度表达式（可空）
     * @param speedStep        速度步长
     * @param group            粒子组
     * @param data             从主表达式执行后得到的 ParticleStruct（包含所有动态属性）
     * @return 生成的粒子，若失败返回 null
     */
    public static Particle buildParticle(ParticleOptions effect, double x, double y, double z, double cx, double cy, double cz, String speedExpression, double speedStep, String group, ParticleStruct data) {
        // 1. 调用原始 ParticleUtil.spawnParticle 创建基础粒子
        Particle particle = ParticleUtil.spawnParticle(effect,
                x, y, z, cx, cy, cz,
                (float) data.cr, (float) data.cg, (float) data.cb, (float) data.alpha,
                data.vx, data.vy, data.vz,
                (int) data.age,
                speedExpression, speedStep, group
        );
        // 2. 如果粒子创建成功，通过 IParticle 接口设置扩展属性
        if (particle != null) {
            // 设置尺寸和光照
            particle.setCustomSize(data.size);
            particle.setCustomLight(data.light);
            // 设置重力和摩擦系数（一次性，不参与后续 speedExpression 更新）
            particle.setGravity((float) data.gravity);
            particle.setFriction((float) data.friction);
        }
        return particle;
    }
}