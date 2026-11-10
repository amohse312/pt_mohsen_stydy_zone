# PT Study Zone — تطبيق أندرويد + واجهة API
# منصة العلاج الطبيعي | https://ptmohsen.free.nf

تطبيق أندرويد كامل (Kotlin + Jetpack Compose + Material3) مرتبط بنفس سيرفر الموقع
ونفس قاعدة بياناته، عبر واجهة PHP (REST JSON) — بدون أي اتصال مباشر من الهاتف
بـ MySQL، وبدون أي كلمة مرور لقاعدة البيانات داخل كود الأندرويد.

> **ملاحظة أمنية مهمة:** الاتصال المباشر من الهاتف بـ MySQL على InfinityFree
> محظور تقنياً (السيرفر يقبل الاتصال من نفس الاستضافة فقط)، لذلك الربط يتم عبر
> `api/` على نفس السيرفر ونفس قاعدة البيانات. التطبيق يتصل فقط بـ
> `https://ptmohsen.free.nf/api/` — وهذا هو التصميم الصحيح والوحيد الذي يعمل.

---

## محتويات الحزمة

```
PTStudyZone/
├── site/                  ملفات الموقع الأصلية كما هي (index.php, config.php, admin.php, ...)
├── database/              ملف SQL الأصلي كما هو (if0_40085718_study_management.sql)
├── api/                   واجهة REST JSON (index.php + .htaccess + config.example.php)
│   └── index.php          كل نقاط النهاية — يستعمل require ../config.php
├── android/               مشروع Android Studio كامل (Kotlin + Compose)
│   ├── settings.gradle.kts / build.gradle.kts / gradle.properties
│   └── app/
│       ├── build.gradle.kts
│       ├── src/main/AndroidManifest.xml
│       └── src/main/java/com/ptstudyzone/app/
│           ├── MainActivity.kt            التنقل + الشريط السفلي
│           ├── data/ApiConfig.kt          BASE_URL فقط — لا أسرار هنا
│           ├── data/Models.kt             نماذج البيانات
│           ├── data/ApiService.kt         Retrofit — كل نقاط النهاية
│           ├── data/Repository.kt         طبقة الوصول للبيانات
│           ├── data/SessionStore.kt       حفظ التوكن (DataStore)
│           ├── ui/AppViewModel.kt         حالة التطبيق
│           ├── ui/theme/Theme.kt          ثيم فاخر: كحلي + ذهبي + تركوازي
│           └── ui/screens/
│               ├── ScreensAuth.kt         Splash ترحيبية + Onboarding + دخول/تسجيل
│               ├── ScreensMain.kt         الرئيسية + المواد + تفاصيل مادة + التحديات
│               └── ScreensMore.kt         اشتراكاتي + إشعارات + ملفي + لوحة الأدمن
│       └── src/main/res/                  أيقونة وشعار Vector + ألوان وثيم
├── config.example.php     مثال لملف الإعدادات (أسماء متغيرات فقط، بدون قيم)
├── .gitignore             ملفات تقنية فقط (build/ و .gradle/ و local.properties)
└── README_AR.md           هذا الملف
```

### المواد الحالية في قاعدة البيانات (9 مواد)
Neurology, Neurosurgery, Psychiatry, Electrodiagnosis, Human genetics,
Radiology, P.T Pediatrics, P.T Pediatric surgery, growth and development.

### مزايا التطبيق
- شاشة Splash ترحيبية فاخرة + Onboarding (3 صفحات) يُعرض مرة واحدة.
- تسجيل دخول (بريد أو اسم مستخدم + كلمة مرور — نفس `password_hash` في الموقع)
  وإنشاء حساب جديد (نفس شروط الموقع: 8 أحرف + حرف ورقم).
- لوحة تحكم: رسم دائري لنسبة الإنجاز، شريط تحفيزي، إحصائيات
  (محاضرات/سكاشن/كلينيكال، إشعارات غير مقروءة، تحديات نشطة).
