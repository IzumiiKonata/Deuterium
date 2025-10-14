package tech.konata.obfuscator.transformers.obfuscators;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import tech.konata.obfuscator.asm.ClassWrapper;
import tech.konata.obfuscator.asm.MethodWrapper;
import tech.konata.obfuscator.exclusions.ExclusionType;
import tech.konata.obfuscator.transformers.Transformer;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author IzumiiKonata
 * Date: 2025/7/12 07:41
 */
public class ParameterHider extends Transformer {

    // 存储被 lambda 或 InvokeDynamic 引用的方法
    private Set<String> lambdaReferencedMethods = new HashSet<>();

    // 存储已经处理过的方法, 避免重复处理
    private Set<String> processedMethods = new HashSet<>();

    @Override
    protected ExclusionType getExclusionType() {
        return ExclusionType.PARAMETER_HIDER;
    }

    private ClassNode loadClassNode(String className) {
        ClassWrapper classWrapper = this.getClasses().get(className);

        if (classWrapper == null) {
            ClassWrapper classWrapper1 = this.getClassPath().get(className);

            if (classWrapper1 == null) {
                dbg("Class not found: " + className);
            } else {
                return classWrapper1.getClassNode();
            }

            return null;
        }

        return classWrapper.getClassNode();
    }

    /**
     * 判断类是否是 library node (外部库类)
     */
    private boolean isLibraryClass(String className) {
        // 如果类在 classes 中则不是库节点
        if (this.getClasses().containsKey(className)) {
            return false;
        }
        // 如果只在 classpath 中, 说明是库节点
        return this.getClassPath().containsKey(className);
    }

