package org.forkjoin.apikit.wrapper;

import org.apache.commons.collections.CollectionUtils;
import org.forkjoin.apikit.Context;
import org.forkjoin.apikit.Utils;
import org.forkjoin.apikit.info.AnnotationInfo;
import org.forkjoin.apikit.info.MessageInfo;
import org.forkjoin.apikit.info.PropertyInfo;
import org.forkjoin.apikit.info.TypeInfo;

import java.util.List;

/**
 * @author zuoge85 on 15/6/14.
 */
public class JavaMessageWrapper extends JavaWrapper<MessageInfo> {
    private boolean isAnnotations = false;

    public JavaMessageWrapper(Context context, MessageInfo messageInfo, String rootPackage) {
        super(context, messageInfo, rootPackage);
    }


    @Override
    public String formatAnnotations(List<AnnotationInfo> annotations, String start) {
        return null;
    }

    @Override
    public void init() {
        addExclude("javax.validation");
        addExclude("org.hibernate.validator");
        addExclude("org.forkjoin.apikit.core.Message");
        super.init();
    }


    public String typeParameters() {
        List<String> typeParameters = moduleInfo.getTypeParameters();
        if (CollectionUtils.isNotEmpty(typeParameters)) {
            StringBuilder sb = new StringBuilder("<");
            for (String typeParameter : typeParameters) {
                if (sb.length() > 1) {
                    sb.append(",");
                }
                sb.append(typeParameter).append(" extends ApiMessage");
            }
            sb.append(">");
            return sb.toString();
        } else {
            return "";
        }
    }

    public String getTypeParametersField(String start) {
        List<String> typeParameters = moduleInfo.getTypeParameters();
        StringBuilder sb = new StringBuilder();
        for (String typeParameter : typeParameters) {
            sb.append(start);
            sb.append("private Class<").append(typeParameter).append(">");
            sb.append(" ");
            sb.append(typeParameter.toLowerCase()).append("Cls;\n");
        }
        return sb.toString();
    }

    public String getTypeParametersAssign(String start) {
        List<String> typeParameters = moduleInfo.getTypeParameters();
        StringBuilder sb = new StringBuilder();
        for (String typeParameter : typeParameters) {
            sb.append(start);
            String typeParameterLowerCase = typeParameter.toLowerCase();
            sb.append("this.").append(typeParameterLowerCase).append("Cls = ");
            sb.append(" ");
            sb.append(typeParameterLowerCase).append("Cls;\n");
        }
        return sb.toString();
    }

