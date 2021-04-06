package com.joeys.binding_core

import android.app.Activity
import android.view.View

object InnerBinding {
    fun bind(activity: Activity) {
        val canonicalName = activity.javaClass.canonicalName + "Binding"
        val clazz = Class.forName(canonicalName)
        val constructor = clazz.getDeclaredConstructor()
        val newInstance = constructor.newInstance()
        val declaredMethod = clazz.getDeclaredMethod("bind", activity.javaClass)
        declaredMethod.invoke(newInstance, activity)
    }

    fun bind(target: Any, root: View) {
        val canonicalName = target.javaClass.canonicalName + "Binding"
        val clazz = Class.forName(canonicalName)
        val constructor = clazz.getDeclaredConstructor()
        val newInstance = constructor.newInstance()
        val declaredMethod = clazz.getDeclaredMethod("bind", target.javaClass, View::class.java)
        declaredMethod.invoke(newInstance, target, root)
    }


}