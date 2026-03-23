package com.github.rooneyandshadows.lightbulb.apt.android.core.routing

import android.os.Bundle
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
import kotlin.math.max
import kotlin.math.min


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

    fun goTo(newScreen: FragmentScreen) {
        goTo(newScreen, newScreen.getId(), OnExistStrategy.BACK_TO_EXISTING)
    }

    fun goTo(newScreen: FragmentScreen, backStackEntryName: String) {
        goTo(newScreen, backStackEntryName, OnExistStrategy.BACK_TO_EXISTING)
    }

    fun goTo(newScreen: FragmentScreen, mode: OnExistStrategy) {
        goTo(newScreen, newScreen.getId(), mode)
    }

    fun goTo(newScreen: FragmentScreen, backStackEntryName: String, mode: OnExistStrategy) {
        val existingPosition = backStack.getScreenPosition(backStackEntryName)

        if (existingPosition != -1) {
            when (mode) {
                OnExistStrategy.BACK_TO_EXISTING -> {
                    val backSteps = backStack.getEntriesCount() - (existingPosition + 1)
                    backNtimes(backSteps)
                }

                OnExistStrategy.MOVE_EXISTING_TO_TOP -> {
                    moveScreenToTop(existingPosition)
                }
            }
        } else {
            forward(newScreen, backStackEntryName)
        }
    }

    fun forward(newScreen: FragmentScreen) {
        forward(newScreen, TransitionTypes.ENTER, newScreen.getId())
    }

    fun forward(newScreen: FragmentScreen, backStackEntryName: String) {
        forward(newScreen, TransitionTypes.ENTER, backStackEntryName)
    }

    fun forward(newScreen: FragmentScreen, transition: TransitionTypes) {
        forward(newScreen, transition, newScreen.getId())
    }

    fun forward(
        newScreen: FragmentScreen,
        transition: TransitionTypes,
        backStackEntryName: String,
    ) {
        if (backStack.hasScreen(backStackEntryName)) {
            Log.e(
                logTag,
                String.format(
                    "Failed to execute forward routing to \"%s\". Error: Screen with name \"%s\" " +
                            "already exists in backstack. Ignoring route command",
                    backStackEntryName,
                    backStackEntryName
                )
            )
        }
        runOnUiThread {
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
        backNtimes(1)
    }

    fun backNtimes(n: Int) {
        if (n <= 0) return
        val moveTaskToBack = backStack.getEntriesCount() <= n
        runOnUiThread {
            startTransaction(TransitionTypes.EXIT).apply transaction@{
                val initialSize = backStack.getEntriesCount()
                while (backStack.getEntriesCount() > max(1, initialSize - n)) {
                    val fragToRemove = popCurrentFragment()
                    remove(fragToRemove!!)
                }

                val nextFragment = getCurrentFragment()
                attach(nextFragment!!)
                commitAndExecute(this@transaction)
            }

            if (moveTaskToBack) {
                contextActivity.moveTaskToBack(true)
            }
        }
    }

    fun backNTimesAndReplace(n: Int, newScreen: FragmentScreen) {
        backNTimesAndReplace(n, newScreen, true)
    }

    fun backNTimesAndReplace(n: Int, newScreen: FragmentScreen, animate: Boolean) {
        val backStackName = newScreen.getId()
        val existingPosition = backStack.getScreenPosition(backStackName)
        if (existingPosition != -1 && existingPosition < backStack.getEntriesCount() - n) {
            Log.e(
                logTag,
                String.format(
                    "Failed to execute backAndReplace routing to \"%s\". Error: Screen with name \"%s\" " +
                            "already exists on position \"%d\" in backstack. Ignoring route command",
                    backStackName,
                    backStackName,
                    existingPosition
                )
            )
        }

        runOnUiThread {
            val transitionType = if (animate) TransitionTypes.ENTER else TransitionTypes.NONE
            startTransaction(transitionType).apply transaction@{
                val initialSize = backStack.getEntriesCount()
                while (backStack.getEntriesCount() > max(1, initialSize - n)) {
                    val fragToRemove = popCurrentFragment()
                    remove(fragToRemove!!)
                }
                val fragmentToAdd = newScreen.getFragment()

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

    private fun moveScreenToTop(position: Int) {
        runOnUiThread {
            val targetName = backStack.getAt(position)!!
            val targetFragment = fragmentManager.findFragmentByTag(targetName)
            val currentFragment = getCurrentFragment()

            startTransaction(TransitionTypes.ENTER).apply transaction@{
                if (currentFragment != null)
                    detach(currentFragment)
                if (targetFragment != null) {
                    attach(targetFragment)
                }

                runOnCommit {
                    backStack.removeAt(position)
                    backStack.add(targetName)
                }
                commitAndExecute(this@transaction)
            }
        }
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
        val addedScreens = mutableMapOf<String, Boolean>()
        val errors = mutableListOf<String>()
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

                var prevFragment: Fragment? = null
                screens.forEachIndexed { index, fragmentScreen ->
                    val backStackName = fragmentScreen.getId()

                    if (addedScreens.containsKey(backStackName)) {
                        errors.add("$backStackName is dublicated in the provided chain")

                        return@forEachIndexed
                    }

                    val fragmentToAdd = fragmentScreen.getFragment()

                    add(fragmentContainerId, fragmentToAdd, backStackName)
                    addedScreens[backStackName] = true
                    prevFragment?.let { frag -> detach(frag) }
                    prevFragment = fragmentToAdd

                    runOnCommit {
                        backStack.add(backStackName)
                    }
                }
                commitAndExecute(this@transaction)
            }
        }

        if (errors.size > 0) {
            Log.e(
                logTag,
                String.format("New chaing command executed partially. Errors: \n%s", errors.joinToString("\n"))
            )
        }
    }

    private fun startTransaction(transition: TransitionTypes?): FragmentTransaction {
        //fragmentManager.executePendingTransactions()
        val transaction = fragmentManager.beginTransaction().apply {
            setReorderingAllowed(true)
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

    enum class OnExistStrategy(val type: Int) {
        BACK_TO_EXISTING(1),
        MOVE_EXISTING_TO_TOP(2),
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
        abstract fun getId(): String
        abstract fun getFragment(): Fragment
    }
}