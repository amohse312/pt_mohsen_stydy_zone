<?php
// config.example.php — PT Study Zone
// ================================
// نسخة مثال فقط — لا تحتوي أي قيم حقيقية.
// انسخ هذا الملف باسم config.php على السيرفر، ثم ضع بياناتك فيه هناك فقط.
// لا تضع بيانات الاتصال بقاعدة البيانات داخل كود الأندرويد أبداً —
// الأندرويد يتصل بالـ API فقط، والـ API يقرأ config.php من السيرفر.
//
//   cp config.example.php config.php
//   # ثم عدّل القيم التالية في config.php على السيرفر
//
// ملاحظة: ملف config.php الكامل موجود أصلاً ضمن مجلد site/ في هذه الحزمة
// كما رفعه صاحب المشروع — هذا الملف هنا للتوضيح ولمن يريد إنشاء نسخة جديدة.

define('DB_HOST', 'YOUR_MYSQL_HOST_HERE');       // ضع اسم مضيف MySQL الخاص بك هنا
define('DB_USER', 'YOUR_MYSQL_USERNAME_HERE');   // مثال: if0_xxxxxxxx
define('DB_PASS', 'YOUR_MYSQL_PASSWORD_HERE');   // ضع كلمة المرور هنا على السيرفر فقط
define('DB_NAME', 'YOUR_DATABASE_NAME_HERE');    // مثال: if0_xxxxxxxx_study_management
define('DB_PORT', 3306);

define('ENCRYPTION_KEY', 'CHANGE_ME_TO_A_LONG_RANDOM_SECRET');

// باقي الإعدادات (UPLOAD_DIR, MAX_FILE_SIZE, ALLOWED_TYPES ...)
// موجودة كاملة في ملف config.php الأصلي داخل مجلد site/.
