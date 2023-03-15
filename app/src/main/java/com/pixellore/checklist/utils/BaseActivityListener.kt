package com.pixellore.checklist.utils

import com.pixellore.checklist.DataClass.TextFont
import com.pixellore.checklist.DataClass.Theme
import java.util.HashMap

/**
 * For fragments to call the methods in the BaseActivity
 * (and maintain their(fragment) modularity by not directly calling methods in an activity)
 * this interface is used
 *
 * Note: Interface is not required for other activities to call methods in BaseActivity. This is only
 * for fragments to call BaseActivity methods
 * */
interface BaseActivityListener {

    fun getThemesData(): ArrayList<Theme>

    fun getColorsFromTheme(themeResId:Int): HashMap<String, Int>

    fun getFontsData() : ArrayList<TextFont>

}