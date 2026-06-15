package org.weever.gravitymod.compat;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.weever.gravitymod.GravityMod;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GravityCompatMixinPlugin implements IMixinConfigPlugin {
    private final Set<String> appliedModFixes = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final String CLASS_DEPENDENT_MIXIN = descriptorOf(ClassDependentMixin.class);
    private static final String METHOD_DEPENDENT_MIXIN = descriptorOf(MethodDependentMixin.class);
    private static final String FIELD_DEPENDENT_MIXIN = descriptorOf(FieldDependentMixin.class);

    private static String descriptorOf(Class<?> annotation) {
        return "L" + annotation.getName().replace('.', '/') + ";";
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        try {
            ClassNode mixinClass = MixinService.getService().getBytecodeProvider().getClassNode(mixinClassName);

            if (mixinClass.visibleAnnotations != null) {
                for (AnnotationNode annotation : mixinClass.visibleAnnotations) {
                    if (CLASS_DEPENDENT_MIXIN.equals(annotation.desc)) {
                        if (!classExists(stringValue(annotation))) {
                            return false;
                        }
                    } else if (METHOD_DEPENDENT_MIXIN.equals(annotation.desc)) {
                        if (!methodExists(targetClassName, stringValue(annotation))) {
                            return false;
                        }
                    } else if (FIELD_DEPENDENT_MIXIN.equals(annotation.desc)) {
                        if (!fieldExists(targetClassName, stringValue(annotation))) {
                            return false;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            GravityMod.LOGGER.info("[GravityCompat] Skipping mixin '{}': {}", mixinClassName, t.toString());
            return false;
        }

        logAppliedPatch(mixinClassName);
        return true;
    }

    private void logAppliedPatch(String mixinClassName) {
        String prefix = "org.weever.gravitymod.compat.";
        if (!mixinClassName.startsWith(prefix)) {
            return;
        }
        String mod = mixinClassName.substring(prefix.length()).split("\\.")[0];
        if (appliedModFixes.add(mod)) {
            GravityMod.LOGGER.info("[GravityCompat] Applying '{}' compatibility patch", mod);
        }
    }

    private static String stringValue(AnnotationNode annotation) {
        return (String) annotation.values.get(1);
    }

    private static boolean classExists(String className) {
        try {
            MixinService.getService().getBytecodeProvider().getClassNode(className);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean methodExists(String targetClassName, String methodName) {
        try {
            ClassNode target = MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
            return target.methods.stream().anyMatch(m -> methodName.equals(m.name));
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean fieldExists(String targetClassName, String fieldName) {
        try {
            ClassNode target = MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
            return target.fields.stream().anyMatch(f -> fieldName.equals(f.name));
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}