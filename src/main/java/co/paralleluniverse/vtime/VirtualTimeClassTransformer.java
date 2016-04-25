/*
 * Copyright (c) 2015-2016, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are license under the terms of the
 * MIT license.
 */
package co.paralleluniverse.vtime;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author pron
 */
public class VirtualTimeClassTransformer extends ASMClassFileTransformer {
    private static final String PACKAGE = Clock_.class.getPackage().getName().replace('.', '/');
    private static final String CLOCK = Type.getInternalName(Clock_.class);
    private static final String DISPATCHER_SERVLET = Type.getInternalName(DispatcherServlet_.class);

    @Override
    protected boolean filter(String className) {
        return className == null || className.startsWith(PACKAGE);
    }

    @Override
    protected ClassVisitor createVisitor(ClassVisitor next) {
        return new ClassVisitor(Opcodes.ASM5, next) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        if (!captureTimeCall(owner, name, desc) && !captureDispatcherCall(owner, name, desc))
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                    }

                    private boolean captureDispatcherCall(String owner, String name, String desc) {

                        switch (owner) {
                            case "org/springframework/web/servlet/DispatcherServlet":
                                if ("doDispatch".equals(name))
                                    return callMethod(DISPATCHER_SERVLET, "DispatcherServlet_doDispatch", instanceToStatic(owner, desc));
                                break;
                            case "org/mortbay/jetty/Handler":
                                if ("handle".equals(name))
                                    return callMethod(DISPATCHER_SERVLET, "MortbayJetty_Handler_handle", instanceToStatic(owner, desc));
                                break;
                        }

                        return false;
                    }

                    private boolean captureTimeCall(String owner, String name, String desc) {
                        switch (owner) {
                            case "java/lang/Object":
                                if ("wait".equals(name) && !desc.startsWith("()"))
                                    return callMethod(CLOCK, "Object_wait", instanceToStatic(owner, desc));
                                break;
                            case "java/lang/System":
                                switch (name) {
                                    case "nanoTime":
                                        return callMethod(CLOCK, "System_nanoTime", desc);
                                    case "currentTimeMillis":
                                        return callMethod(CLOCK, "System_currentTimeMillis", desc);
                                }
                                break;
                            case "java/lang/Thread":
                                if ("sleep".equals(name))
                                    return callMethod(CLOCK, "Thread_sleep", desc);
                                break;
                            case "sun/misc/Unsafe":
                                if ("park".equals(name))
                                    return callMethod(CLOCK, "Unsafe_park", instanceToStatic(owner, desc));
                                break;
                        }
                        return false;
                    }

                    private boolean callMethod(String typeName, String name, String desc) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, typeName, name, desc, false);
                        return true;
                    }

                    private String instanceToStatic(String owner, String desc) {
                        return "(L" + owner + ";" + desc.substring(1);
                    }
                };
            }
        };
    }
}
