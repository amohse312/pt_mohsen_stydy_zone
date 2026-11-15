# PT Study Zone — Android App (WebView)

تطبيق أندرويد احترافي لمنصة **PT Study Zone** — نفس الموقع بالظبط، في تطبيق يتثبّت APK.

الموقع: https://ptmohsen.free.nf/

> **مش محتاج ملفات الموقع.** التطبيق WebView بيفتح الموقع لايف من الرابط، فأي تحديث تعمله على الموقع يظهر في التطبيق فورًا من غير ما تحدّث التطبيق.

## المميزات

- **نفس الموقع بالظبط:** نفس الصفحات، تسجيل الدخول، المواد، المحاضرات، وتتبّع التقدم.
- **حفظ التقدم على الهاتف:** الـ WebView بيحفظ `localStorage` و `IndexedDB` والكوكيز والكاش داخل التطبيق، وبيفضل محفوظ حتى بعد قفل التطبيق أو إعادة تشغيل الهاتف. التطبيق لا يمسح أي بيانات عند الخروج.
- **مزامنة مع السيرفر:** لما يكون فيه إنترنت، الموقع بيتصل بالسيرفر عادي وبيزامن التقدم زي المتصفح بالظبط، لأن ده نفس الموقع ونفس الكود.
- **أوفلاين:** لو مفيش إنترنت، التطبيق بيحاول يفتح النسخة المحفوظة في الكاش أولًا، ولو مش متاحة بيظهر شاشة أوفلاين مع زر إعادة المحاولة.
- **حديث ومتجاوب:** Android 7.0 (API 24) لحد Android 15 (API 35)، موبايل وتابلت وأجهزة قابلة للطي، تدوير الشاشة من غير ما يفقد الصفحة أو مكان التمرير، Edge-to-Edge، وشاشة بداية Splash حديثة.
- **رفع ملفات وتنزيل:** رفع الملفات من الموقع شغال، والتنزيلات بتروح لمجلد Downloads مع إشعار، وبنفس تسجيل الدخول.
- **تنقّل طبيعي:** زر الرجوع بيرجع للصفحة اللي قبلها داخل التطبيق، والسحب لتحت بيعمل تحديث، وروابط الموقع بتفتح جوّه التطبيق والروابط الخارجية (واتساب، تليجرام، مكالمات، إيميل) بتفتح في تطبيقها.
- **روابط مباشرة:** أي رابط `ptmohsen.free.nf` يتفتح من برّه بيفتح في التطبيق.

## إزاي تجيب الـ APK

1. ارفع المشروع ده على GitHub (فرع `main`).
2. افتح تبويب **Actions** في المستودع → شغّلة **Build APK** → هتلاقي ملف `pt-study-zone-apk` في الـ Artifacts.
3. نزّل الملف، فك الضغط، وثبّت `PT-Study-Zone.apk` على أي جهاز أندرويد.

لو عملت Tag زي `v1.0.0` ودفعته، الـ APK هيتنشر تلقائيًا في **Releases**.

## البناء محليًا

افتح المشروع في Android Studio (Ladybug أو أحدث) وشغّل `Run`، أو من سطر الأوامر:

```bash
gradle assembleDebug
# الناتج: app/build/outputs/apk/debug/app-debug.apk
```

## تغيير الرابط أو الاسم

- الرابط: غيّر `HOME_URL` و `SITE_HOST` في `app/src/main/java/com/ptstudyzone/app/MainActivity.kt`، وعدّل الـ `host` في `AndroidManifest.xml`.
- اسم التطبيق: `app_name` في `app/src/main/res/values/strings.xml`.
- رقم الإصدار: `versionCode` / `versionName` في `app/build.gradle.kts` — زوّد `versionCode` مع كل تحديث.

## ملحوظة عن حفظ التقدم

التقدم اللي الموقع بيحفظه في المتصفح (localStorage) أو في حساب المستخدم على السيرفر هيتحفظ بنفس الطريقة في التطبيق، لأن الـ WebView بيستخدم نفس آليات التخزين. لو الموقع نفسه بيحتاج إنترنت عشان يسجّل التقدم على السيرفر، ده هيفضل محتاج إنترنت — التطبيق مش بيضيف مزامنة منفصلة، هو بيعرض الموقع زي ما هو.

---

## English summary

Native Android WebView wrapper for PT Study Zone (`https://ptmohsen.free.nf/`). No website source files needed — the app loads the live site. Login and progress persist in the WebView's localStorage / IndexedDB / cookies / cache on the device and sync with the server whenever online, exactly like the website. Targets Android 7.0–15, phones/tablets/foldables. GitHub Actions builds an installable debug APK on every push to `main`.
