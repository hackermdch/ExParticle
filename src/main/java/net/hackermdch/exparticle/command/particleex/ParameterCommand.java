package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.hackermdch.exparticle.command.argument.*;
import net.hackermdch.exparticle.network.ParameterPayload;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector4f;

import java.util.List;

/**
 * 参数化粒子命令的主类。
 * 注册 /particlex parameter 下的所有模式子命令，每个模式对应不同的参数组合。
 * 命令结构：/particlex parameter <模式> <name> <pos> [其他参数...]
 * 模式包括：normal, polar, tick, tick-polar, exp, exp-polar, exp-tick, exp-tick-polar
 */
public class ParameterCommand {

    /**
     * 内部记录类，用于描述一个子命令的配置信息。
     *
     * @param literal            模式字面量（例如 "normal"）
     * @param polar              是否为极坐标模式
     * @param tick               是否为 tick 模式（每 tick 生成粒子）
     * @param exp                是否为表达式模式（允许动态改变颜色、大小等）
     * @param expressionSuggests 表达式参数的补全建议列表
     */
    private record SubCommandConfig(
            String literal,
            boolean polar,
            boolean tick,
            boolean exp,
            String[] expressionSuggests
    ) {}

    /**
     * 所有模式的配置列表。
     * 每个配置对应一个独立的模式，通过循环注册，避免重复代码。
     */
    private static final List<SubCommandConfig> CONFIGS = List.of(
            new SubCommandConfig("normal", false, false, false,
                    new String[]{"null", "\"x,y=t,sin(t)\"", "\"x=t;y=t^2\"", "\"x,y,z=t,sin(t),0;(x,y,z)=(x,y,z,1)*rotateDeg(0,60,0)*translate(5,0,0)\""}),
            new SubCommandConfig("polar", true, false, false,
                    new String[]{"null", "\"s1,s2,dis=t*10,t*PI/20,1\""}),
            new SubCommandConfig("tick", false, true, false,
                    new String[]{"null", "\"x,y=t,sin(t)\"", "\"x=t;y=t^2\"", "\"x,y,z=t,sin(t),0;(x,y,z)=(x,y,z,1)*rotateDeg(0,60,0)*translate(5,0,0)\""}),
            new SubCommandConfig("tick-polar", true, true, false,
                    new String[]{"null", "\"s1,s2,dis=t*10,t*PI/20,1\""}),
            new SubCommandConfig("exp", false, false, true,
                    new String[]{"null", "\"x,y,cr,cg,cb=t,sin(t),sin(t/7)/4+0.75,sin(t/5)/4+0.75,sin(t/3)/4+0.75\"", "\"x,y,z=t,sin(t),0;(x,y,z)=(x,y,z,1)*rotateDeg(0,60,0)*translate(5,0,0)\""}),
            new SubCommandConfig("exp-polar", true, false, true,
                    new String[]{"null", "\"s1,s2,dis=t*10,t*PI/20,1;cr,cg,cb=sin(t/7)/4+0.75,sin(t/5)/4+0.75,sin(t/3)/4+0.75\""}),
            new SubCommandConfig("exp-tick", false, true, true,
                    new String[]{"null", "\"x,y,cr,cg,cb=t,sin(t),sin(t/7)/4+0.75,sin(t/5)/4+0.75,sin(t/3)/4+0.75\"", "\"x,y,z=t,sin(t),0;(x,y,z)=(x,y,z,1)*rotateDeg(0,60,0)*translate(5,0,0)\""}),
            new SubCommandConfig("exp-tick-polar", true, true, true,
                    new String[]{"null", "\"s1,s2,dis=t*10,t*PI/20,1;cr,cg,cb=sin(t/7)/4+0.75,sin(t/5)/4+0.75,sin(t/3)/4+0.75\""})
    );