- المواد: بحث فوري، إنشاء/تعديل/حذف مادة، اشتراك/إلغاء اشتراك، فلتر "موادي فقط".
- تفاصيل مادة: تبويبات محاضرات/سكاشن/كلينيكال + ملفات PDF (فتح وتحميل)
  + تحديث التقدم (مكتمل / قيد الانتظار / يحتاج مراجعة) + إضافة محتوى + رفع PDF.
- التحديات: قائمة، إنشاء تحدي (أهداف + مدة + ظهور)، انضمام/مغادرة/إكمال/إلغاء/حذف،
  تفاصيل التحدي مع نسبة تقدمك وقائمة المشاركين.
- اشتراكاتي، الإشعارات (تعليم كمقروء / تعليم الكل)، الملف الشخصي
  (تعديل الاسم والهاتف + تغيير كلمة المرور)، بحث.
- لوحة أدمن (للأدمن والمشرف): إحصائيات الموقع، بحث عن المستخدمين،
  حظر مؤقت/دائم، إلغاء حظر، تغيير الدور، تفعيل/تعطيل، حذف مستخدم.
- ثيم فاخر داكن/فاتح تلقائياً (كحلي + ذهبي + تركوازي طبي)، وعربي RTL أولاً.

---

## 1) رفع الـ API على الاستضافة (InfinityFree)

1. في File Manager أو FTP على `ptmohsen.free.nf`، ارفع مجلد `api/` كاملاً
   **داخل جذر الموقع (htdocs)**، بجانب `config.php` الموجود أصلاً.
   المسار النهائي يجب أن يكون:
   ```
   htdocs/
   ├── config.php        (موجود أصلاً — لا ترفعه من جديد إن كان موجوداً)
   ├── index.php         (ملفات الموقع في site/ — إن لم تكن مرفوعة)
   └── api/
       ├── index.php
       └── .htaccess
   ```
2. تأكد أن `api/index.php` يستطيع قراءة `../config.php` — هو يفعل ذلك بنفسه
   (`require __DIR__ . '/../config.php'`) ويستعمل نفس `$pdo` ونفس دوال الموقع
   (`createUserSession`, `createChallenge`, `joinChallenge`, `uploadFile`, ...).
3. اختبر من المتصفح أو curl:
   ```
   GET  https://ptmohsen.free.nf/api/
   → {"success":true,"data":{"service":"PT Study Zone API", ...}}

   POST https://ptmohsen.free.nf/api/auth/login
   Content-Type: application/json
   {"identifier":"PT_Mohsen","password":"كلمة المرور"}
   → {"success":true,"data":{"token":"...","user":{...}}}

   GET  https://ptmohsen.free.nf/api/subjects
   Authorization: Bearer <token>
   → {"success":true,"data":{"subjects":[...],"count":9}}
   ```
4. كل الطلبات المحمية تحتاج الترويسة `Authorization: Bearer <token>`.
   التوكن يُحفظ في جدول `user_sessions` (نفس جدول جلسات الموقع)،
   وينتهي/يُلغى عند `POST auth/logout`.

