package com.miduo.butterknife_compiler;

import com.google.auto.service.AutoService;

import java.util.HashSet;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;


@AutoService(Processor.class)//自动注册 注意这儿的Processor不要写成Process
//还是习惯在方法中使用
@SupportedAnnotationTypes({"com.miduo.butterknife_annotation.BindView","com.miduo.butterknife_annotation.OnClick"})//指明要处理的注解类型
@SupportedSourceVersion(SourceVersion.RELEASE_7)//指明支持的java版本
public class BindViewProcessor extends AbstractProcessor {

    //打印日志的类
    private Messager messager;

    //输出到文件使用
    private Filer filer;

    //元素节点工具
    private Elements elements;

//    @Override
//    public Set<String> getSupportedAnnotationTypes() {
//        Set<String> set=new HashSet<>();
//        set.add("com.miduo.butterknife_annotation.BindView");
//        set.add("com.miduo.butterknife_annotation.OnClick");
//        return set;
//    }

//    @Override
//    public SourceVersion getSupportedSourceVersion() {
//        //支持的jdk版本,一般返回支持的最新的
////        return SourceVersion.latestSupported();
//        return SourceVersion.RELEASE_7;
//    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer=processingEnvironment.getFiler();
        messager=processingEnvironment.getMessager();
        elements=processingEnvironment.getElementUtils();

        messager.printMessage(Diagnostic.Kind.NOTE,"--------init------");

    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        messager.printMessage(Diagnostic.Kind.NOTE,"--------process------");
        return false;
    }
}