    /**
     * 查找指定方法
     */
    private static MethodNode findMethod(ClassNode classNode, String name, String desc) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 判断方法是否继承自library类
     */
    public boolean isInheritedFromLibrary(String className, String methodName, String methodDesc) {
        ClassNode classNode = loadClassNode(className);
        if (classNode == null) {
            return false;
        }

        MethodNode targetMethod = findMethod(classNode, methodName, methodDesc);
        if (targetMethod == null) {
            return false;
        }

        if (isStaticOrConstructor(targetMethod)) {
            return false;
        }

        // 检查父类
        if (hasMethodInLibrarySuperClass(classNode.superName, methodName, methodDesc)) {
            return true;
        }

        // 检查接口
        if (classNode.interfaces != null) {
            for (String interfaceName : classNode.interfaces) {
                if (hasMethodInLibraryInterface(interfaceName, methodName, methodDesc)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 收集整个方法树中的所有需要 transform 的方法
     */
    private Set<String> collectMethodTree(String className, String methodName, String methodDesc) {
        Set<String> methodTree = new HashSet<>();
        Set<String> visitedClasses = new HashSet<>(); // 记录已访问的类, 避免循环
        collectMethodTreeRecursive(className, methodName, methodDesc, methodTree, visitedClasses);
        return methodTree;
    }

    /**
     * 递归收集方法树
     */
    private void collectMethodTreeRecursive(String className, String methodName, String methodDesc,
                                            Set<String> methodTree, Set<String> visitedClasses) {
        if (className == null || "java/lang/Object".equals(className)) {
            return;
        }

        // 如果已经访问过这个类, 避免循环
        if (visitedClasses.contains(className)) {
            return;
        }
        visitedClasses.add(className);

        // 如果是 library 类, 停止收集
        if (isLibraryClass(className)) {
            return;
        }

        ClassNode classNode = loadClassNode(className);
        if (classNode == null) {
            return;
        }

        // 检查当前类是否有这个方法
        MethodNode method = findMethod(classNode, methodName, methodDesc);
        if (method != null) {
            if (!isPrivate(method) && !isStaticOrConstructor(method)) {
                String methodKey = className + "." + methodName + methodDesc;
                methodTree.add(methodKey);
                dbg("Added to method tree: " + methodKey);
            }
        }

        // 递归检查父类
        if (classNode.superName != null && !visitedClasses.contains(classNode.superName)) {
            collectMethodTreeRecursive(classNode.superName, methodName, methodDesc, methodTree, visitedClasses);
        }

        // 递归检查接口
        if (classNode.interfaces != null) {
            for (String interfaceName : classNode.interfaces) {
                if (!visitedClasses.contains(interfaceName)) {
                    collectMethodTreeRecursive(interfaceName, methodName, methodDesc, methodTree, visitedClasses);
                }
            }
        }

        // 检查子类
        for (ClassWrapper cw : this.getClasses().values()) {
            ClassNode cn = cw.getClassNode();
            // 避免访问已经处理过的类
            if (!visitedClasses.contains(cn.name)) {
                if (className.equals(cn.superName) || (cn.interfaces != null && cn.interfaces.contains(className))) {
                    collectMethodTreeRecursive(cn.name, methodName, methodDesc, methodTree, visitedClasses);
                }
            }
        }
    }

    /**
     * 递归检查library接口链中是否存在指定方法
     */
    private boolean hasMethodInLibraryInterface(String interfaceName, String methodName, String methodDesc) {
        if (interfaceName == null) {
            return false;
        }

        ClassNode interfaceNode = loadClassNode(interfaceName);
        if (interfaceNode == null) {
            return false;
        }

        // 如果接口本身是library类
        if (isLibraryClass(interfaceName)) {
            MethodNode method = findMethod(interfaceNode, methodName, methodDesc);
            if (method != null) {
                dbg("Found method in library interface: " + interfaceName + " - " + method.name + ", " + method.desc);
                return true;
            }
        }

        // 递归检查父接口
        if (interfaceNode.interfaces != null) {
            for (String parentInterface : interfaceNode.interfaces) {
                if (hasMethodInLibraryInterface(parentInterface, methodName, methodDesc)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 递归检查library父类链中是否存在指定方法
     */
    private boolean hasMethodInLibrarySuperClass(String className, String methodName, String methodDesc) {
        if (className == null || "java/lang/Object".equals(className)) {
            return false;
        }

        ClassNode classNode = loadClassNode(className);
        if (classNode == null) {
            return false;
        }

        // 如果父类是 library 类
        if (isLibraryClass(className)) {
            MethodNode method = findMethod(classNode, methodName, methodDesc);
            if (method != null && !isPrivate(method)) {
                dbg("Found method in library superclass: " + className + " - " + method.name + ", " + method.desc);
                return true;
            }
        }

        // 递归检查父类
        if (hasMethodInLibrarySuperClass(classNode.superName, methodName, methodDesc)) {
            return true;
        }

        // 也要检查父类实现的接口
        if (classNode.interfaces != null) {
            for (String interfaceName : classNode.interfaces) {
                if (hasMethodInLibraryInterface(interfaceName, methodName, methodDesc)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断是否为静态方法或构造函数
     */
    private static boolean isStaticOrConstructor(MethodNode method) {
        return (method.access & Opcodes.ACC_STATIC) != 0 || "<init>".equals(method.name) || "<clinit>".equals(method.name);
    }

    /**
     * 判断是否为私有方法
     */
    private static boolean isPrivate(MethodNode method) {
        return (method.access & Opcodes.ACC_PRIVATE) != 0;
    }

    /**
     * 收集所有被 lambda 或 InvokeDynamic 引用的方法
     */
    private void collectLambdaReferencedMethods() {
        dbg("Collecting lambda and InvokeDynamic referenced methods...");

        this.getClassWrappers().stream()
                .filter(classWrapper -> !excluded(classWrapper))
                .filter(wrapper -> !wrapper.getName().startsWith("obfuscated/by/IzumiKonata/"))
                .forEach(classWrapper ->
                        classWrapper.methods.stream()
                                .filter(methodWrapper -> !excluded(methodWrapper) && hasInstructions(methodWrapper.methodNode))
                                .forEach(methodWrapper -> {
                                    MethodNode methodNode = methodWrapper.methodNode;
                                    InsnList instructions = methodNode.instructions;

                                    for (AbstractInsnNode instruction : instructions) {
                                        if (instruction instanceof InvokeDynamicInsnNode) {
                                            InvokeDynamicInsnNode invokeDynamicInsnNode = (InvokeDynamicInsnNode) instruction;

                                            // 检查 bsm 中的 Handle
                                            for (Object bsmArg : invokeDynamicInsnNode.bsmArgs) {
                                                if (bsmArg instanceof Handle) {
                                                    Handle handle = (Handle) bsmArg;
                                                    String methodKey = handle.getOwner() + "." + handle.getName() + handle.getDesc();
                                                    lambdaReferencedMethods.add(methodKey);
                                                    dbg("Found lambda/InvokeDynamic referenced method: " + methodKey);
                                                }
                                            }
                                        }
                                    }
                                })
                );

        dbg("Total lambda/InvokeDynamic referenced methods found: " + lambdaReferencedMethods.size());
    }

    /**
     * 检查方法是否被 lambda 或 InvokeDynamic 引用
     */
    private boolean isLambdaReferencedMethod(String owner, String name, String desc) {
        String methodKey = owner + "." + name + desc;
        return lambdaReferencedMethods.contains(methodKey);
    }

    private boolean canTreeBeTransformed(List<String> params, Set<String> methodTree) {

        for (String treeMethodKey : methodTree) {

            String[] parts = treeMethodKey.split("\\.");
            String treeClassName = parts[0];
            String treeMethodName = parts[1].substring(0, parts[1].indexOf("("));
            String treeMethodDesc = parts[1].substring(parts[1].indexOf("("));

            ClassWrapper treeClassWrapper = this.getClasses().get(treeClassName);
            if (treeClassWrapper == null) {
                continue;
            }

            MethodWrapper treeMethodWrapper = treeClassWrapper.methods.stream()
                    .filter(mw -> mw.methodNode.name.equals(treeMethodName) && mw.methodNode.desc.equals(treeMethodDesc))
                    .findFirst()
                    .orElse(null);

            if (treeMethodWrapper == null) {
                continue;
            }

            MethodNode treeMethodNode = treeMethodWrapper.methodNode;
            String modifiedDesc = this.modifyDesc(treeMethodDesc, params);

            // 如果参数中没有任何可以被改写成 Object 的, 或无法 transform 就返回 false
            if (params.stream().noneMatch(p -> p.endsWith(";")) || !(transformable(treeMethodNode, modifiedDesc, treeClassWrapper.methods) && this.tryTransform(treeMethodNode, params))) {
                return false;
            }

        }

        return true;
    }

    @Override
    public void transform() {
        // 首先收集所有被 lambda 或 InvokeDynamic 引用的方法
        // 呵呵 因为我是 asm 低手, transform λ 或者 indy 方法都会炸掉
        collectLambdaReferencedMethods();

        Map<String, Map<String, Map<String, String>>> modifiedMethodMap = new HashMap<>();

        // 修改方法签名
        this.getClassWrappers().stream()
                .filter(classWrapper -> !excluded(classWrapper) && !classWrapper.getName().endsWith("KonataShield"))
                .forEach(classWrapper ->
                        classWrapper.methods.stream()
                                .filter(methodWrapper -> !excluded(methodWrapper)
                                        && hasInstructions(methodWrapper.methodNode))
                                .forEach(methodWrapper -> {
                                    MethodNode methodNode = methodWrapper.methodNode;
                                    String desc = methodNode.desc;
                                    String methodKey = classWrapper.getName() + "." + methodNode.name + desc;

                                    // 如果已经处理过, 跳过
                                    if (processedMethods.contains(methodKey)) {
                                        dbg("Method already processed: " + methodKey);
                                        return;
                                    }

                                    // 检查是否被 lambda 或 InvokeDynamic 引用
                                    if (isLambdaReferencedMethod(classWrapper.getName(), methodNode.name, desc)) {
                                        dbg("Method: " + methodNode.name + " in class: " + classWrapper.classNode.name + " is referenced by lambda/InvokeDynamic, skipping");
                                        return;
                                    }

                                    // 检查是否继承自 library 类
                                    if (isInheritedFromLibrary(classWrapper.getName(), methodNode.name, desc)) {
                                        dbg("Method: " + methodNode.name + " in class: " + classWrapper.classNode.name + " is inherited from library class, skipping");
                                        return;
                                    }

                                    // skip if there's no parameter
                                    if (desc.startsWith("()"))
                                        return;

                                    List<String> params = this.extractParameterFromDesc(desc);

                                    if (params.isEmpty())
                                        return;

                                    // 收集整个方法树
                                    Set<String> methodTree;

                                    // 如果这个方法不能被重载则直接将其添加到方法树里然后返回
                                    if (isStaticOrConstructor(methodWrapper.methodNode) || methodWrapper.isPrivate()) {
                                        methodTree = new HashSet<>();
                                        methodTree.add(methodKey);
                                    } else {
                                        methodTree = collectMethodTree(classWrapper.getName(), methodNode.name, desc);
                                    }

                                    if (!this.canTreeBeTransformed(params, methodTree)) {

                                        for (String treeMethodKey : methodTree) {
                                            if (processedMethods.contains(treeMethodKey)) {
                                                continue;
                                            }
                                            processedMethods.add(treeMethodKey);
                                        }

                                        return;
                                    }

                                    // 处理整个方法树
                                    for (String treeMethodKey : methodTree) {
                                        if (processedMethods.contains(treeMethodKey)) {
                                            continue;
                                        }

                                        String[] parts = treeMethodKey.split("\\.");
                                        String treeClassName = parts[0];
                                        String treeMethodName = parts[1].substring(0, parts[1].indexOf("("));
                                        String treeMethodDesc = parts[1].substring(parts[1].indexOf("("));

                                        ClassWrapper treeClassWrapper = this.getClasses().get(treeClassName);
                                        if (treeClassWrapper == null) {
                                            continue;
                                        }

                                        MethodWrapper treeMethodWrapper = treeClassWrapper.methods.stream()
                                                .filter(mw -> mw.methodNode.name.equals(treeMethodName) && mw.methodNode.desc.equals(treeMethodDesc))
                                                .findFirst()
                                                .orElse(null);

                                        if (treeMethodWrapper == null) {
                                            continue;
                                        }

                                        MethodNode treeMethodNode = treeMethodWrapper.methodNode;

                                        dbg("Processing method in tree: " + treeMethodKey);
                                        dbg("    Desc: " + treeMethodDesc);
                                        List<String> strings = this.extractParameterFromDesc(treeMethodDesc);
                                        dbg("    Params: " + Arrays.toString(strings.toArray()));
                                        String modifiedDesc = this.modifyDesc(treeMethodDesc, params);
                                        dbg("    Modified: " + modifiedDesc);

                                        if (params.stream().anyMatch(p -> p.endsWith(";")) && transformable(treeMethodNode, modifiedDesc, treeClassWrapper.methods) && this.transformMethod(treeMethodNode, modifiedDesc, params)) {
                                            Map<String, Map<String, String>> nameToDescToModifiedDescMap =
                                                    modifiedMethodMap.computeIfAbsent(treeClassName, s -> new HashMap<>());
                                            Map<String, String> descToModifiedDescMap =
                                                    nameToDescToModifiedDescMap.computeIfAbsent(treeMethodName, s -> new HashMap<>());
                                            descToModifiedDescMap.put(treeMethodDesc, modifiedDesc);

                                            processedMethods.add(treeMethodKey);
                                            dbg("Put: " + treeClassName + " -> " + treeMethodName + " -> " + treeMethodDesc + " -> " + modifiedDesc);
                                        }
                                    }
                                })
                );

        // 然后修改所有对于这些 method 的调用
        updateMethodInstructions(modifiedMethodMap);
    }

    private void dbg(String text) {
        
        if (true)
            System.out.println(text);
    }

    private String findActualMethodOwner(String currentOwner, String methodName, String methodDesc, Map<String, Map<String, Map<String, String>>> modifiedMethodMap) {
        if (modifiedMethodMap.containsKey(currentOwner)) {
            Map<String, Map<String, String>> nameToDescMap = modifiedMethodMap.get(currentOwner);
            if (nameToDescMap.containsKey(methodName) && nameToDescMap.get(methodName).containsKey(methodDesc)) {
                return currentOwner;
            }
        }

        return findMethodOwnerInHierarchy(currentOwner, methodName, methodDesc, modifiedMethodMap);
    }

    /**
     * 在继承层次结构中查找方法的真实 owner
     */
    private String findMethodOwnerInHierarchy(String className, String methodName, String methodDesc, Map<String, Map<String, Map<String, String>>> modifiedMethodMap) {
        if (className == null || "java/lang/Object".equals(className)) {
            return null;
        }

        ClassNode classNode = loadClassNode(className);
        if (classNode == null) {
            return null;
        }

        // 检查当前类是否包含被修改的类
        if (modifiedMethodMap.containsKey(className)) {
            Map<String, Map<String, String>> nameToDescMap = modifiedMethodMap.get(className);
            if (nameToDescMap.containsKey(methodName) && nameToDescMap.get(methodName).containsKey(methodDesc)) {
                return className;
            }
        }

        // 检查当前类是否定义了这个方法 (即使没有被修改)
        MethodNode method = findMethod(classNode, methodName, methodDesc);
        if (method != null && !isPrivate(method)) {
            // 找到了方法定义, 但没有被修改, 返回 null 表示不需要修改调用
            return null;
        }

        // 继续向上查找父类
        String parentOwner = findMethodOwnerInHierarchy(classNode.superName, methodName, methodDesc, modifiedMethodMap);
        if (parentOwner != null) {
            return parentOwner;
        }

        // 查找接口
        if (classNode.interfaces != null) {
            for (String interfaceName : classNode.interfaces) {
                String interfaceOwner = findMethodOwnerInHierarchy(interfaceName, methodName, methodDesc, modifiedMethodMap);
                if (interfaceOwner != null) {
                    return interfaceOwner;
                }
            }
        }

        return null;
    }

    /**
     * 获取修改后的方法描述符
     */
    private String getModifiedMethodDesc(String owner, String methodName, String originalDesc, Map<String, Map<String, Map<String, String>>> modifiedMethodMap) {
        Map<String, Map<String, String>> nameToDescMap = modifiedMethodMap.get(owner);
        if (nameToDescMap != null) {
            Map<String, String> descToModifiedDescMap = nameToDescMap.get(methodName);
            if (descToModifiedDescMap != null) {
                return descToModifiedDescMap.get(originalDesc);
            }
        }
        return null;
    }

    private void updateMethodInstructions(Map<String, Map<String, Map<String, String>>> modifiedMethodMap) {
        this.getClassWrappers()
                .forEach(classWrapper ->
                        classWrapper.methods.stream()
                                .filter(methodWrapper -> !excluded(methodWrapper) && hasInstructions(methodWrapper.methodNode))
                                .forEach(methodWrapper -> {
                                    MethodNode methodNode = methodWrapper.methodNode;
                                    InsnList instructions = methodNode.instructions;

                                    for (AbstractInsnNode instruction : instructions) {
                                        if (instruction instanceof MethodInsnNode) {
                                            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;

                                            String actualOwner = null;
                                            String modifiedDesc = null;

                                            switch (methodInsnNode.getOpcode()) {
                                                case Opcodes.INVOKEVIRTUAL:
                                                case Opcodes.INVOKEINTERFACE:
                                                    // 虚拟方法调用查找继承链
                                                    actualOwner = findActualMethodOwner(
                                                            methodInsnNode.owner,
                                                            methodInsnNode.name,
                                                            methodInsnNode.desc,
                                                            modifiedMethodMap
                                                    );
                                                    break;

                                                case Opcodes.INVOKESTATIC:
                                                case Opcodes.INVOKESPECIAL:
                                                    // 静态方法和特殊方法调用查找指定的 owner
                                                    if (modifiedMethodMap.containsKey(methodInsnNode.owner)) {
                                                        actualOwner = methodInsnNode.owner;
                                                    }
                                                    break;
                                            }

                                            // 找到了实际的owner, 获取修改后的描述符
                                            if (actualOwner != null) {
                                                modifiedDesc = getModifiedMethodDesc(
                                                        actualOwner,
                                                        methodInsnNode.name,
                                                        methodInsnNode.desc,
                                                        modifiedMethodMap
                                                );
                                            }

                                            // 找到了修改后的描述符, 更新方法调用
                                            if (modifiedDesc != null) {
                                                // 虚拟方法调用, 使用原始的 owner, 但使用修改后的描述符
                                                String finalOwner = (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL ||
                                                        methodInsnNode.getOpcode() == Opcodes.INVOKEINTERFACE) ?
                                                        methodInsnNode.owner : actualOwner;

                                                instructions.set(instruction, new MethodInsnNode(
                                                        instruction.getOpcode(),
                                                        finalOwner,
                                                        methodInsnNode.name,
                                                        modifiedDesc
                                                ));

                                                dbg("Updated method call:");
                                                dbg("  Original: " + methodInsnNode.owner + "." + methodInsnNode.name + methodInsnNode.desc);
                                                dbg("  Modified: " + finalOwner + "." + methodInsnNode.name + modifiedDesc);
                                                dbg("  Actual owner found: " + actualOwner);
                                                dbg("  Called from: " + methodWrapper.methodNode.name + " in " + classWrapper.classNode.name);
                                            } else {
                                                dbg("Modified desc not found for function: " + methodInsnNode.name + "(Actual owner: " + actualOwner + "), in " + classWrapper.classNode.name + "(Originally " + classWrapper.originalName + "), desc: " + methodInsnNode.desc);
                                            }
                                        }
                                        // TODO: transform InvokeDynamicInsnNode
                                    }
                                })
                );
    }

    private boolean tryTransform(MethodNode node, List<String> params) {

        InsnList instructions = node.instructions;

        boolean isStatic = Modifier.isStatic(node.access);

        // 检查是否有对参数的赋值操作
        for (AbstractInsnNode insn : instructions) {
            if (insn instanceof VarInsnNode) {
                VarInsnNode varInsnNode = (VarInsnNode) insn;

                if (varInsnNode.getOpcode() == ASTORE) {
                    int paramIdx = varInsnNode.var - (isStatic ? 0 : 1);

                    if (paramIdx >= 0 && paramIdx < params.size()) {
                        dbg("    ASTORE to param detected - not modifying");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean transformMethod(MethodNode node, String modifiedDesc, List<String> params) {
        InsnList instructions = node.instructions;

        boolean isStatic = Modifier.isStatic(node.access);

        if (!tryTransform(node, params))
            return false;

        dbg("    Transforming");

        node.desc = modifiedDesc;

        // double 和 long primitive 占用两个 aload 槽位
        // 呵呵 我没看过 jvm 字节码规范 这个问题困扰了我两个小时
        List<String> fixedParams = new ArrayList<>();
        for (String param : params) {
            String paramType = getTypeForCast(param);
            fixedParams.add(param);
            if (paramType.equals("D") || paramType.equals("J")) {
                fixedParams.add(param);
            }
        }

        if (params.size() != fixedParams.size()) {
            dbg("    Fixed params: " + Arrays.toString(fixedParams.toArray()));
        }

        // 创建参数到原始类型的映射
        Map<Integer, String> paramTypeMap = new HashMap<>();
        for (int i = 0; i < fixedParams.size(); i++) {
            String param = fixedParams.get(i);
            String paramType = getTypeForCast(param);
            paramTypeMap.put(i, paramType);
        }

        // 遍历所有指令, 处理 aload 指令
        AbstractInsnNode currentInsn = instructions.getFirst();
        while (currentInsn != null) {
            AbstractInsnNode nextInsn = currentInsn.getNext();

            if (currentInsn instanceof VarInsnNode) {
                VarInsnNode varInsnNode = (VarInsnNode) currentInsn;

                if (varInsnNode.getOpcode() == Opcodes.ALOAD) {
                    dbg("Found VarInsnNode, var: " + varInsnNode.var);
                    int paramIdx = varInsnNode.var;

                    String paramType = paramTypeMap.get(paramIdx - (isStatic ? 0 : 1));
                    dbg("    Param: ");
                    dbg("        Static: true");
                    dbg("        Idx: " + (paramIdx));
                    dbg("        Type: " + paramType);

                    // 在ALOAD指令后插入类型转换
                    if (paramType != null) {
                        if (paramType.length() != 1 && paramType.contains("/")) {
                            instructions.insert(currentInsn, new TypeInsnNode(Opcodes.CHECKCAST, paramType));
                        }
                    }
                }
            }

            currentInsn = nextInsn;
        }

        return true;
    }

    /**
     * 获取用于类型转换的类型字符串
     */
    private String getTypeForCast(String param) {
        if (param.startsWith("[")) {
            // 数组类型
            return param;
        } else if (param.length() == 1) {
            // 原始类型
            return param;
        } else {
            // 引用类型, 去掉 L 和 ;
            return param.substring(1, param.length() - 1);
        }
    }

    private String modifyDesc(String desc, List<String> params) {
        StringBuilder sb = new StringBuilder();

        sb.append("(");

        for (String param : params) {
            if (param.length() == 1) {
                // 原始类型保持不变
                sb.append(param);
            } else if (param.startsWith("[")) {
                // 数组类型保持不变
                sb.append(param);
            } else {
                // 引用类型改为Object
                sb.append("Ljava/lang/Object;");
            }
        }

        sb.append(desc.substring(desc.lastIndexOf(")")));

        return sb.toString();
    }

    private boolean transformable(MethodNode methodNode, String d, List<MethodWrapper> methods) {

        for (MethodWrapper method : methods) {
            MethodNode node = method.methodNode;

            if (methodNode == node)
                continue;

            String desc = node.desc;

            List<String> params = this.extractParameterFromDesc(desc);
            String modifiedDesc = this.modifyDesc(desc, params);

            // 对 access 的判断
            // 检查是否有相同的方法名、相同的修改后描述符, 以及相同的静态/非静态属性
            boolean sameName = methodNode.name.equals(node.name);
            boolean sameModifiedDesc = d.equals(modifiedDesc);

            // 冲突提示
            // 懒得搞自动冲突解决了,,,
            if (sameName && sameModifiedDesc) {
                dbg("Method transformation conflict detected:");
                dbg("  Method 1: " + methodNode.name + " " + methodNode.desc + " (access: " + methodNode.access + ")");
                dbg("  Method 2: " + node.name + " " + node.desc + " (access: " + node.access + ")");
                dbg("  Modified desc would be: " + modifiedDesc);
                return false;
            }
        }

        return true;
    }

    private List<String> extractParameterFromDesc(String desc) {

        List<String> parameters = new ArrayList<>();

        // trimming
        String params = desc.substring(1, desc.lastIndexOf(")"));

        char[] charArray = params.toCharArray();
        int arrayDepth = 0;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];

            StringBuilder type;

            if (c == 'L') {
                int endIndex = params.indexOf(";", i + 1);
                type = new StringBuilder((params.substring(i, endIndex + 1)));
                i += (endIndex - i);
            }

            else if (c == 'I') {
                type = new StringBuilder(("I"));
            }

            else if (c == 'F') {
                type = new StringBuilder(("F"));
            }

            else if (c == 'D') {
                type = new StringBuilder(("D"));
            }

            else if (c == 'Z') {
                type = new StringBuilder(("Z"));
            }

            else if (c == 'B') {
                type = new StringBuilder(("B"));
            }

            else if (c == 'S') {
                type = new StringBuilder(("S"));
            }

            else if (c == 'C') {
                type = new StringBuilder(("C"));
            }

            else if (c == 'V') {
                throw new IllegalArgumentException("WTF?! type = void.class!");
            }

            else if (c == 'J') {
                type = new StringBuilder(("J"));
            }

            else if (c == '[') {
                arrayDepth ++;
                continue;
            } else {
                throw new IllegalStateException("Unknown type: " + c);
            }

            if (arrayDepth > 0) {

                for (int j = 0; j < arrayDepth; j++) {
                    type.insert(0, "[");
                }

                arrayDepth = 0;
            }

            parameters.add(type.toString());
        }

        return parameters;
    }

    @Override
    public String getName() {
        return "Parameter Hider";
    }
}