### نقاط النهاية (كلها ترجع JSON عربي)
| المجموعة | النقاط |
|---|---|
| auth | `POST auth/login` ، `POST auth/register` ، `GET auth/me` ، `POST auth/logout` ، `POST auth/change-password` |
| dashboard | `GET dashboard/stats` |
| subjects | `GET subjects` ، `GET subjects/{id}` ، `POST subjects` ، `PUT subjects/{id}` ، `DELETE subjects/{id}` ، `POST subjects/{id}/subscribe` ، `POST subjects/{id}/unsubscribe` ، `GET subjects/{id}/contents` ، `POST subjects/{id}/contents` |
| contents | `PUT contents/{type}/{id}` ، `DELETE contents/{type}/{id}` ، `POST contents/{type}/{id}/progress` — النوع: `lecture` / `section` / `clinical` |
| files | `GET files` ، `GET files/{id}/download-url` ، `POST files/upload` (multipart) ، `DELETE files/{id}` |
| challenges | `GET challenges` ، `GET challenges/{id}` ، `POST challenges` ، `DELETE challenges/{id}` ، `POST challenges/{id}/join` ، `POST challenges/{id}/leave` ، `POST challenges/{id}/complete` ، `POST challenges/{id}/cancel` ، `GET challenges/{id}/progress` ، `GET challenges/{id}/records` ، `GET challenges/{id}/report` |
| subscriptions | `GET subscriptions` |
| notifications | `GET notifications` ، `POST notifications/{id}/read` ، `POST notifications/read-all` |
| profile | `GET profile` ، `PUT profile` |
| admin | `GET admin/stats` ، `GET admin/users` ، `POST admin/users/{id}/ban` ، `POST admin/users/{id}/unban` ، `POST admin/users/{id}/role` ، `POST admin/users/{id}/toggle-active` ، `POST admin/users/{id}/change-password` ، `DELETE admin/users/{id}` ، `POST admin/assign` ، `GET admin/activity` |

> ملاحظة: `api/index.php` يقبل أيضاً صيغة `?route=auth/login` إن لم يعمل
> الـ Rewrite على الاستضافة. تحقق من ذلك أولاً إن ظهر خطأ 404.

---

## 2) بناء تطبيق الأندرويد (APK)

### المتطلبات
- Android Studio (الأحدث، Hedgehog أو أحدث) + JDK 17.
- اتصال بالإنترنت أول مرة لتحميل Gradle والاعتماديات من Google Maven Central.
- هاتف/محاكي Android 8.0+ (minSdk 26).

### الخطوات
1. افتح Android Studio ← **Open** ← اختر مجلد `android/`.
2. انتظر مزامنة Gradle (قد تستغرق عدة دقائق أول مرة).
3. إن طلب Android Studio تثبيت SDK أو JDK 17، وافق من SDK Manager.
4. من القائمة: **Build ← Build App Bundle(s) / APK(s) ← Build APK(s)**،
   أو من الطرفية داخل مجلد `android/`:
   ```
   ./gradlew assembleDebug
   ```
   وستجد الـ APK في:
   ```
   android/app/build/outputs/apk/debug/app-debug.apk
   ```
5. ثبّت الـ APK على هاتفك، وسجّل الدخول بنفس حسابك على الموقع
   (مثال: نفس اسم المستخدم وكلمة المرور) — ستظهر موادك وتقدمك فوراً،
   لأن التطبيق والموقع يقرآن نفس قاعدة البيانات.

### تغيير رابط السيرفر (إن تغيّر الدومين لاحقاً)
عدّل سطراً واحداً فقط في:
```
android/app/src/main/java/com/ptstudyzone/app/data/ApiConfig.kt
BASE_URL = "https://ptmohsen.free.nf/api/"
```
ولا تضع أي بيانات قاعدة بيانات في هذا الملف أبداً.

---

## 3) الرفع على GitHub

> حسب طلب صاحب المشروع: تُرفع ملفات الموقع وملف SQL **كما هي**،
> والبيانات الموجودة في هذه الحزمة بيانات تجريبية/وهمية حسب إفادته.
> ملف `.gitignore` في هذه الحزمة مخصص للملفات التقنية فقط
> (`build/` و `.gradle/` و `local.properties`) ولا يمنع رفع `config.php`
> ولا ملف SQL — هذا قرار صاحب المشروع وتحت مسؤوليته.

### إنشاء الريبو والرفع
```bash
cd PTStudyZone
git init
git add .
git status        # راجع القائمة قبل أي commit — تأكد أن build/ و local.properties غير موجودين
git commit -m "PT Study Zone: موقع + API + تطبيق أندرويد"
git branch -M main
git remote add origin https://github.com/<username>/<repo>.git
git push -u origin main
```

