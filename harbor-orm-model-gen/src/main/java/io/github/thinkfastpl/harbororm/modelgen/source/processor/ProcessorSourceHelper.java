// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.source.processor;

import lombok.NonNull;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

class ProcessorSourceHelper {

    static String getTypeClassNameForGenericUse(@NonNull TypeMirror typeMirror) {
        if (typeMirror.getKind().isPrimitive()) {
            return getOpaqueTypeName(typeMirror);
        } else if (typeMirror instanceof DeclaredType declaredType && declaredType.asElement() instanceof TypeElement typeElement) {
            if (getPackageName(typeElement).equals("java.lang")) {
                return typeElement.getSimpleName().toString();
            } else {
                return typeElement.getQualifiedName().toString();
            }
        } else if (typeMirror instanceof ArrayType arrayType) {
            if (arrayType.getComponentType().getKind().isPrimitive()) {
                return getBasicTypeName(arrayType.getComponentType()) + "[]";
            } else {
                return getTypeClassNameForGenericUse(arrayType.getComponentType()) + "[]";
            }
        } else {
            throw new IllegalStateException("Unsupported " + typeMirror.getKind());
        }
    }

    static String getPackageName(@NonNull TypeMirror typeMirror) {
        if (typeMirror.getKind().isPrimitive()) {
            return "java.lang";
        } else if (typeMirror instanceof ArrayType arrayType) {
            return getPackageName(arrayType.getComponentType());
        } else if (typeMirror instanceof DeclaredType declaredType && declaredType.asElement() instanceof TypeElement typeElement) {
            return getPackageName(typeElement);
        } else {
            throw new IllegalStateException("Unsupported " + typeMirror.getKind());
        }
    }

    static String getSimpleClassNameWithClassParentsUnderscore(@NonNull TypeElement element) {
        if (element.getEnclosingElement() instanceof PackageElement) {
            return element.getSimpleName().toString();
        } else if (element.getEnclosingElement() instanceof TypeElement typeElement) {
            return getSimpleClassNameWithClassParentsDot(typeElement) + "_" + element.getSimpleName().toString();
        } else {
            throw new IllegalStateException("Unexpected enclosing element " + element.getEnclosingElement());
        }
    }

    static String getSimpleClassNameWithClassParentsDot(@NonNull TypeElement element) {
        if (element.getEnclosingElement() instanceof PackageElement) {
            return element.getSimpleName().toString();
        } else if (element.getEnclosingElement() instanceof TypeElement typeElement) {
            return getSimpleClassNameWithClassParentsDot(typeElement) + "." + element.getSimpleName().toString();
        } else {
            throw new IllegalStateException("Unexpected enclosing element " + element.getEnclosingElement());
        }
    }

    static String getPackageName(@NonNull TypeElement element) {
        if (element.getEnclosingElement() instanceof PackageElement packageElement) {
            return packageElement.getQualifiedName().toString();
        } else if (element.getEnclosingElement() instanceof TypeElement typeElement) {
            return getPackageName(typeElement);
        } else {
            throw new IllegalStateException("Unexpected enclosing element " + element.getEnclosingElement());
        }
    }

    private static String getOpaqueTypeName(@NonNull TypeMirror typeMirror) {
        if (!typeMirror.getKind().isPrimitive()) {
            throw new IllegalStateException("Unsupported " + typeMirror.getKind());
        }
        return switch (typeMirror.getKind()) {
            case BOOLEAN -> Boolean.class.getSimpleName();
            case BYTE -> Byte.class.getSimpleName();
            case SHORT -> Short.class.getSimpleName();
            case INT -> Integer.class.getSimpleName();
            case LONG -> Long.class.getSimpleName();
            case CHAR -> Character.class.getSimpleName();
            case FLOAT -> Float.class.getSimpleName();
            case DOUBLE -> Double.class.getSimpleName();
            default -> throw new IllegalStateException("Unsupported " + typeMirror.getKind());
        };
    }

    static String getBasicTypeName(@NonNull TypeMirror typeMirror) {
        if (!typeMirror.getKind().isPrimitive()) {
            throw new IllegalStateException("Unsupported " + typeMirror.getKind());
        }
        return switch (typeMirror.getKind()) {
            case BOOLEAN -> "boolean";
            case BYTE -> "byte";
            case SHORT -> "short";
            case INT -> "int";
            case LONG -> "long";
            case CHAR -> "char";
            case FLOAT -> "float";
            case DOUBLE -> "double";
            default -> throw new IllegalStateException("Unsupported " + typeMirror.getKind());
        };
    }
}
