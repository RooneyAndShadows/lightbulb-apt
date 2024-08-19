package com.github.rooneyandshadows.lightbulb.apt.android.core.routing

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.github.rooneyandshadows.lightbulb.apt.android.core.utils.BundleUtils
import com.github.rooneyandshadows.lightbulb.apt.core.R
import java.util.*


@Suppress("CanBePrimaryConstructorProperty", "unused", "MemberVisibilityCanBePrivate")
open class BaseActivityRouter(contextActivity: AppCompatActivity, fragmentContainerId: Int) : DefaultLifecycleObserver {
    protected val contextActivity: AppCompatActivity = contextActivity
    private val fragmentContainerId: Int = fragmentContainerId
    private val fragmentManager: FragmentManager
    private val logTag: String
    private var backStack = ActivityRouterBackStack()
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            back()
        }
    }

    companion object {
        private const val ACTIVITY_ROUTER_BACKSTACK = "ACTIVITY_ROUTER_BACKSTACK"
    }

    init {
        fragmentManager = contextActivity.supportFragmentManager
        logTag = "[".plus(javaClass.simpleName).plus("]")
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        onBackPressedCallback.remove()
        contextActivity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        onBackPressedCallback.remove()
    }

    fun attach() {
        contextActivity.lifecycle.removeObserver(this)
        contextActivity.lifecycle.addObserver(this)
    }

    fun detach() {
        contextActivity.lifecycle.removeObserver(this)
    }

    fun saveState(activityBundle: Bundle) {
        BundleUtils.putParcelable(
            ACTIVITY_ROUTER_BACKSTACK,
            activityBundle,
            backStack
        )
    }

    fun restoreState(activitySavedState: Bundle) {
        backStack = BundleUtils.getParcelable(
            ACTIVITY_ROUTER_BACKSTACK,
            activitySavedState,
            ActivityRouterBackStack::class.java
        )!!
    }

    fun getBackStackEntriesCount(): Int {
        return backStack.getEntriesCount()
    }

    fun isCurrentScreenRoot(): Boolean {
        return backStack.getEntriesCount() == 1
    }

    fun forward(newScreen: FragmentScreen) {
        forward(newScreen, TransitionTypes.ENTER, UUID.randomUUID().toString())
    }

    fun forward(newScreen: FragmentScreen, backStackEntryName: String) {
        forward(newScreen, TransitionTypes.ENTER, backStackEntryName)
    }

    fun forward(newScreen: FragmentScreen, transition: TransitionTypes) {
        forward(newScreen, transition, UUID.randomUUID().toString())
    }

    fun forward(
        newScreen: FragmentScreen,
        transition: TransitionTypes,
        backStackEntryName: String,
    ) {
        runOnUiThread {
            //val currentFragment = fragmentManager.findFragmentById(fragmentContainerId)
            val requestedFragment = newScreen.getFragment()
            val currentFragment = getCurrentFragment()
            startTransaction(transition).apply transaction@{
                add(fragmentContainerId, requestedFragment, backStackEntryName)
                if (currentFragment != null)
                    detach(currentFragment)
                runOnCommit {
                    backStack.add(backStackEntryName)
                }
                commitAndExecute(this@transaction)
            }
        }
    }



    fun back() {
        runOnUiThread {
            if (backStack.getEntriesCount() <= 1) contextActivity.moveTaskToBack(true)
            else startTransaction(TransitionTypes.EXIT).apply transaction@{
                val currentFrag = popCurrentFragment()
                val nextFragment = getCurrentFragment()
                if (currentFrag != null)
                    remove(currentFrag)
                attach(nextFragment!!)
                commitAndExecute(this@transaction)
            }
        }
    }

    fun backNTimesAndReplace(n: Int, newScreen: FragmentScreen) {
        backNTimesAndReplace(n, newScreen, true)
    }

    fun backNTimesAndReplace(n: Int, newScreen: FragmentScreen, animate: Boolean) {
        runOnUiThread {
            startTransaction(null).apply transaction@{
                val initialSize = backStack.getEntriesCount()
                while (backStack.getEntriesCount() > initialSize - n) {
                    val fragToRemove = popCurrentFragment()
                    remove(fragToRemove!!)
                }
                val backStackName = UUID.randomUUID().toString()
                val fragmentToAdd = newScreen.getFragment()
                if (animate)
                    setCustomAnimations(
                        0,
                        0,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right
                    )
                add(fragmentContainerId, fragmentToAdd, backStackName)
                runOnCommit {
                    backStack.add(backStackName)
                }
                commitAndExecute(this@transaction)
            }
        }
    }

    fun replaceTop(newScreen: FragmentScreen) {
        replaceTop(newScreen, true)
    }

    fun replaceTop(newScreen: FragmentScreen, animate: Boolean) {
        backNTimesAndReplace(1, newScreen, animate)
    }

    fun backToRoot() {
        runOnUiThread {
            startTransaction(null).apply transaction@{
                while (backStack.getEntriesCount() > 1) {
                    val fragToRemove = popCurrentFragment()
                    remove(fragToRemove!!)
                }
                attach(getCurrentFragment()!!)
                commitAndExecute(this@transaction)
            }
        }
    }

    fun newRootChain(vararg screens: FragmentScreen) {
        newChain(true, *screens)
    }

    fun addChain(vararg screens: FragmentScreen) {
        newChain(false, *screens)
    }

    fun newRootScreen(newRootScreen: FragmentScreen) {
        newRootChain(newRootScreen)
    }

    fun customAction(action: CustomRouterAction) {
        action.execute(fragmentContainerId, fragmentManager, backStack)
    }

    private fun getCurrentFragment(): Fragment? {
        val currentTag = backStack.getCurrent() ?: return null
        return fragmentManager.findFragmentByTag(currentTag)
    }

    private fun popCurrentFragment(): Fragment? {
        val currentTag = backStack.pop() ?: return null
        return fragmentManager.findFragmentByTag(currentTag)
    }

    private fun newChain(root: Boolean, vararg screens: FragmentScreen) {
        runOnUiThread {
            startTransaction(null).apply transaction@{
                if (root) {
                    while (backStack.getEntriesCount() > 0) {
                        val fragToRemove = popCurrentFragment()
                        remove(fragToRemove!!)
                    }
                } else {
                    val fragToDetach = getCurrentFragment()
                    detach(fragToDetach!!)
                }
                screens.forEachIndexed { index, fragmentScreen ->
                    val isLast = index == screens.size - 1
                    val backStackName = UUID.randomUUID().toString()
                    val fragmentToAdd = fragmentScreen.getFragment()
                    add(fragmentContainerId, fragmentToAdd, backStackName)
                    if (!isLast)
                        detach(fragmentToAdd)
                    runOnCommit {
                        backStack.add(backStackName)
                    }
                }
                commitAndExecute(this@transaction)
            }
        }
    }

    private fun startTransaction(transition: TransitionTypes?): FragmentTransaction {
        //fragmentManager.executePendingTransactions()
        val transaction = fragmentManager.beginTransaction().apply {
            setReorderingAllowed(false)
            when (transition) {
                TransitionTypes.ENTER -> setCustomAnimations(
                    R.anim.enter_from_right,
                    R.anim.exit_to_left,
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                )

                TransitionTypes.EXIT -> setCustomAnimations(
                    R.anim.enter_from_left,
                    R.anim.exit_to_right,
                    R.anim.enter_from_right,
                    R.anim.exit_to_left
                )

                else -> {}
            }
        }
        return transaction
    }

    private fun runOnUiThread(action: () -> Unit) {
        contextActivity.runOnUiThread {
            action.invoke()
        }
        //Handler(Looper.getMainLooper()).post({ })
    }

    private fun commitAndExecute(transaction: FragmentTransaction) {
        transaction.commitNow()
        //transaction.commit()
        //fragmentManager.executePendingTransactions()
    }

    fun printBackStack() {
        Log.i(logTag, "CURRENT BACKSTACK")
        Log.i(logTag, "=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+")
        val entriesCount = backStack.getEntriesCount()
        if (entriesCount == 0)
            Log.i(logTag, "Backstack is empty")
        for (entryPosition in 0 until entriesCount)
            Log.i(
                logTag,
                entryPosition.toString().plus(" " + backStack.getAt(entryPosition))
            )
    }

    enum class TransitionTypes(val type: Int) {
        NONE(1),
        ENTER(2),
        EXIT(3)
    }

    interface CustomRouterAction {
        fun execute(
            fragmentContainerId: Int,
            fragmentManager: FragmentManager,
            backStack: ActivityRouterBackStack
        )
    }

    abstract class FragmentScreen {
        abstract fun getFragment(): Fragment
    }
}