### ما الذي يُرفع وما الذي لا يُرفع أبداً
| يُرفع | لا يُرفع أبداً |
|---|---|
| `site/` (ملفات الموقع كما هي) | `android/app/build/` (مجلد البناء) |
| `database/*.sql` (كما هو) | `android/.gradle/` |
| `api/` | `android/local.properties` (فيه مسار SDK الخاص بجهازك) |
| `android/` (كود المصدر) | `*.apk` / `*.aab` الكبيرة — ارفعها من Releases بدلاً من الريبو إن أردت مشاركتها |
| `README_AR.md` و `config.example.php` | أي ملف `.env` إن أنشأته لاحقاً |

### ربط Android Studio بـ GitHub
1. في Android Studio: **VCS ← Enable Version Control Integration ← Git**.
2. **Git ← GitHub ← Share Project on GitHub**، أو من الطرفية كما في الأعلى.
3. لاستنساخ المشروع على جهاز آخر: **File ← New ← Project from Version Control ← GitHub**،
   والصق رابط الريبو — ثم افتح مجلد `android/` وابنِ الـ APK كما في القسم 2.
4. **تنبيه:** إن جعلت الريبو عاماً (Public)، فكل من يراه سيرى ملفات الموقع
   وملف SQL كما رفعتهما — تأكد أن البيانات فيهما تجريبية فعلاً قبل النشر العام،
   وغيّر كلمة مرور قاعدة البيانات على الاستضافة بعد النشر إن لزم الأمر،
   لأن أي سر منشور في Git يبقى في سجل الـ commits حتى بعد حذفه.

---

## 4) ملاحظات أمنية

- لا تضع كلمة مرور MySQL داخل أي ملف أندرويد — التطبيق الحالي لا يفعل ذلك،
  والتحقق يتم بـ `grep` على مجلد `android/` و `api/`.
- `api/` يستعمل PDO Prepared Statements في كل الاستعلامات، ويتحقق من التوكن
  ومن الدور (user / moderator / admin) قبل كل عملية حساسة.
- غيّر `ENCRYPTION_KEY` في `config.php` على السيرفر إن كانت ما زالت القيمة الافتراضية.
- ارفع ملفات PDF عبر التطبيق فقط بعد تسجيل الدخول، والحد الأقصى للملف 50MB
  (نفس حد الموقع).

## 5) ماذا راجعنا قبل التسليم (نتائج فعلية بتاريخ 2026-11-10)

- `php -l` لكل ملفات PHP الـ 28 في الحزمة (api + أمثلة + site) — كلها
  `No syntax errors detected`.
- اختبار وظيفي فعلي لـ API عبر محاكاة SQLite: أكثر من 20 حالة نجحت برد JSON
  صحيح — تسجيل/دخول/توكن/`auth/me`/قائمة المواد/محتوى مادة/إحصائيات اللوحة/
  إنشاء مادة ومحتوى/تحديث التقدم إلى مكتمل 100%/إنشاء تحدي وانضمام وإكمال/
  اشتراكات/إشعارات/تحديث الملف/رفض كلمة مرور خاطئة 401/منع أدمن لغير الأدمن 403/
  حظر مستخدم ثم فك الحظر/تسجيل خروج ثم 401/مسار غير معروف 404.
  ملاحظة: الاشتراك/إلغاء الاشتراك يستعمل `ON DUPLICATE KEY UPDATE` الخاص بـ MySQL
  (نفس صيغة الموقع الأصلية) — يعمل على MySQL، ولم يُختبر في محاكاة SQLite.
- التحقق من وجود كل نقاط النهاية في `api/index.php` (58 استدعاء `prepare()`)
  وأنها ترجع JSON بالصيغة `{"success":...}`.
- فحص ترجمة Kotlin لكل ملفات `.kt` الـ 11 بـ kotlinc مع كل اعتماديات Compose
  (117 مكتبة محلولة من Google Maven Central): بعد إصلاح خطأين حقيقيين
  (ترتيب معاملات `GoldButton` حتى يعمل الـ trailing lambda، وإضافة
  `import kotlinx.coroutines.launch` المفقود في شاشة الأدمن) لم يبقَ أي خطأ
  نوع/مرجع. آخر عقبة في بيئة الفحص فقط: توليد كود `remember` يتطلب Compose
  Compiler Plugin الذي لا يعمل مع kotlinc من سطر الأوامر (خطأ توافق معروف)،
  وهو يعمل في Android Studio عبر Kotlin Gradle Plugin 1.9.23 مع
  Compose Compiler 1.5.14 — أي أن البناء داخل Android Studio هو المسار الصحيح.
