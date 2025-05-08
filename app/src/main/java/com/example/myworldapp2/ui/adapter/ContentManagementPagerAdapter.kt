package com.example.myworldapp2.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Адаптер для ViewPager, управляющий вкладками в ContentManagementFragment
 */
class ContentManagementPagerAdapter(
    fragment: Fragment,
    private val tabs: List<Pair<Int, Fragment>>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment {
        return tabs[position].second
    }
    
    /**
     * Получить ID строкового ресурса для заголовка вкладки
     */
    fun getTabTitle(position: Int): Int {
        return tabs[position].first
    }
} 