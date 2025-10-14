package tech.konata.obfuscator.utils;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.Remapper;
import tech.konata.obfuscator.asm.ClassWrapper;
import tech.konata.obfuscator.asm.FieldWrapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RadonRemapper extends Remapper {

    private final Map<String, String> mappings = new ConcurrentHashMap<>();
    private final Map<String, ClassHierarchyInfo> hierarchyInfo = new ConcurrentHashMap<>();
    private final Set<String> libraryClasses = ConcurrentHashMap.newKeySet();

    private final Callback callback;

    // 添加一个映射来跟踪每个类中的lambda表达式重命名
    private final Map<String, Map<String, String>> lambdaMappings = new ConcurrentHashMap<>();
    
    // 添加一个名称生成器用于lambda表达式
    private final SequencedNameGenerator nameGenerator = new SequencedNameGenerator();

    public interface Callback {
        ClassWrapper getClassWrapper(String className);
        Collection<ClassWrapper> getAllClasses();
        boolean isLibraryClass(String className);
    }

    public RadonRemapper(Callback callback) {
        this.callback = callback;
        buildHierarchyInfo();
    }

    private void buildHierarchyInfo() {
        callback.getAllClasses().forEach(classWrapper -> {
            String className = classWrapper.getOriginalName();
            ClassHierarchyInfo info = new ClassHierarchyInfo();

            // 构建父类链
            String currentClass = className;
            Set<String> visitedClasses = new HashSet<>();
            
            while (currentClass != null && !visitedClasses.contains(currentClass)) {
                visitedClasses.add(currentClass);
                ClassWrapper current = callback.getClassWrapper(currentClass);
                if (current == null) {
                    // 在输入类或类路径中都找不到该类
                    break;
                }

                // 检查是否为库类
                if (current.isLibraryNode() || callback.isLibraryClass(currentClass)) {
                    libraryClasses.add(currentClass);
                    break;
                }

                info.superClasses.add(currentClass);
                currentClass = current.getSuperName();
            }

            // 构建接口层次结构
            Queue<String> interfaceQueue = new LinkedList<>(classWrapper.getInterfaceNames());
            Set<String> processedInterfaces = new HashSet<>();
            
            while (!interfaceQueue.isEmpty()) {
                String iface = interfaceQueue.poll();
                if (processedInterfaces.contains(iface)) {
                    continue;
                }
                processedInterfaces.add(iface);
                
                if (info.interfaces.add(iface)) {
                    // 检查是否为库类
                    ClassWrapper ifaceWrapper = callback.getClassWrapper(iface);
                    if (ifaceWrapper != null && (ifaceWrapper.isLibraryNode() || callback.isLibraryClass(iface))) {
                        libraryClasses.add(iface);
                    } else if (ifaceWrapper != null) {
                        interfaceQueue.addAll(ifaceWrapper.getInterfaceNames());
                    } else {
                        libraryClasses.add(iface);
                    }
                }
            }

            hierarchyInfo.put(className, info);
        });
    }

    @Override
    public String map(String key) {
        return mappings.get(key);
    }

    public void addMapping(String oldName, String newName) {
        mappings.put(oldName, newName);
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        // 不重命名特殊方法（构造函数和静态初始化块）
        if (name.startsWith("<")) {
            return name;
        }

        // 不重命名库类方法
        if (libraryClasses.contains(owner) || callback.isLibraryClass(owner)) {
            return name;
        }

        // 对于匿名类，特别处理实现接口的方法
        // 匿名类通常具有类似 "ClassName$Number" 的名称格式
        if (owner.contains("$")) {
            // 检查该类是否实现接口
            ClassWrapper ownerWrapper = callback.getClassWrapper(owner);
            if (ownerWrapper != null) {
                for (String ifaceName : ownerWrapper.getInterfaceNames()) {
                    // 如果实现的接口是库类，则不重命名任何方法
                    if (libraryClasses.contains(ifaceName) || callback.isLibraryClass(ifaceName)) {
                        ClassWrapper ifaceWrapper = callback.getClassWrapper(ifaceName);
                        if (ifaceWrapper != null && hasMethod(ifaceWrapper, name, descriptor)) {
                            return name; // 保持实现接口的方法名不变
                        }
                    }
                }
            }
        }

        // 检查是否在继承链中重写库类方法
        ClassWrapper currentClass = callback.getClassWrapper(owner);
        while (currentClass != null) {
            String className = currentClass.getOriginalName();
            
            // 如果当前类是库类，检查是否包含该方法
            if (libraryClasses.contains(className) || callback.isLibraryClass(className)) {
                if (hasMethod(currentClass, name, descriptor)) {
                    return name; // 保持重写库类方法的名称不变
                }
                break;
            }
            
            // 检查当前类实现的接口
            for (String ifaceName : currentClass.getInterfaceNames()) {
                if (libraryClasses.contains(ifaceName) || callback.isLibraryClass(ifaceName)) {
                    ClassWrapper ifaceWrapper = callback.getClassWrapper(ifaceName);
                    if (ifaceWrapper != null && hasMethod(ifaceWrapper, name, descriptor)) {
                        return name; // 保持实现库接口方法的名称不变
                    }
                }
            }
            
            // 移动到父类
            String superName = currentClass.getSuperName();
            if (superName != null) {
                currentClass = callback.getClassWrapper(superName);
            } else {
                break;
            }
        }

        // 原始映射逻辑
        String key = owner + "." + name + descriptor;
        String mapped = mappings.get(key);
        if (mapped != null) {
            return mapped;
        }

        // 如果在继承链中找不到需要保持原名的方法，则使用默认处理
        return name;
    }

    /**
     * 特殊处理lambda表达式的方法名映射
     * 确保同一个类中的不同lambda表达式获得不同的重命名
     */
    /**
     * 获取lambda表达式的映射名称
     * 如果该lambda表达式已经被映射，则返回映射名称
     * 否则返回原始名称（不重命名lambda表达式）
     */
    private String getMappedLambdaName(String owner, String name, String descriptor) {
        String key = owner + "." + name + descriptor;
        return mappings.getOrDefault(key, name);
    }

    private boolean hasMethod(ClassWrapper wrapper, String name, String descriptor) {
        return wrapper.getMethods().stream()
                .anyMatch(m -> m.getOriginalName().equals(name) &&
                        m.getOriginalDescriptor().equals(descriptor));
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        // 不重命名库类字段
        if (libraryClasses.contains(owner) || callback.isLibraryClass(owner)) {
            return name;
        }

        String key = owner + "." + name + " " + descriptor;
        String mapped = mappings.get(key);
        if (mapped != null) {
            return mapped;
        }

        // 检查父类中的继承字段
        ClassHierarchyInfo info = hierarchyInfo.get(owner);
        if (info != null) {
            for (String superClass : info.superClasses) {
                // 检查父类是否为库类
                if (libraryClasses.contains(superClass) || callback.isLibraryClass(superClass)) {
                    // 检查库类是否包含该字段
                    ClassWrapper superWrapper = callback.getClassWrapper(superClass);
                    if (superWrapper != null) {
                        for (FieldWrapper superField : superWrapper.getFields()) {
                            if (superField.getOriginalName().equals(name) &&
                                    superField.getOriginalType().equals(descriptor)) {
                                // 库类有这个字段，不应该重命名
                                return name;
                            }
                        }
                    }
                } else {
                    // 输入类父类
                    String superKey = superClass + "." + name + " " + descriptor;
                    mapped = mappings.get(superKey);
                    if (mapped != null) {
                        // 如果父类字段已经被映射，则使用相同的映射
                        // 同时将当前类的字段也添加到映射中，保证一致性
                        mappings.put(key, mapped);
                        return mapped;
                    } else {
                        // 检查父类是否包含该字段（即使未被混淆）
                        ClassWrapper superWrapper = callback.getClassWrapper(superClass);
                        if (superWrapper != null) {
                            for (FieldWrapper superField : superWrapper.getFields()) {
                                if (superField.getOriginalName().equals(name) &&
                                        superField.getOriginalType().equals(descriptor)) {
                                    // 父类有这个字段，但我们没有它的映射
                                    // 这意味着它应该保持原名以确保继承一致性
                                    return name;
                                }
                            }
                        }
                    }
                }
            }
        }

        return name;
    }

    @Override
    public String mapDesc(String descriptor) {
        if (descriptor == null) {
            return null;
        }

        // 处理方法类型描述符
        if (descriptor.startsWith("(")) {
            Type methodType = Type.getMethodType(descriptor);
            Type[] argumentTypes = methodType.getArgumentTypes();
            Type returnType = methodType.getReturnType();

            // 重映射参数类型
            StringBuilder newDesc = new StringBuilder("(");
            for (Type argType : argumentTypes) {
                newDesc.append(mapType(argType));
            }
            newDesc.append(")").append(mapType(returnType));

            return newDesc.toString();
        }

        // 处理普通类型描述符
        StringBuilder sb = new StringBuilder();
        int arrayDim = 0;

        while (descriptor.charAt(arrayDim) == '[') {
            arrayDim++;
        }
        sb.append(descriptor, 0, arrayDim);

        if (arrayDim == descriptor.length()) {
            return descriptor;
        }

        char type = descriptor.charAt(arrayDim);
        if (type == 'L') {
            int semi = descriptor.indexOf(';', arrayDim);
            String className = descriptor.substring(arrayDim + 1, semi);
            String mappedName = mapType(className);
            sb.append('L').append(mappedName).append(';');
            sb.append(descriptor, semi + 1, descriptor.length());
        } else {
            sb.append(descriptor, arrayDim, descriptor.length());
        }

        return sb.toString();
    }

    private String mapType(Type type) {
        switch (type.getSort()) {
            case Type.OBJECT:
                return mapType(type.getInternalName());
            case Type.ARRAY:
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < type.getDimensions(); i++) {
                    sb.append('[');
                }
                Type elementType = type.getElementType();
                if (elementType.getSort() == Type.OBJECT) {
                    sb.append('L').append(mapType(elementType.getInternalName())).append(';');
                } else {
                    sb.append(elementType.getDescriptor());
                }
                return sb.toString();
            default:
                return type.getDescriptor();
        }
    }

    @Override
    public Object mapValue(Object value) {
        if (value instanceof Type) {
            Type type = (Type) value;
            // 特殊处理方法类型
            if (type.getSort() == Type.METHOD) {
                return Type.getMethodType(mapMethodDesc(type.getDescriptor()));
            }
            return Type.getType(mapDesc(type.getDescriptor()));
        }
        if (value instanceof Handle) {
            Handle h = (Handle) value;
            return new Handle(
                    h.getTag(),
                    mapType(h.getOwner()),
                    h.getTag() <= Opcodes.H_PUTSTATIC
                            ? mapFieldName(h.getOwner(), h.getName(), h.getDesc())
                            : mapMethodName(h.getOwner(), h.getName(), h.getDesc()),
                    mapMethodDesc(h.getDesc()),
                    h.isInterface()
            );
        }
        // 其他类型的值保持不变
        return value;
    }

    private static class ClassHierarchyInfo {
        final Set<String> superClasses = new LinkedHashSet<>();
        final Set<String> interfaces = new LinkedHashSet<>();
    }
}