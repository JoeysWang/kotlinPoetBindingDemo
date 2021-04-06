package com.joeys.binding_processor

import com.google.auto.service.AutoService
import com.joeys.binding_annotation.BindsView
import com.squareup.kotlinpoet.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind


import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * 生成的binding文件最终如下，Activity和Fragment/ViewHolder需要分开处理，
 * 因此bind方法需要区分有两个参数和单参数版本
 *
 * public class MainActivityBinding {
 *      public fun bind(target: MainActivity): Unit {
 *          target.tv = target.findViewById(2131231126)
 *      }
 *  }
 *
 * public class BlankFragmentBinding {
 *      public fun bind(target: BlankFragment, rootView: View): Unit {
 *          target.tv2 = rootView.findViewById(2131231127)
 *      }
 *  }
 */
//自动发现Processor
@AutoService(Processor::class)
//支持Java 8
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class BindingProcessor : AbstractProcessor() {

    // 打印日志工具类
    private lateinit var messager: Messager

    // 文件操作类，我们将通过此类生成kotlin文件
    private lateinit var filer: Filer

    // 类型工具类，处理Element的类型
    private lateinit var typeTools: Types
    private lateinit var elementUtils: Elements
    private val PARAM_TARGET = "target"// 被注入的对象
    private val PARAM_ROOTVIEW = "rootView"//findViewById的对象，activity时忽略这个参数

    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        filer = env.filer
        messager = env.messager
        elementUtils = env.elementUtils
        typeTools = env.typeUtils
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment?
    ): Boolean {
        if (annotations.isNullOrEmpty() || roundEnv == null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "没有地方使用注解")
            return false
        }

        // 获取activity的类型，转换成TypeMirror，用于判断是不是Activity
        val activityType = elementUtils.getTypeElement("android.app.Activity").asType()
        // 获取fragment的类型，转换成TypeMirror，用于判断
        val fragmentType = elementUtils.getTypeElement("androidx.fragment.app.Fragment").asType()

        for (rootElement in roundEnv.rootElements) {

            val target = rootElement.simpleName.toString()
            val packageName = rootElement.enclosingElement.toString()

            //bind方法的第一个参数类型
            val targetName = ClassName(packageName, target)
            //bind方法的第二个参数类型
            val rootViewName = ClassName("android.view", "View")

            var isActivity = false
            when {
                typeTools.isSubtype(rootElement.asType(), activityType) -> {
                    isActivity = true
                }
                typeTools.isSubtype(rootElement.asType(), fragmentType) -> {
                    isActivity = false
                }
                else -> isActivity = false
            }


            var hasBindView = false
            //构造bind方法
            val funBuilder = FunSpec.builder("bind")//方法名
                .addModifiers(KModifier.PUBLIC)
                .addParameter(PARAM_TARGET, targetName)//添加第一个参数

            if (!isActivity) {
                //如果不是Activity，添加第二个参数
                funBuilder.addParameter(PARAM_ROOTVIEW, rootViewName)
            }

            for (enclosedElement in rootElement.enclosedElements) {
                if (enclosedElement.kind == ElementKind.FIELD) {
                    val bindView = enclosedElement.getAnnotation(BindsView::class.java)
                    if (bindView != null) {
                        hasBindView = true
                        if (isActivity)
                            funBuilder.addStatement("$PARAM_TARGET.$enclosedElement = $PARAM_TARGET.findViewById(${bindView.id})")
                        else
                            funBuilder.addStatement("$PARAM_TARGET.$enclosedElement = $PARAM_ROOTVIEW.findViewById(${bindView.id})")
                    }
                }
            }

            //如果这个类里有BindsView注解，再去创建注解文件
            if (hasBindView) {
                //创建binding类
                val clazzSpec = TypeSpec.classBuilder(ClassName(packageName, target + "Binding"))
                    .addFunction(funBuilder.build())
                    .build()

                //创建文件
                FileSpec.builder(packageName, target + "Binding")
                    .addType(clazzSpec)//把binding类添加进文件里
                    .build()
                    .writeTo(filer)
            }
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        //说明这个processor只支持BindsView注解
        return mutableSetOf(BindsView::class.java.canonicalName)
    }
}