- التأكد من اكتمال مشروع الأندرويد: `settings.gradle.kts` و `build.gradle.kts`
  و `AndroidManifest.xml` وكل الشاشات (Splash/Onboarding/دخول/تسجيل/رئيسية/مواد/
  تفاصيل مادة/تحديات/تفاصيل تحدي/اشتراكات/إشعارات/ملفي/أدمن) وملف `ApiConfig.kt`
  وفيه `BASE_URL = https://ptmohsen.free.nf/api/`، و12 مسار تنقّل في NavHost.
- التأكد بـ `grep` أن مجلدي `android/` و `api/` لا يحتويان أي كلمة مرور
  لقاعدة البيانات ولا مضيفها ولا اسم المستخدم (الفحص أُعيد بعد كل تعديل؛
  الإصابة الوحيدة في الحزمة هي ذكر اسم ملف SQL في هذا الـ README نفسه،
  وهو مقصود لأن الملف مرفوع كما هو). ملفا `site/config.php` و `database/*.sql`
  هما نسخة طبق الأصل من ملفات صاحب المشروع كما طلب رفعها كما هي، وهما خارج
  هذا الفحص. (`site/` مطابقة بالأمر `diff -rq`، وملف SQL مطابق بـ MD5.)
- إحداثيات مكتبات Compose تم التحقق منها: `androidx.compose.material:material-icons-extended`
  على BOM 2024.09.00 (الإصدار 1.7.0) — الإحداثي القديم
  `androidx.compose.material-icons-extended` غير موجود أصلاً على Google Maven.
- محاولة تجميع APK عبر `./gradlew assembleDebug` تعذّرت في بيئة البناء هذه:
  كل اتصالات TCP — حتى على loopback — محجوبة في هذه الآلة الافتراضية
  (اختبار Java أثبت أن الاتصال المحلي يُعترض برسالة حجب)، و Gradle يحتاج
  Daemon عبر TCP على loopback، فلا يمكن تشغيل أي بناء Gradle هنا، ولا توليد
  ملف `gradlew`. المشروع كامل وجاهز للبناء في Android Studio كما في القسم 2،
  و Android Studio يولّد الـ wrapper عند أول فتح للمشروع.

---

## البناء على GitHub (GitHub Actions) — خطوة بخطوة

المشروع فيه ملف جاهز: `.github/workflows/build-apk.yml` — بيبني APK تلقائياً.

1. **أنشئ ريبو جديد على GitHub** (عام أو خاص، زي ما تحب) وارفع عليه كل محتويات مجلد `PTStudyZone/` كما هي.
2. **افتح تبويب Actions في الريبو** — هتلاقي مهمة باسم `Build PT Study Zone APK` شغالة لوحدها، أو شغلها يدوياً من زر `Run workflow`.
3. **استنى البناء يخلص** (أول مرة بياخد حوالي 5-10 دقائق عشان بينزل مكتبات أندرويد).
4. **نزل الـ APK:** من صفحة الـ Action اللي خلصت، تحت في قسم `Artifacts` هتلاقي `PTStudyZone-debug-apk` — نزله، فك الضغط، وثبت ملف `app-debug.apk` على موبايلك.
5. **قبل التشغيل:** ارفع مجلد `api/` جنب `config.php` على `ptmohsen.free.nf`، عشان التطبيق يتصل بنفس السيرفر ونفس قاعدة البيانات.

لو البناء فشل، افتح الـ Action وشوف آخر سطر أحمر في اللوج — غالباً بيكون نقص مكتبة أو إصدار، وابعتلي اللوج وأنا أصلحهولك.