    /**
     * 注册所有参数化模式子命令到 /particlex parameter 节点。
     *
     * @param parent 父命令节点（通常是 /particlex）
     * @param ctx    命令构建上下文，用于获取粒子类型注册表等
     */
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent, CommandBuildContext ctx) {
        // 创建 /particlex parameter 根节点
        var parameterNode = Commands.literal("parameter");
        // 为每个配置添加一个模式字面量子节点
        for (SubCommandConfig config : CONFIGS) {
            parameterNode.then(buildModeSubCommand(config, ctx));
        }
        // 将 parameter 节点挂载到父命令上
        parent.then(parameterNode);
    }

    /**
     * 根据配置构建单个模式子命令的完整树。
     * 返回的模式字面量节点下依次连接 name, pos 以及后续参数链。
     *
     * @param config 模式配置
     * @param ctx    命令构建上下文
     * @return 构建好的 LiteralArgumentBuilder（模式字面量节点）
     */
    private static LiteralArgumentBuilder<CommandSourceStack> buildModeSubCommand(SubCommandConfig config, CommandBuildContext ctx) {
        return Commands.literal(config.literal())
                .then(withParticleAndPos(buildChain(config), ctx));
    }

    /**
     * 构建 name 和 pos 参数节点，并将后续链作为 pos 的 then 分支。
     *
     * @param then 后续参数链的起始节点
     * @param ctx  命令构建上下文
     * @return name 参数节点（其下已连接 pos 和后续链）
     */
    private static ArgumentBuilder<CommandSourceStack, ?> withParticleAndPos(ArgumentBuilder<CommandSourceStack, ?> then, CommandBuildContext ctx) {
        return Commands.argument("name", ParticleArgument.particle(ctx))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                .then(then));
    }

    /**
     * 构建基础参数链：size -> color -> light -> speed -> lifetime。
     * 返回的 size 节点已通过 then 连接了后续链。
     *
     * @param then 在 lifetime 之后要连接的后续链
     * @return size 参数节点
     */
    private static ArgumentBuilder<CommandSourceStack, ?> withBaseParameters(ArgumentBuilder<CommandSourceStack, ?> then) {
        return Commands.argument("size", SizeArgumentType.size())
                .then(Commands.argument("color", Color4ArgumentType.color4())
                .then(Commands.argument("light", SuggestIntegerArgumentType.integer(-1, 15, 15))
                .then(Commands.argument("speed", Speed3ArgumentType.speed3())
                .then(Commands.argument("lifetime", SuggestIntegerArgumentType.integer(-1, Integer.MAX_VALUE, 0))
                .then(then)))));
    }

    /**
     * 构建 begin -> end -> expression -> step 链。
     * expression 节点包含两个执行分支（无 step 和有 step），并将后续链作为 step 的 then 分支。
     *
     * @param suggests expression 参数的补全建议列表
     * @param command1 无 step 版本（expression 直接执行）的命令
     * @param command2 有 step 版本（expression 后跟 step）的命令
     * @param then     step 节点后要连接的后续链
     * @return begin 参数节点
     */
    private static ArgumentBuilder<CommandSourceStack, ?> withBeginEndExpression(String[] suggests, Command<CommandSourceStack> command1, Command<CommandSourceStack> command2, ArgumentBuilder<CommandSourceStack, ?> then) {
        return Commands.argument("begin", SuggestDoubleArgumentType.doubleArg(-Double.MAX_VALUE, Double.MAX_VALUE, -10.0))
                .then(Commands.argument("end", SuggestDoubleArgumentType.doubleArg(-Double.MAX_VALUE, Double.MAX_VALUE, 10.0))
                .then(Commands.argument("expression", SuggestStringArgumentType.argument(suggests))
                .executes(command1)
                .then(Commands.argument("step", SuggestDoubleArgumentType.doubleArg(Math.ulp(0.0), Double.MAX_VALUE, 0.1))
                .executes(command2)
                .then(then))));
    }

    /**
     * 构建 cpt 参数节点，并将后续链作为其 then 分支。
     *
     * @param command 只有 cpt 参数（无后续 speed 链）时的执行命令
     * @param then    cpt 节点后要连接的后续链
     * @return cpt 参数节点
     */
    private static ArgumentBuilder<CommandSourceStack, ?> withCpt(Command<CommandSourceStack> command, ArgumentBuilder<CommandSourceStack, ?> then) {
        return Commands.argument("cpt", SuggestIntegerArgumentType.integer(1, Integer.MAX_VALUE, 10))
                .executes(command)
                .then(then);
    }

    /**
     * 构建 speedExpression -> speedStep -> group 链。
     * 返回 speedExpression 节点，其下连接了 speedStep 和 group 的可选分支。
     *
     * @param command1 只有 speedExpression 时的执行命令
     * @param command2 有 speedExpression 和 speedStep 时的执行命令
     * @param command3 同时有 speedExpression、speedStep 和 group 时的执行命令
     * @return speedExpression 参数节点
     */
    private static ArgumentBuilder<CommandSourceStack, ?> withSpeedExpressionAndGroup(Command<CommandSourceStack> command1, Command<CommandSourceStack> command2, Command<CommandSourceStack> command3) {
        return Commands.argument("speedExpression", SuggestStringArgumentType.argument("null", "\"vy=0.1\"", "\"(vx,vy,vz)=((random(),random(),random())-0.5)*t/100\""))
                .executes(command1)
                .then(Commands.argument("speedStep", SuggestDoubleArgumentType.doubleArg(Math.ulp(0.0), Double.MAX_VALUE, 1.0))
                .executes(command2)
                .then(Commands.argument("group", SuggestStringArgumentType.argument("null"))
                .executes(command3)));
    }

    /**
     * 根据配置动态构建完整的参数链。
     * 从后向前组装：先构建 speed 部分，然后根据 tick 模式决定是否插入 cpt，
     * 然后构建 expression 部分，最后根据 exp 模式决定是否添加基础参数。
     *
     * @param config 子命令配置
     * @return 完整的参数链（从第一个参数节点开始，例如 size 或 begin）
     */
    private static ArgumentBuilder<CommandSourceStack, ?> buildChain(SubCommandConfig config) {
        // 先构建最内层的 speed 部分（speedExpression -> speedStep -> group）
        ArgumentBuilder<CommandSourceStack, ?> endPart = withSpeedExpressionAndGroup(
                createExecuteCommand(config, true, false, true),               // step + speedExpression
                createExecuteCommand(config, true, false, true, true),         // step + speedExpression + speedStep
                createExecuteCommand(config, true, false, true, true, true)    // step + speedExpression + speedStep + group
        );

        // 如果是 tick 模式，需要在 step 后插入 cpt 节点
        if (config.tick()) {
            endPart = withCpt(
                    createExecuteCommand(config, true, true), // step + cpt
                    endPart
            );
        }

        // 构建中间部分：begin -> end -> expression -> step，并将 endPart 作为 step 的后续
        ArgumentBuilder<CommandSourceStack, ?> midPart = withBeginEndExpression(
                config.expressionSuggests(),
                createExecuteCommand(config, false, false), // 无 step
                createExecuteCommand(config, true, false),  // 有 step
                endPart
        );

        // 如果是 exp 模式，没有基础参数，直接返回 midPart（以 begin 开头）
        if (config.exp()) {
            return midPart;
        } else {
            // 否则在前面加上基础参数链（以 size 开头）
            return withBaseParameters(midPart);
        }
    }

    /**
     * 创建特定参数组合的执行命令。
     * 通过 flags 数组指示当前命令节点包含哪些可选参数，从而从 context 中读取相应值，
     * 缺失的参数使用默认值。
     *
     * @param config 子命令配置（提供 polar、tick、exp 标志）
     * @param flags  变长参数，按顺序表示：
     *               flags[0] - 是否有 step
     *               flags[1] - 是否有 cpt
     *               flags[2] - 是否有 speedExpression
     *               flags[3] - 是否有 speedStep
     *               flags[4] - 是否有 group
     * @return 执行命令的 Command 对象
     */
    private static Command<CommandSourceStack> createExecuteCommand(SubCommandConfig config, boolean... flags) {
        return context -> {
            // 基础必选参数
            ParticleOptions effect = ParticleArgument.getParticle(context, "name");
            Vec3 pos = Vec3Argument.getVec3(context, "pos");

            // 根据 exp 模式决定是否读取固定属性
            double size = config.exp() ? 1.0 : SizeArgumentType.getSize(context, "size");
            Vector4f color = config.exp() ? new Vector4f() : Color4ArgumentType.getColor4(context, "color");
            int light = config.exp() ? 15 : IntegerArgumentType.getInteger(context, "light");
            Vec3 speed = config.exp() ? Vec3.ZERO : Speed3ArgumentType.getSpeed3(context, "speed");
            int lifetime = config.exp() ? 0 : IntegerArgumentType.getInteger(context, "lifetime");

            // 必选的 expression 相关参数
            double begin = DoubleArgumentType.getDouble(context, "begin");
            double end = DoubleArgumentType.getDouble(context, "end");
            String expression = StringArgumentType.getString(context, "expression");

            // 可选参数，根据 flags 读取，否则使用默认值
            double step = flags.length > 0 && flags[0] ? DoubleArgumentType.getDouble(context, "step") : 0.1;
            int cpt = flags.length > 1 && flags[1] ? IntegerArgumentType.getInteger(context, "cpt") : 10;
            String speedExpression = flags.length > 2 && flags[2] ? StringArgumentType.getString(context, "speedExpression") : null;
            double speedStep = flags.length > 3 && flags[3] ? DoubleArgumentType.getDouble(context, "speedStep") : 1.0;
            String group = flags.length > 4 && flags[4] ? StringArgumentType.getString(context, "group") : null;

            // 发送网络包到客户端
            PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(),
                    new ParameterPayload(config.polar(), config.tick(), config.exp(), effect, pos, size, color, light, speed, lifetime, begin, end, expression, step, cpt, speedExpression, speedStep, group));
            return 1;
        };
    }
}