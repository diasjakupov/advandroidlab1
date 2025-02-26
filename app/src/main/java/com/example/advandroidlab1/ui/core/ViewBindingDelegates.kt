package com.example.advandroidlab1.ui.core

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified T : ViewBinding> Fragment.viewBinding(
    noinline binder: (LayoutInflater) -> T
) = FragmentViewBindingDelegate(T::class.java, binder)

class FragmentViewBindingDelegate<T : ViewBinding>(
    private val bindingClass: Class<T>,
    private val binder: (LayoutInflater) -> T
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        binding?.let { return it }

        val lifecycle = thisRef.viewLifecycleOwner.lifecycle
        val bindingNew = binder(thisRef.layoutInflater)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                binding = null
                lifecycle.removeObserver(this)
            }
        })

        return bindingNew.also { binding = it }
    }
}

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) {
    bindingInflater.invoke(layoutInflater)
}