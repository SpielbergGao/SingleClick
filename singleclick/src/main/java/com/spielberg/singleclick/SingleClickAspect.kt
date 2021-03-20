package com.spielberg.singleclick

import android.view.View
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import java.util.*

@Aspect
class SingleClickAspect {

    @Pointcut(POINTCUT_METHOD)
    fun methodPointcut() {
    }

    @Pointcut(POINTCUT_ANNOTATION)
    fun annotationPointcut() {
    }

    @Pointcut(POINTCUT_VIEW_METHOD)
    fun viewMethodPointcut() {
    }

    @Around("methodPointcut() || annotationPointcut() || viewMethodPointcut()")
    @Throws(Throwable::class)
    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint) {
        try {
            val signature = joinPoint.signature as MethodSignature
            val method = signature.method
            //检查方法是否有注解
            val hasAnnotation = method != null && method.isAnnotationPresent(
                SingleClick::class.java
            )
            //计算点击间隔，没有注解默认500，有注解按注解参数来，注解参数为空默认500；
            var interval = SingleClickManager.clickInterval
            if (hasAnnotation) {
                val annotation = method?.getAnnotation(SingleClick::class.java)
                interval = annotation?.value ?: 0
            }
            //获取被点击的view对象
            val args = joinPoint.args
            val view = findViewInMethodArgs(args)
            if (view != null) {
                val id = view.id
                //注解排除某个控件不防止双击
                if (hasAnnotation) {
                    val annotation = method?.getAnnotation(SingleClick::class.java)
                    //按id值排除不防止双击的按钮点击
                    val except = annotation?.except
                    if (except != null) {
                        for (i in except) {
                            if (i == id) {
                                mLastClickTime = Calendar.getInstance().timeInMillis
                                joinPoint.proceed()
                                return
                            }
                        }
                    }

                    //按id名排除不防止双击的按钮点击（非app模块）
                    val idName = annotation?.exceptIdName
                    val resources = view.resources
                    if (idName != null) {
                        for (name in idName) {
                            val resId =
                                resources.getIdentifier(name, "id", view.context.packageName)
                            if (resId == id) {
                                mLastClickTime = Calendar.getInstance().timeInMillis
                                joinPoint.proceed()
                                return
                            }
                        }
                    }
                }
                if (canClick(interval)) {
                    mLastClickTime = Calendar.getInstance().timeInMillis
                    joinPoint.proceed()
                    return
                }
            }

            //检测间隔时间是否达到预设时间并且线程空闲
            if (canClick(interval)) {
                mLastClickTime = Calendar.getInstance().timeInMillis
                joinPoint.proceed()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //出现异常不拦截点击事件
            joinPoint.proceed()
        }
    }

    private fun findViewInMethodArgs(args: Array<Any>?): View? {
        if (args.isNullOrEmpty()) {
            return null
        }
        repeat(args.size) {
            if (args[it] is View) {
                val view = args[it] as View
                if (view.id != View.NO_ID) {
                    return view
                }
            }
        }
        return null
    }

    private fun canClick(interval: Int): Boolean {
        val l = System.currentTimeMillis() - mLastClickTime
        if (l > interval) {
            mLastClickTime = Calendar.getInstance().timeInMillis
            return true
        }
        return false
    }

    companion object {

        private var mLastClickTime: Long = 0

        private const val POINTCUT_METHOD = "execution(* onClick(..))"

        private const val POINTCUT_ANNOTATION =
            "execution(@com.spielberg.singleclick.SingleClick * *(..))"

        private const val POINTCUT_VIEW_METHOD =
            "execution(* android.view.View.OnClickListener.onClick(..))"
    }

}