    public String getTypeParametersConstructorString() {
        List<String> typeParameters = moduleInfo.getTypeParameters();
        StringBuilder sb = new StringBuilder();
        for (String typeParameter : moduleInfo.getTypeParameters()) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append("Class<").append(typeParameter).append(">");
            sb.append(" ");
            sb.append(typeParameter.toLowerCase()).append("Cls");
        }
        return sb.toString();
    }

    public String getToString() {
        StringBuilder sb = new StringBuilder();
        sb.append('\"');
        sb.append(moduleInfo.getName());
        sb.append(" [");

        for (PropertyInfo attr : moduleInfo.getProperties()) {
            sb.append(attr.getName());

            if (attr.getTypeInfo().isArray()) {

                if (attr.getTypeInfo().isBytes()) {
                    sb.append("=length:\" + ");

                    sb.append("(").append(attr.getName())
                            .append(" == null?-1:")
                            .append(attr.getName())
                            .append(".length)");
                } else {
                    sb.append("=size:\" + ");

                    sb.append("(").append(attr.getName())
                            .append(" == null?-1:")
                            .append(attr.getName())
                            .append(".size())");
                }
                sb.append(" + \"");
            } else {
                sb.append("=\" + ");
                sb.append(attr.getName());
                sb.append(" + \"");
            }

            sb.append(',');
        }
        sb.append(" ]\"");
        return sb.toString();
    }

    public String getConstructorString() {
        StringBuilder sb = new StringBuilder();
        for (PropertyInfo attr : moduleInfo.getProperties()) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            TypeInfo typeInfo = attr.getTypeInfo();
            sb.append(toJavaTypeString(typeInfo, false, true));
            sb.append(" ");
            sb.append(attr.getName());
        }
        return sb.toString();
    }

    public String getDecodeCode(String start) {
        StringBuilder sb = new StringBuilder();
        for (PropertyInfo attr : moduleInfo.getProperties()) {
            sb.append('\n');
            TypeInfo typeInfo = attr.getTypeInfo();
            String name = attr.getName();
            if (typeInfo.isOtherType()) {
//                List<TypeInfo> typeArguments = typeInfo.getTypeArguments();
                if (typeInfo.isArray()) {
                    if (typeInfo.isOtherType()) {
                        sb.append(start).append(" if (").append(name).append(" != null && (!").append(name).append(".isEmpty())) {\n");
                        sb.append(start).append("for (int i = 0; i < ").append(name).append(".size(); i++) {\n");
                        sb.append(start).append("    ").append(name).append(".get(i).encode(parent + \"")
                                .append(name).append("\" + \"[\" + i + \"].\", $list);\n");
                        sb.append(start).append("    }\n");
                        sb.append(start).append("}\n");
                    } else {
                        sb.append(start).append(" if (").append(name).append(" != null && (!").append(name).append(".isEmpty())) {\n");
                        sb.append(start).append("for (int i = 0; i < ").append(name).append(".size(); i++) {\n");
                        sb.append("$list.add(new SimpleImmutableEntry<String, Object>(parent + \"")
                                .append(name).append("\", ")
                                .append(name)
                                .append(".get(i)));\n");
                        sb.append(start).append("    }\n");
                        sb.append(start).append("}\n");
                    }
                } else {
                    sb.append(start).append(" if (").append(name).append(" != null) {\n");
                    sb.append(start).append("    ").append(name).append(".encode(parent + \"").append(name).append(".\", $list);");
                    sb.append(start).append("}\n");
                }
            } else {
                if (typeInfo.getType().isHasNull()) {
                    sb.append(start).append("if (").append(name).append(" != null) {\n");
                    getEncodeCodeItemBase(start, sb, name);
                    sb.append(start).append("}\n");
                } else {
                    getEncodeCodeItemBase(start, sb, name);
                }
            }
        }
        return sb.toString();
    }

    public String getEncodeCode(String start) {
        StringBuilder sb = new StringBuilder();
        for (PropertyInfo attr : moduleInfo.getProperties()) {
            sb.append('\n');
            TypeInfo typeInfo = attr.getTypeInfo();
            String name = attr.getName();
            if (typeInfo.isOtherType()) {
//                List<TypeInfo> typeArguments = typeInfo.getTypeArguments();
                if (typeInfo.isArray()) {
                    if (typeInfo.isOtherType()) {
                        sb.append(start).append(" if (").append(name).append(" != null && (!").append(name).append(".isEmpty())) {\n");
                        sb.append(start).append("for (int i = 0; i < ").append(name).append(".size(); i++) {\n");
                        sb.append(start).append("    ").append(name).append(".get(i).encode(parent + \"")
                                .append(name).append("\" + \"[\" + i + \"].\", $list);\n");
                        sb.append(start).append("    }\n");
                        sb.append(start).append("}\n");
                    } else {
                        sb.append(start).append(" if (").append(name).append(" != null && (!").append(name).append(".isEmpty())) {\n");
                        sb.append(start).append("for (int i = 0; i < ").append(name).append(".size(); i++) {\n");
                        sb.append("$list.add(new SimpleImmutableEntry<String, Object>(parent + \"")
                                .append(name).append("\", ")
                                .append(name)
                                .append(".get(i)));\n");
                        sb.append(start).append("    }\n");
                        sb.append(start).append("}\n");
                    }
                } else {
                    sb.append(start).append(" if (").append(name).append(" != null) {\n");
                    sb.append(start).append("    ").append(name).append(".encode(parent + \"").append(name).append(".\", $list);");
                    sb.append(start).append("}\n");
                }
            } else {
                if (typeInfo.getType().isHasNull()) {
                    sb.append(start).append("if (").append(name).append(" != null) {\n");
                    getEncodeCodeItemBase(start, sb, name);
                    sb.append(start).append("}\n");
                } else {
                    getEncodeCodeItemBase(start, sb, name);
                }
            }
        }
        return sb.toString();
    }


    public String toJavaAddName(PropertyInfo propertyInfo) {
        return "add" + Utils.toClassName(propertyInfo.getName());
    }

    public String toJavaSetName(PropertyInfo propertyInfo) {
        return "set" + Utils.toClassName(propertyInfo.getName());
    }

    public String toJavaGetName(PropertyInfo propertyInfo) {
        return "get" + Utils.toClassName(propertyInfo.getName());
    }

    /**
     * 基本类型
     */
    private void getEncodeCodeItemBase(String start, StringBuilder sb, String name) {
        sb.append(start)
                .append("    ")
                .append("$list.add(new SimpleImmutableEntry<String, Object>(parent + \"")
                .append(name).append("\",")
                .append(name)
                .append("));\n");
    }


    public boolean isAnnotations() {
        return isAnnotations;
    }

    public void setAnnotations(boolean annotations) {
        isAnnotations = annotations;
    }
//    if (id != null) {
//			$list.add(new SimpleImmutableEntry<String, Object>(parent + "id",id));
//		}
//		$list.add(new SimpleImmutableEntry<String, Object>(parent+ "booleanValue", booleanValue));
//
//		if (bytesValue != null) {
//			$list.add(new SimpleImmutableEntry<String, Object>(parent
//					+ "bytesValue", bytesValue));
//		}
//
//		if (booleanValueArray != null && (!booleanValueArray.isEmpty())) {
//			for (int i = 0; i < booleanValueArray.size(); i++) {
//				$list.add(new SimpleImmutableEntry<String, Object>(parent
//						+ "booleanValueArray", booleanValueArray.get(i)));
//			}
//		}
//
//		if (user != null) {
//			user.encode(parent + "user.", $list);
//		}
//
//		if (users != null && (!users.isEmpty())) {
//			for (int i = 0; i < users.size(); i++) {
//				users.get(i).encode(parent + "users" + "[" + i + "].", $list);
//			}
//		}
//
}
