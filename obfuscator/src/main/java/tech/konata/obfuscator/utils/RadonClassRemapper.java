package tech.konata.obfuscator.utils;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class RadonClassRemapper extends ClassRemapper {

  private final String originalClassName;
  private final RadonRemapper remapper;

  public RadonClassRemapper(ClassVisitor classVisitor, RadonRemapper remapper, String originalClassName) {
    super(Opcodes.ASM9, classVisitor, remapper);
    this.originalClassName = originalClassName;
    this.remapper = remapper;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
    String remappedName = remapper.mapMethodName(originalClassName, name, descriptor);
    String remappedDesc = remapper.mapMethodDesc(descriptor);
    String remappedSignature = remapper.mapSignature(signature, false);
    String[] remappedExceptions = exceptions == null ? null : remapper.mapTypes(exceptions);

    MethodVisitor mv = super.visitMethod(access, remappedName, remappedDesc, remappedSignature, remappedExceptions);
    if (mv != null) {
      return new MethodRemapper(mv, remapper);
    }
    return null;
  }

  class MethodRemapper extends org.objectweb.asm.commons.MethodRemapper {

    public MethodRemapper(MethodVisitor methodVisitor, Remapper remapper) {
      super(Opcodes.ASM9, methodVisitor, remapper);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
      // 重映射 descriptor
      String remappedDesc = remapper.mapMethodDesc(descriptor);

      // 处理 bootstrap 方法参数
      Object[] remappedArgs = new Object[bootstrapMethodArguments.length];
      for (int i = 0; i < bootstrapMethodArguments.length; i++) {
        Object arg = bootstrapMethodArguments[i];
        if (arg instanceof Type) {
          // 重映射类型描述符
          Type type = (Type) arg;
          if (type.getSort() == Type.METHOD) {
            // 处理方法类型
            remappedArgs[i] = Type.getMethodType(remapper.mapMethodDesc(type.getDescriptor()));
          } else {
            // 处理其他类型
            remappedArgs[i] = Type.getType(remapper.mapDesc(type.getDescriptor()));
          }
        } else if (arg instanceof Handle) {
          // 重映射方法句柄
          Handle handle = (Handle) arg;
          // 对于 LambdaMetafactory 的参数，我们需要正确映射所有部分
          remappedArgs[i] = new Handle(
                  handle.getTag(),
                  remapper.mapType(handle.getOwner()),
                  remapper.mapMethodName(handle.getOwner(), handle.getName(), handle.getDesc()),
                  remapper.mapMethodDesc(handle.getDesc()),
                  handle.isInterface()
          );
        } else {
          // 其他类型的参数保持不变
          remappedArgs[i] = remapper.mapValue(arg);
        }
      }

      // 处理 bootstrap 方法句柄
      Handle remappedBootstrap = (Handle) remapper.mapValue(bootstrapMethodHandle);

      // 使用重映射后的参数调用父类方法
      super.visitInvokeDynamicInsn(
              name,
              remappedDesc,
              remappedBootstrap,
              remappedArgs
      );
    }
    
    /**
     * 判断是否为标准库引导方法
     * @param handle 引导方法句柄
     * @return 如果是标准库引导方法返回true
     */
    private boolean isStandardLibraryBootstrap(Handle handle) {
      if (handle == null || handle.getOwner() == null) {
        return true;
      }
      
      String owner = handle.getOwner();
      // Java标准库中的引导方法不需要重映射
      return owner.startsWith("java/") || 
             owner.startsWith("javax/") || 
             owner.startsWith("jdk/") ||
             owner.equals("java/lang/invoke/LambdaMetafactory") ||
             owner.equals("java/lang/invoke/StringConcatFactory");
    }
  }

  @Override
  public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
    String remappedName = remapper.mapFieldName(originalClassName, name, descriptor);
    String remappedDesc = remapper.mapDesc(descriptor);
    String remappedSignature = remapper.mapSignature(signature, true);
    Object remappedValue = remapper.mapValue(value);

    return super.visitField(access, remappedName, remappedDesc, remappedSignature, remappedValue);
  }

  @Override
  public void visitInnerClass(String name, String outerName, String innerName, int access) {
    String remappedName = remapper.mapType(name);
    String remappedOuter = outerName == null ? null : remapper.mapType(outerName);
    String remappedInner = innerName == null ? null :
            remapper.mapInnerClassName(name, outerName, innerName);

    super.visitInnerClass(remappedName, remappedOuter, remappedInner, access);
  }

  @Override
  public void visitOuterClass(String owner, String name, String descriptor) {
    String remappedOwner = remapper.mapType(owner);
    String remappedName = name == null ? null :
            remapper.mapMethodName(owner, name, descriptor);
    String remappedDesc = descriptor == null ? null :
            remapper.mapMethodDesc(descriptor);

    super.visitOuterClass(remappedOwner, remappedName, remappedDesc);
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    // Don't remap certain runtime annotations
    if (isRuntimeAnnotation(descriptor)) {
      return super.visitAnnotation(descriptor, visible);
    }
    return super.visitAnnotation(remapper.mapDesc(descriptor), visible);
  }

  private boolean isRuntimeAnnotation(String descriptor) {
    return descriptor.startsWith("Ljava/lang/") ||
            descriptor.startsWith("Ljava/annotation/") ||
            descriptor.equals("Ljava/lang/Override;") ||
            descriptor.equals("Ljava/lang/Deprecated;") ||
            descriptor.equals("Ljava/lang/FunctionalInterface;");
  }
}