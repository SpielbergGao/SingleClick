package com.spielberg.singleclick


object SingleClickManager {

    @JvmField
    var clickInterval = 500

    /**
     * 设置全局点击事件防重间隔
     *
     * @param clickIntervalMillis 间隔毫秒值
     */
    @JvmStatic
    fun setClickInterval(clickIntervalMillis: Int) {
        clickInterval = clickIntervalMillis
    }

}