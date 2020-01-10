package com.miduo.butterknife_compiler;

import com.google.auto.service.AutoService;
import com.miduo.butterknife_annotation.BindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;


//@AutoService(Processor.class)//自动注册 注意这儿的Processor不要写成Process
//还是习惯在方法中使用
//@SupportedAnnotationTypes({"com.miduo.butterknife_annotation.BindView","com.miduo.butterknife_annotation.OnClick"})//指明要处理的注解类型
//@SupportedSourceVersion(SourceVersion.RELEASE_7)//指明支持的java版本
public class ViewProcessor extends AbstractProcessor {

    //打印日志的类
    private Messager messager;

    //输出到文件使用
    private Filer filer;

    //元素节点工具
    private Elements elements;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set=new HashSet<>();
        set.add("com.miduo.butterknife_annotation.BindView");
        set.add("com.miduo.butterknife_annotation.OnClick");
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        //支持的jdk版本,一般返回支持的最新的
//        return SourceVersion.latestSupported();
        return SourceVersion.RELEASE_7;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer=processingEnvironment.getFiler();
        messager=processingEnvironment.getMessager();
        elements=processingEnvironment.getElementUtils();

        messager.printMessage(Diagnostic.Kind.NOTE,"--------init------");

    }

    private Map<String, List<Element>> classMap = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        messager.printMessage(Diagnostic.Kind.NOTE,"--------process------");

        for (TypeElement typeElement : set) {
            messager.printMessage(Diagnostic.Kind.NOTE, "typeElement: " + typeElement.toString());
            //typeElement就相当于com.cool.butterknife.annoation.BindView
            //通过roundEnvironment来获取所有被BindView注解注解了的字段
            Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(typeElement);
            for (Element element : elementsAnnotatedWith) {
                //如果在MainActivity中有这段代码 @BindView(R.id.textView)TextView textView;
                //此处的element就是TextView节点元素
                //element.getEnclosingElement()为获取其父节点元素，即MainActivty
                Element enclosingElement = element.getEnclosingElement();
                TypeMirror classTypeMirror = enclosingElement.asType();
                //className为MainActivty的全类名
                String className = classTypeMirror.toString();
                //上面只是拿MainActivty举个例子，但是真实的使用注解的可能还有SecondActivity等等，所有需要以类名为键保存里面所有使用了BindView注解的节点
                List<Element> elements = classMap.get(className);
                if (elements == null) {
                    elements = new ArrayList<>();
                    elements.add(element);
                    classMap.put(className, elements);
                }else {
                    elements.add(element);
                }
            }

            Set<Map.Entry<String, List<Element>>> entries = classMap.entrySet();
            for (Map.Entry<String, List<Element>> entry : entries) {
                String key = entry.getKey();
                List<Element> value = entry.getValue();
                //生成java代码
                generateViewBinding(key,value);
            }

        }
        return false;
    }


    private void generateViewBinding(String key, List<Element> values) {
        //生成类元素节点 此处key为com.cool.butterknife.MainActivity或者com.cool.butterknife.SecondActivity
        TypeElement classTypeElement = elements.getTypeElement(key);
        //生成参数 final MainActivity target
        ParameterSpec targetParameterSpec = ParameterSpec
                .builder(ClassName.get(classTypeElement), "target", Modifier.FINAL)
                .build();
        //生成参数  View source
        ParameterSpec viewParameterSpec = ParameterSpec.builder(ClassName.get("android.view", "View"), "source")
                .build();

        MethodSpec constructor = null;

        //生成构造函数
        MethodSpec.Builder constructorMethodBuilder = MethodSpec.constructorBuilder()
                .addParameter(targetParameterSpec)
                .addParameter(viewParameterSpec)
                .addAnnotation(ClassName.bestGuess("androidx.annotation.UiThread"))
                .addModifiers(Modifier.PUBLIC);

        //  构造函数中添加代码块
        constructorMethodBuilder.addStatement("this.target = target");
        for (Element element : values) {
            BindView bindView = element.getAnnotation(BindView.class);

            int id = bindView.value();
            Name simpleName = element.getSimpleName();
            constructorMethodBuilder.addStatement("target.$L = source.findViewById($L)", simpleName.toString(), id);
        }
        constructor = constructorMethodBuilder.build();

        //生成unbind方法
        MethodSpec.Builder unbindMethodSpec = MethodSpec.methodBuilder("unbind")
                .addModifiers(Modifier.PUBLIC);
        unbindMethodSpec.addStatement("$T target = this.target", ClassName.get(classTypeElement));
        unbindMethodSpec.addStatement("this.target = null");

        for (Element element : values) {
            Name simpleName = element.getSimpleName();
            unbindMethodSpec.addStatement("target.$L = null", simpleName.toString());
        }

        //生成MainActivity_ViewBinding类
        TypeSpec typeSpec = TypeSpec.classBuilder(classTypeElement.getSimpleName() + "_ViewBinding")
                .addField(ClassName.get(classTypeElement), "target", Modifier.PRIVATE)
                .addMethod(constructor)
                .addMethod(unbindMethodSpec.build())
                .addSuperinterface(ClassName.bestGuess("com.miduo.butterknife_core.Unbinder"))
                .addModifiers(Modifier.PUBLIC)
                .build();

        //获取包名
        String packageName = elements.getPackageOf(classTypeElement).getQualifiedName().toString();
        messager.printMessage(Diagnostic.Kind.NOTE, "packageName: " + packageName.toString());
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .build();

        try {
            //写入java文件
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
