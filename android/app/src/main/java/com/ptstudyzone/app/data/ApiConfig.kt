package com.ptstudyzone.app.data

/**
 * إعدادات الاتصال بالسيرفر — PT Study Zone
 *
 * القاعدة الوحيدة للاتصال: https://ptmohsen.free.nf/api/
 * ممنوع تماماً وضع أي كلمة مرور MySQL أو بيانات اتصال قاعدة بيانات هنا.
 * الاتصال المباشر من الهاتف بـ MySQL على InfinityFree محظور،
 * لذلك كل الطلبات تمر عبر واجهة PHP (api/) على نفس السيرفر ونفس قاعدة البيانات.
 */
object ApiConfig {
    const val BASE_URL = "https://ptmohsen.free.nf/api/"
    const val SITE_URL = "https://ptmohsen.free.nf/"
    const val APP_NAME = "PT Study Zone"
    const val APP_NAME_AR = "منصة العلاج الطبيعي"
}
