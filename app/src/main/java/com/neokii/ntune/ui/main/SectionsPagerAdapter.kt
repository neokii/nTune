package com.neokii.ntune.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.neokii.ntune.TuneItemInfo


class SectionsPagerAdapter(private val context: Context, fm: FragmentManager, private val itemInfos: List<TuneItemInfo>,
    private val host: String, private val remoteConfFile: String) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {

        val itemInfo = itemInfos[position]
        if(itemInfo.step == 0.0f)
            return TuneSwitchFragment.newInstance(itemInfos[position], host, remoteConfFile)
        else
            return TuneFragment.newInstance(itemInfos[position], host, remoteConfFile)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return itemInfos[position].key
    }

    override fun getCount(): Int {
        return itemInfos.size
    }
}